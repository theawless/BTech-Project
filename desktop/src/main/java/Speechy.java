import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.Arrays;

public class Speechy extends JFrame {
    private static final double DEFAULT_DURATION = 1.25;
    private static final String DEFAULT_NAME = "__test__";

    private enum State {
        WAITING, INITIAL, AUTO
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
    private File train_folder;
    private File model_folder;
    private File setting;
    private File wordList;
    private File sentenceList;
    private double duration = DEFAULT_DURATION;
    private String name = DEFAULT_NAME;
    private WavRecorder wavRecorder = new WavRecorder();

    static {
        System.out.println(System.getProperty("java.class.path"));
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

        File chosenFolder = fileChooser.getSelectedFile();
        if (!chosenFolder.isDirectory()) {
            addOutputMessage("Invalid folder");
            return;
        }

        addOutputMessage("Setting up");
        folder = chosenFolder;
        train_folder = new File(folder, "train");
        model_folder = new File(folder, "model");
        textFieldFolder.setText(folder.getAbsolutePath());
        setting = new File(folder, "sr-lib.config");
        wordList = new File(folder, "sr-lib.words");
        sentenceList = new File(folder, "sr-lib.sentences");
        populateTextArea(setting, textAreaSetting);
        populateTextArea(wordList, textAreaWordList);
        populateTextArea(sentenceList, textAreaSentenceList);
        statefulActionGUI(() -> setup(getFolderName()));
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
        statefulActionGUI(() -> {
            wordClean();
            sentenceClean();
        });
    }

    private void onSettingKeys() {
        addOutputMessage("Show keys");
        try {
            Desktop.getDesktop().browse(new URL("https://github.com/theawless/sr-lib").toURI());
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
        train_folder.mkdirs();
        String word = textFieldWord.getText();
        if (word.isEmpty()) {
            return;
        }

        addOutputMessage("Adding word: " + word);
        addToItemList(word, wordList);
        populateTextArea(wordList, textAreaWordList);
        statefulActionGUI(() -> {
            // invalidate old models
            wordClean();
            record(getTrainFilename(word));
        });
    }

    private void onWordClean() {
        addOutputMessage("Cleaning word");
        statefulActionGUI(this::wordClean);
    }

    private void onWordTrain() {
        addOutputMessage("Training word");
        model_folder.mkdirs();
        statefulActionGUI(this::wordTrain);
    }

    private void onWordTest() {
        addOutputMessage("Testing word");
        statefulActionGUI(() -> {
            record(getTestFilename());
            String word = wordTest(getTestFilename());
            addOutputMessage("Tested word: " + word);
        });
    }

    private void onSentenceAdd() {
        train_folder.mkdirs();
        String sentence = textFieldSentence.getText();
        if (sentence.isEmpty()) {
            return;
        }

        addOutputMessage("Adding sentence: " + sentence);
        addToItemList(sentence, sentenceList);
        populateTextArea(sentenceList, textAreaSentenceList);
        String words[] = sentence.split("\\s+");
        Arrays.stream(words).forEach((word) -> addToItemList(word, wordList));
        populateTextArea(wordList, textAreaWordList);
        statefulActionGUI(() -> {
            // invalidate old models
            wordClean();
            sentenceClean();
            for (String word : words) {
                addOutputMessage("Adding word: " + word);
                record(getTrainFilename(word));
            }
        });
    }

    private void onSentenceClean() {
        addOutputMessage("Cleaning sentence");
        statefulActionGUI(this::sentenceClean);
    }

    private void onSentenceTrain() {
        addOutputMessage("Training sentence");
        model_folder.mkdirs();
        statefulActionGUI(this::sentenceTrain);
    }

    private void onSentenceTest() {
        addOutputMessage("Testing sentence");
        statefulActionGUI(() -> {
            StringBuilder sentence = new StringBuilder();
            record(getTestFilename());
            String word = sentenceTest(getTestFilename(), true);
            while (!word.isEmpty()) {
                sentence.append(" ").append(word);
                addOutputMessage("Tested word: " + word);
                record(getTestFilename());
                word = sentenceTest(getTestFilename(), false);
            }

            addOutputMessage("Tested sentence: " + sentence.toString());
        });
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

    private String getTestFilename() {
        return new File(train_folder, name).getAbsolutePath();
    }

    private String getTrainFilename(String word) {
        for (int word_index = 0; ; ++word_index) {
            String filename = word + '_' + word_index;
            if (!checkFile(new File(train_folder, filename + '.' + wavRecorder.getExtension()))) {
                return new File(train_folder, filename).getAbsolutePath();
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
                    // do not add line separator new file
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

    private void statefulActionGUI(Runnable action) {
        // so users can't double start a task
        new Thread(() -> {
            setState(State.WAITING);
            action.run();
            setState(State.AUTO);
        }).start();
    }

    private void setState(State endState) {
        SwingUtilities.invokeLater(() -> {
            setEnabledGUI(contentPane, false);
            switch (endState) {
                case AUTO:
                    boolean wordTrainable = checkFile(wordList) && train_folder.listFiles((f, p) -> p.matches(".*\\." + wavRecorder.getExtension())).length > 0;
                    boolean wordTrained = checkFile(wordList) && checkFile(new File(model_folder, "0.model"));
                    boolean sentenceTrainable = checkFile(sentenceList);
                    boolean sentenceTrained = wordTrained && checkFile(sentenceList) && checkFile(new File(model_folder, "0.gram"));

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
                // only user interaction elements
                component.setEnabled(enabled);
            }
        }
    }

    private native void setup(String folder);

    private native void wordTrain();

    private native String wordTest(String filename);

    private void wordClean() {
        String patterns[] = {".*\\.codebook", ".*\\.universe", ".*\\.features", ".*\\.observations", ".*\\.model.*"};
        Arrays.stream(model_folder.listFiles((f, p) -> Arrays.stream(patterns).anyMatch(p::matches))).forEach(File::delete);
    }

    private native void sentenceTrain();

    private native String sentenceTest(String filename, boolean restart);

    private void sentenceClean() {
        String patterns[] = {".*\\.gram"};
        Arrays.stream(model_folder.listFiles((f, p) -> Arrays.stream(patterns).anyMatch(p::matches))).forEach(File::delete);
    }
}