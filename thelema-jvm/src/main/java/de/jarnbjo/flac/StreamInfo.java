/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: StreamInfo.java,v 1.1 2003/03/03 21:53:17 jarnbjo Exp $
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
 * $Log: StreamInfo.java,v $
 * Revision 1.1  2003/03/03 21:53:17  jarnbjo
 * no message
 *
 */

package de.jarnbjo.flac;

import java.io.IOException;
import de.jarnbjo.util.io.*;

public class StreamInfo extends MetadataBlock {

   private final int minimumBlockSize;
   private final int maximumBlockSize;
   private final int minimumFrameSize;
   private final int maximumFrameSize;
   private final int sampleRate;
   private final int channels;
   private final int bitsPerSample;
   private final long totalSamples;

   protected StreamInfo(BitInputStream source) throws IOException {

      int length=source.getInt(24);

      minimumBlockSize=source.getInt(16);
      maximumBlockSize=source.getInt(16);
      minimumFrameSize=source.getInt(24);
      maximumFrameSize=source.getInt(24);

      sampleRate=source.getInt(20);
      channels=source.getInt(3)+1;
      bitsPerSample=source.getInt(5)+1;
      totalSamples=source.getLong(36);

      source.getLong(64);
      source.getLong(64);
   }

   public int getMinimumBlockSize() {
      return minimumBlockSize;
   }

   public int getMaximumBlockSize() {
      return maximumBlockSize;
   }

   public int getMinimumFrameSize() {
      return minimumFrameSize;
   }

   public int getMaximumFrameSize() {
      return maximumFrameSize;
   }

   public int getSampleRate() {
      return sampleRate;
   }

   public int getChannels() {
      return channels;
   }

   public int getBitsPerSample() {
      return bitsPerSample;
   }

}