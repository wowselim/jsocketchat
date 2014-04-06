import java.io.*;

import javax.sound.sampled.*;

public class AudioPlayer implements Runnable {
  private final int BUFFER_SIZE = 1024;
  private File soundFile;
  private AudioInputStream audioStream;
  private AudioFormat audioFormat;
  private SourceDataLine sourceLine;
  private String filename;

  public AudioPlayer(String filename) {
    this.filename = filename;
  }  

  public void run() {
    try {
      soundFile = new File(filename);
      audioStream = AudioSystem.getAudioInputStream(soundFile);
    } catch (Exception e){
      System.out.println("Sound playback failed.");
    }

    audioFormat = audioStream.getFormat();

    DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
    try {
      sourceLine = (SourceDataLine) AudioSystem.getLine(info);
      sourceLine.open(audioFormat);
    } catch (Exception e) {
      System.out.println("Sound playback failed.");
    }

    sourceLine.start();

    int nBytesRead = 0;
    byte[] abData = new byte[BUFFER_SIZE];
    while (nBytesRead != -1) {
      try {
          nBytesRead = audioStream.read(abData, 0, abData.length);
      } catch (IOException e) {
          e.printStackTrace();
      }
      if (nBytesRead >= 0) {
          int nBytesWritten = sourceLine.write(abData, 0, nBytesRead);
      }
    }

    sourceLine.drain();
    sourceLine.close();
  }
}