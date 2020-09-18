/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: PbInstance.java,v 1.1 2003/03/03 22:09:02 jarnbjo Exp $
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
 * $Log: PbInstance.java,v $
 * Revision 1.1  2003/03/03 22:09:02  jarnbjo
 * no message
 *
 */

package de.jarnbjo.theora;

import java.io.IOException;
import java.util.*;
import de.jarnbjo.util.io.*;

public class PbInstance {

   Header    info;

   /* how far do we shift the granulepos to seperate out P frame counts? */
   int keyframeGranuleShift;


   /***********************************************************************/
   /* Decoder and Frame Type Information */

   int           decoderErrorCode;
   int           framesHaveBeenSkipped;

   int           postProcessEnabled;
   int           postProcessingLevel;    /* Perform post processing */

   /* Frame Info */
   CodingMode   codingMode;
   byte frameType;
   byte keyFrameType;
   int  qualitySetting;
   int  frameQIndex;            /* Quality specified as a
                                           table index */
   int  thisFrameQualityValue;  /* Quality value for this frame  */
   int  lastFrameQualityValue;  /* Last Frame's Quality */
   int  codedBlockIndex;        /* Number of Coded Blocks */
   int  codedBlocksThisFrame;   /* Index into coded blocks */
   int  frameSize;              /* The number of bytes in the frame. */

   /**********************************************************************/
   /* Frame Size & Index Information */

   int  yPlaneSize;
   int  uvPlaneSize;
   int  yStride;
   int  uvStride;
   int  vFragments;
   int  hFragments;
   int  unitFragments;
   int  yPlaneFragments;
   int  uvPlaneFragments;

   int  reconYPlaneSize;
   int  reconUVPlaneSize;

   int  yDataOffset;
   int  uDataOffset;
   int  vDataOffset;
   int  reconYDataOffset;
   int  reconUDataOffset;
   int  reconVDataOffset;
   int  ySuperBlocks;   /* Number of SuperBlocks in a Y frame */
   int  uvSuperBlocks;  /* Number of SuperBlocks in a U or V frame */
   int  superBlocks;    /* Total number of SuperBlocks in a
                          Y,U,V frame */

   int  ysbRows;        /* Number of rows of SuperBlocks in a
                                   Y frame */
   int  ysbCols;        /* Number of cols of SuperBlocks in a
                                   Y frame */
   int  uvsbRows;       /* Number of rows of SuperBlocks in a
                                   U or V frame */
   int  uvsbCols;       /* Number of cols of SuperBlocks in a
                                   U or V frame */

   int  yMacroBlocks;   /* Number of Macro-Blocks in Y component */
   int  uvMacroBlocks;  /* Number of Macro-Blocks in U/V component */
   int  macroBlocks;    /* Total number of Macro-Blocks */

   /**********************************************************************/
   /* Frames  */
   byte[] thisFrameRecon;
   byte[] goldenFrame;
   byte[] lastFrameRecon;
   byte[] postProcessBuffer;

   /**********************************************************************/
   /* Fragment Information */
   int[]  pixelIndexTable;        /* start address of first
					      pixel of fragment in
					      source */
   int[]  reconPixelIndexTable;  /* start address of first
					      pixel in recon buffer */

   byte[] displayFragments;        /* Fragment update map */
   byte[] skippedDisplayFragments;/* whether fragment YUV
					      Conversion and update is to be
					      skipped */
   int[]   codedBlockList;           /* A list of fragment indices for
					      coded blocks. */
   MotionVector[] fragMVect;                /* fragment motion vectors */

   int[]  fragTokenCounts;          /* Number of tokens per fragment */
   int[][]  tokenList=new int[128][];         /* Fragment Token Pointers */

   int[]   fragmentVariances;
   int[]  fragQIndex;               /* Fragment Quality used in
                                              PostProcess */
   short[][] ppCoefBuffer=new short[64][];        /* PostProcess Buffer for
                                              coefficients data */

   byte[] fragCoeffs;                /* # of coeffs decoded so far for
					       fragment */
   byte[] fragCoefEOB;               /* Position of last non 0 coef
						within QFragData */
   short[][] qFragData=new short[64][];            /* Fragment Coefficients
                                               Array Pointers */
   CodingMode[]   fragCodingMethod;          /* coding method for the
                                               fragment */

   //**********************************************************************/
   /* pointers to addresses used for allocation and deallocation the
      others are rounded up to the nearest 32 bytes */

   CoeffNode[]     _nodes;
   int[]  transIndex;                    /* ptr to table of
						   transposed indexes */

   /**********************************************************************/
   int    bumpLast;

   /* Macro Block and SuperBlock Information */
   int[][][]  blockMap;//=new int[4][4][];
                  /* super block + sub macro
						   block + sub frag ->
						   FragIndex */

   /* Coded flag arrays and counters for them */
   byte[] sbCodedFlags;
   byte[] sbFullyFlags;
   byte[] mbCodedFlags;
   byte[] mbFullyFlags;

   /**********************************************************************/
   int   eobRun;

