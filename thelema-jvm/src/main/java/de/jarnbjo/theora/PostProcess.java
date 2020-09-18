/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: PostProcess.java,v 1.1 2003/03/03 22:09:02 jarnbjo Exp $
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
 * $Log: PostProcess.java,v $
 * Revision 1.1  2003/03/03 22:09:02  jarnbjo
 * no message
 *
 */

package de.jarnbjo.theora;

import java.util.Arrays;

public class PostProcess {

   private static final int[] sharpenModifier = {
      -12, -11, -10, -10,  -9,  -9,  -9,  -9,
      -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,
      -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,
      -2,  -2,  -2,  -2,  -2,  -2,  -2,  -2,
      -2,  -2,  -2,  -2,  -2,  -2,  -2,  -2,
      0,  0,  0,  0,  0,  0,  0,  0,
      0,  0,  0,  0,  0,  0,  0,  0,
      0,  0,  0,  0,  0,  0,  0,  0
   };

   private static final int[] dcQuantScaleV1 = {
      22, 20, 19, 18, 17, 17, 16, 16,
      15, 15, 14, 14, 13, 13, 12, 12,
      11, 11, 10, 10, 9,  9,  9,  8,
      8,  8,  7,  7,  7,  6,  6,  6,
      6,  5,  5,  5,  5,  4,  4,  4,
      4,  4,  3,  3,  3,  3,  3,  3,
      3,  2,  2,  2,  2,  2,  2,  2,
      2,  1,  1,  1,  1,  1,  1,  1
   };

   private static int deringModifierV1;//=DcQuantScaleV1;

   static void postProcess(PbInstance pbi) {

      switch (pbi.postProcessingLevel){
      case 8:
         /* on a slow machine, use a simpler and faster deblocking filter */
         deblockFrame(pbi, pbi.lastFrameRecon, pbi.postProcessBuffer);
         break;
      case 6:
         deblockFrame(pbi, pbi.lastFrameRecon, pbi.postProcessBuffer);
         updateUmvBorder(pbi, pbi.postProcessBuffer);
         deringFrame(pbi, pbi.postProcessBuffer, pbi.postProcessBuffer);
         break;
      case 5:
         deblockFrame(pbi, pbi.lastFrameRecon, pbi.postProcessBuffer);
         updateUmvBorder(pbi, pbi.postProcessBuffer );
         deringFrame(pbi, pbi.postProcessBuffer, pbi.postProcessBuffer);
         break;

      case 4:
         deblockFrame(pbi, pbi.lastFrameRecon, pbi.postProcessBuffer);
         break;

      case 1:
         updateFragQIndex(pbi);
         break;

      case 0:
         break;

      default:
         deblockFrame(pbi, pbi.lastFrameRecon, pbi.postProcessBuffer);
         updateUmvBorder(pbi, pbi.postProcessBuffer);
         deringFrame(pbi, pbi.postProcessBuffer, pbi.postProcessBuffer);
         break;
      }
   }

   static void deblockFrame(PbInstance pbi, byte[] sourceBuffer, byte[] destinationBuffer) {

      Arrays.fill(pbi.fragmentVariances, 0);

      updateFragQIndex(pbi);
      setupLoopFilter(pbi);

      /* Y */
      /** @todo  */
      //deblockPlane(pbi, sourceBuffer, destinationBuffer, 0);

      /* U */
      /** @todo  */
      //deblockPlane(pbi, sourceBuffer, destinationBuffer, 1);

      /* V */
      /** @todo  */
      //deblockPlane(pbi, sourceBuffer, destinationBuffer, 2);
   }


