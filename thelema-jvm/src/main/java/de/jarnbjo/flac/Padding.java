/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: Padding.java,v 1.1 2003/03/03 21:53:17 jarnbjo Exp $
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
 * $Log: Padding.java,v $
 * Revision 1.1  2003/03/03 21:53:17  jarnbjo
 * no message
 *
 */

package de.jarnbjo.flac;

import java.io.IOException;
import de.jarnbjo.util.io.BitInputStream;

public class Padding extends MetadataBlock {

   protected Padding(BitInputStream source) throws FlacFormatException, IOException {
      int length=source.getInt(24);
      for(int i=0; i<length; i++) {
         source.getInt(8);
      }
   }

}