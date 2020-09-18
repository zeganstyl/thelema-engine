package de.jarnbjo.flac;

import java.io.IOException;
import de.jarnbjo.util.io.*;

public class FlacFormatException extends IOException {

   private ByteArrayBitInputStream source;

   public FlacFormatException() {
   }

   public FlacFormatException(String message) {
      super(message);
   }

   public FlacFormatException(String message, ByteArrayBitInputStream source) {
      super(message);
      this.source=source;
   }

   public void printStackTrace() {
      super.printStackTrace();
      if(source!=null) {
         for(int i=0; i<source.getSource().length; ) {
            int lineEnd=i+8;
            for(; i<lineEnd && i<source.getSource().length; i++) {
               String val=Integer.toBinaryString(((int)source.getSource()[i])&0xff);
               while(val.length()<8) val="0"+val;
               System.err.print(val+" ");
            }
            System.err.println("");
         }
      }
   }

}