   static void deringBlockStrong(
      byte[] srcPtr, int srcOffset, byte[] dstPtr, int dstOffset,
      int pitch, int fragQIndex, int[] quantScale) {

      short[] udMod=new short[72];
      short[] lrMod=new short[72];
      int j,k,l;
      int src;
      int qValue = quantScale[fragQIndex];

      byte p, pl, pr, pu, pd;
      int al, ar, au, ad, atot, b, newVal;

      byte[] curRow=srcPtr;
      byte[] dstRow=dstPtr;
      //const unsigned char *curRow = SrcPtr - 1; /* avoid negative array indexes */
      //unsigned char *dstRow = DstPtr;
      //const unsigned char *lastRow = SrcPtr-Pitch;
      //const unsigned char *nextRow = SrcPtr+Pitch;
      int lastRowOffset=srcOffset-pitch;
      int nextRowOffset=srcOffset+pitch;

      int rowOffset=0, round=1<<6;
      int high, low, tmpMod;

      int sharpen=sharpenModifier[fragQIndex];

      high=3*qValue;
      if(high>32) {
         high=32;
      }
      low=0;

      src=srcOffset-pitch;
      for(k=0; k<9; k++) {
         for(j=0; j<8; k++) {
            tmpMod=srcPtr[src+j+pitch]-srcPtr[src+j];
            if(tmpMod>0) tmpMod=-tmpMod;
            tmpMod+=32+qValue;

            if(tmpMod<-64) {
               tmpMod=sharpen;
            }
            else if(tmpMod<low) {
               tmpMod=low;
            }
            else if(tmpMod>high) {
               tmpMod=high;
            }

            udMod[k*8+j]=(short)tmpMod;
         }
         src+=pitch;
      }
  /* Initialize the Mod Data */
  /*
  Src = SrcPtr-Pitch;
  for(k=0;k<9;k++){
    for(j=0;j<8;j++){

      TmpMod = 32 + QValue - (abs(Src[j+Pitch]-Src[j]));

      if(TmpMod< -64)
	TmpMod = Sharpen;

      else if(TmpMod<Low)
	TmpMod = Low;

      else if(TmpMod>High)
	TmpMod = High;

      UDMod[k*8+j] = (ogg_int16_t)TmpMod;
    }
    Src +=Pitch;
  }
   */

      src=srcOffset-1;
      for(k=0; k<9; k++) {
         for(j=0; j<8; k++) {
            tmpMod=srcPtr[src+j+1]-srcPtr[src+j];
            if(tmpMod>0) {
               tmpMod=-tmpMod;
            }
            tmpMod+=32+qValue;

            if(tmpMod<-64) {
               tmpMod=sharpen;
            }
            if(tmpMod<0) {
               tmpMod=low;
            }
            if(tmpMod>high) {
               tmpMod=high;
            }

            lrMod[k*9+j]=(short)tmpMod;
         }
         src+=pitch;
      }
   /*
  Src = SrcPtr-1;

  for(k=0;k<8;k++){
    for(j=0;j<9;j++){
      TmpMod = 32 + QValue - (abs(Src[j+1]-Src[j]));

      if(TmpMod< -64 )
	TmpMod = Sharpen;

      else if(TmpMod<0)
	TmpMod = Low;

      else if(TmpMod>High)
	TmpMod = High;

      LRMod[k*9+j] = (ogg_int16_t)TmpMod;
    }
    Src+=Pitch;
  }
   */

      /* In the case that this function called with same buffer for
         source and destination, To keep the c and the mmx version to have
         consistant results, intermediate buffer is used to store the
         eight pixel value before writing them to destination
         (i.e. Overwriting souce for the speical case) */

      for(k=0; k<8; k++) {
         for(l=0; l<8; l++) {
            atot=128;
            b=round;
            p=srcPtr[srcOffset+rowOffset+1+1];

            pl=srcPtr[srcOffset+rowOffset+1];
            al=lrMod[k*9+l];
            atot-=al;
            b+=al*pl;

            pu=srcPtr[lastRowOffset+rowOffset+1];
            au=udMod[k*8+l];
            atot-=au;
            b+=au*pu;

            pd=srcPtr[nextRowOffset+rowOffset+l];
            ad=udMod[(k+1)*8+l];
            atot-=ad;
            b+=ad*pd;

            pr=srcPtr[srcOffset+rowOffset+l+2];
            ar=lrMod[k*9+l+1];
            atot-=ar;
            b+=ar*pr;

            newVal=(atot*p+b)>>7;

            dstPtr[dstOffset+rowOffset+l]= clamp255(newVal);
         }
         rowOffset+=pitch;
      }

   /*
  for(k=0;k<8;k++){
    for(l=0;l<8;l++){

      atot = 128;
      B = round;
      p = curRow[ rowOffset +l +1];

      pl = curRow[ rowOffset +l];
      al = LRMod[k*9+l];
      atot -= al;
      B += al * pl;

      pu = lastRow[ rowOffset +l];
      au = UDMod[k*8+l];
      atot -= au;
      B += au * pu;

      pd = nextRow[ rowOffset +l];
      ad = UDMod[(k+1)*8+l];
      atot -= ad;
      B += ad * pd;

      pr = curRow[ rowOffset +l+2];
      ar = LRMod[k*9+l+1];
      atot -= ar;
      B += ar * pr;

      newVal = ( atot * p + B) >> 7;

      dstRow[ rowOffset +l]= clamp255( newVal );
    }
    rowOffset += Pitch;
  }
      */
   }


