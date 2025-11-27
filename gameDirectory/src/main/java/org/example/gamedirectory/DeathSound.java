package org.example.gamedirectory;
import javax.sound.sampled.*;


public class DeathSound {
    private Clip clip;

    public DeathSound(String resourcePath) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(
                    getClass().getResource(resourcePath)
            );

            clip = AudioSystem.getClip();
            clip.open(ais);  // loads audio into RAM
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if (clip.isRunning()) {
            clip.stop();
        }
        clip.setFramePosition(0); // rewind
        clip.start();
    }
}
