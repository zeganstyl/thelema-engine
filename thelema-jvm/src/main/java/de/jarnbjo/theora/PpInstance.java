/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: PpInstance.java,v 1.1 2003/03/03 22:09:02 jarnbjo Exp $
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
 * $Log: PpInstance.java,v $
 * Revision 1.1  2003/03/03 22:09:02  jarnbjo
 * no message
 *
 */

package de.jarnbjo.theora;

public class PpInstance {

   int prevFrameLimit;

   int[] scanPixelIndexTable;
   byte[] scanDisplayFragments;

   byte[][] prevFragments=new byte[Constants.MAX_PREV_FRAMES][];

   int[] fragScores; /* The individual frame difference ratings. */
   byte[] sameGreyDirPixels;
   byte[] barBlockMap;

   /* Number of pixels changed by diff threshold in row of a fragment. */
   byte[] fragDiffPixels;

   byte[] pixelScores;
   byte[] pixelChangedMap;
   byte[] chLocals;
   short[] yuvDifferences;
   int[] rowChangedPixels;
   byte[] tmpCodedMap;

   /* Plane pointers and dimension variables */
   byte[] yPlanePtr0;
   byte[] yPlanePtr1;
   byte[] uPlanePtr0;
   byte[] uPlanePtr1;
   byte[] vPlanePtr0;
   byte[] vPlanePtr1;

   int    videoYPlaneWidth;
   int    videoYPlaneHeight;
   int    videoUVPlaneWidth;
   int    videoUVPlaneHeight;

   int    videoYPlaneStride;
   int    videoUPlaneStride;
   int    videoVPlaneStride;

   /* Scan control variables. */
   byte   hFragPixels;
   byte   vFragPixels;

   int    scanFrameFragments;
   int    scanYPlaneFragments;
   int    scanUVPlaneFragments;
   int    scanHFragments;
   int    scanVFragments;

   int    yFramePixels;
   int    uvFramePixels;

   int    sgcThresh;

   int    outputBlocksUpdated;
   int    kfIndicator;

   /* The pre-processor scan configuration. */
   ScanConfigData scanConfig;

   int   srfGreyThresh;
   int   srfColThresh;
   int   sgcLevelThresh;
   int   suvcLevelThresh;

   int  noiseSupLevel;

   /* Block Thresholds. */
   int  primaryBlockThreshold;
   byte[] lineSearchTripTresh;

   int   pakEnabled;

   int   levelThresh;
   int   negLevelThresh;
   int   srfThresh;
   int   negSrfThresh;
   int   highChange;
   int   negHighChange;

   /* Threshold lookup tables */
   byte[] srfPakThreshTable=new byte[512];
   byte[] srfThreshTable=new byte[512];
   byte[] sgcThreshTable=new byte[512];

   /* Variables controlling S.A.D. break outs. */
   int grpLowSadThresh;
   int grpHighSadThresh;
   int modifiedGrpLowSadThresh;
   int modifiedGrpHighSadThresh;

   int  planeHFragments;
   int  planeVFragments;
   int  planeHeight;
   int  planeWidth;
   int  planeStride;

   int blockThreshold;
   int blockSgcThresh;
   double uvBlockThreshCorrection;
   double uvSgcCorrection;

   double yuvPlaneCorrectionFactor;
   double[] absDiff_ScoreMultiplierTable=new double[256];
   byte[] noiseScoreBoostTable=new byte[256];
   byte maxLineSearchLen;

   int yuvDiffsCircularBufferSize;
   int chLocalsCircularBufferSize;
   int pixelMapCircularBufferSize;

   void initFrameInfo() {
      int i;
      //PClearFrameInfo(ppi);

      scanPixelIndexTable = new int[scanFrameFragments];
      scanDisplayFragments = new byte[scanFrameFragments];

      for(i=0; i<Constants.MAX_PREV_FRAMES; i++) {
         prevFragments[i]=new byte[scanFrameFragments];
      }

      fragScores=new int[scanFrameFragments];
      sameGreyDirPixels=new byte[scanFrameFragments];
      fragDiffPixels=new byte[scanFrameFragments];

      barBlockMap=new byte[3*scanHFragments];
      tmpCodedMap=new byte[scanHFragments];
      rowChangedPixels=new int[3*scanConfig.videoFrameHeight];

      pixelScores=new byte[scanConfig.videoFrameWidth*Constants.PSCORE_CB_ROWS];
      pixelChangedMap=new byte[scanConfig.videoFrameWidth*Constants.PMAP_CB_ROWS];
      chLocals=new byte[scanConfig.videoFrameWidth*Constants.CHLOCALS_CB_ROWS];
      yuvDifferences=new short[scanConfig.videoFrameWidth*Constants.YDIFF_CB_ROWS];
   }

   void initInstance() {

      /* Initializations */
      prevFrameLimit = 3; /* Must not exceed MAX_PREV_FRAMES (Note
                  that this number includes the current
                  frame so "1 = no effect") */

      /* Scan control variables. */
      hFragPixels = 8;
      vFragPixels = 8;

      srfGreyThresh = 4;
      srfColThresh = 5;
      noiseSupLevel = 3;
      sgcLevelThresh = 3;
      suvcLevelThresh = 4;

      /* Variables controlling S.A.D. breakouts. */
      grpLowSadThresh = 10;
      grpHighSadThresh = 64;
      primaryBlockThreshold = 5;
      sgcThresh = 16;  /* (Default values for 8x8 blocks). */

      uvBlockThreshCorrection = 1.25;
      uvSgcCorrection = 1.5;

      maxLineSearchLen = Constants.MAX_SEARCH_LINE_LEN;
   }

   void clear() {
      int i;

      scanPixelIndexTable=null;
      scanDisplayFragments=null;

      for(i = 0 ; i < Constants.MAX_PREV_FRAMES ; i ++) {
         prevFragments[i]=null;
      }

      fragScores=null;
      sameGreyDirPixels=null;
      fragDiffPixels=null;
      barBlockMap=null;
      tmpCodedMap=null;
      rowChangedPixels=null;
      pixelScores=null;
      pixelChangedMap=null;
      chLocals=null;
      yuvDifferences=null;
   }


}