   static void deringBlockWeak(
      byte[] srcPtr, int srcOffset, byte[] dstPtr, int dstOffset,
      int pitch, int fragQIndex, int[] quantScale) {

      /** @todo implement */

      deringBlockStrong(srcPtr, srcOffset, dstPtr, dstOffset, pitch, fragQIndex, quantScale);
   }


   static void deringFrame(PbInstance pbi, byte[] src, byte[] dst) {
      int col,row;
      int srcOffset, dstOffset;
      //unsigned char  *SrcPtr;
      //unsigned char  *DestPtr;
      int blocksAcross, blocksDown;
      int[] quantScale;
      int block;
      int lineLength;

      int thresh1, thresh2, thresh3, thresh4;

      thresh1 = 384;
      thresh2 = 4 * thresh1;
      thresh3 = 5 * thresh2/4;
      thresh4 = 5 * thresh2/2;

      quantScale = dcQuantScaleV1;//[deringModifierV1];

      blocksAcross = pbi.hFragments;
      blocksDown = pbi.hFragments;

      srcOffset=pbi.reconYDataOffset;
      dstOffset=pbi.reconYDataOffset;
      //SrcPtr = Src + pbi->ReconYDataOffset;
      //DestPtr = Dst + pbi->ReconYDataOffset;
      lineLength = pbi.yStride;

      block = 0;

      for ( row = 0 ; row < blocksDown; row ++){
         for (col = 0; col < blocksAcross; col ++){
            int quality = pbi.fragQIndex[block];
            int variance = pbi.fragmentVariances[block];

            if(pbi.postProcessingLevel>5 && variance>thresh3) {
	            deringBlockStrong(src, srcOffset+8*col, dst, dstOffset+8*col, lineLength, quality, quantScale);

	            if((col > 0 && pbi.fragmentVariances[block-1] > thresh4 ) ||
                  (col + 1 < blocksAcross && pbi.fragmentVariances[block+1] > thresh4 ) ||
	               (row + 1 < blocksDown && pbi.fragmentVariances[block+blocksAcross] > thresh4) ||
	               (row > 0 && pbi.fragmentVariances[block-blocksAcross] > thresh4)) {

	               deringBlockStrong(src, srcOffset + 8 * col, dst, dstOffset + 8 * col, lineLength,quality,quantScale);
	               deringBlockStrong(src, srcOffset + 8 * col, dst, dstOffset + 8 * col, lineLength,quality,quantScale);
	            }
            } else if(variance > thresh2 ) {
	            deringBlockStrong(src, srcOffset + 8 * col, dst, dstOffset + 8 * col, lineLength,quality,quantScale);
            } else if(variance > thresh1 ) {
	            deringBlockWeak(src, srcOffset + 8 * col, dst, dstOffset + 8 * col, lineLength,quality,quantScale);
            } else {
	            copyBlock(src, srcOffset + 8 * col, dst, dstOffset + 8 * col, lineLength);
            }

            ++block;

         }
         srcOffset+=8*lineLength;
         dstOffset+=8*lineLength;
      }

      /* Then U */

      blocksAcross /= 2;
      blocksDown /= 2;
      lineLength /= 2;

      srcOffset=pbi.reconUDataOffset;
      dstOffset=pbi.reconUDataOffset;
      //SrcPtr = Src + pbi->ReconUDataOffset;
      //DestPtr = Dst + pbi->ReconUDataOffset;
      for ( row = 0 ; row < blocksDown; row ++) {
         for (col = 0; col < blocksAcross; col ++) {
            int quality = pbi.fragQIndex[block];
            int variance = pbi.fragmentVariances[block];

            if( pbi.postProcessingLevel >5 && variance > thresh4 ) {
               deringBlockStrong(src, srcOffset + 8 * col, dst, dstOffset + 8 * col, lineLength,quality,quantScale);
               deringBlockStrong(src, srcOffset + 8 * col, dst, dstOffset + 8 * col, lineLength,quality,quantScale);
               deringBlockStrong(src, srcOffset + 8 * col, dst, dstOffset + 8 * col, lineLength,quality,quantScale);
            }else if(variance > thresh2 ){
               deringBlockStrong(src, srcOffset + 8 * col, dst, dstOffset + 8 * col, lineLength,quality,quantScale);
            }else if(variance > thresh1 ){
               deringBlockWeak(src, srcOffset + 8 * col, dst, dstOffset + 8 * col, lineLength,quality,quantScale);
            }else{
	            copyBlock(src, srcOffset+ + 8 * col, dst, dstOffset+ 8 * col, lineLength);
            }

            ++block;
         }
         srcOffset+=8*lineLength;
         dstOffset+=8*lineLength;
      }

      /* Then V */
      srcOffset=pbi.reconVDataOffset;
      dstOffset=pbi.reconVDataOffset;
      //SrcPtr = Src + pbi->ReconVDataOffset;
      //DestPtr = Dst + pbi->ReconVDataOffset;

      for ( row = 0 ; row < blocksDown; row ++){
         for (col = 0; col < blocksAcross; col ++) {

            int quality = pbi.fragQIndex[block];
            int variance = pbi.fragmentVariances[block];

            if( pbi.postProcessingLevel >5 && variance > thresh4 ) {
               deringBlockStrong(src, srcOffset + 8 * col, dst, dstOffset + 8 * col, lineLength,quality,quantScale);
               deringBlockStrong(src, srcOffset + 8 * col, dst, dstOffset + 8 * col, lineLength,quality,quantScale);
               deringBlockStrong(src, srcOffset + 8 * col, dst, dstOffset + 8 * col, lineLength,quality,quantScale);
            } else if(variance > thresh2 ) {
               deringBlockStrong(src, srcOffset + 8 * col, dst, dstOffset + 8 * col, lineLength,quality,quantScale);
            }else if(variance > thresh1 ) {
               deringBlockWeak(src, srcOffset + 8 * col, dst, dstOffset + 8 * col, lineLength,quality,quantScale);
            } else{
               copyBlock(src, srcOffset + 8 * col, dst, dstOffset + 8 * col, lineLength);
            }

            ++block;

         }
         srcOffset+=8*lineLength;
         dstOffset+=8*lineLength;
      }
   }

