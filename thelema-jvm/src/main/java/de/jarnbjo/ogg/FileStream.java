/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: FileStream.java,v 1.1 2003/04/10 19:48:22 jarnbjo Exp $
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
 * $Log: FileStream.java,v $
 * Revision 1.1  2003/04/10 19:48:22  jarnbjo
 * no message
 *
 *
 */

package de.jarnbjo.ogg;

import java.io.*;
import java.util.*;

/**
 * Implementation of the <code>PhysicalOggStream</code> interface for accessing
 * normal disk files.
 */

public class FileStream implements PhysicalOggStream {

   private boolean closed=false;
   private final RandomAccessFile source;
   private final long[] pageOffsets;

   private final HashMap<Integer, LogicalOggStream> logicalStreams = new HashMap<>();

   /**
    * Creates access to the specified file through the <code>PhysicalOggStream</code> interface.
    * The specified source file must have been opened for reading.
    *
    * @param source the file to read from
    *
    * @throws OggFormatException if the stream format is incorrect
    * @throws IOException if some other IO error occurs when reading the file
    */

   public FileStream(RandomAccessFile source) throws OggFormatException, IOException {
      this.source = source;

      ArrayList<Long> po=new ArrayList<>();
      int pageNumber=0;
      try {
         while(true) {
            po.add(this.source.getFilePointer());

            // skip data if pageNumber>0
            OggPage op=getNextPage(pageNumber>0);
            if(op==null) {
               break;
            }

            LogicalOggStreamImpl los=(LogicalOggStreamImpl)getLogicalStream(op.getStreamSerialNumber());
            if(los==null) {
               los=new LogicalOggStreamImpl(this, op.getStreamSerialNumber());
               logicalStreams.put(op.getStreamSerialNumber(), los);
            }

            if(pageNumber==0) {
               los.checkFormat(op);
            }

            los.addPageNumberMapping(pageNumber);
            los.addGranulePosition(op.getAbsoluteGranulePosition());

            if(pageNumber>0) {
               this.source.seek(this.source.getFilePointer()+op.getTotalLength());
            }

            pageNumber++;
         }
      }
      catch(EndOfOggStreamException e) {
         // ok
      }
      //System.out.println("pageNumber: "+pageNumber);
      this.source.seek(0L);
      pageOffsets=new long[po.size()];
      int i=0;
      for (Long aLong : po) {
         pageOffsets[i++] = aLong;
      }
   }

   public Collection<LogicalOggStream> getLogicalStreams() {
      return logicalStreams.values();
   }

   public boolean isOpen() {
      return !closed;
   }

   public void close() throws IOException {
      closed=true;
      source.close();
   }

   private OggPage getNextPage() throws IOException {
      return getNextPage(false);
   }

   private OggPage getNextPage(boolean skipData) throws IOException {
      return OggPage.create(source, skipData);
   }

   public OggPage getOggPage(int index) throws IOException {
      source.seek(pageOffsets[index]);
      return OggPage.create(source);
   }

   private LogicalOggStream getLogicalStream(int serialNumber) {
      return logicalStreams.get(serialNumber);
   }

   public void setTime(long granulePosition) throws IOException {
      for (LogicalOggStream los : logicalStreams.values()) {
         los.setTime(granulePosition);
      }
   }

	/**
	 *  @return always <code>true</code>
	 */

   public boolean isSeekable() {
      return true;
   }
}