   Coordinate[] fragCoordinates;
   MotionVector  mVector;
   int    reconPtr2Offset;       /* Offset for second reconstruction
					   in half pixel MC */
   short[] quantizedList;
   short[] reconDataBuffer;

   short   invLastIntraDC;
   short   invLastInterDC;
   short   lastIntraDC;
   short   lastInterDC;

   int   blocksToDecode;        /* Blocks to be decoded this frame */
   int   dcHuffChoice;          /* Huffman table selection variables */
   byte  acHuffChoice;
   int   quadMBListIndex;

   int    byteCount;

   int   bitPattern;
   byte  bitsSoFar;
   byte  nextBit;
   int   bitsLeft;

   short[] dequantBuffer;

   int[]    fpQuantInterUvCoeffs=new int[64];
   int[]    fpQuantInterUvRound=new int[64];
   int[]    fpZeroBinSizeInterUv=new int[64];

   short[] tmpReconBuffer;
   short[] tmpDataBuffer;

   /* Loop filter bounding values */
   int[] filtBoundingValue=new int[512];

   /* Dequantiser and rounding tables */
   int[] qThreshTable=new int[Constants.Q_TABLE_SIZE];
   short[]  dequantInterUvCoeffs;
   int[] quantIndex=new int[64];
   int[] quantYCoeffs=new int[64];
   int[] quantUvCoeffs=new int[64];
   int[] fpQuantYCoeffs=new int[64]; /* used in reiniting quantizers */

   HuffEntry[] huffRootVP3x;
   int[][] huffCodeArrayVP3x;
   byte[][] huffCodeLengthArrayVP3x;
   byte[][] extraBitLengthsVP3x;

   /* Quantiser and rounding tables */
   int[]    fpQuantUvCoeffs=new int[64];
   int[]    fpQuantInterCoeffs=new int[64];
   int[]    fpQuantYRound=new int[64];
   int[]    fpQuantUvRound=new int[64];
   int[]    fpQuantInterRound=new int[64];
   int[]    fpZeroBinSizeY=new int[64];
   int[]    fpZeroBinSizeUv=new int[64];
   int[]    fpZeroBinSizeInter=new int[64];
   int[]   fquantCoeffs;
   int[]   fquantRound;
   int[]   fquantZbSize;
   short[]  dequantYCoeffs;
   short[]  dequantUvCoeffs;
   short[]  dequantInterCoeffs;
   short[]  dequantCoeffs;

   /* Predictor used in choosing entropy table for decoding block patterns. */
   byte blockPatternPredictor;

   short[][] modifier=new short[4][512];
   int[][] modifierPointer=new int[4][2];

   byte[] dataOutputInPtr;




   public PbInstance(Header header) {
      this.info=header;

      reconDataBuffer=new short[64];
      dequantBuffer=new short[64];
      tmpDataBuffer=new short[64];
      tmpReconBuffer=new short[64];
      dequantYCoeffs=new short[64];
      dequantUvCoeffs=new short[64];
      dequantInterCoeffs=new short[64];
      dequantInterUvCoeffs=new short[64];

      modifierPointer[0][0]=0;
      modifierPointer[0][1]=255;
      modifierPointer[1][0]=1;
      modifierPointer[1][1]=255;
      modifierPointer[2][0]=2;
      modifierPointer[2][1]=255;
      modifierPointer[3][0]=3;
      modifierPointer[3][1]=255;

      decoderErrorCode=0;
      keyFrameType=Constants.DCT_KEY_FRAME;
      framesHaveBeenSkipped=0;
   }

   void initFrameDetails() {
      int frameSize;

      postProcessingLevel = 0;


      /* Set the frame size etc. */

      yPlaneSize = info.getWidth() * info.getHeight();
      uvPlaneSize = yPlaneSize / 4;
      hFragments = info.getWidth() / Constants.HFRAGPIXELS;
      vFragments = info.getHeight() / Constants.VFRAGPIXELS;
      unitFragments = ((vFragments * hFragments)*3)/2;
      yPlaneFragments = hFragments * vFragments;
      uvPlaneFragments = yPlaneFragments / 4;

      yStride = info.getWidth() + Constants.STRIDE_EXTRA;
      uvStride = yStride / 2;
      reconYPlaneSize = yStride * (info.getHeight() + Constants.STRIDE_EXTRA);
      reconUVPlaneSize = reconYPlaneSize / 4;
      frameSize = reconYPlaneSize + 2 * reconUVPlaneSize;

      yDataOffset = 0;
      uDataOffset = yPlaneSize;
      vDataOffset = yPlaneSize + uvPlaneSize;
      reconYDataOffset = (yStride * Constants.UMV_BORDER) + Constants.UMV_BORDER;
      reconUDataOffset = reconYPlaneSize + uvStride *
         (Constants.UMV_BORDER/2) + (Constants.UMV_BORDER/2);
      reconVDataOffset = reconYPlaneSize + reconUVPlaneSize +
         (uvStride * (Constants.UMV_BORDER/2)) + (Constants.UMV_BORDER/2);

      /* Image dimensions in Super-Blocks */
      ysbRows = (info.getHeight()/32)+(info.getHeight()%32>0?1:0);
      ysbCols = (info.getWidth()/32)+(info.getWidth()%32>0?1:0);

      uvsbRows = ((info.getHeight()/2)/32)+((info.getHeight()/2)%32>0?1:0);
      uvsbCols = ((info.getWidth()/2)/32)+((info.getWidth()/2)%32>0?1:0);

      /* Super-Blocks per component */
      ySuperBlocks = ysbRows * ysbCols;
      uvSuperBlocks = uvsbRows * uvsbCols;
      superBlocks = ySuperBlocks+2*uvSuperBlocks;

      /* Useful externals */
      yMacroBlocks = ((vFragments+1)/2)*((hFragments+1)/2);
      uvMacroBlocks = ((vFragments/2+1)/2)*((hFragments/2+1)/2);
      macroBlocks = yMacroBlocks+2*uvMacroBlocks;

      initFragmentInfo();
      initFrameInfo(frameSize);
      initializeFragCoordinates();

      /* Configure mapping between quad-tree and fragments */
      createBlockMapping();//ySuperBlocks, uvSuperBlocks, hFragments, vFragments);

      /* Re-initialise the pixel index table. */

      calcPixelIndexTable();
   }

