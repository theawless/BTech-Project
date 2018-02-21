#include <iostream>
#include <jni.h>
#include <string>
#include <vector>

#include "logger.h"
#include "recognizer.h"
#include "recorder.h"

using namespace std;

extern "C" JNIEXPORT jint JNICALL
Java_com_gobbledygook_theawless_speechy_app_MainFragment_getWordIndexHMM(JNIEnv *env, jobject jobj, jstring jpath)
{
    const char *cpath = env->GetStringUTFChars(jpath, NULL);
    string path = string(cpath) + "/recording/";

    Config config(path);
    config.load("word.config");
    Recognizer recognizer(config);

    string test_filename = "test";
    int word_index = recognizer.recognize(test_filename);
    Logger::logger() << "The recognised word is: " << config.audio_names[word_index] << endl;

    return word_index;
}
