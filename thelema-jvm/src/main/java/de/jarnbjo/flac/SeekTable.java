/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: SeekTable.java,v 1.1 2003/03/03 21:53:17 jarnbjo Exp $
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
 * $Log: SeekTable.java,v $
 * Revision 1.1  2003/03/03 21:53:17  jarnbjo
 * no message
 *
 */

package de.jarnbjo.flac;

import java.io.IOException;
import de.jarnbjo.util.io.*;

public class SeekTable extends MetadataBlock {

   protected SeekTable(BitInputStream source) throws IOException {

      int length=source.getInt(24);

      if(length%18!=0) {
         throw new FlacFormatException("SEEKTABLE header length not dividible by 18");
      }

      int points=length/18;

      SeekPoint[] seekPoints = new SeekPoint[points];

      for(int i=0; i<points; i++) {
         long l1=source.getLong(64);
         long l2=source.getLong(64);
         int i3=source.getInt(16);
         seekPoints[i]=new SeekPoint(l1, l2, i3);
      }
   }

}