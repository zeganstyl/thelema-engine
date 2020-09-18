/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: Subframe.java,v 1.1 2003/03/03 21:53:17 jarnbjo Exp $
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
 * $Log: Subframe.java,v $
 * Revision 1.1  2003/03/03 21:53:17  jarnbjo
 * no message
 *
 */

package de.jarnbjo.flac;

import java.io.IOException;
import java.util.*;

import de.jarnbjo.util.io.*;

public abstract class Subframe {

   public static Subframe createInstance(BitInputStream source, Frame frame, StreamInfo streamInfo, boolean sideChannel) throws FlacFormatException, IOException {

      if(source.getBit()) {
         throw new FlacFormatException("Sync error when trying to read subframe");
      }

      int type=source.getInt(6);

      int wastedBits=0;

      if(source.getBit()) {
         // count wasted bits;
         do {
            wastedBits++;
         } while(!source.getBit());
      }

      if(type==0) {
         // SUBFRAME_CONSTANT
         return new Constant(source, frame, streamInfo);
      }
      else if(type==1) {
         // SUBFRAME_VERBATIM
         return new Verbatim(source, frame, streamInfo);
      }
      else if((type&0x38)==0x08 && (type&0x07)<=4) {
         // SUBFRAME_FIXED
         int order=type&0x07;
         return new Fixed(source, frame, streamInfo, wastedBits, order, sideChannel);
      }
      else if((type&0x20)==0x20) {
         // SUBFRAME_LPC
         int order=(type&0x1f)+1;
         return new Lpc(source, frame, streamInfo, wastedBits, order, sideChannel);
      }

      throw new FlacFormatException("Unknown or unsupported subframe type");
   }

   public abstract int[] getPcm();

   public static class Constant extends Subframe {

      int[] pcm;

      public Constant(BitInputStream source, Frame frame, StreamInfo streamInfo) throws FlacFormatException, IOException {
         int c=source.getSignedInt(frame.getBitsPerSample());
         pcm=new int[frame.getBlockSize()];
         Arrays.fill(pcm, c);
      }

      public int[] getPcm() {
         return pcm;
      }

   }

   public static class Verbatim extends Subframe {

      int[] pcm;

      public Verbatim(BitInputStream source, Frame frame, StreamInfo streamInfo) throws FlacFormatException, IOException {
         int bps=frame.getBitsPerSample();
         int blockSize=frame.getBlockSize();

         pcm=new int[blockSize];

         for(int i=0; i<blockSize; i++) {
            pcm[i]=source.getSignedInt(bps);
         }
      }

      public int[] getPcm() {
         return pcm;
      }

   }

   public static class Fixed extends Subframe {

      int[] pcm;

      int[] warmup;
      int[] residue;

      public Fixed(BitInputStream source, Frame frame, StreamInfo streamInfo, int wastedBits, int order, boolean sideChannel) throws FlacFormatException, IOException {

         int bitsPerSample=frame.getBitsPerSample();
         if(sideChannel) {
            bitsPerSample++;
         }

         warmup=new int[order];
         for(int i=0; i<order; i++) {
            warmup[i]=source.getSignedInt(bitsPerSample-wastedBits);
         }

         int codingMethod=source.getInt(2);

         if(codingMethod!=0) {
            throw new FlacFormatException("SUBFRAME_FIXED: residual coding method "+codingMethod+" not supported");
         }

         int partitionOrder=source.getInt(4);

         int partitions=1<<partitionOrder;

         int partitionSamples=
            partitionOrder > 0 ?
               frame.getBlockSize() >> partitionOrder :
               frame.getBlockSize() - order;

         if(Properties.analyze()) {
            System.out.print("\tsubframe=?\twasted_bits="+wastedBits+"\t");
            System.out.print("type=FIXED\t");
            System.out.print("order="+order+"\t");
            System.out.println("partition_order="+partitionOrder);
         }

         pcm=new int[frame.getBlockSize()];
         residue=new int[frame.getBlockSize()];

         int sample=0;

         for(int partition=0; partition<partitions; partition++) {
            int riceParameter=source.getInt(4);
            if(riceParameter<15) {
               int u=(partitionOrder == 0 || partition > 0) ?
                  partitionSamples : partitionSamples - order;
               source.readSignedRice(riceParameter, residue, sample, u);
               sample+=u;
            }
            else {
               riceParameter=source.getInt(5);
               for(int u = (partitionOrder == 0 || partition > 0)? 0 : order; u < partitionSamples; u++, sample++) {
                  residue[sample]=source.getSignedInt(riceParameter);
               }
            }
         }

         for(int i=0; i<order; i++) {
            pcm[i]=warmup[i];
            if(Properties.analyze()) {
               System.out.println("\t\twarmup["+i+"]="+warmup[i]);
            }
         }

         int i, len=frame.getBlockSize()-order;

         switch(order) {
         case 0:
            for(i = 0; i < len; i++) {
               pcm[i] = residue[i];
            }
            break;
         case 1:
            for(i = 0; i < len; i++) {
               pcm[i+1] = residue[i] + pcm[i];
            }
            break;
         case 2:
            for(i = 0; i < len; i++) {
               pcm[i+2] = residue[i] + (pcm[i+1]<<1) - pcm[i];
            }
            break;
         case 3:
            for(i = 0; i < len; i++) {
               pcm[i+3] = residue[i] + (((pcm[i+2]-pcm[i+1])<<1) + (pcm[i+2]-pcm[i+1])) + pcm[i];
            }
            break;
         case 4:
            for(i = 0; i < len; i++) {
               pcm[i+4] = residue[i] + ((pcm[i+3]+pcm[i+1])<<2) - ((pcm[i+2]<<2) + (pcm[i+2]<<1)) - pcm[i];
            }
            break;
         default:
            throw new FlacFormatException("Illegal SUBFRAME_FIXED order");
         }

         if(wastedBits>0) {
            for(i=0; i<pcm.length; i++) {
               pcm[i]<<=wastedBits;
            }
         }
      }

