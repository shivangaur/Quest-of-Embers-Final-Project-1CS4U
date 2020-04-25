import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Sound {
    private ArrayList<Sound> madeSounds = new ArrayList<>();
    private AudioInputStream inputStream;
    private Clip clip;
    private String filePath;
    FloatControl volume;
    // Constructor
    public Sound(String filePath, int volumeLevel){
        this.filePath = filePath;
        try {
            clip = AudioSystem.getClip();
        }
        catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        loadClip();
        volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        setVolume(volumeLevel);
        madeSounds.add(this);
    }
    private void loadClip(){
        try{
            inputStream = AudioSystem.getAudioInputStream(new File(filePath));
            clip.open(inputStream);
        }
        catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("Sound error");
            e.printStackTrace();
        }
    }
    public void play(){
        clip.setMicrosecondPosition(0);
        clip.start();
    }
    public void resume(){
        clip.start();
    }
    public void pause(){
        clip.stop();
    }
    public void closeSound(){
        clip.close();
    }
    public boolean hasStarted(){
        return clip.isOpen();
    }
    public boolean isPlaying(){
        return clip.isActive();
    }
    public void setVolume(int volumeLevel){
        float range = volume.getMaximum() - volume.getMinimum();
        float gain = (float) (range * (volumeLevel/100.0)) + volume.getMinimum();
        volume.setValue(gain);
    }
}
