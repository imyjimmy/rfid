public class ReaderTest {
    public static void main(String[] args) {
        RFIDReader r = new RFIDReader(new RFIDChannel(new RFIDTag[0],1.0));
    }
}