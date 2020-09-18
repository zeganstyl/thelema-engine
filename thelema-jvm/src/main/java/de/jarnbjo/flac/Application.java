/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: Application.java,v 1.1 2003/03/03 21:53:17 jarnbjo Exp $
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
 * $Log: Application.java,v $
 * Revision 1.1  2003/03/03 21:53:17  jarnbjo
 * no message
 *
 */

package de.jarnbjo.flac;

import java.io.IOException;
import de.jarnbjo.util.io.BitInputStream;

public class Application extends MetadataBlock {

   private final byte[] data;

   protected Application(BitInputStream source) throws IOException {

      int length=source.getInt(24);
      data=new byte[length];

      for(int i=0; i<length; i++) {
         data[i]=(byte)source.getInt(8);
      }
   }

   public byte[] getData() {
      return data;
   }
}