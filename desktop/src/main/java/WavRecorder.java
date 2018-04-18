import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class WavRecorder {
    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(16000, 16, 1, true, false);
    private static final AudioFileFormat.Type AUDIO_TYPE = AudioFileFormat.Type.WAVE;

    void record(String filename, double duration) {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, AUDIO_FORMAT);
        try (TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info)) {
            new Thread(() -> {
                try {
                    Thread.sleep((long) (duration * 1000));
                    line.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

            line.open(AUDIO_FORMAT);
            line.start();
            AudioInputStream audioInputStream = new AudioInputStream(line);
            AudioSystem.write(audioInputStream, AUDIO_TYPE, new File(filename + '.' + getExtension()));
        } catch (LineUnavailableException | IOException e) {
            e.printStackTrace();
        }
    }

    String getExtension() {
        return AUDIO_TYPE.getExtension();
    }
}
