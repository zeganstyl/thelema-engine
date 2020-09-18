/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: FlacStream.java,v 1.2 2003/04/10 19:48:52 jarnbjo Exp $
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
 * $Log: FlacStream.java,v $
 * Revision 1.2  2003/04/10 19:48:52  jarnbjo
 * no message
 *
 * Revision 1.1  2003/03/03 21:53:17  jarnbjo
 * no message
 *
 */

package de.jarnbjo.flac;

import java.io.*;
import java.util.*;

import de.jarnbjo.ogg.*;
import de.jarnbjo.util.io.*;


public class FlacStream {

   private LogicalOggStream oggStream;

   private final List<MetadataBlock> metadataBlocks=new ArrayList<>();

   private StreamInfo streamInfo;
   private VorbisComment vorbisComment;

   private static final int MODE_HEADER = 0;
   private static final int MODE_AUDIO = 1;

   private int mode=MODE_HEADER;

   public FlacStream() {
   }

   public FlacStream(LogicalOggStream oggStream) throws IOException {
      this.oggStream=oggStream;

      MetadataBlock mdBlock=null;

      byte[] header=oggStream.getNextOggPacket();

      if (!(header.length==4 &&
         header[0]==0x66 &&
         header[1]==0x4c &&
         header[2]==0x61 &&
         header[3]==0x43)) {

         throw new FlacFormatException("Header mismatch");
      }

      do {
         BitInputStream source=new ByteArrayBitInputStream(oggStream.getNextOggPacket(), ByteArrayBitInputStream.BIG_ENDIAN);

         mdBlock=MetadataBlock.createInstance(source);
         metadataBlocks.add(mdBlock);

         if(mdBlock instanceof StreamInfo) {
            streamInfo=(StreamInfo)mdBlock;
         }
         
         if(mdBlock instanceof VorbisComment) {
         	vorbisComment=(VorbisComment)mdBlock;	
         }

      } while(!mdBlock.isLastBlock());

      if(streamInfo==null) {
         throw new FlacFormatException("Missing STREAM_INFO header block");
      }

   }

   public Frame getNextFrame() throws FlacFormatException, IOException {
      return new Frame(new ByteArrayBitInputStream(oggStream.getNextOggPacket(), ByteArrayBitInputStream.BIG_ENDIAN), streamInfo);
   }

   public int readPcm(byte[] buffer, int offset, int length) throws IOException {

      //System.out.println("readPcm()");

      final int channels=streamInfo.getChannels();

      Frame lastFrame = getNextFrame();

      int[][] pcm= lastFrame.getPcm();

      int written=0;

      for(int i=0; i<pcm[0].length; i++) {
         for(int j=0; j<channels; j++) {
            int sample=(int)pcm[j][i];
            buffer[offset+written++]=(byte)(sample>>8);
            buffer[offset+written++]=(byte)(sample&255);
         }
      }

      return written;
   }

   public StreamInfo getStreamInfo() {
      return streamInfo;
   }
   
   public VorbisComment getVorbisComment() {
   	return vorbisComment;	
   }


   public byte[] processPacket(byte[] packet) throws FlacFormatException, IOException {
      if(packet.length==0) {
         throw new FlacFormatException("Cannot decode a flac frame with length = 0");
      }
      if(mode==MODE_HEADER) {
         BitInputStream source=new ByteArrayBitInputStream(packet, ByteArrayBitInputStream.BIG_ENDIAN);

         MetadataBlock mdBlock=MetadataBlock.createInstance(source);
         metadataBlocks.add(mdBlock);

         if(mdBlock instanceof StreamInfo) {
            streamInfo=(StreamInfo)mdBlock;
         }

         if(mdBlock.isLastBlock()) {
            mode=MODE_AUDIO;
         }

         return null;
      }
      else {
         // audio packet
         //System.out.println("AUDIO_PACKET");
         if(streamInfo==null) {

            throw new FlacFormatException("Cannot decode audio frame before STREAM_INFO header packet has been decoded.");
         }

         Frame frame=new Frame(new ByteArrayBitInputStream(packet, ByteArrayBitInputStream.BIG_ENDIAN), streamInfo);

         int[][] pcm=frame.getPcm();

         byte[] res=new byte[pcm.length*pcm[0].length*2];

         int k=0;

         for(int i=0; i<pcm[0].length; i++) {
            for(int j=0; j<pcm.length; j++) {
               res[k++]=(byte)(pcm[j][i]>>8);
               res[k++]=(byte)(pcm[j][i]&0xff);
            }
         }

         return res;
      }
   }
}