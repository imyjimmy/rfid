//RFID Simulation, CSE461 Fall 2009, by Josh Goodwin

//RFIDConstants simply provides a single place to define
//various constants used in a frame, so both
//the reader and the tags are more easily updated.

public class RFIDConstants {
  
	//Add or change frame components as needed
  public static final byte ACK = 'a';
  public static final byte QUERY = 'q';
  public static final byte REQUERY = 'r';
  public static final byte PREID = 'p';
  public static final byte TAGID = 't';
  
  private static int bucketSize = 4;
  
  public static void setBucketSize(int size) {
    bucketSize = size;
  }
  
  public static int getBucketSize() {
    return bucketSize;
  }
}