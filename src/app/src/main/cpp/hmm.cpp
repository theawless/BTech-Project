#include <algorithm>
#include <iostream>
#include <jni.h>
#include <limits>
#include <string>

#include "audio.h"
#include "codebook.h"
#include "hmm.h"
#include "lpc.h"
#include "utils.h"

#define N_VALUE 5
#define M_VALUE 16

#define N_RETRAIN 3
#define UNIVERSE_FILENAME RECORD_FOLDER "universe"
#define CODEBOOK_FILENAME RECORD_FOLDER "codebook"

using namespace std;

// Must correspond to the values in Kotlin Constants!
const int N_UTTERANCES = 5;
const vector<string> WORDS = {"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine"};

/// Loads, preprocesses the amplitudes and then returns their lpc coefficients.
static vector<vector<double>> get_coefficients(string filename, bool cache) {
    vector<vector<double>> coefficients;
    string lpcs_filename = filename + "_lpcs";
    cout << "Getting lpcs for " << filename << endl;

    if (cache) {
        coefficients = get_matrix_from_csv(lpcs_filename);
        if (!coefficients.empty()) {
            return coefficients;
        }
    }

    vector<double> unprocessed_amplitudes = get_vector_from_txt(filename);
    vector<double> amplitudes = preprocess(unprocessed_amplitudes);
    vector<vector<double>> segments = fixed_segment(amplitudes);
    coefficients = speech_to_coefficients(segments);

    if (cache) {
        set_matrix_to_csv(coefficients, lpcs_filename);
    }

    return coefficients;
}

/// Builds the universe by accumulation lpcs of all frames of all signals.
static vector<vector<double>> get_universe(string path) {
    vector<vector<double>> universe;
    string universe_filename = path + "universe";
    cout << "Getting universe" << endl;

    universe = get_matrix_from_csv(universe_filename);
    if (!universe.empty()) {
        return universe;
    }

    for (int i = 0; i < WORDS.size(); ++i) {
        for (int j = 0; j < N_UTTERANCES; ++j) {
            string filename = path + WORDS[i] + "_" + to_string(j);
            vector<vector<double>> coefficients = get_coefficients(filename, true);
            for (int k = 0; k < coefficients.size(); ++k) {
                universe.push_back(coefficients[k]);
            }
        }
    }
    set_matrix_to_csv(universe, universe_filename);

    return universe;
}

/// Builds a codebook from the universe using lbg.
static vector<vector<double>> get_codebook(string path) {
    vector<vector<double>> codebook;
    string codebook_filename = path + "codebook";
    cout << "Getting codebook" << endl;

    codebook = get_matrix_from_csv(codebook_filename);
    if (!codebook.empty()) {
        return codebook;
    }

    vector<vector<double>> universe = get_universe(path);
    codebook = lbg_codebook(universe, M_VALUE);
    set_matrix_to_csv(codebook, codebook_filename);

    return codebook;
}

/// Gets the observations sequence from the codebook.
static vector<int> get_observations(string filename, const vector<vector<double>> &codebook, bool cache) {
    vector<int> observations;
    string obs_filename = filename + "_obs";
    cout << "Getting observation sequence for " << filename << endl;

    if (cache) {
        vector<double> temp_obs = get_vector_from_txt(obs_filename);
        for (int i = 0; i < temp_obs.size(); ++i) {
            observations.push_back((int) temp_obs[i]);
        }
        if (!observations.empty()) {
            return observations;
        }
    }

    vector<vector<double>> coefficients = get_coefficients(filename, cache);
    observations = observation_sequence(coefficients, codebook);

    if (cache) {
        set_vector_to_txt(vector<double>(observations.begin(), observations.end()), obs_filename);
    }

    return observations;
}

/// Optimises the given train model use the observations of the given filename.
static Model get_utterance_model(string filename, const Model &train_model, const vector<vector<double>> &codebook, int t) {
    Model model;
    string base_filename = filename + "_lambda_" + to_string(t);
    string a_filename = base_filename + "_a";
    string b_filename = base_filename + "_b";
    string pi_filename = base_filename + "_pi";
    cout << "Getting model for " << base_filename << endl;

    model.a = get_matrix_from_csv(a_filename);
    model.b = get_matrix_from_csv(b_filename);
    model.pi = get_vector_from_txt(pi_filename);
    if (!model.pi.empty()) {
        return model;
    }

    vector<int> observations = get_observations(filename, codebook, true);
    model = optimise(train_model, observations);

    set_matrix_to_csv(model.a, a_filename);
    set_matrix_to_csv(model.b, b_filename);
    set_vector_to_txt(model.pi, pi_filename);

    return model;
}

/// Gets the model for all utterances and merges them.
static Model get_digit_model(const Model &train_model, const vector<vector<double>> &codebook, int d, int t, string path) {
    vector<Model> utterance_models;

    for (int i = 0; i < N_UTTERANCES; ++i) {
        string filename = path + WORDS[d] + "_" + to_string(i);
        Model model = get_utterance_model(filename, train_model, codebook, t);

        utterance_models.push_back(model);
    }

    return merge(utterance_models);
}

/// Gets and trains the models for all digits.
static vector<Model> get_models(const vector<vector<double>> &codebook, string path) {
    vector<Model> models;

    for (int i = 0; i < WORDS.size(); ++i) {
        Model model = bakis(N_VALUE, M_VALUE);

        // Train the model.
        for (int j = 1; j <= N_RETRAIN; ++j) {
            model = get_digit_model(model, codebook, i, j, path);
        }

        models.push_back(model);
    }

    return models;
}

/// Decides the probable digit by comparing P for given models for given observations.
static int decide_word(const vector<Model> &models, const vector<int> observations) {
    double max_P = numeric_limits<double>::min();
    int most_probable_word = -1;

    for (int i = 0; i < models.size(); ++i) {
        double P = forward(models[i], observations).first;
        if (P > max_P) {
            max_P = P;
            most_probable_word = i;
        }
    }

    return most_probable_word;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_gobbledygook_theawless_speechy_app_MainFragment_getWordIndexHMM(JNIEnv *env, jobject jobj, jstring jpath) {
    const char *cpath = env->GetStringUTFChars(jpath, NULL);
    string path = string(cpath) + "/recording/";

    vector<vector<double>> codebook = get_codebook(path);
    vector<Model> models = get_models(codebook, path);
    vector<int> observations = get_observations(path + "test", codebook, false);
    int word_index = decide_word(models, observations);

    return word_index;
}
