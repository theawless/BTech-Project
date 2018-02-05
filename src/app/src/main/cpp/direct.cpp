#include <iostream>
#include <jni.h>
#include <limits>
#include <string>

#include "audio.h"
#include "trim.h"
#include "lpc.h"
#include "utils.h"

using namespace std;

// Must correspond to the values in Kotlin Constants!
const int N_UTTERANCES = 5;
const vector<string> WORDS = {"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine"};

/// Loads, preprocesses the amplitudes and then returns their lpc coefficients.
static vector<vector<double>> get_coefficients(string filename, bool cache) {
    vector<vector<double>> coefficients;
    string lpcs_filename = filename + "_lpcs";

    if (cache) {
        coefficients = get_matrix_from_csv(lpcs_filename);
        if (!coefficients.empty()) {
            return coefficients;
        }
    }

    vector<double> unprocessed_amplitudes = get_vector_from_txt(filename);
    vector<double> amplitudes = preprocess(unprocessed_amplitudes);
    pair<vector<double>, pair<vector<double>, vector<double>>> trimmed_amplitudes = trim_by_auto(amplitudes);
    vector<vector<double>> segments = fixed_segment(trimmed_amplitudes.first);
    coefficients = speech_to_coefficients(segments);

    if (cache) {
        set_matrix_to_csv(coefficients, lpcs_filename);
    }

    return coefficients;
}

/// Gets map of vowels and their lpc coefficients.
static vector<vector<vector<double>>> get_train_coefficients(string train_filename) {
    vector<vector<vector<double>>> train_coefficients(N_UTTERANCES * WORDS.size());
    for (int i = 0; i < N_UTTERANCES; ++i) {
        for (int j = 0; j < WORDS.size(); ++j) {
            int k = j + i * N_UTTERANCES;
            string filename = train_filename + WORDS[j] + "_" + to_string(i);
            train_coefficients[k] = get_coefficients(filename, true);
        }
    }

    return train_coefficients;
}

/// Decides word by taking the one which is most similar.
static int
decide_word(const vector<vector<vector<double>>> &train_coefficients, const vector<vector<double>> &test_coefficients) {
    int most_similar_word_index = -1;
    double min_similarity_distance = numeric_limits<double>::max();

    for (int i = 0; i < N_UTTERANCES; ++i) {
        for (int j = 0; j < WORDS.size(); ++j) {
            int k = j + i * N_UTTERANCES;
            double similarity_distance = coefficients_similarity(train_coefficients[k], test_coefficients);
            if (similarity_distance < min_similarity_distance) {
                min_similarity_distance = similarity_distance;
                most_similar_word_index = j;
            }
        }
    }

    return most_similar_word_index;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_gobbledygook_theawless_speechy_app_MainFragment_getWordIndexDirect(JNIEnv *env, jobject jobj, jstring jpath) {
    const char *cpath = env->GetStringUTFChars(jpath, NULL);
    string path = string(cpath) + "/recording/";

    vector<vector<vector<double>>> train_coefficients = get_train_coefficients(path);
    vector<vector<double>> test_coefficients = get_coefficients(path + "test", false);
    int word_index = decide_word(train_coefficients, test_coefficients);

    return word_index;
}
