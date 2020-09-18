/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: HuffEntry.java,v 1.1 2003/03/03 22:09:02 jarnbjo Exp $
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
 * $Log: HuffEntry.java,v $
 * Revision 1.1  2003/03/03 22:09:02  jarnbjo
 * no message
 *
 */

package de.jarnbjo.theora;

public class HuffEntry {
   public HuffEntry zeroChild, oneChild, previous, next;
   int value, frequency;
}