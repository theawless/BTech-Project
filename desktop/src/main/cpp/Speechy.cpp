/// This file is the glue between native C++ code and Java code.
/// Checkout https://github.com/thefangbear/JNI-By-Examples for more examples.

#include "Speechy.h"

#include <memory>
#include <string>
#include <utility>
#include <vector>

#include "config.h"
#include "gram-trainer.h"
#include "logger.h"
#include "model-tester.h"
#include "model-trainer.h"
#include "recogniser.h"
#include "utils.h"

using namespace std;

// need to be saved across calls
string folder;
vector<string> words;
vector<vector<string>> sentences;
Config config;

// reuse the pointers by calling reset()
unique_ptr<ModelTester> model_tester;
unique_ptr<Recogniser> recogniser;

JNIEXPORT void JNICALL Java_Speechy_setup(JNIEnv *jenv, jobject jobj, jstring jfolder)
{
	Logger::info("Setup");

	folder = jenv->GetStringUTFChars(jfolder, NULL);
	const string words_filename = folder + "sr-lib.words";
	const string sentences_filename = folder + "sr-lib.sentences";
	const string config_filename = folder + "sr-lib.config";

	words = Utils::get_vector_from_file<string>(words_filename);
	sentences = Utils::get_matrix_from_file<string>(sentences_filename, ' ');
	config = Utils::get_item_from_file<Config>(config_filename);

	model_tester.reset(ModelTester::Builder(folder, config).build().release());
	recogniser.reset(Recogniser::Builder(folder, words, sentences, config).build().release());
}

JNIEXPORT void JNICALL Java_Speechy_wordTrain(JNIEnv *jenv, jobject jobj)
{
	Logger::info("Word train");

	ModelTrainer::Builder(folder, words, config).build();
	model_tester.reset(ModelTester::Builder(folder, config).build().release());
}

JNIEXPORT jstring JNICALL Java_Speechy_wordTest(JNIEnv *jenv, jobject jobj, jstring jfilename)
{
	Logger::info("Word test");

	const string filename = jenv->GetStringUTFChars(jfilename, NULL);
	const pair<bool, vector<double>> scores = model_tester->test(filename);
	const int word_index = max_element(scores.second.begin(), scores.second.end()) - scores.second.begin();
	const string word = !scores.first || scores.second[word_index] == 0.0 ? string() : words[word_index];

	return jenv->NewStringUTF(word.c_str());
}

JNIEXPORT void JNICALL Java_Speechy_sentenceTrain(JNIEnv *jenv, jobject jobj)
{
	Logger::info("Sentence train");

	GramTrainer::Builder(folder, sentences, config).build();
	recogniser.reset(Recogniser::Builder(folder, words, sentences, config).build().release());
}

JNIEXPORT jstring JNICALL Java_Speechy_sentenceTest(JNIEnv *jenv, jobject jobj, jstring jfilename, jboolean jrestart)
{
	Logger::info("Sentence test");

    // new sentence, hence clear context
	if (jrestart)
	{
		recogniser->reset();
	}

	const string filename = jenv->GetStringUTFChars(jfilename, NULL);
	const pair<bool, string> result = recogniser->recognise(filename);

	return jenv->NewStringUTF(result.first ? result.second.c_str() : string().c_str());
}
