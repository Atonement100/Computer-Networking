import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;


public class sender {
    public static void main(String[] args) throws IOException {
        if (args.length != 3){
            System.err.println("Usage: java client <hostname> <port #> <message filename>");
            return;
        }

        String hostname = args[0],
               filename = args[2];
        int port = Integer.parseInt(args[1]);


        try(    //Socket initialization
            Socket clientSocket = new Socket(hostname, port);
            PrintWriter toServer = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            ObjectOutputStream objToServer = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream objFromServer = new ObjectInputStream(clientSocket.getInputStream());
        )
        {
            //Successfully connected to our server
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            String serverStr, clientStr;

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

                Boolean alive = true;
                byte state = 0b000;
                byte currentPacket = 0;
                //noinspection ConstantConditions
                while (alive) {
                    switch(state){
                        case 0b00:
                        case 0b10:
                            if (currentPacket > outPackets.size()) {System.out.println("All packets sent"); state = 0b100;}
                            objToServer.writeObject(outPackets.elementAt(currentPacket));
                            state += 0b01;
                            break;
                        case 0b01:
                        case 0b11:
                            currentPacket++;
                            state = (byte)((state + 0b01) % 0b100);
                            break;
                        case 0b100:
                            alive = false;
                        default:
                            state = 0b100;
                    }

                }
            }
            catch(FileNotFoundException ex) {
                System.out.println("Unable to open file '" + filename + "'");
            }
            catch(IOException ex) {
                System.out.println("Error reading file '" + filename + "'");
            }
        } catch(UnknownHostException ex) {
            System.err.println("Couldn't find host " + hostname);
            System.exit(1);

        } catch (IOException ex){
            System.err.println("Couldn't make connection to " + hostname);
            System.exit(1);
        }
    }
}
