#include <iostream>
#include <jni.h>
#include <map>
#include <limits>
#include <string>

#include "audio.h"
#include "trim.h"
#include "lpc.h"
#include "utils.h"

using namespace std;

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
    pair<vector<double>, pair<vector<double>, vector<double>>> trimmed_amplitudes = trim_by_auto(
            amplitudes);
    vector<vector<double>> segments = fixed_segment(trimmed_amplitudes.first);
    coefficients = speech_to_coefficients(segments);

    if (cache) {
        set_matrix_to_csv(coefficients, lpcs_filename);
    }

    return coefficients;
}

/// Gets map of vowels and their lpc coefficients.
static vector<vector<vector<double>>> get_train_coefficients(string train_filename) {
    vector<vector<vector<double>>> train_coefficients;
    for (int i = 0; i < 10; ++i) {
        string filename = train_filename + "_" + to_string(i);
        train_coefficients.push_back(get_coefficients(filename, true));
    }

    return train_coefficients;
}

/// Decides vowel by taking the one which is most similar.
static int decide_digit(const vector<vector<vector<double>>> &train_coefficients,
                        const vector<vector<double>> &test_coefficients) {
    int most_similar_digit = -1;
    double min_similarity_distance = numeric_limits<double>::max();

    for (int i = 0; i < 10; ++i) {
        double similarity_distance = coefficients_similarity(train_coefficients[i],
                                                             test_coefficients);
        if (similarity_distance < min_similarity_distance) {
            min_similarity_distance = similarity_distance;
            most_similar_digit = i;
        }
    }

    return most_similar_digit;
}

extern "C"
JNIEXPORT jint

JNICALL
Java_com_gobbledygook_theawless_speechy_app_MainFragment_getWordInner(JNIEnv *env, jobject jobj,
                                                                      jstring jpath) {
    const char *cpath = env->GetStringUTFChars(jpath, NULL);
    string path = string(cpath) + "/" + "recording";
    vector<vector<vector<double>>> train_coefficients = get_train_coefficients(path);
    vector<vector<double>> test_coefficients = get_coefficients(path, false);
    int digit = decide_digit(train_coefficients, test_coefficients);
    return digit;
}