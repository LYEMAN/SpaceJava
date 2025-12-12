package SpaceJava;

import javax.sound.sampled.*;
import java.io.File;

public class Sound {

    private Clip clip;

    public Sound(String filePath) {
        try {
            File soundFile = new File(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            clip = AudioSystem.getClip();
            clip.open(audioStream);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if (clip == null) return;
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }
}

