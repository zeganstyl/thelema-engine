package de.jarnbjo.theora;

import java.io.*;
import java.util.*;

import de.jarnbjo.ogg.*;
import de.jarnbjo.vorbis.Util;
import de.jarnbjo.util.io.*;

/**
 */

public class TheoraStream {

   private LogicalOggStream oggStream;
   private Header header;
   private PbInstance pbInstance;

   private long granulePos=-1;


   public TheoraStream() {
   }

   public TheoraStream(LogicalOggStream oggStream) throws TheoraFormatException, IOException {
      this.oggStream=oggStream;

      header=new Header(new ByteArrayBitInputStream(oggStream.getNextOggPacket(), ByteArrayBitInputStream.BIG_ENDIAN));

      pbInstance=new PbInstance(header);
      pbInstance.initFrameDetails();

      pbInstance.keyframeGranuleShift=Util.ilog(header.getKeyframeFrequencyForce()-1);

      pbInstance.lastFrameQualityValue=0;
      pbInstance.decoderErrorCode=0;

      Arrays.fill(pbInstance.skippedDisplayFragments, (byte)0);

      /* Initialise version specific quantiser values */
      pbInstance.initQTables();

      /* Huffman setup */
      pbInstance.initHuffmanSet();
   }

   public Header getHeader() {
      return header;
   }

   public PbInstance getPbi() {
      return pbInstance;
   }

   public byte[] decodePacket(byte[] packet) throws TheoraFormatException, IOException {

      /*
      System.out.println("oggPage.getPageSequenceNumber: "+oggPage.getPageSequenceNumber());
      int firstByte=oggPage.getData()[0];
      firstByte&=0xff;
      System.out.println("byte 0: "+Integer.toHexString(firstByte));
      System.out.println("");
      */

      if(header==null) {
         header=new Header(new ByteArrayBitInputStream(packet, ByteArrayBitInputStream.BIG_ENDIAN));

         System.out.println("-- header --");
         System.out.println(header.getWidth()+" "+header.getHeight());

         pbInstance=new PbInstance(header);
         pbInstance.initFrameDetails();

         pbInstance.keyframeGranuleShift=Util.ilog(header.getKeyframeFrequencyForce()-1);

         pbInstance.lastFrameQualityValue=0;
         pbInstance.decoderErrorCode=0;

         Arrays.fill(pbInstance.skippedDisplayFragments, (byte)0);

         /* Initialise version specific quantiser values */
         pbInstance.initQTables();

         /* Huffman setup */
         pbInstance.initHuffmanSet();

         return null;
      }

      BitInputStream source=new ByteArrayBitInputStream(packet);
      //BitInputStream source=new ByteArrayBitInputStream(oggPage.getData(), ByteArrayBitInputStream.BIG_ENDIAN);
      pbInstance.decoderErrorCode=0;

      //oggpackB_readinit(&pbi->opb,op->packet,op->bytes);

      if(source.getBit()) {
         throw new TheoraFormatException("Not a video frame");
      }

      loadAndDecode(source);

      if(pbInstance.postProcessingLevel!=0) {
         PostProcess.postProcess(pbInstance);
      }

      granulePos=0;

      //if(oggPage.getAbsoluteGranulePosition()>-1) {
      //   granulePos=oggPage.getAbsoluteGranulePosition();
      if(false) {
         //granulePos=oggPage.getAbsoluteGranulePosition();
      } else {
         if(granulePos==-1) {
	         granulePos=0;
         } else {
	         if(pbInstance.frameType==Constants.BASE_FRAME) {
               long frames = granulePos & ((1<<pbInstance.keyframeGranuleShift)-1);
               granulePos>>=pbInstance.keyframeGranuleShift;
               granulePos+=frames+1;
               granulePos<<=pbInstance.keyframeGranuleShift;
            } else {
               granulePos++;
            }
         }
      }

      Arrays.fill(pbInstance.postProcessBuffer, (byte)0x44);
      return pbInstance.postProcessBuffer;
   }

   void loadAndDecode(BitInputStream source) throws TheoraFormatException, IOException {

      /* Reset the DC predictors. */
      pbInstance.invLastIntraDC=0;
      pbInstance.invLastInterDC=0;

      /* Load the next frame. */
      loadFrame(source);

      if ( (pbInstance.thisFrameQualityValue != pbInstance.lastFrameQualityValue) ){
         /* Initialise DCT tables. */
         pbInstance.updateQ(pbInstance.thisFrameQualityValue );
         pbInstance.lastFrameQualityValue = pbInstance.thisFrameQualityValue;
      }


      /* Decode the data into the fragment buffer. */
      decodeData(source);
   }

   void loadFrame(BitInputStream source) throws TheoraFormatException, IOException {
      loadFrameHeader(source);
      quadDecodeDisplayFragments(source);
   }

