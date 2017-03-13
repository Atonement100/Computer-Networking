import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;


public class sender {

    public static void main(String[] args) throws IOException {
        boolean startTermination = false;

        if (args.length != 3){
            System.err.println("Usage: java client <hostname> <port #> <message filename>");
            return;
        }

        String hostname = args[0],
               filename = args[2];
        int port = Integer.parseInt(args[1]);


        try(    //Socket initialization
            Socket clientSocket = new Socket(hostname, port);
            ObjectOutputStream objToServer = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream objFromServer = new ObjectInputStream(clientSocket.getInputStream());
        )
        {
            // " The sender program first reads the message file and converts it into many packets "
            // This requirement is why all packets are made before sending any.
            Vector<packet> outPackets = new Vector<>();
            try{
                BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
                String line;
                byte seqNum = 0, packetId = 0;
                int checkSum = 0;
                while ((line = bufferedReader.readLine()) != null){
                    String[] tokens = line.split("[ ]+");
                    for (String token : tokens){
                        for (char ch : token.toCharArray()){
                            checkSum += ch;
                        }
                        outPackets.add(new packet(seqNum, packetId, checkSum, token));
                        seqNum = (byte)(seqNum ^ 1); //Sequence num alternates between 0 and 1
                        packetId += 1;
                        checkSum = 0;
                    }
                }

            }
            catch(FileNotFoundException ex) {
                System.out.println("Unable to open file '" + filename + "'");
            }
            catch(IOException ex) {
                System.out.println("Error reading file '" + filename + "'");
            }

            Boolean alive = true;
            byte state = 0b00;
            byte currentPacket = 0;
            int totalPacketsSent = 0;
            while (alive) {
                switch(state){
                    case 0b00:
                    case 0b10:
                        if (currentPacket >= outPackets.size() || outPackets.get(currentPacket).getContent().endsWith(".")) {startTermination =  true;}
                        objToServer.writeUnshared(outPackets.elementAt(currentPacket));
                        totalPacketsSent++;
                        state += 0b01;
                        break;
                    case 0b01:
                    case 0b11:
                        ack inAck = (ack) objFromServer.readObject();

                        boolean resendPacket = inAck.getSequenceNum() == 2 || (inAck.getSequenceNum() != (state >> 1) && inAck.getChecksum() == 0);
                        String ackRec = inAck.getSequenceNum() < 2 ? "ACK" + inAck.getSequenceNum() : "DROP",
                                actionStr;
                        if (startTermination) {actionStr = "no more packets to send";}
                        else if (resendPacket) {actionStr = "resend Packet" + (state >> 1);}
                        else {actionStr = "send Packet" + ((state >> 1) ^ 1);}

                        System.out.println("Waiting ACK" + (state >> 1) + ", " + totalPacketsSent + ", " + ackRec + ", " + actionStr);

                        if(resendPacket){ //If the sequence number is not what we are expecting
                            outPackets.elementAt(currentPacket).recalculateChecksum();
                            objToServer.writeUnshared(outPackets.elementAt(currentPacket)); //rewrite and maintain state
                            totalPacketsSent++;
                            break;
                        }

                        if (startTermination) {
                            objToServer.writeUnshared((byte)(-1));
                            clientSocket.close();
                            System.exit(1);
                        }

                        currentPacket++;
                        state = (byte)((state + 0b01) % 0b100);
                        break;
                    case 0b100:
                        alive = false;
                    default:
                        state = 0b100;
                }
            }
        } catch(UnknownHostException ex) {
            System.err.println("Couldn't find host " + hostname);
            System.exit(1);

        } catch (IOException ex){
            System.err.println("IOException occurred");
            System.exit(1);
        }
        catch(ClassNotFoundException ex){
            System.out.println("Invalid class received from network.");
        }
    }
}
