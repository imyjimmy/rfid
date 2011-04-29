//RFID Simulation, CSE461 Fall 2009, by Josh Goodwin
//Credit to RFIDTag.java, zahorjan, 2009/01/28

//RFIDTag.java represents an RFID tag, with a random
//64 bit EPC assigned during creation.  The tag only has
//the capability to respond to an incoming message, and can
//maintain internal state as necessary.

import java.util.*;
import java.io.*;

public class RFIDTag {
	private static Random generator = new Random();
  
	//64 bit tag identifier
	private byte[] tagEPC;
  //1 bit preId message
  private byte[] preId;
  //will send preId during this slot
  private int slot;
  //keeps track of num of rq's from reader.
  private int numRequery;
  
	//Insert other needed state here
	private boolean beenInventoried;
  private boolean sendPermitted;
	private boolean sentEPC;
  
	public RFIDTag() {
		//generate random unsigned 64 bit identifier for this Tag
		tagEPC = new byte[8];
		generator.nextBytes(tagEPC);
    
    //set the pre-Id
    preId = new byte[2];
    preId[0] = RFIDConstants.PREID;
    byte[] random = new byte[1];
    generator.nextBytes(random);
    System.arraycopy(random, 0, preId, 1, random.length);
    
    numRequery = 0;
		beenInventoried = false;
    sendPermitted = false;
		sentEPC = false;
	}
  
	/*
   "magic" method used by the simulator to directly
   access the EPC of the given tag.  Your reader can't do
   magic, (or see the tags directly), so you should not
   use this method.
   */
	public byte[] getEPC() {
		return tagEPC;
	}
  
  
	/*
   responds to an incoming message, encoded as a byte array.
   Return null if the tag does not reply to the message.
   */
	public byte[] respond(byte[] message) {
		/*
     Currently, the strawman protocol is implemented.
     Replace with your own protocol.
     */
    /*System.out.println("RFIDTag, Receiving message: ");
    for (byte b : message) {
      System.out.print(b);
    }*/
    System.out.println();
    
		assert(message != null);
    
		if(Arrays.equals(message, RFIDChannel.GARBLE)){
			//do nothing (and don't check message further)
		} else {
			//unpack the message
			//see comment in RFIDReader concerning use of Streams
			ByteArrayInputStream bytesIn = new ByteArrayInputStream(message);
			DataInputStream dataIn = new DataInputStream(bytesIn);
      
			byte flag = 0;
			try {
				flag = dataIn.readByte();
        if(flag == RFIDConstants.QUERY && !beenInventoried){
          numRequery = -1;
          Byte bucketSizeByte = dataIn.readByte();
          int bucketSize = bucketSizeByte.intValue();
          System.out.println("Reading bucket size byte: " + bucketSize);
          slot = generator.nextInt((int) Math.pow(2.0, (double) bucketSize));
          System.out.println("Picked slot number: " + slot);
        } else if (flag == RFIDConstants.REQUERY && !beenInventoried){
          //untested
          numRequery++;
          //System.out.println("Requery received, incremented numRequery: " + numRequery);
          if (numRequery == slot) {
            System.out.println("Sending a pre-id: " + preId + " at slot num: " + slot);
            return preId;
          }
        } else if (flag == RFIDConstants.ACK) {
          boolean ourAck = true;
          for (byte b : preId) {
            Byte otherB = dataIn.readByte();
            if (b != otherB) {
              System.out.println("Not our ack");
              ourAck = false;
            }
          } 
          if (ourAck && !sentEPC) { 
            System.out.println("Got ack, ok to send");
            
            System.out.println("Sending the tag id:\n");
            for (byte b : tagEPC) 
              System.out.print(b);
            System.out.println();
            sentEPC = true;
            return tagEPC;
          }
          if (ourAck && sentEPC) {
            System.out.println("Inventoried");
            beenInventoried = true;
          }
        }
      } catch (Exception e) {
				System.out.println("Error during read in Tag");
			}
		}
    
		//if we reach here, we didn't respond
		return null;
	}
  
  //debugging purposes
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (byte b : tagEPC) {
      sb.append(b);
    }
    return sb.toString();
  }
  
}