      public int[] getPcm() {
         return pcm;
      }

   }

   public static class Lpc extends Subframe {

      int[] pcm;

      int[] warmup;
      int[] residue;

      int[] coeffs;

      public Lpc(BitInputStream source, Frame frame, StreamInfo streamInfo, int wastedBits, int order, boolean sideChannel) throws FlacFormatException, IOException {

         int bitsPerSample=frame.getBitsPerSample();
         if(sideChannel) {
            bitsPerSample++;
         }

         warmup=new int[order];
         for(int i=0; i<order; i++) {
            warmup[i]=source.getSignedInt(bitsPerSample-wastedBits);
         }

         int coeffPrecision=source.getInt(4)+1;
         if(coeffPrecision==16) {
            throw new FlacFormatException("Illegal linear predictor coefficients' precision in SUBFRAME_LPC");
         }

         int quantizationLevel=source.getSignedInt(5);

         coeffs=new int[order];
         for(int i=0; i<order; i++) {
            coeffs[i]=source.getSignedInt(coeffPrecision);
         }

         int codingMethod=source.getInt(2);

         if(codingMethod!=0) {
            throw new FlacFormatException("SUBFRAME_FIXED: residual coding method "+codingMethod+" not supported", (ByteArrayBitInputStream)source);
         }

         int partitionOrder=source.getInt(4);

         int partitions=1<<partitionOrder;

         int partitionSamples=
            partitionOrder > 0 ?
               frame.getBlockSize() >> partitionOrder :
               frame.getBlockSize() - order;

         pcm=new int[frame.getBlockSize()];
         residue=new int[frame.getBlockSize()];

         int sample=0;

         for(int partition=0; partition<partitions; partition++) {
            int riceParameter=source.getInt(4);
            if(riceParameter<15) {
               int u=(partitionOrder == 0 || partition > 0) ?
                  partitionSamples : partitionSamples - order;
               source.readSignedRice(riceParameter, residue, sample, u);
               sample+=u;
            }
            else {
               riceParameter=source.getInt(5);
               for(int u = (partitionOrder == 0 || partition > 0)? 0 : order; u < partitionSamples; u++, sample++) {
                  residue[sample]=source.getSignedInt(riceParameter);
               }
            }
         }

         if(Properties.analyze()) {
            System.out.print("\tsubframe=?\twasted_bits="+wastedBits+"\t");
            System.out.print("type=LPC\t");
            System.out.print("order="+order+"\t");
            System.out.print("partition_order="+partitionOrder+"\t");
            System.out.print("qlp_coeff_precision="+coeffPrecision+"\t");
            System.out.println("quantization_level="+quantizationLevel);
         }

         for(int i=0; i<order; i++) {
            pcm[i]=warmup[i];
            if(Properties.analyze()) {
               System.out.println("\t\twarmup["+i+"]="+warmup[i]);
            }
         }

         for(int i = 0; i < frame.getBlockSize()-order; i++) {
            long sum = 0;
            int history = i+order;
            for(int j = 0; j < order; j++) {
               sum += ((long)coeffs[j]) * ((long)pcm[--history]);
            }
            if(quantizationLevel>=0) {
               pcm[i+order]=residue[i] + (int)(sum >> quantizationLevel);
            }
            else {
               pcm[i+order]=residue[i] + (int)(sum << -quantizationLevel);
            }
         }

         if(wastedBits>0) {
            for(int i=0; i<pcm.length; i++) {
               pcm[i]<<=wastedBits;
            }
         }
      }

      public int[] getPcm() {
         return pcm;
      }

   }
}