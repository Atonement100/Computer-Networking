import java.io.Serializable;

public class ack implements Serializable{
    private byte sequenceNum;
    private byte checksum;

    ack(byte sequenceNum){
        this.sequenceNum = sequenceNum;
        this.checksum = 0;
    }

    public byte getSequenceNum(){
        return this.sequenceNum;
    }

    public byte getChecksum(){
        return this.checksum;
    }

    public void corruptAck(){
        this.checksum = (byte)(this.checksum + 1);
    }
}
