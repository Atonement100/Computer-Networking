import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class network {
    private static ServerSocket network;
    private static volatile packet nextPacket;
    private static volatile ack nextAck;
    private static volatile boolean movePacket, moveAck, startTermination = false, receiverConnected = false;

    public static void main(String[] args) throws IOException {
        if (args.length != 1){
            System.err.println("Usage: java network <port #>");
            return;
        }

        int port = Integer.parseInt(args[0]);

        try {
            network = new ServerSocket(port);
        } catch (IOException ex){
            System.out.println("Exception while listening for connection on port " + port);
            System.out.println(ex.getMessage());
            System.exit(-1);
        }

        Thread receiverThread = new Thread(){
            public void run(){
                try (
                    Socket receiver = network.accept();
                    ObjectOutputStream objToReceiver = new ObjectOutputStream(receiver.getOutputStream());
                    ObjectInputStream objFromReceiver = new ObjectInputStream(receiver.getInputStream());
                ){
                    //first communication with receiver
					receiverConnected = true;
                    while (!movePacket);
                    objToReceiver.writeUnshared(nextPacket);

                    Object inObj;
                    ack inAck;
                    while ((inObj = objFromReceiver.readUnshared()) != null) {
                        if (inObj instanceof ack) {
                            inAck = (ack) inObj;
                            networkAction action = networkAction.generateNetworkAction();
                            String outLine = "Received: ACK" + inAck.getSequenceNum() + ", " + action;
                            System.out.println(outLine);
                            switch (action) {
                                case CORRUPT:
                                    inAck.corruptAck(); //fallthrough
                                case PASS:
                                    nextAck = inAck;
                                    moveAck = true;
                                    movePacket = false;
                                    break;
                                case DROP:
                                    nextAck = new ack((byte) 2);
                                    moveAck = true;
                                    movePacket = false;
                                    break;
                                default:
                                    throw new IllegalStateException();
                            }

                            while (!(movePacket || startTermination)) ;
                            if (startTermination) {
                                objToReceiver.writeUnshared((byte)(-1));
                                receiver.close();
                                return;
                            }
                            else if (movePacket) {
                                objToReceiver.writeUnshared(nextPacket);
                                nextPacket = null;
                            }

                        }
                    }
                } catch (IOException ex){
                    System.out.println("Exception while attempting to make receiver connection");
                } catch (ClassNotFoundException ex){
                        System.out.println("Invalid class received from receiver");
                }
            }
        };

        receiverThread.start();
		
		while(!receiverConnected){};

        Thread senderThread = new Thread(){
            public void run(){
                try (
                    Socket sender = network.accept();
                    ObjectOutputStream objToSender = new ObjectOutputStream(sender.getOutputStream());
                    ObjectInputStream objFromSender = new ObjectInputStream(sender.getInputStream());
                ){
                    String outLine;
                    Object inObj;
                    packet inPacket;
                    while ((inObj = objFromSender.readUnshared()) != null) {
                        if (inObj instanceof packet) {
                            inPacket = (packet) inObj;
                            networkAction action = networkAction.generateNetworkAction();
                            outLine = "Received: Packet" + inPacket.getSequenceNum() + ", " + inPacket.getPacketId() + ", " + action;
                            System.out.println(outLine);
                            switch (action) {
                                case CORRUPT:
                                    inPacket.corruptPacket(); //fallthrough
                                case PASS:
                                    nextPacket = inPacket;
                                    movePacket = true;
                                    moveAck = false;
                                    break;
                                case DROP:
                                    nextAck = new ack((byte) 2);
                                    moveAck = true;
                                    movePacket = false;
                                    break;
                                default:
                                    throw new IllegalStateException();
                            }

                            while (!moveAck) ;
                            moveAck = false;
                            objToSender.writeUnshared(nextAck);
                            nextAck = null;
                        }
                        else if (inObj instanceof Byte){
                            byte inByte = (Byte) inObj;
                            if (inByte == -1){
                                startTermination = true;
                                sender.close();
                                return;
                            }
                        }
                    }
                } catch (IOException ex){
                    System.out.println("Exception while attempting to make sender connection");
                } catch (ClassNotFoundException ex){
                    System.out.println("Invalid class received from sender");
                }
            }
        };

        senderThread.start();
    }
}
