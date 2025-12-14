package SpaceJava;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Sound {

    private Clip clip;
    private final String resourcePath;

    public Sound(String resourcePath) {
        this.resourcePath = resourcePath;
        loadClip();
    }

    private void loadClip() {
        try {
            URL soundURL = locateResource(resourcePath);

            if (soundURL == null) {
                System.err.println("[Sound] Unable to locate audio: " + resourcePath);
                return;
            }

            try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL)) {
                clip = AudioSystem.getClip();
                clip.open(audioStream);
            }
        } catch (UnsupportedAudioFileException e) {
            System.err.println("[Sound] Unsupported format: " + resourcePath);
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            System.err.println("[Sound] Audio line unavailable for: " + resourcePath);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("[Sound] I/O error while loading: " + resourcePath);
            e.printStackTrace();
        }
    }

    private URL locateResource(String path) throws IOException {
        String normalized = path.startsWith("/") ? path.substring(1) : path;

        // Try classpath (resources inside jar / build output)
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL resource = cl.getResource(normalized);

        if (resource != null) {
            return resource;
        }

        // Try with leading slash (for legacy getResource usages)
        resource = cl.getResource("/" + normalized);
        if (resource != null) {
            return resource;
        }

        // Fallback: treat as regular file on disk
        File file = new File(path);
        if (file.exists()) {
            return file.toURI().toURL();
        }

        return null;
    }

    public void play() {
        if (clip == null) {
            // Try reloading once in case the clip failed earlier.
            loadClip();
            if (clip == null) return;
        }

        if (clip.isRunning()) {
            clip.stop();
        }

        clip.setFramePosition(0);
        clip.start();
    }
}