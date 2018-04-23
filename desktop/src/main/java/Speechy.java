import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.Arrays;

public class Speechy extends JFrame {
    private static final double DEFAULT_DURATION = 1.25;
    private static final String DEFAULT_NAME = "__test__";

    private enum State {
        WAITING, INITIAL, FOLDER_SET, WORD_TRAINED, SENTENCE_TRAINED
    }

    private JPanel contentPane;
    private JTextField textFieldFolder;
    private JButton buttonFolderOpen;
    private JTextArea textAreaSetting;
    private JButton buttonSettingSet;
    private JButton buttonSettingKeys;
    private JButton buttonSettingReset;
    private JTextField textFieldDuration;
    private JButton buttonDurationSet;
    private JTextField textFieldName;
    private JButton buttonNameSet;
    private JTextArea textAreaOutput;
    private JProgressBar progressBarRecord;
    private JTextArea textAreaWordList;
    private JTextField textFieldWord;
    private JButton buttonWordAdd;
    private JButton buttonWordClean;
    private JButton buttonWordTrain;
    private JButton buttonWordTest;
    private JTextArea textAreaSentenceList;
    private JTextField textFieldSentence;
    private JButton buttonSentenceAdd;
    private JButton buttonSentenceClean;
    private JButton buttonSentenceTest;
    private JButton buttonSentenceTrain;

    private File folder;
    private File setting;
    private File wordList;
    private File sentenceList;
    private double duration = DEFAULT_DURATION;
    private String name = DEFAULT_NAME;
    private WavRecorder wavRecorder = new WavRecorder();
    private State state;

    static {
        System.loadLibrary("Speechy");
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Speechy speechy = new Speechy();
        speechy.pack();
        speechy.setLocationRelativeTo(null);
        speechy.setVisible(true);
    }

    private Speechy() {
        setContentPane(contentPane);
        setTitle(Speechy.class.getSimpleName());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        textFieldDuration.setText(String.valueOf(DEFAULT_DURATION));
        textFieldName.setText(String.valueOf(DEFAULT_NAME));
        textAreaOutput.setText("");
        progressBarRecord.setValue(100);

        buttonFolderOpen.addActionListener(e -> onFolderOpen());
        buttonSettingSet.addActionListener(e -> onSettingSet());
        buttonSettingKeys.addActionListener(e -> onSettingKeys());
        buttonSettingReset.addActionListener(e -> onSettingReset());
        buttonDurationSet.addActionListener(e -> onDurationSet());
        buttonNameSet.addActionListener(e -> onNameSet());
        buttonWordAdd.addActionListener(e -> onWordAdd());
        buttonWordClean.addActionListener(e -> onWordClean());
        buttonWordTrain.addActionListener(e -> onWordTrain());
        buttonWordTest.addActionListener(e -> onWordTest());
        buttonSentenceAdd.addActionListener(e -> onSentenceAdd());
        buttonSentenceClean.addActionListener(e -> onSentenceClean());
        buttonSentenceTrain.addActionListener(e -> onSentenceTrain());
        buttonSentenceTest.addActionListener(e -> onSentenceTest());

        addOutputMessage("Welcome to Speechy");
        setState(State.INITIAL);
    }

