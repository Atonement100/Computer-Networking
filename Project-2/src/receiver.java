import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;

public class receiver {
    public static void main(String[] args) throws IOException {
        if (args.length != 2){
            System.err.println("Usage: java client <hostname> <port #>");
            return;
        }

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        try(    //Socket initialization
            Socket clientSocket = new Socket(hostname, port);
            ObjectOutputStream objToServer = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream objFromServer = new ObjectInputStream(clientSocket.getInputStream());
        )
        {
            packet inPacket;
            Vector<String> messageTokens = new Vector<>();
            byte expectedSequenceNum = 0b0, waitingSequenceNum = 0b0;
            byte ackNumToReturn = 0b0;
            int totalPacketsReceived = 0;
            Object inObj;
            while ((inObj = objFromServer.readUnshared()) != null){
                if (inObj instanceof packet) {
                    inPacket = (packet) inObj;
                    totalPacketsReceived++;

                    int localChecksum = 0;
                    for (char ch : inPacket.getContent().toCharArray()) {
                        localChecksum += ch;
                    }

                    if (expectedSequenceNum == inPacket.getSequenceNum()) {
                        if (localChecksum == inPacket.getChecksum()){
                            messageTokens.add(inPacket.getContent());
                            ackNumToReturn = expectedSequenceNum;
                            waitingSequenceNum = expectedSequenceNum;
                            expectedSequenceNum = (byte) (expectedSequenceNum ^ 0b1);
                            //System.out.println("Good seq num good checksum. ack num ret: " + ackNumToReturn + ". exp seq num: " + expectedSequenceNum);
                        }
                        else {
                            ackNumToReturn = (byte) (expectedSequenceNum ^ 0b1);
                            waitingSequenceNum = expectedSequenceNum;
                            //System.out.println("Good seq num BAD checksum. ack num ret: " + ackNumToReturn + ". exp seq num: " + expectedSequenceNum);
                        }
                    }
                    else if (inPacket.getContent().equals(messageTokens.lastElement())){
                        if (localChecksum == inPacket.getChecksum()){
                            ackNumToReturn = (byte) (expectedSequenceNum ^ 0b1);
                            waitingSequenceNum = expectedSequenceNum;
                            //System.out.println("bad seq num good checksum. ack num ret: " + ackNumToReturn + ". exp seq num: " + expectedSequenceNum);
                        }
                        else {
                            ackNumToReturn = expectedSequenceNum;
                            waitingSequenceNum = (byte)(expectedSequenceNum ^ 0b1);
                            //System.out.println("bad seq num bad checksum. ack num ret: " + ackNumToReturn + ". exp seq num: " + expectedSequenceNum);
                        }
                    }

                    System.out.println("Waiting " + waitingSequenceNum + ", " + totalPacketsReceived + ", " +
                            inPacket.getSequenceNum() + " " + inPacket.getPacketId() + " " + inPacket.getChecksum() + " " + inPacket.getContent() + ", " + "ACK" + ackNumToReturn);
                    objToServer.writeUnshared(new ack(ackNumToReturn));
                }
                else if (inObj instanceof Byte){
                    if ((byte) inObj == -1){
                        for (String str : messageTokens){
                            System.out.print(str + " ");
                        }
                        System.out.println();
                        return;
                    }
                }
            }
        } catch(UnknownHostException ex) {
            System.err.println("Couldn't find host " + hostname);
            System.exit(1);

        } catch (IOException ex){
            System.err.println("Couldn't make connection to " + hostname);
            System.exit(1);
        } catch (ClassNotFoundException ex){
            System.out.println("Invalid class received from network.");
        }
    }
}