   void loadFrameHeader(BitInputStream source) throws TheoraFormatException, IOException {
      int dctQMask, spareBits;

      pbInstance.frameType=(byte)source.getInt(1);
      dctQMask=source.getInt(6);

      if(pbInstance.frameType==Constants.BASE_FRAME) {
         pbInstance.keyFrameType=(byte)source.getInt(1);
         spareBits=source.getInt(2);
      }

      /* Set this frame quality value from Q Index */
      pbInstance.thisFrameQualityValue = pbInstance.qThreshTable[dctQMask];
   }

   void quadDecodeDisplayFragments(BitInputStream source) throws IOException {
      int sb, mb, b, dataToDecode;
      int dfIndex, mbIndex=0;

      /* Reset various data structures common to key frames and inter frames. */
      pbInstance.codedBlockIndex = 0;
      Arrays.fill(pbInstance.displayFragments, (byte)0);

      /* For "Key frames" mark all blocks as coded and return. */
      /* Else initialise the ArrayPtr array to 0 (all blocks uncoded by default) */
      if (pbInstance.frameType==Constants.BASE_FRAME) {
         Arrays.fill(pbInstance.sbFullyFlags, (byte)1);
         Arrays.fill(pbInstance.sbCodedFlags, (byte)1);
         Arrays.fill(pbInstance.mbCodedFlags, (byte)1);
      } else {
         Arrays.fill(pbInstance.sbCodedFlags, (byte)1);
         Arrays.fill(pbInstance.mbCodedFlags, (byte)1);

         /* Un-pack the list of partially coded Super-Blocks */
         pbInstance.getNextSbInit(source);
         for(sb=0; sb<pbInstance.superBlocks; sb++) {
            pbInstance.sbCodedFlags[sb] = pbInstance.getNextSbBit(source);
         }

         /* Scan through the list of super blocks.  Unless all are marked
            as partially coded we have more to do. */
         dataToDecode = 0;
         for (sb=0; sb<pbInstance.superBlocks; sb++ ) {
            if (pbInstance.sbCodedFlags[sb]==0) {
               dataToDecode = 1;
               break;
            }
         }

         /* Are there further block map bits to decode ? */
         if (dataToDecode!=0) {
            /* Un-pack the Super-Block fully coded flags. */
            pbInstance.getNextSbInit(source);
            for(sb=0; sb<pbInstance.superBlocks; sb++) {
               /* Skip blocks already marked as partially coded */
               while((sb < pbInstance.superBlocks) && pbInstance.sbCodedFlags[sb]!=0) {
                  sb++;
               }

               if (sb < pbInstance.superBlocks) {
                  pbInstance.sbFullyFlags[sb] = pbInstance.getNextSbBit(source);

                  if (pbInstance.sbFullyFlags[sb]!=0) {       /* If SB is fully coded. */
                     pbInstance.sbCodedFlags[sb] = 1;       /* Mark the SB as coded */
                  }
               }
            }
         }

         /* Scan through the list of coded super blocks.  If at least one
            is marked as partially coded then we have a block list to
            decode. */
         for ( sb=0; sb<pbInstance.superBlocks; sb++ ) {
            if ((pbInstance.sbCodedFlags[sb]!=0) && (pbInstance.sbFullyFlags[sb]==0)) {
               /* Initialise the block list decoder. */
               pbInstance.getNextBInit(source);
               break;
            }
         }
      }

      /* Decode the block data from the bit stream. */
      for ( sb=0; sb<pbInstance.superBlocks; sb++ ){
         for ( mb=0; mb<4; mb++ ){
            /* If MB is in the frame */
            if ( quadMapToMBTopLeft(pbInstance.blockMap, sb, mb) >= 0 ){
               /* Only read block level data if SB was fully or partially coded */
               if (pbInstance.sbCodedFlags[sb]!=0) {
                  for ( b=0; b<4; b++ ){
                     /* If block is valid (in frame)... */
                     dfIndex = quadMapToIndex1( pbInstance.blockMap, sb, mb, b);
                     if ( dfIndex >= 0 ){
                        if ( pbInstance.sbFullyFlags[sb]!=0 ) {
                           pbInstance.displayFragments[dfIndex] = 1;
                        }
                        else {
                           pbInstance.displayFragments[dfIndex] = pbInstance.getNextBBit(source);
                        }

                        /* Create linear list of coded block indices */
                        if ( pbInstance.displayFragments[dfIndex]!=0 ) {
                           pbInstance.mbCodedFlags[mbIndex] = 1;
                           pbInstance.codedBlockList[pbInstance.codedBlockIndex] = dfIndex;
                           pbInstance.codedBlockIndex++;
                        }
                     }
                  }
               }
               mbIndex++;
            }
         }
      }
   }