   static void updateFragQIndex(PbInstance pbi){

      int thisFrameQIndex=pbi.frameQIndex, i;

      /* It is not a key frame, so only reset those are coded */
      for(i = 0; i < pbi.unitFragments; i++  ) {
         if( pbi.displayFragments[i]!=0) {
            pbi.fragQIndex[i] = thisFrameQIndex;
         }
      }
   }

   static byte clamp255(int x) {
      if(x<0) return (byte)0;
      if(x>255) return (byte)255;
      return (byte)x;
      //return ((byte)((((x)<0)-1) & ((x) | -((x)>255))));
   }

   static void copyBlock(byte[] src, int srcOff, byte[] dest, int destOff, int stride) {

      int j;

      for ( j = 0; j < 8; j++ ) {
         dest[destOff]=src[srcOff];
         dest[destOff+1]=src[srcOff+1];
         srcOff+=stride;
         destOff+=stride;
      }
   }

   static void updateUmvBorder(PbInstance pbi, byte[] destReconPtr) {
      int planeFragOffset;

      /* Y plane */
      planeFragOffset = 0;
      updateUmvVBorders(pbi, destReconPtr, planeFragOffset);
      updateUmvHBorders(pbi, destReconPtr, planeFragOffset);

      /* Then the U and V Planes */
      planeFragOffset = pbi.yPlaneFragments;
      updateUmvVBorders(pbi, destReconPtr, planeFragOffset );
      updateUmvHBorders(pbi, destReconPtr, planeFragOffset );

      planeFragOffset = pbi.yPlaneFragments + pbi.uvPlaneFragments;
      updateUmvVBorders(pbi, destReconPtr, planeFragOffset );
      updateUmvHBorders(pbi, destReconPtr, planeFragOffset );
   }



