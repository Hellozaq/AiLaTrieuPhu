package org.example.model;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public  class PlayAudio {
    private static Clip clip;

    public PlayAudio() {
    }

    public static void playAudio(String path,float volume) {
        try {
            File audioFile = new File(path);
            AudioInputStream udioStream = AudioSystem.getAudioInputStream(audioFile);
            clip = AudioSystem.getClip();
            clip.open(udioStream);
            setVolume(clip,volume);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            ex.printStackTrace();
        }
    }
    public static Clip playStartAudio(String path,float volume) {
        Clip clipStart = null;
        try {
            clipStart = AudioSystem.getClip();
            File audioFile = new File(path);
            AudioInputStream udioStream = AudioSystem.getAudioInputStream(audioFile);
            clipStart = AudioSystem.getClip();
            clipStart.open(udioStream);
            setVolume(clipStart,volume);
            clipStart.start();

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            ex.printStackTrace();
        }
        return clipStart;
    }

    public static void playClickAudio(){
        playAudio("src/main/java/org/example/file/audio/pop-3-269281.wav");
    }

    public static void playPopOnAudio(){
        playAudio("src/main/java/org/example/file/audio/pop-on-269286.wav");
    }

    public static void playAudio(String path) {
        try {
            File audioFile = new File(path);
            AudioInputStream udioStream = AudioSystem.getAudioInputStream(audioFile);
            clip = AudioSystem.getClip();
            clip.open(udioStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            ex.printStackTrace();
        }
    }

    public static void stopAudio() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }
    public static void stopAudio(Clip clip) {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    public static void setVolume(Clip clip, float volume) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            volumeControl.setValue(+volume); // Đặt âm lượng } }
        }
    }
    public static void wrongSound(){
        playAudio("src/main/java/org/example/file/audio/wrong-47985.wav",-10);
    }
}