   void initFragmentInfo() {

      /* Perform Fragment Allocations */
      displayFragments = new byte[unitFragments];

      pixelIndexTable = new int[unitFragments];

      reconPixelIndexTable=new int[unitFragments];
      fragTokenCounts=new int[unitFragments];
      codedBlockList=new int[unitFragments];
      fragMVect=new MotionVector[unitFragments];
      fragCoefEOB =new byte[unitFragments];
      skippedDisplayFragments =new byte[unitFragments];
      qFragData =new short[unitFragments][];
      tokenList =new int[unitFragments][];
      fragCodingMethod =new CodingMode[unitFragments];
      fragCoordinates =new Coordinate[unitFragments];
      fragQIndex =new int[unitFragments];
      ppCoefBuffer =new short[unitFragments][];
      fragmentVariances =new int[unitFragments];
      _nodes =new CoeffNode[unitFragments];

      /* Super Block Initialization */
      sbCodedFlags=new byte[superBlocks];
      sbFullyFlags=new byte[superBlocks];

      /* Macro Block Initialization */
      mbCodedFlags=new byte[macroBlocks];
      mbFullyFlags=new byte[macroBlocks];
      blockMap=new int[superBlocks][4][4];
   }

   void initFrameInfo(int frameSize) {
      thisFrameRecon=new byte[frameSize];
      goldenFrame=new byte[frameSize];
      lastFrameRecon=new byte[frameSize];
      postProcessBuffer=new byte[frameSize];
   }

   void initializeFragCoordinates() {

      int i, j, horizFrags=hFragments, vertFrags=vFragments, startFrag=0;

      /* Y */

      for(i = 0; i<vertFrags; i++){
         for(j = 0; j<horizFrags; j++){
            int thisFrag = i * horizFrags + j;
            fragCoordinates[thisFrag]=new Coordinate();
            fragCoordinates[thisFrag].x=j * Constants.BLOCK_HEIGHT_WIDTH;
            fragCoordinates[thisFrag].y=i * Constants.BLOCK_HEIGHT_WIDTH;
         }
      }

      /* U */
      horizFrags >>= 1;
      vertFrags >>= 1;
      startFrag = yPlaneFragments;

      for(i = 0; i<vertFrags; i++) {
         for(j = 0; j<horizFrags; j++) {
            int thisFrag = startFrag + i * horizFrags + j;
            fragCoordinates[thisFrag]=new Coordinate();
            fragCoordinates[thisFrag].x=j * Constants.BLOCK_HEIGHT_WIDTH;
            fragCoordinates[thisFrag].y=i * Constants.BLOCK_HEIGHT_WIDTH;
         }
      }

      /* V */
      startFrag = yPlaneFragments + uvPlaneFragments;
      for(i = 0; i<vertFrags; i++) {
         for(j = 0; j<horizFrags; j++) {
            int thisFrag = startFrag + i * horizFrags + j;
            fragCoordinates[thisFrag]=new Coordinate();
            fragCoordinates[thisFrag].x=j * Constants.BLOCK_HEIGHT_WIDTH;
            fragCoordinates[thisFrag].y=i * Constants.BLOCK_HEIGHT_WIDTH;
         }
      }
   }


   void createBlockMapping () { //int blockMap  (*BlockMap)[4][4],
//			  ogg_uint32_t YSuperBlocks,
//			  ogg_uint32_t UVSuperBlocks,
//			  ogg_uint32_t HFrags, ogg_uint32_t VFrags ) {

      int i, j;

      for ( i=0; i<ySuperBlocks + uvSuperBlocks * 2; i++) {
         for ( j=0; j<4; j++) {
            blockMap[i][j][0] = -1;
            blockMap[i][j][1] = -1;
            blockMap[i][j][2] = -1;
            blockMap[i][j][3] = -1;
         }
      }

      createMapping(0, 0, hFragments, vFragments );
      createMapping(ySuperBlocks, hFragments*vFragments, hFragments/2, vFragments/2 );
      createMapping(ySuperBlocks + uvSuperBlocks, (hFragments*vFragments*5)/4, hFragments/2, vFragments/2 );
   }