    private void onFolderOpen() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        addOutputMessage("Setting up");
        folder = fileChooser.getSelectedFile();
        if (!folder.isDirectory()) {
            addOutputMessage("Invalid folder");
            setState(State.INITIAL);

            return;
        }
        textFieldFolder.setText(folder.getAbsolutePath());
        setting = new File(folder, "sr-lib.config");
        wordList = new File(folder, "sr-lib.words");
        sentenceList = new File(folder, "sr-lib.sentences");
        populateTextArea(setting, textAreaSetting);
        populateTextArea(wordList, textAreaWordList);
        populateTextArea(sentenceList, textAreaSentenceList);
        statefulActionGUI(() -> setup(getFolderName()), State.FOLDER_SET);
    }

    private void onSettingSet() {
        addOutputMessage("Setting setting");
        try {
            setting.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (FileWriter fileWriter = new FileWriter(setting)) {
            fileWriter.write(textAreaSetting.getText().trim());
        } catch (IOException e) {
            e.printStackTrace();
        }
        onWordClean();
        onSentenceClean();
        statefulActionGUI(() -> setup(getFolderName()), State.FOLDER_SET);
    }

    private void onSettingKeys() {
        addOutputMessage("Show keys");
        try {
            Desktop.getDesktop().browse(new URL("https://github.com/theawless/sr-lib/blob/master/word/inc/config.h").toURI());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onSettingReset() {
        addOutputMessage("Resetting setting");
        textAreaSetting.setText("");
        onSettingSet();
    }

    private void onDurationSet() {
        addOutputMessage("Setting duration");
        duration = Double.parseDouble(textFieldDuration.getText());
    }

    private void onNameSet() {
        addOutputMessage("Setting name");
        name = textFieldName.getText();
    }

    private void onWordAdd() {
        String word = textFieldWord.getText();
        if (word.isEmpty()) {
            return;
        }

        // 1. clean 2. add 3. setup - for only words
        addOutputMessage("Adding word: " + word);
        onWordClean();
        addToItemList(word, wordList);
        populateTextArea(wordList, textAreaWordList);
        statefulActionGUI(() -> setup(getFolderName()), State.FOLDER_SET);
        statefulActionGUI(() -> record(getFilenameUnderFolder(getWordFilename(word))), State.FOLDER_SET);
    }

    private void onWordClean() {
        addOutputMessage("Cleaning word");
        statefulActionGUI(() -> {
            String patterns[] = {".*\\.codebook", ".*\\.universe", ".*\\.features", ".*\\.observations", ".*\\.model.*"};
            Arrays.stream(folder.listFiles((f, p) -> Arrays.stream(patterns).anyMatch(p::matches))).forEach(File::delete);
        }, State.FOLDER_SET);
    }

    private void onWordTrain() {
        addOutputMessage("Training word");
        statefulActionGUI(this::wordTrain, State.WORD_TRAINED);
    }

    private void onWordTest() {
        addOutputMessage("Testing word");
        statefulActionGUI(() -> {
            record(getFilenameUnderFolder(name));
            String word = wordTest(getFilenameUnderFolder(name));
            addOutputMessage("Tested word: " + word);
        }, state);
    }

    private void onSentenceAdd() {
        String sentence = textFieldSentence.getText();
        if (sentence.isEmpty()) {
            return;
        }

        // 1. clean 2. add 3. setup - for both words and sentences, because new words might be added in a sentence
        addOutputMessage("Adding sentence: " + sentence);
        onWordClean();
        onSentenceClean();
        addToItemList(sentence, sentenceList);
        populateTextArea(sentenceList, textAreaSentenceList);
        String words[] = sentence.split("\\s+");
        Arrays.stream(words).forEach((word) -> addToItemList(word, wordList));
        populateTextArea(wordList, textAreaWordList);
        statefulActionGUI(() -> setup(getFolderName()), State.FOLDER_SET);
        statefulActionGUI(() -> {
            for (String word : words) {
                addOutputMessage("Adding word: " + word);
                record(getFilenameUnderFolder(getWordFilename(word)));
            }
        }, State.FOLDER_SET);
    }

    private void onSentenceClean() {
        addOutputMessage("Cleaning sentence");
        statefulActionGUI(() -> {
            String patterns[] = {".*\\.gram"};
            Arrays.stream(folder.listFiles((f, p) -> Arrays.stream(patterns).anyMatch(p::matches))).forEach(File::delete);
        }, State.WORD_TRAINED);
    }

    private void onSentenceTrain() {
        addOutputMessage("Training sentence");
        statefulActionGUI(this::sentenceTrain, State.SENTENCE_TRAINED);
    }

    private void onSentenceTest() {
        addOutputMessage("Testing sentence");
        statefulActionGUI(() -> {
            StringBuilder sentence = new StringBuilder();
            record(getFilenameUnderFolder(name));
            String word = sentenceTest(getFilenameUnderFolder(name), true);
            while (!word.isEmpty()) {
                sentence.append(" ").append(word);
                addOutputMessage("Tested word: " + word);
                record(getFilenameUnderFolder(name));
                word = sentenceTest(getFilenameUnderFolder(name), false);
            }
            addOutputMessage("Tested sentence: " + sentence.toString());
        }, state);
    }

    private void record(String filename) {
        // show progress of recording
        new Thread(() -> {
            // frequency of refresh
            long flicker = 5;
            double progress = 0.0;
            do {
                try {
                    progress += flicker / (duration * 10);
                    int progressValue = (int) progress;
                    SwingUtilities.invokeLater(() -> progressBarRecord.setValue(progressValue));
                    Thread.sleep(flicker);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (progress < 100);
        }).start();

        wavRecorder.record(filename, duration);
    }

    private String getFolderName() {
        // file separator is expected in the end by sr-lib
        return folder.getAbsolutePath() + File.separator;
    }

    private String getFilenameUnderFolder(String filename) {
        return new File(folder, filename).getAbsolutePath();
    }

    private String getWordFilename(String word) {
        for (int word_index = 0; ; ++word_index) {
            String filename = word + '_' + word_index;
            if (!checkFile(new File(folder, filename + '.' + wavRecorder.getExtension()))) {
                return filename;
            }
        }
    }

    private boolean checkFile(File file) {
        return file.exists() && file.length() > 0;
    }

    private void addToItemList(String item, File itemList) {
        try {
            itemList.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(itemList))) {
            if (bufferedReader.lines().noneMatch(item::matches)) {
                try (FileWriter fileWriter = new FileWriter(itemList, true)) {
                    // do not add line separator if first item
                    fileWriter.write((itemList.length() == 0 ? "" : '\n') + item);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void populateTextArea(File file, JTextArea textArea) {
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        textArea.setText("");
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            bufferedReader.lines().forEach((line) -> textArea.append(line + '\n'));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addOutputMessage(String message) {
        // can be called from any thread
        SwingUtilities.invokeLater(() -> {
            textAreaOutput.append(message + '\n');
            textAreaOutput.setCaretPosition(textAreaOutput.getDocument().getLength());
        });
    }

    private void statefulActionGUI(Runnable action, State endState) {
        // so users can't double start a task
        new Thread(() -> {
            setState(State.WAITING);
            action.run();
            setState(endState);
        }).start();
    }

    private void setState(State endState) {
        SwingUtilities.invokeLater(() -> {
            setEnabledGUI(contentPane, false);
            switch (state = endState) {
                case SENTENCE_TRAINED:
                case WORD_TRAINED:
                case FOLDER_SET:
                    boolean wordTrainable = checkFile(wordList) && folder.listFiles((f, p) -> p.matches(".*\\." + wavRecorder.getExtension())).length > 0;
                    boolean wordTrained = checkFile(wordList) && checkFile(new File(folder, "0.model"));
                    boolean sentenceTrainable = checkFile(sentenceList);
                    boolean sentenceTrained = wordTrained && checkFile(sentenceList) && checkFile(new File(folder, "0.gram"));

                    buttonSentenceTest.setEnabled(sentenceTrained);
                    buttonSentenceTrain.setEnabled(sentenceTrainable);
                    buttonSentenceClean.setEnabled(sentenceTrained);
                    buttonSentenceAdd.setEnabled(true);
                    textFieldSentence.setEnabled(true);
                    buttonWordTest.setEnabled(wordTrained);
                    buttonWordTrain.setEnabled(wordTrainable);
                    buttonWordClean.setEnabled(wordTrained);
                    buttonWordAdd.setEnabled(true);
                    textFieldWord.setEnabled(true);
                    buttonNameSet.setEnabled(true);
                    textFieldName.setEnabled(true);
                    buttonDurationSet.setEnabled(true);
                    textFieldDuration.setEnabled(true);
                    buttonSettingReset.setEnabled(checkFile(setting));
                    buttonSettingKeys.setEnabled(true);
                    buttonSettingSet.setEnabled(true);
                case INITIAL:
                    buttonFolderOpen.setEnabled(true);
                    textFieldFolder.setEnabled(true);
                case WAITING:
            }
        });
    }

    private void setEnabledGUI(JPanel panel, boolean enabled) {
        for (Component component : panel.getComponents()) {
            if (component.getClass() == JPanel.class) {
                setEnabledGUI((JPanel) component, enabled);
            } else if (component.getClass() == JButton.class || component.getClass() == JTextField.class) {
                // only user actable elements
                component.setEnabled(enabled);
            }
        }
    }

    private native void setup(String folder);

    private native void wordTrain();

    private native String wordTest(String filename);

    private native void sentenceTrain();

    private native String sentenceTest(String filename, boolean restart);
}