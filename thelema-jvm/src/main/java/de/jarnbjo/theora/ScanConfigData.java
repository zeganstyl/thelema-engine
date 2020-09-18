/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: ScanConfigData.java,v 1.1 2003/03/03 22:09:02 jarnbjo Exp $
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
 * $Log: ScanConfigData.java,v $
 * Revision 1.1  2003/03/03 22:09:02  jarnbjo
 * no message
 *
 */

package de.jarnbjo.theora;

public class ScanConfigData {
   byte[] yuv0ptr;
   byte[] yuv1ptr;
   byte[] srfWorkSpcPtr;
   byte[] dispFragments;

   int[] regionIndex; /* Gives pixel index for top left of each block */
   int videoFrameHeight;
   int videoFrameWidth;
}