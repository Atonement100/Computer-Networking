
public class ack {
    private byte sequenceNum;
    private byte checksum;

    ack(byte sequenceNum){
        this.sequenceNum = sequenceNum;
        this.checksum = 0;
    }
}
