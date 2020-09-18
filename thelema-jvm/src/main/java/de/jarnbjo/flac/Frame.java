/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: Frame.java,v 1.1 2003/03/03 21:53:17 jarnbjo Exp $
 * -----------------------------------------------------------
 *
 * $Author: jarnbjo $
 *
 * Description:
 *
 * Copyright 2002-2003 Tor-Einar Jarnbjo
 * -----------------------------------------------------------
 *
 * Change History
 * -----------------------------------------------------------
 * $Log: Frame.java,v $
 * Revision 1.1  2003/03/03 21:53:17  jarnbjo
 * no message
 *
 */

package de.jarnbjo.flac;

import java.io.IOException;
import de.jarnbjo.util.io.*;

public class Frame {

   private int blockSize;
   private final int channels;
   private int bitsPerSample;

   private static final int[] SAMPLE_RATES =
      {0, -1, -1, -1, 8000, 16000, 22050, 24000, 32000, 44100, 48000, 96000, 0, 0, 0, -1};

   private static final int[] SAMPLE_SIZES =
      {0, 8, 12, -1, 16, 20, 24, -1};

   private static final String[] CHANNEL_ASSIGNMENTS = {
      "INDEPENDENT",
      "INDEPENDENT",
      "INDEPENDENT",
      "INDEPENDENT",
      "INDEPENDENT",
      "INDEPENDENT",
      "INDEPENDENT",
      "INDEPENDENT",
      "LEFT_SIDE",
      "RIGHT_SIDE",
      "MID_SIDE"};

   private final Subframe[] subframes;
   private final int[][] pcm;

   public Frame(BitInputStream source, StreamInfo streamInfo) throws IOException {

      if(source.getInt(16)!=0xfff8) {
         throw new FlacFormatException("Audio frame header mismatch");
      }

      int bs=source.getInt(4);
      int sr=source.getInt(4);
      int channelAssignment = source.getInt(4);
      int ss=source.getInt(3);
      source.getBit();

      if(bs==0) {
         /** @todo how?? */
         blockSize=0;
      }
      else if(bs==1) {
         blockSize=192;
      }
      else if(bs>=2 && bs<=5) {
         blockSize=576*(1<<(bs-2));
      }
      else if(bs>=8) {
         blockSize=256*(1<<(bs-8));
      }

      int sampleRate = SAMPLE_RATES[sr];
      if(sampleRate ==-1) {
         throw new FlacFormatException("Invalid sample rate in frame");
      }

      bitsPerSample=SAMPLE_SIZES[ss];
      if(sampleRate ==-1) {
         throw new FlacFormatException("Invalid sample rate in frame");
      }

      long frameNumber = readUtf8Int(source);

      switch(bs) {
      case 0:
         /** @todo read from stream info */
         blockSize=streamInfo.getMaximumBlockSize();
         break;
      case 6:
         blockSize=source.getInt(8)+1;
         break;
      case 7:
         blockSize=source.getInt(16)+1;
         break;
      }

      switch(sr) {
      case 0:
         sampleRate =streamInfo.getSampleRate();
         break;
      case 12:
         sampleRate =source.getInt(8)*1000;
         break;
      case 13:
         sampleRate =source.getInt(16);
         break;
      case 14:
         sampleRate =source.getInt(16)*10;
         break;
      }

      if(ss==0) {
         bitsPerSample=streamInfo.getBitsPerSample();
      }

      if(channelAssignment <8) {
         channels= channelAssignment +1;
      }
      else if(channelAssignment <11) {
         channels=2;
      }
      else {
         throw new FlacFormatException("Unsupported channel assignment in frame");
      }

      int crc=source.getInt(8);


      if(Properties.analyze()) {
         System.out.print("frame="+ frameNumber +"\t");
         System.out.print("blocksize="+blockSize+"\t");
         System.out.print("sample_rate="+ sampleRate +"\t");
         System.out.print("channels="+channels+"\t");
         System.out.println("channel_assignment"+CHANNEL_ASSIGNMENTS[channelAssignment]);
      }

      subframes=new Subframe[channels];

      for(int i=0; i<channels; i++) {
         boolean sideChannel=
            channelAssignment ==8 && i==1 ||
            channelAssignment ==9 && i==0 ||
            channelAssignment ==10 && i==1;

         subframes[i]=Subframe.createInstance(source, this, streamInfo, sideChannel);
      }

      pcm=new int[channels][];

      if(channelAssignment <8) {
         for(int i=0; i<channels; i++) {
            pcm[i]=subframes[i].getPcm();
         }
      }
      else if(channelAssignment ==8) {
         pcm[0]=subframes[0].getPcm();
         pcm[1]=subframes[1].getPcm();
         for(int i=0; i<pcm[0].length; i++) {
            pcm[1][i]=pcm[0][i]-pcm[1][i];
         }
      }
      else if(channelAssignment ==9) {
         pcm[0]=subframes[0].getPcm();
         pcm[1]=subframes[1].getPcm();
         for(int i=0; i<pcm[0].length; i++) {
            pcm[0][i]+=pcm[1][i];
         }
      }
      else if(channelAssignment ==10) {
         pcm[0]=subframes[0].getPcm();
         pcm[1]=subframes[1].getPcm();
         for(int i=0; i<pcm[0].length; i++) {
            int mid=pcm[0][i];
            int side=pcm[1][i];
            mid<<=1;
            if((side&1)==1) mid++;
            pcm[0][i]=(mid+side)>>1;
            pcm[1][i]=(mid-side)>>1;
         }
      }
   }

   public int getBitsPerSample() {
      return bitsPerSample;
   }

   public int getChannels() {
      return channels;
   }

   public int getBlockSize() {
      return blockSize;
   }

   private long readUtf8Int(BitInputStream source) throws IOException {

      long v=0;
      int x=0, i=0;

      x=source.getInt(8);

      if((x&0x80)==0) {
         v=x;
         i=0;
      }
      else if((x&0xe0)==0xc0) { /* 110xxxxx */
         v = x & 0x1f;
         i = 1;
      }
      else if((x&0xf0)==0xe0) { /* 1110xxxx */
         v = x & 0x0F;
         i = 2;
      }
      else if((x&0xf8)==0xf0) { /* 11110xxx */
         v = x & 0x07;
         i = 3;
      }
      else if((x&0xfc)==0xf8) { /* 111110xx */
         v = x & 0x03;
         i = 4;
      }
      else if((x&0xfe)==0xfc) { /* 1111110x */
         v = x & 0x01;
         i = 5;
      }
      else if(x==0xfe) { /* 11111110 */
         v = 0;
         i = 6;
      }
      else {
         return Long.MIN_VALUE;
      }

      for( ; i>0; i--) {
         x=source.getInt(8);

         if((x&0xc0)!=0x80) { /* 10xxxxxx */
            return Long.MIN_VALUE;
         }
         v <<= 6;
         v |= (x & 0x3f);
      }

      return v;
   }

   public Subframe[] getSubframes() {
      return subframes;
   }

   public int[][] getPcm() {
      return pcm;
   }
}