   void decodeData(BitInputStream source) throws IOException {
      int i;

      /* Bail out immediately if a decode error has already been reported. */
      if (pbInstance.decoderErrorCode!=0) return;

      /* Clear down the macro block level mode and MV arrays. */
      for ( i = 0; i < pbInstance.unitFragments; i++ ){
         pbInstance.fragCodingMethod[i] = CodingMode.CODE_INTER_NO_MV; /* Default coding mode */
         pbInstance.fragMVect[i]=new Coordinate();
         pbInstance.fragMVect[i].x = 0;
         pbInstance.fragMVect[i].y = 0;
      }

      /* Zero Decoder EOB run count */
      pbInstance.eobRun = 0;

      /* Make a not of the number of coded blocks this frame */
      pbInstance.codedBlocksThisFrame = pbInstance.codedBlockIndex;

      /* Decode the modes data */
      decodeModes(source, pbInstance.ysbRows, pbInstance.ysbCols);

      /* Unpack and decode the motion vectors. */
      /** @todo  */
      //decodeMVectors (pbInstance.ysbRows, pbInstance.ysbCols);

      /* Unpack and decode the actual video data. */
      /** @todo  */
      //unPackVideo();

      /* Reconstruct and display the frame */
      /** @todo  */
      //reconRefFrames();
   }


   void decodeModes(BitInputStream source, int sbRows, int sbCols) throws IOException {
      int fragIndex, mb, sbRow, sbCol, sb=0;
      CodingMode codingMethod;

      int uvRow, uvColumn, uvFragOffset;
      int codingScheme, mbListIndex=0, i;

      /* If the frame is an intra frame then all blocks have mode intra. */
      if (pbInstance.frameType==Constants.BASE_FRAME){
         for ( i = 0; i < pbInstance.unitFragments; i++ ){
            pbInstance.fragCodingMethod[i] = CodingMode.CODE_INTRA;
         }
      } else {
         int modeEntry; /* Mode bits read */

         /* Read the coding method */
         codingScheme = source.getInt(Constants.MODE_METHOD_BITS);

         /* If the coding method is method 0 then we have to read in a
            custom coding scheme */
         if (codingScheme == 0){
            /* Read the coding scheme. */
            for ( i = 0; i < Constants.MAX_MODES; i++ ) {
	            Constants.modeAlphabet[0][source.getInt(Constants.MODE_BITS)]=CodingMode.MODES[i];
            }
         }

         /* Unravel the quad-tree */
         for (sbRow=0; sbRow<sbRows; sbRow++ ){
            for ( sbCol=0; sbCol<sbCols; sbCol++ ){
	            for ( mb=0; mb<4; mb++ ){
                  /* There may be MB's lying out of frame which must be
                     ignored. For these MB's top left block will have a negative
                     Fragment Index. */
                  if (quadMapToMBTopLeft(pbInstance.blockMap, sb, mb) >= 0){
	                  /* Is the Macro-Block coded: */
	                  if (pbInstance.mbCodedFlags[mbListIndex++]!=0) {
	                     /* Upack the block level modes and motion vectors */
	                     fragIndex = quadMapToMBTopLeft( pbInstance.blockMap, sb, mb);

	                     /* Unpack the mode. */
	                     if (codingScheme==Constants.MODE_METHODS-1){
                           /* This is the fall back coding scheme. */
                           /* Simply MODE_BITS bits per mode entry. */
                           codingMethod = CodingMode.MODES[source.getInt(Constants.MODE_BITS)];
                        } else {
		                     modeEntry = pbInstance.frArrayUnpackMode(source).getValue();
		                     codingMethod =  Constants.modeAlphabet[codingScheme][modeEntry];
	                     }

                        /* Note the coding mode for each block in macro block. */
                        pbInstance.fragCodingMethod[fragIndex] = codingMethod;
                        pbInstance.fragCodingMethod[fragIndex + 1] = codingMethod;
                        pbInstance.fragCodingMethod[fragIndex + pbInstance.hFragments] = codingMethod;
	                     pbInstance.fragCodingMethod[fragIndex + pbInstance.hFragments + 1] = codingMethod;

                        /* Matching fragments in the U and V planes */
                        uvRow = (fragIndex / (pbInstance.hFragments * 2));
                        uvColumn = (fragIndex % pbInstance.hFragments) / 2;
                        uvFragOffset = (uvRow * (pbInstance.hFragments / 2)) + uvColumn;
                        pbInstance.fragCodingMethod[pbInstance.yPlaneFragments + uvFragOffset] = codingMethod;
	                     pbInstance.fragCodingMethod[pbInstance.yPlaneFragments + pbInstance.uvPlaneFragments + uvFragOffset] = codingMethod;
	                  }
	               }
	            }

               /* Next Super-Block */
               sb++;
            }
         }
      }
   }

   private static int quadMapToIndex1(int[][][] blockMap, int sb, int mb, int b) {
      return blockMap[sb][Constants.mbOrderMap[mb]][Constants.blockOrderMap1[mb][b]];
   }


   private static int quadMapToMBTopLeft(int[][][] blockMap,int sb, int mb) {
      return blockMap[sb][Constants.mbOrderMap[mb]][0];
   }

}