   void createMapping (int firstSb, int firstFrag, int hFrags, int vFrags) {

      int i=0, j=0, xpos, ypos, sbRow, sbCol, sbRows, sbCols, mb, b;

      int sb=firstSb, fragIndex=firstFrag;

      /* Set Super-Block dimensions */
      sbRows = vFrags/4 + ( vFrags%4!=0 ? 1 : 0 );
      sbCols = hFrags/4 + ( hFrags%4!=0 ? 1 : 0 );

      /* Map each Super-Block */
      for(sbRow=0; sbRow<sbRows; sbRow++) {
         for (sbCol=0; sbCol<sbCols; sbCol++) {
            /* Y co-ordinate of Super-Block in Block units */
            ypos = sbRow<<2;

            /* Map Blocks within this Super-Block */
            for( i=0; (i<4) && (ypos<vFrags); i++, ypos++) {
               /* X co-ordinate of Super-Block in Block units */
               xpos = sbCol<<2;

	            for( j=0; (j<4) && (xpos<hFrags); j++, xpos++) {
	               if(i<2) {
                     mb = ( j<2 ? 0 : 1 );
	               } else {
                     mb = ( j<2 ? 2 : 3 );
	               }

	               if(i%2!=0) {
	                  b = ( j%2!=0 ? 3 : 2 );
	               } else {
	                  b = ( j%2!=0 ? 1 : 0 );
	               }

                  /* Set mapping and move to next fragment */
                  blockMap[sb][mb][b] = fragIndex++;
	            }

               /* Move to first fragment in next row in Super-Block */
               fragIndex += hFrags-j;
            }

            /* Move on to next Super-Block */
            sb++;
            fragIndex -= i*hFrags-j;
         }

         /* Move to first Super-Block in next row */
         fragIndex += 3*hFrags;
      }
   }

   void calcPixelIndexTable() {
      int i;
      int[] pixelIndexTablePtr;

      /* Calculate the pixel index table for normal image buffers */
      pixelIndexTablePtr = pixelIndexTable;//  pbi->pixel_index_table;
      for ( i = 0; i < yPlaneFragments; i++ ) {
         pixelIndexTablePtr[i] =
            ((i / hFragments) * Constants.VFRAGPIXELS * info.getWidth()) +
            (i % hFragments) * Constants.HFRAGPIXELS;
      }

      //pixelIndexTablePtr = &pbi->pixel_index_table[pbi->YPlaneFragments];
      int offset=yPlaneFragments;
      for ( i = 0; i < ((hFragments >> 1) * vFragments); i++ ) {
         pixelIndexTablePtr[i+offset] =
            ((i / (hFragments / 2) ) * (Constants.VFRAGPIXELS * info.getWidth() / 2)) +
            ((i % (hFragments / 2) ) * Constants.HFRAGPIXELS) + yPlaneSize;
      }

      /************************************************************************/
      /* Now calculate the pixel index table for image reconstruction buffers */
      pixelIndexTablePtr = reconPixelIndexTable;
      for ( i = 0; i < yPlaneFragments; i++ ){
         pixelIndexTablePtr[i] =
            ((i / hFragments) * Constants.VFRAGPIXELS * yStride) +
            ((i % hFragments) * Constants.HFRAGPIXELS) + reconYDataOffset;
      }

      /* U blocks */
      //PixelIndexTablePtr = &pbi->recon_pixel_index_table[pbi->YPlaneFragments];
      //offset=yPlaneFragments;
      for (i = 0; i < uvPlaneFragments; i++ ) {
         pixelIndexTablePtr[i+offset] =
            ((i / (hFragments / 2) ) * (Constants.VFRAGPIXELS * (uvStride))) +
            ((i % (hFragments / 2) ) * Constants.HFRAGPIXELS) + reconUDataOffset;
      }

      /* V blocks */
      //PixelIndexTablePtr =
      //   &pbi->recon_pixel_index_table[pbi->YPlaneFragments +
      //   pbi->UVPlaneFragments];
      offset=yPlaneFragments+uvPlaneFragments;
      for (i = 0; i < uvPlaneFragments; i++ ) {
         pixelIndexTablePtr[i+offset] =
            ((i / (hFragments / 2) ) * (Constants.VFRAGPIXELS * (uvStride))) +
            ((i % (hFragments / 2) ) * Constants.HFRAGPIXELS) + reconVDataOffset;
      }
   }

   void initQTables() {
      //memcpy ( pbi->QThreshTable, QThreshTableV1, sizeof( pbi->QThreshTable ) );
      System.arraycopy(Constants.qThreshTableV1, 0, qThreshTable, 0, qThreshTable.length);
   }

