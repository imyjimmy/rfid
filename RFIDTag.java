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
        preId = new byte[1];
        preId[0] = tagEpC[0]; //might as well give a sneak preview

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
        System.out.println("RFIDTag, Receiving message: ");
        for (byte b : message) {
            System.out.print(b);
        }
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
                if(flag == RFIDConstants.ACK && sentEPC){
                    //we've been inventoried, don't respond anymore.
                    beenInventoried = true;
                } else if(flag == RFIDConstants.QUERY && !beenInventoried){
                    numRequery = 0;
                    Byte bucketSizeByte = dataIn.readByte();
                    int bucketSize = bucketSizeByte.intValue();
                    System.out.println("Reading bucket size byte: " + bucketSize);
                    slot = generator.nextInt((int) Math.pow(2.0, (double) bucketSize));
                    System.out.println("Picked slot number: " + slot);
                } else if (flag == RFIDConstants.REQUERY && !beenInventoried && sendPermitted){
                    //untested
                    numRequery++;
                    if (numRequery == slot)
                        return preId;
                } else if (flag == RFIDConstants.ACK && !sentEPC) {
                    //ok to send 
                    sendPermitted = true;
                }
            } catch (Exception e) {
				System.out.println("Error during read in Tag");
			}
		}

		//if we reach here, we didn't respond with tag.  Return null (no reply).
		sentEPC = false;
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