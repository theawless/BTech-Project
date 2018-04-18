#include <jni.h>
#include <memory>
#include <string>
#include <utility>
#include <vector>

#include "config.h"
#include "logger.h"
#include "recogniser.h"
#include "utils.h"

using namespace std;

unique_ptr<Recogniser> recogniser;

extern "C" JNIEXPORT void JNICALL
Java_com_gobbledygook_theawless_speechy_FloatingService_setup(JNIEnv *jenv, jobject jobj, jstring jfolder)
{
    Logger::info("Setup");

    const string folder = jenv->GetStringUTFChars(jfolder, NULL);
    const string words_filename = folder + "sr-lib.words";
    const string sentences_filename = folder + "sr-lib.sentences";
    const string config_filename = folder + "sr-lib.config";

    const vector<string> words = Utils::get_vector_from_file<string>(words_filename);
    const vector<vector<string>> sentences = Utils::get_matrix_from_file<string>(sentences_filename,
                                                                                 ' ');
    const Config config = Utils::get_item_from_file<Config>(config_filename);

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

    const string filename = jenv->GetStringUTFChars(jfilename, NULL);
    const pair<bool, string> result = recogniser->recognise(filename);

    return jenv->NewStringUTF(result.first ? result.second.c_str() : string().c_str());
}