   void initHuffmanSet() {
      int i;

      huffRootVP3x=new HuffEntry[Constants.NUM_HUFF_TABLES];
      huffCodeArrayVP3x=new int[Constants.NUM_HUFF_TABLES][];
      huffCodeLengthArrayVP3x=new byte[Constants.NUM_HUFF_TABLES][];
      extraBitLengthsVP3x=new byte[Constants.NUM_HUFF_TABLES][];

      for ( i = 0; i < Constants.NUM_HUFF_TABLES; i++ ){
         huffCodeArrayVP3x[i] = new int[Constants.MAX_ENTROPY_TOKENS];
         huffCodeLengthArrayVP3x[i] = new byte[Constants.MAX_ENTROPY_TOKENS];
         extraBitLengthsVP3x[i] = new byte[Constants.MAX_ENTROPY_TOKENS];

         buildHuffmanTree(huffRootVP3x,
		      huffCodeArrayVP3x[i],
		      huffCodeLengthArrayVP3x[i],
		      i, Constants.frequencyCountsVP31[i]);
      }
   }

   void  buildHuffmanTree(
      HuffEntry[] huffRoot, int[] huffCodeArray,
      byte[] huffCodeLengthArray, int hIndex, int[] freqList) {

      HuffEntry entryPtr, searchPtr;

      /* First create a sorted linked list representing the frequencies of
        each token. */
      createHuffmanList(huffRoot, hIndex, freqList);

      /* Now build the tree from the list. */

      /* While there are at least two items left in the list. */
      while(huffRoot[hIndex].next!=null) {
         /* Create the new node as the parent of the first two in the list. */
         entryPtr=new HuffEntry();
         entryPtr.value=-1;
         entryPtr.frequency=huffRoot[hIndex].frequency+huffRoot[hIndex].next.frequency;
         entryPtr.zeroChild=huffRoot[hIndex];
         entryPtr.oneChild=huffRoot[hIndex].next;

         /* If there are still more items in the list then insert the new
          node into the list. */
         if(entryPtr.oneChild.next != null) {
            /* Set up the provisional 'new root' */
            huffRoot[hIndex]=entryPtr.oneChild.next;
            huffRoot[hIndex].previous=null;

            /* Now scan through the remaining list to insert the new entry
               at the appropriate point. */
            if(entryPtr.frequency <= huffRoot[hIndex].frequency) {
               entryPtr.next = huffRoot[hIndex];
               huffRoot[hIndex].previous = entryPtr;
               entryPtr.previous = null;
               huffRoot[hIndex] = entryPtr;
            } else {
               searchPtr = huffRoot[hIndex];
               while((searchPtr.next != null) && (searchPtr.frequency < entryPtr.frequency)){
                 searchPtr = searchPtr.next;
               }

               if (searchPtr.frequency < entryPtr.frequency ){
                  entryPtr.next = null;
                  entryPtr.previous = searchPtr;
                  searchPtr.next = entryPtr;
               } else {
                  entryPtr.next = searchPtr;
                  entryPtr.previous = searchPtr.previous;
                  searchPtr.previous.next = entryPtr;
                  searchPtr.previous = entryPtr;
	            }
            }
         } else {
            /* Build has finished. */
            entryPtr.next = null;
            entryPtr.previous = null;
            huffRoot[hIndex] = entryPtr;
         }

         /* Delete the Next/Previous properties of the children (PROB NOT NEC). */
         entryPtr.zeroChild.next = null;
         entryPtr.zeroChild.previous = null;
         entryPtr.oneChild.next = null;
         entryPtr.oneChild.previous = null;

      }

      /* Now build a code array from the tree. */
      createCodeArray( huffRoot[hIndex], huffCodeArray, huffCodeLengthArray, 0, (byte)0);
   }

   void createHuffmanList(HuffEntry[] huffRoot, int hIndex, int[] freqList ) {

      int i;
      HuffEntry entryPtr, searchPtr;

      /* Create a HUFF entry for token zero. */
      huffRoot[hIndex]=new HuffEntry();

      huffRoot[hIndex].previous=null;
      huffRoot[hIndex].next=null;
      huffRoot[hIndex].zeroChild=null;
      huffRoot[hIndex].oneChild=null;
      huffRoot[hIndex].value=0;
      huffRoot[hIndex].frequency=freqList[0];

      if(huffRoot[hIndex].frequency == 0)
         huffRoot[hIndex].frequency = 1;

      /* Now add entries for all the other possible tokens. */
      for(i=1; i<Constants.MAX_ENTROPY_TOKENS; i++) {
         entryPtr=new HuffEntry();

         entryPtr.value=i;
         entryPtr.frequency=freqList[i];
         entryPtr.zeroChild=null;
         entryPtr.oneChild=null;

         /* Force min value of 1. This prevents the tree getting too deep. */
         if(entryPtr.frequency==0)
            entryPtr.frequency=1;

         if (entryPtr.frequency <= huffRoot[hIndex].frequency ){
            entryPtr.next = huffRoot[hIndex];
            huffRoot[hIndex].previous = entryPtr;
            entryPtr.previous = null;
            huffRoot[hIndex] = entryPtr;
         }else{
            searchPtr = huffRoot[hIndex];
            while((searchPtr.next != null) && (searchPtr.frequency < entryPtr.frequency) ){
               searchPtr=searchPtr.next;
            }

            if ( searchPtr.frequency < entryPtr.frequency ){
               entryPtr.next = null;
               entryPtr.previous = searchPtr;
               searchPtr.next = entryPtr;
            }else{
               entryPtr.next = searchPtr;
               entryPtr.previous = searchPtr.previous;
               searchPtr.previous.next = entryPtr;
               searchPtr.previous = entryPtr;
            }
         }
      }
   }

