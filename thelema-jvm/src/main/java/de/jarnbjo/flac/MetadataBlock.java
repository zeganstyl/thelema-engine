/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: MetadataBlock.java,v 1.1 2003/03/03 21:53:17 jarnbjo Exp $
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
 * $Log: MetadataBlock.java,v $
 * Revision 1.1  2003/03/03 21:53:17  jarnbjo
 * no message
 *
 */

package de.jarnbjo.flac;

import java.io.IOException;
import de.jarnbjo.util.io.*;

public abstract class MetadataBlock {

   private static final int STREAMINFO = 0;
   private static final int PADDING = 1;
   private static final int APPLICATION = 2;
   private static final int SEEKTABLE = 3;
   private static final int VORBIS_COMMENT = 4;

   private boolean lastBlock;

   public static MetadataBlock createInstance(BitInputStream source) throws IOException {

      boolean lb=source.getBit();

      int blockType=source.getInt(7);

      MetadataBlock mb;

      switch(blockType) {
         // BLOCK_TYPE
         case STREAMINFO:
            mb=new StreamInfo(source);
            mb.lastBlock=lb;
            return mb;
         case SEEKTABLE:
            mb=new SeekTable(source);
            mb.lastBlock=lb;
            return mb;
         case VORBIS_COMMENT:
            mb=new VorbisComment(source);
            mb.lastBlock=lb;
            return mb;
         case PADDING:
            mb=new Padding(source);
            mb.lastBlock=lb;
            return mb;
         case APPLICATION:
            mb=new Application(source);
            mb.lastBlock=lb;
            return mb;
      }

      throw new FlacFormatException("Unsupported block type "+blockType);
   }

   boolean isLastBlock() {
      return lastBlock;
   }
}