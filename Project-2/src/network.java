import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class network {
    static ServerSocket network;
    static PrintWriter toReceiver, toSender;
    static BufferedReader fromReceiver, fromSender;

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
                    PrintWriter toReceiver = new PrintWriter(receiver.getOutputStream(), true);
                    BufferedReader fromReceiver = new BufferedReader(new InputStreamReader(receiver.getInputStream()));
                    ObjectOutputStream objToReceiver = new ObjectOutputStream(receiver.getOutputStream());
                    ObjectInputStream objFromReceiver = new ObjectInputStream(receiver.getInputStream());
                ){
                    toReceiver.println("a");
                    String inLine, outLine;
                    while ((inLine = fromReceiver.readLine()) != null) {
                        outLine = inLine;
                        toReceiver.println(outLine);
                        System.out.println("get: " + inLine + ", return: " + outLine);
                    }
                } catch (IOException ex){
                    System.out.println("Exception while attempting to make receiver connection");
                }
            }
        };

        Thread senderThread = new Thread(){
            public void run(){
                try (
                    Socket sender = network.accept();
                    PrintWriter toSender = new PrintWriter(sender.getOutputStream(), true);
                    BufferedReader fromSender = new BufferedReader(new InputStreamReader(sender.getInputStream()));
                    ObjectOutputStream objToSender = new ObjectOutputStream(sender.getOutputStream());
                    ObjectInputStream objFromSender = new ObjectInputStream(sender.getInputStream());
                ){
                    String inLine, outLine;
                    packet inPacket;
                    while ((inPacket = (packet) objFromSender.readObject()) != null) {
                        networkAction action = networkAction.generateNetworkAction();
                        outLine = "Received: Packet" + inPacket.getSequenceNum() + ", " + inPacket.getPacketId() + ", " + action;
                        System.out.println(outLine);
                    }
                } catch (IOException ex){
                    System.out.println("Exception while attempting to make sender connection");
                } catch (ClassNotFoundException ex){
                    System.out.println("Invalid class received from sender");
                }
            }
        };

        receiverThread.start();
        senderThread.start();
    }
}
