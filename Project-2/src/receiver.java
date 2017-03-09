import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

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
            PrintWriter toServer = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        )
        {
            //Successfully connected to our server
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            String serverStr, clientStr;

            //Allow user to keep sending input until they receive the terminate code, then shut down.
            while ((serverStr = fromServer.readLine()) != null){
                System.out.println("receive: " + serverStr);

                clientStr = stdIn.readLine();
                if (clientStr != null){
                    //System.out.println("Client: " + clientStr);
                    toServer.println(clientStr);
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

