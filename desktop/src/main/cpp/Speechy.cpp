/// This file is the glue between native C++ code and Java code.
/// Checkout https://github.com/thefangbear/JNI-By-Examples for more examples.

#include "Speechy.h"

#include <memory>
#include <string>
#include <utility>
#include <vector>

#include "file-io.h"
#include "config.h"
#include "gram-trainer.h"
#include "logger.h"
#include "model-tester.h"
#include "model-trainer.h"
#include "recogniser.h"

using namespace std;

// need to be saved across calls
string folder;
string train_folder;
string model_folder;
string config_filename;
string words_filename;
string sentences_filename;

// reuse the pointers by calling reset()
unique_ptr<ModelTester> model_tester;
unique_ptr<Recogniser> recogniser;

JNIEXPORT void JNICALL Java_Speechy_setup(JNIEnv *jenv, jobject jobj, jstring jfolder)
{
	Logger::info("Setup");

	folder = jenv->GetStringUTFChars(jfolder, nullptr);
	train_folder = folder + "train/";
	model_folder = folder + "model/";
	words_filename = folder + "sr-lib.words";
	sentences_filename = folder + "sr-lib.sentences";
	config_filename = folder + "sr-lib.config";

	vector<string> words = FileIO::get_vector_from_file<string>(words_filename);
	vector<vector<string>> sentences = FileIO::get_matrix_from_file<string>(sentences_filename, ' ');
	Config config = FileIO::get_item_from_file<Config>(config_filename);

	model_tester.reset(ModelTester::Builder(model_folder, config).build().release());
	recogniser.reset(Recogniser::Builder(model_folder, words, sentences, config).build().release());
}

JNIEXPORT void JNICALL Java_Speechy_wordTrain(JNIEnv *jenv, jobject jobj)
{
	Logger::info("Word train");

	vector<string> words = FileIO::get_vector_from_file<string>(words_filename);
	Config config = FileIO::get_item_from_file<Config>(config_filename);

	ModelTrainer::Builder(train_folder, model_folder, words, config).build();
	model_tester.reset(ModelTester::Builder(model_folder, config).build().release());
}

JNIEXPORT jstring JNICALL Java_Speechy_wordTest(JNIEnv *jenv, jobject jobj, jstring jfilename)
{
	Logger::info("Word test");

	const string filename = jenv->GetStringUTFChars(jfilename, nullptr);
	vector<string> words = FileIO::get_vector_from_file<string>(words_filename);

	const pair<bool, vector<double>> scores = model_tester->test(filename);
	const int word_index = max_element(scores.second.begin(), scores.second.end()) - scores.second.begin();
	const string word = !scores.first || scores.second[word_index] == 0.0 ? string() : words[word_index];

	return jenv->NewStringUTF(word.c_str());
}

JNIEXPORT void JNICALL Java_Speechy_sentenceTrain(JNIEnv *jenv, jobject jobj)
{
	Logger::info("Sentence train");

	vector<string> words = FileIO::get_vector_from_file<string>(words_filename);
	vector<vector<string>> sentences = FileIO::get_matrix_from_file<string>(sentences_filename, ' ');
	Config config = FileIO::get_item_from_file<Config>(config_filename);

	GramTrainer::Builder(model_folder, sentences, config).build();
	recogniser.reset(Recogniser::Builder(model_folder, words, sentences, config).build().release());
}

JNIEXPORT jstring JNICALL Java_Speechy_sentenceTest(JNIEnv *jenv, jobject jobj, jstring jfilename, jboolean jrestart)
{
	Logger::info("Sentence test");

	// new sentence, hence clear context
	if (jrestart)
	{
		recogniser->reset();
	}

	const string filename = jenv->GetStringUTFChars(jfilename, nullptr);
	const pair<bool, string> result = recogniser->recognise(filename);

	return jenv->NewStringUTF(result.first ? result.second.c_str() : string().c_str());
}