   static void updateUmvHBorders(PbInstance pbi, byte[] destReconPtr, int planeFragOffset) {

      int i, pixelIndex, planeStride, blockVStep, planeFragments, lineFragments, planeBorderWidth;

      int sOff1, sOff2, dOff1, dOff2;

      /* Work out various plane specific values */
      if (planeFragOffset==0) {
         /* Y Plane */
         blockVStep = (pbi.yStride * (Constants.VFRAGPIXELS - 1));
         planeStride = pbi.yStride;
         planeBorderWidth = Constants.UMV_BORDER;
         planeFragments = pbi.yPlaneFragments;
         lineFragments = pbi.hFragments;
      } else {
         /* U or V plane. */
         blockVStep = (pbi.uvStride * (Constants.VFRAGPIXELS - 1));
         planeStride = pbi.uvStride;
         planeBorderWidth = Constants.UMV_BORDER / 2;
         planeFragments = pbi.uvPlaneFragments;
         lineFragments = pbi.hFragments / 2;
      }

      /* Setup the source and destination pointers for the top and bottom
         borders */
      pixelIndex = pbi.reconPixelIndexTable[planeFragOffset];
      sOff1=pixelIndex-planeBorderWidth;
      dOff1=sOff1-planeBorderWidth*planeStride;
      //SrcPtr1 = &DestReconPtr[ PixelIndex - PlaneBorderWidth ];
      //DestPtr1 = SrcPtr1 - (PlaneBorderWidth * PlaneStride);

      pixelIndex = pbi.reconPixelIndexTable[planeFragOffset + planeFragments - lineFragments] + blockVStep;
      sOff2=pixelIndex-planeBorderWidth;
      dOff2=sOff2+planeStride;
      //SrcPtr2 = &DestReconPtr[ PixelIndex - PlaneBorderWidth];
      //DestPtr2 = SrcPtr2 + PlaneStride;

      /* Now copy the top and bottom source lines into each line of the
         respective borders */
      for ( i = 0; i < planeBorderWidth; i++ ) {
         System.arraycopy(destReconPtr, sOff1, destReconPtr, dOff1, planeStride);
         System.arraycopy(destReconPtr, sOff2, destReconPtr, dOff2, planeStride);
         //memcpy( DestPtr1, SrcPtr1, PlaneStride );
         //memcpy( DestPtr2, SrcPtr2, PlaneStride );
         dOff1+=planeStride;
         dOff2+=planeStride;
      }
   }

   static void updateUmvVBorders(PbInstance pbi, byte[] destReconPtr, int planeFragOffset) {

      int i, pixelIndex, planeStride, lineFragments, planeBorderWidth, planeHeight;
      int sOff1, sOff2, dOff1, dOff2;

      /* Work out various plane specific values */
      if ( planeFragOffset == 0 ) {
         /* Y Plane */
         planeStride = pbi.yStride;
         planeBorderWidth = Constants.UMV_BORDER;
         lineFragments = pbi.hFragments;
         planeHeight = pbi.info.getHeight();
      }else{
         /* U or V plane. */
         planeStride = pbi.uvStride;
         planeBorderWidth = Constants.UMV_BORDER / 2;
         lineFragments = pbi.hFragments / 2;
         planeHeight = pbi.info.getHeight() / 2;
      }

      /* Setup the source data values and destination pointers for the
         left and right edge borders */
      pixelIndex = pbi.reconPixelIndexTable[planeFragOffset];
      sOff1=pixelIndex;
      dOff1=pixelIndex-planeBorderWidth;

      pixelIndex = pbi.reconPixelIndexTable[planeFragOffset+lineFragments-1]+Constants.HFRAGPIXELS-1;
      sOff2=pixelIndex;
      dOff2=pixelIndex+1;

      /* Now copy the top and bottom source lines into each line of the
         respective borders */
      for ( i = 0; i < planeHeight; i++ ) {
         System.arraycopy(destReconPtr, sOff1, destReconPtr, dOff1, planeBorderWidth);
         System.arraycopy(destReconPtr, sOff2, destReconPtr, dOff2, planeBorderWidth);
         sOff1+=planeStride;
         sOff2+=planeStride;
         dOff1+=planeStride;
         dOff2+=planeStride;
      }
   }

   static void setupLoopFilter(PbInstance pbi){
      int fLimit;

      fLimit = Constants.loopFilterLimitValuesV2[pbi.frameQIndex];
      pbi.setupBoundingValueArrayGeneric(fLimit);
   }

}