   void createCodeArray(
      HuffEntry huffRoot, int[] huffCodeArray, byte[] huffCodeLengthArray,
      int codeValue, byte codeLength ) {

      /* If we are at a leaf then fill in a code array entry. */
      if ((huffRoot.zeroChild==null) && (huffRoot.oneChild==null)) {
         huffCodeArray[huffRoot.value]=codeValue;
         huffCodeLengthArray[huffRoot.value]=codeLength;
      } else {
         /* Recursive calls to scan down the tree. */
         createCodeArray(huffRoot.zeroChild, huffCodeArray, huffCodeLengthArray, ((codeValue << 1) + 0), (byte)(codeLength + 1));
         createCodeArray(huffRoot.oneChild, huffCodeArray, huffCodeLengthArray, ((codeValue << 1) + 1), (byte)(codeLength + 1));
      }
   }

   void getNextSbInit(BitInputStream source) throws IOException {
      nextBit=(byte)source.getInt(1);

      /* Read run length */
      frArrayDeCodeInit();
      while(frArrayDeCodeSBRun(source.getBit())==0);
}

   byte getNextSbBit(BitInputStream source) throws IOException {
      if (bitsLeft==0) {
         /* Toggle the value.   */
         nextBit=nextBit==1?(byte)0:(byte)1;

         /* Read next run */
         frArrayDeCodeInit();
         while (frArrayDeCodeSBRun(source.getBit())==0);
      }

      /* Have  read a bit */
      bitsLeft--;

      /* Return next bit value */
      return nextBit;
   }

   void getNextBInit(BitInputStream source) throws IOException {
      nextBit=(byte)source.getInt(1);

      /* Read run length */
      frArrayDeCodeInit();
      while (frArrayDeCodeBlockRun(source.getBit())==0);
   }

   byte getNextBBit(BitInputStream source) throws IOException {
      if (bitsLeft==0) {
         /* Toggle the value.   */
         nextBit = nextBit==1?(byte)0:(byte)1;

         /* Read next run */
         frArrayDeCodeInit();
         while(frArrayDeCodeBlockRun(source.getBit())==0);
      }

      /* Have  read a bit */
      bitsLeft--;

      /* Return next bit value */
      return nextBit;
   }

   void frArrayDeCodeInit() {
      /* Initialise the decoding of a run.  */
      bitPattern = 0;
      bitsSoFar = 0;
   }

   int frArrayDeCodeSBRun (boolean bitValue) {
      int retVal = 0;

      /* Add in the new bit value. */
      bitsSoFar++;
      bitPattern=(bitPattern<<1)+(bitValue?1:0);

      /* Coding scheme:
         Codeword            RunLength
         0                       1
         10x                    2-3
         110x                   4-5
         1110xx                 6-9
         11110xxx              10-17
         111110xxxx            18-33
         111111xxxxxxxxxxxx    34-4129
      */

      switch(bitsSoFar) {
      case 1:
         if(bitPattern==0) {
            retVal = 1;
            bitsLeft = 1;
         }
         break;

      case 3:
         /* Bit 1 clear */
         if ((bitPattern&2)==0){
            retVal=1;
            bitsLeft = (bitPattern&1)+2;
         }
         break;

      case 4:
         /* Bit 1 clear */
         if((bitPattern&2)==0) {
            retVal = 1;
            bitsLeft=(bitPattern&1)+4;
         }
         break;

      case 6:
         /* Bit 2 clear */
         if ((bitPattern&4)==0) {
            retVal=1;
            bitsLeft=(bitPattern&3)+6;
         }
         break;

      case 8:
         /* Bit 3 clear */
         if ((bitPattern&8)==0) {
            retVal=1;
            bitsLeft=(bitPattern&7)+10;
         }
         break;

      case 10:
         /* Bit 4 clear */
         if ((bitPattern&0x10)==0) {
            retVal = 1;
            bitsLeft=(bitPattern&0xF)+18;
         }
         break;

      case 18:
         retVal=1;
         bitsLeft=(bitPattern&0xFFF)+34;
         break;

      default:
         retVal=0;
         break;
      }

      return retVal;
   }

   int frArrayDeCodeBlockRun(boolean bitValue) {

      int retVal=0;

      /* Add in the new bit value. */
      bitsSoFar++;
      bitPattern=(bitPattern<<1)+(bitValue?(byte)0:(byte)1);

      /* Coding scheme:
         Codeword           RunLength
         0x                    1-2
         10x                   3-4
         110x                  5-6
         1110xx                7-10
         11110xx              11-14
         11111xxxx            15-30
      */

      switch (bitsSoFar) {
      case 2:
         /* If bit 1 is clear */
         if ((bitPattern&2)==0) {
            retVal = 1;
            bitsLeft =(bitPattern&1)+1;
         }
      break;

      case 3:
         /* If bit 1 is clear */
         if ((bitPattern&2)==0) {
            retVal = 1;
            bitsLeft=(bitPattern&1)+3;
         }
         break;

      case 4:
         /* If bit 1 is clear */
         if ((bitPattern&2)==0) {
            retVal = 1;
            bitsLeft=(bitPattern&1)+5;
         }
         break;

      case 6:
         /* If bit 2 is clear */
         if ((bitPattern&4)==0) {
            retVal = 1;
            bitsLeft=(bitPattern&3)+7;
         }
         break;

      case 7:
         /* If bit 2 is clear */
         if ((bitPattern&4)==0) {
            retVal = 1;
            bitsLeft=(bitPattern&3)+11;
         }
         break;

      case 9:
         retVal = 1;
         bitsLeft=(bitPattern&0xf)+15;
         break;
      }

      return retVal;
   }

