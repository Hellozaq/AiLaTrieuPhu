package org.example.model;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public  class PlayAudioURL {
    private static Clip clip;

    public PlayAudioURL() {
    }

    public static void playAudio(URL location,float volume) {
        try {
//            File audioFile = new File(path);
            AudioInputStream udioStream = AudioSystem.getAudioInputStream(location);
            clip = AudioSystem.getClip();
            clip.open(udioStream);
            setVolume(clip,volume);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            ex.printStackTrace();
        }
    }
    public static Clip playStartAudio(URL location,float volume) {
        Clip clipStart = null;
        try {
            clipStart = AudioSystem.getClip();
//            File audioFile = new File(path);
            AudioInputStream udioStream = AudioSystem.getAudioInputStream(location);
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
        playAudio(PlayAudioURL.class.getResource("/audio/pop-3-269281.wav"));
    }

    public static void playPopOnAudio(){
        playAudio(PlayAudioURL.class.getResource("/audio/pop-on-269286.wav"));
    }

    public static void playAudio(URL location) {
        try {
//            File audioFile = new File();
            AudioInputStream udioStream = AudioSystem.getAudioInputStream(location);
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
        playAudio(PlayAudioURL.class.getResource("/audio/wrong-47985.wav"),-10);
    }
}
