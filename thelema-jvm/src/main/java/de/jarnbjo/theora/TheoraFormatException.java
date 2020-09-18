/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: TheoraFormatException.java,v 1.1 2003/03/03 22:09:02 jarnbjo Exp $
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
 * $Log: TheoraFormatException.java,v $
 * Revision 1.1  2003/03/03 22:09:02  jarnbjo
 * no message
 *
 */

package de.jarnbjo.theora;

import java.io.IOException;

/**
 * Exception thrown when trying to read a corrupted Vorbis stream.
 */

public class TheoraFormatException extends IOException {

   public TheoraFormatException() {
      super();
   }

   public TheoraFormatException(String message) {
      super(message);
   }
}