   void updateQ(int newQ) {
      int qscale;

      /* Do bounds checking and convert to a float. */
      qscale = newQ;
      if ( qscale < qThreshTable[Constants.Q_TABLE_SIZE-1] ) {
         qscale = qThreshTable[Constants.Q_TABLE_SIZE-1];
      }
      else if ( qscale > qThreshTable[0] ) {
         qscale = qThreshTable[0];
      }

      /* Set the inter/intra descision control variables. */
      frameQIndex = Constants.Q_TABLE_SIZE - 1;
      while ( (int) frameQIndex >= 0 ) {
         if ( (frameQIndex == 0) || ( qThreshTable[frameQIndex] >= newQ) ) {
            break;
         }
         frameQIndex--;
      }

      /* Re-initialise the q tables for forward and reverse transforms. */
      initDequantizer(qscale, (byte)frameQIndex);
   }

   void initDequantizer(int scaleFactor, byte qIndex) {
      int i, j;

      short[] interCoeffs;
      short[] yCoeffs;
      short[] uvCoeffs;
      short[] dcScaleFactorTable;
      short[] uvDcScaleFactorTable;

      interCoeffs = Constants.interCoeffsV1;
      yCoeffs=Constants.yCoeffsV1;
      uvCoeffs=Constants.uvCoeffsV1;
      dcScaleFactorTable=Constants.dcScaleFactorTableV1;
      uvDcScaleFactorTable=Constants.dcScaleFactorTableV1;

      /* invert the dequant index into the quant index
         the dxer has a different order than the cxer. */
      buildQuantIndexGeneric();

      /* Reorder dequantisation coefficients into dct zigzag order. */
      for ( i = 0; i < Constants.BLOCK_SIZE; i++ ) {
         j = quantIndex[i];
         dequantYCoeffs[j]=yCoeffs[i];
      }
      for ( i = 0; i < Constants.BLOCK_SIZE; i++ ){
         j = quantIndex[i];
         dequantInterCoeffs[j]=interCoeffs[i];
      }
      for ( i = 0; i < Constants.BLOCK_SIZE; i++ ){
         j = quantIndex[i];
         dequantYCoeffs[j]=uvCoeffs[i];
      }
      for ( i = 0; i < Constants.BLOCK_SIZE; i++ ){
         j = quantIndex[i];
         dequantInterUvCoeffs[j]=interCoeffs[i];
      }

      /* Intra Y */
      dequantYCoeffs[0] = (short)
         ((dcScaleFactorTable[qIndex] * dequantYCoeffs[0])/100);
      if (dequantYCoeffs[0]<Constants.MIN_DEQUANT_VAL*2)
         dequantYCoeffs[0] = (short) Constants.MIN_DEQUANT_VAL * 2;
      dequantYCoeffs[0] = (short)
         (dequantYCoeffs[0] << Constants.IDCT_SCALE_FACTOR);

      /* Intra UV */
      dequantUvCoeffs[0] = (short)
         ((uvDcScaleFactorTable[qIndex] * dequantUvCoeffs[0])/100);
      if (dequantUvCoeffs[0] < Constants.MIN_DEQUANT_VAL * 2 )
         dequantUvCoeffs[0] = (short) Constants.MIN_DEQUANT_VAL * 2;
      dequantUvCoeffs[0] = (short)
         (dequantUvCoeffs[0] << Constants.IDCT_SCALE_FACTOR);

      /* Inter Y */
      dequantInterCoeffs[0] = (short)
         ((dcScaleFactorTable[qIndex] * dequantInterCoeffs[0])/100);
      if ( dequantInterCoeffs[0] < Constants.MIN_DEQUANT_VAL * 4 )
         dequantInterCoeffs[0] = (short) Constants.MIN_DEQUANT_VAL * 4;
      dequantInterCoeffs[0] = (short)
         (dequantInterCoeffs[0] << Constants.IDCT_SCALE_FACTOR);

      /* Inter UV */
      dequantInterUvCoeffs[0]= (short)
         ((uvDcScaleFactorTable[qIndex] * dequantInterUvCoeffs[0])/100);
      if (dequantInterUvCoeffs[0] < Constants.MIN_DEQUANT_VAL * 4 )
         dequantInterUvCoeffs[0] = (short) Constants.MIN_DEQUANT_VAL * 4;
      dequantInterUvCoeffs[0] = (short)
         (dequantInterUvCoeffs[0] << Constants.IDCT_SCALE_FACTOR);

      for ( i = 1; i < 64; i++ ){
         /* now scale coefficients by required compression factor */
         dequantYCoeffs[i]= (short)
            (( scaleFactor * dequantYCoeffs[i] ) / 100);
         if (dequantYCoeffs[i] < Constants.MIN_DEQUANT_VAL )
            dequantYCoeffs[i] = (short) Constants.MIN_DEQUANT_VAL;
         dequantYCoeffs[i] = (short)
            (dequantYCoeffs[i] << Constants.IDCT_SCALE_FACTOR);

         dequantUvCoeffs[i]= (short)
            (( scaleFactor * dequantUvCoeffs[i] ) / 100);
         if (dequantUvCoeffs[i] < Constants.MIN_DEQUANT_VAL )
            dequantUvCoeffs[i] = (short) Constants.MIN_DEQUANT_VAL;
         dequantUvCoeffs[i] = (short)
            (dequantUvCoeffs[i] << Constants.IDCT_SCALE_FACTOR);

         dequantInterCoeffs[i]= (short)
            (( scaleFactor * dequantInterCoeffs[i] ) / 100);
         if (dequantInterCoeffs[i] < (Constants.MIN_DEQUANT_VAL * 2) )
            dequantInterCoeffs[i] = (short) Constants.MIN_DEQUANT_VAL * 2;
         dequantInterCoeffs[i] = (short)
            (dequantInterCoeffs[i] << Constants.IDCT_SCALE_FACTOR);

         dequantInterUvCoeffs[i]= (short)
            (( scaleFactor * dequantInterUvCoeffs[i] ) / 100);
         if (dequantInterUvCoeffs[i] < (Constants.MIN_DEQUANT_VAL * 2) )
            dequantInterUvCoeffs[i] = (short) Constants.MIN_DEQUANT_VAL * 2;
         dequantInterUvCoeffs[i] = (short)
            (dequantInterUvCoeffs[i] << Constants.IDCT_SCALE_FACTOR);
      }

      dequantCoeffs=dequantYCoeffs;
   }

