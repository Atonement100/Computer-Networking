import java.io.Serializable;

public class packet implements Serializable{
    private byte sequenceNum;
    private byte packetId;
    private int checksum;
    private String content;

    public packet(byte seqNum, byte packetId, int checksum, String content){
        this.sequenceNum = seqNum;
        this.packetId = packetId;
        this.checksum = checksum;
        this.content = content;
    }

    public byte getSequenceNum(){
        return this.sequenceNum;
    }

    public byte getPacketId(){
        return this.packetId;
    }

    public int getChecksum(){
        return this.checksum;
    }

    public String getContent(){
        return this.content;
    }

    public void corruptPacket(){
        this.checksum++;
    }

    public void recalculateChecksum(){ //for use by sender only :x
        int checksum = 0;
        for (char ch : this.getContent().toCharArray()) {
            checksum += ch;
        }
        this.checksum = checksum;
    }
}
