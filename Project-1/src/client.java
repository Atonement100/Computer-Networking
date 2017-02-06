import java.io.*;
import java.net.*;

public class client {
    public static void main(String[] args) throws IOException{
        if (args.length != 2){
            System.err.println("Usage: java client <hostname> <port #>");
            return;
        }

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        
        try(
            Socket clientSocket = new Socket(hostname, port);
            PrintWriter toServer = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        )
        {
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            String serverStr, clientStr;

            while ((serverStr = fromServer.readLine()) != null){
                System.out.println("Server: " + serverStr);
                if (serverStr.equals("-5")){
                    break;
                }

                clientStr = stdIn.readLine();
                if (clientStr != null){
                    System.out.println("Client: " + clientStr);
                    toServer.print(clientStr);
                }
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
