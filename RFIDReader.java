//RFID Simulation, CSE461 Fall 2009, by Josh Goodwin
//Credit to RFIDReader.java, zahorjan, 2009/01/28

//RFIDReader.java controls the reader of RFID tags.
//It sends out message on the channel, gets responses
//from the channel, and keeps a list of inventoried
//tag EPC id's.  When it believes that all tags have
//been inventoried, it returns the list of tag EPC id's.
//as a list of byte[]'s.

import java.util.*;
import java.io.*;

public class RFIDReader {
  
	private List<byte[]> currentInventory;
	private RFIDChannel channel;
  private static final int BUCKET_SIZE = 4;
  
  //Data structures for keeping track of its epc id
  //private LinkedList<Byte>
  
	//frames used for the protocol
	byte[] ack;
	byte[] query;
  byte[] requery;
  
	public RFIDReader(RFIDChannel chan) {
		currentInventory = new ArrayList<byte[]>();
		channel = chan;
    
		//Create needed output frames that don't change.
		//*Note: You may choose whether or not to use
		//Output/Input streams in your implementation.
		//They are offered here as one convenient option
		//for encoding/decoding a byte array.
		    
    RFIDConstants.setBucketSize(BUCKET_SIZE);
    createAck();
    createQuery();
    createRequery();
	}
  
  private void createAck() {
    try{
      //create "ack" frame
      ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
      DataOutputStream dataOut = new DataOutputStream(bytesOut);
      
      dataOut.writeByte(RFIDConstants.ACK);
      dataOut.flush();
      ack = bytesOut.toByteArray();
      bytesOut.reset();
      dataOut.close();
      bytesOut.close();
    } catch (Exception e){
      System.out.println("Error in creation of ACK");
    }
  }
  
  private void createTagIDAck(byte[] toAppend) {
    try {
      ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
      DataOutputStream dataOut = new DataOutputStream(bytesOut);
      
      dataOut.writeByte(RFIDConstants.ACK);
      for (byte b : toAppend)
        dataOut.writeByte(b);
      
      dataOut.flush();
      ack = bytesOut.toByteArray();
      bytesOut.reset();
      dataOut.close();
      bytesOut.close();
    } catch (Exception e) {
      System.out.println("error creating tag id ACK");
    }
  }
  
  private void createQuery() {
    try {
      //create "query" frame
      ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
      DataOutputStream dataOut = new DataOutputStream(bytesOut);
      
      dataOut.writeByte(RFIDConstants.QUERY); 
      Integer bucketSize = RFIDConstants.getBucketSize();
      dataOut.writeByte(bucketSize.byteValue());
      
      dataOut.flush();
      query = bytesOut.toByteArray();
      bytesOut.reset();
      
      dataOut.close();
      bytesOut.close();
      //System.out.println("Query byte array: ");
      //for (byte b : query) {
      //  System.out.print(b);
      //}
      //System.out.print("\n");
    } catch (Exception e) {
      System.out.println("Error creating QUERY.");
    }
  }
  
  private void createRequery() {
    try {//create "requery" frame
      ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
      DataOutputStream dataOut = new DataOutputStream(bytesOut);
      
      dataOut.writeByte(RFIDConstants.REQUERY);
      //RFIDConstants.setBucketSize(RFIDConstants.getBucketSize() + 1);
      Integer bucketSize = RFIDConstants.getBucketSize();
      dataOut.writeByte(bucketSize.byteValue());
      
      dataOut.flush();
      requery = bytesOut.toByteArray();
      bytesOut.reset();
      
      dataOut.close();
      bytesOut.close();
      //System.out.println("ReQuery byte array: ");
      //for (byte b : requery) {
      //  System.out.print(b);
      //}
      //System.out.print("\n");
    } catch (Exception e) {
      System.out.println("Error creating REQUERY.");
    }
  }
	
  //This controls the behavoir of the Reader.
	//The inventory method should run
	//until the reader determines that it is unlikely
	//any other tags are uninventored, then return
	//the currentInventory.
	public List<byte[]> inventory() {
		/*
     Currently, the strawman protocol is implemented.
     Replace with your own protocol.
     */
		int count = 0; //count of consecutive no-replies
    
		byte[] response;
    
		response = channel.sendMessage(query);
    
    for (int i=0; i<(int) Math.pow(2.0, (double) RFIDConstants.getBucketSize()); i++) {
      //System.out.println("Sending a requery up to " + (int) Math.pow(2.0, (double) RFIDConstants.getBucketSize()) + " times.");
      response = channel.sendMessage(requery);
      if (response != null) { // unpack the message
        response = readMessage(response);
        if(response != null && !currentInventory.contains(response)){
					currentInventory.add(response);
				}
      }
    }
    
    return currentInventory;
  }
  
  public byte[] readMessage(byte[] response) {
    byte[] toReturn;
    if (response[0] == RFIDConstants.PREID) {
      System.out.println("Reader found a preId response: " + response);
      createTagIDAck(response);
      toReturn = channel.sendMessage(ack);
      System.out.println("Got the tag id:\n");
      for (byte b : toReturn) 
        System.out.print(b);
      System.out.println();
      
      return toReturn;
    } else if (Arrays.equals(response, RFIDChannel.GARBLE)) {
      //yikes;
      return null;
    } 
    return null;
  }
}