   void buildQuantIndexGeneric() {
      int i, j;

      /* invert the dequant index into the quant index */
      for (i = 0; i < Constants.BLOCK_SIZE; i++ ){
         j = Constants.dequantIndex[i];
         quantIndex[j] = i;
      }
   }

   CodingMode frArrayUnpackMode(BitInputStream source) throws IOException {
      /* Coding scheme:
        Token                      Codeword           Bits
        Entry   0 (most frequent)  0                   1
        Entry   1       	        10 	            2
        Entry   2       	        110 		    3
        Entry   3       	        1110 		    4
        Entry   4       	        11110 		    5
        Entry   5       	        111110 	            6
        Entry   6       	        1111110 	    7
        Entry   7       	        1111111 	    7
      */

      /* Initialise the decoding. */
      bitPattern = 0;
      bitsSoFar=0;
      bitPattern=source.getInt(1);

      /* Do we have a match */
      if (bitPattern==0) {
         return CodingMode.MODES[0];
      }

      /* Get the next bit */
      bitPattern<<=1;
      if(source.getBit()) {
         bitPattern+=1;
      }

      /* Do we have a match */
      if (bitPattern==0x0002 ) {
         return CodingMode.MODES[1];
      }

      bitPattern<<=1;
      if(source.getBit()) {
         bitPattern+=1;
      }

      if (bitPattern==0x0006) {
         return CodingMode.MODES[2];
      }

      bitPattern<<=1;
      if(source.getBit()) {
         bitPattern+=1;
      }

      if (bitPattern==0x000E) {
         return CodingMode.MODES[3];
      }

      bitPattern<<=1;
      if(source.getBit()) {
         bitPattern+=1;
      }

      if (bitPattern==0x001E) {
         return CodingMode.MODES[4];
      }

      bitPattern<<=1;
      if(source.getBit()) {
         bitPattern+=1;
      }

      if (bitPattern==0x003E) {
         return CodingMode.MODES[5];
      }

      bitPattern<<=1;
      if(source.getBit()) {
         bitPattern+=1;
      }

      if (bitPattern==0x007E) {
         return CodingMode.MODES[6];
      }

      return CodingMode.MODES[7];
   }

   void setupBoundingValueArrayGeneric(int fLimit) {

      int i;
      //ogg_int32_t * BoundingValuePtr = pbi->FiltBoundingValue+256;

      /* Set up the bounding value array. */
      Arrays.fill(filtBoundingValue, 0);

      for ( i = 0; i < fLimit; i++ ){
         filtBoundingValue[256-i-fLimit] = (-fLimit+i);
         filtBoundingValue[256-i] = -i;
         filtBoundingValue[256+i] = i;
         filtBoundingValue[256+i+fLimit] = fLimit-i;
      }
   }

   public int getYStride() {
      return yStride;
   }

   public int getUvStride() {
      return uvStride;
   }

   public int getYOffset() {
      return yDataOffset;
   }

   public int getUOffset() {
      return uDataOffset;
   }

   public int getVOffset() {
      return vDataOffset;
   }
}