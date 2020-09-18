package de.jarnbjo.theora;

import java.io.*;

import de.jarnbjo.util.io.*;

public class Header {

   private int[] version=new int[3];
   private int width, height, fpsNumerator, fpsDenominator, aspectNumerator, aspectDenominator;
   private int keyframeFrequencyForce, targetBitrate, quality;

   private String versionString;

   private static final int[] HEADER =
      {0x80, 0x74, 0x68, 0x65, 0x6f, 0x72, 0x61};

   public Header(BitInputStream source) throws TheoraFormatException, IOException {

      for(int i=0; i<HEADER.length; i++) {
         if(source.getInt(8)!=HEADER[i]) {
            throw new TheoraFormatException("Theora header mismatch.");
         }
      }

      version[0]=source.getInt(8);
      version[1]=source.getInt(8);
      version[2]=source.getInt(8);

      versionString=version[0]+"."+version[1]+"."+version[2];

      System.out.println("Version: "+versionString);

      if(version[0]!=3 && version[1]>1) {
         throw new TheoraFormatException("Unsupported file format version: "+versionString);
      }

      width=source.getInt(16)<<4;
      height=source.getInt(16)<<4;

      System.out.println("width: "+width);
      System.out.println("height: "+height);

      fpsNumerator=source.getInt(32);
      fpsDenominator=source.getInt(32);

      System.out.println(fpsNumerator);
      System.out.println(fpsDenominator);

      aspectNumerator=source.getInt(24);
      aspectDenominator=source.getInt(24);

      System.out.println(aspectNumerator);
      System.out.println(aspectDenominator);

      keyframeFrequencyForce=1<<source.getInt(5);
      targetBitrate=source.getSignedInt(24);
      quality=source.getSignedInt(6);

      System.out.println(keyframeFrequencyForce);
      System.out.println(targetBitrate);
      System.out.println(quality);
   }

   public int getWidth() {
      return width;
   }

   public int getHeight() {
      return height;
   }

   public double getFrameRate() {
      return (double)fpsNumerator/(double)fpsDenominator;
   }

   public int getKeyframeFrequencyForce() {
      return keyframeFrequencyForce;
   }
}