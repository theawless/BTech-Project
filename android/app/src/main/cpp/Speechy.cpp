#include <jni.h>
#include <memory>
#include <string>
#include <utility>
#include <vector>

#include "file-io.h"
#include "config.h"
#include "gram-trainer.h"
#include "logger.h"
#include "model-trainer.h"
#include "recogniser.h"

using namespace std;

unique_ptr<Recogniser> recogniser;

extern "C" JNIEXPORT void JNICALL
Java_com_gobbledygook_theawless_speechy_FloatingService_setup(JNIEnv *jenv, jobject jobj, jstring jfolder)
{
    Logger::info("Setup");

    const string folder = jenv->GetStringUTFChars(jfolder, nullptr);
    const string words_filename = folder + "sr-lib.words";
    const string sentences_filename = folder + "sr-lib.sentences";
    const string config_filename = folder + "sr-lib.config";

    const vector<string> words = FileIO::get_vector_from_file<string>(words_filename);
    const vector<vector<string>> sentences = FileIO::get_matrix_from_file<string>(sentences_filename, ' ');
    const Config config = FileIO::get_item_from_file<Config>(config_filename);

    recogniser.reset(Recogniser::Builder(folder, words, sentences, config).build().release());
}

extern "C" JNIEXPORT void JNICALL
Java_com_gobbledygook_theawless_speechy_MainActivity_train(JNIEnv *jenv, jobject jobj, jstring jfolder)
{
    Logger::info("Train");

    const string folder = jenv->GetStringUTFChars(jfolder, nullptr);
    const string words_filename = folder + "sr-lib.words";
    const string sentences_filename = folder + "sr-lib.sentences";
    const string config_filename = folder + "sr-lib.config";

    const vector<string> words = FileIO::get_vector_from_file<string>(words_filename);
    const vector<vector<string>> sentences = FileIO::get_matrix_from_file<string>(sentences_filename, ' ');
    const Config config = FileIO::get_item_from_file<Config>(config_filename);

    ModelTrainer::Builder(folder, words, config).build();
    GramTrainer::Builder(folder, sentences, config).build();
    recogniser.reset(Recogniser::Builder(folder, words, sentences, config).build().release());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_gobbledygook_theawless_speechy_FloatingService_recognise(JNIEnv *jenv, jobject *jobj, jstring jfilename, jboolean jrestart)
{
    Logger::info("Recognise");

    if (jrestart)
    {
        recogniser->reset();
    }

    const string filename = jenv->GetStringUTFChars(jfilename, nullptr);
    const pair<bool, string> result = recogniser->recognise(filename);

    return jenv->NewStringUTF(result.first ? result.second.c_str() : string().c_str());
}
