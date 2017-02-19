import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.HashMap;

public class client {

    private static HashMap<String, String> errorCodes;
    static {
        HashMap<String, String> errorCodesTemp = new HashMap<>();
        errorCodesTemp.put("-1", "incorrect operation command.");
        errorCodesTemp.put("-2", "number of inputs is less than two.");
        errorCodesTemp.put("-3", "number of inputs is more than four.");
        errorCodesTemp.put("-4", "one or more of the inputs contain(s) non-number(s)");
        errorCodesTemp.put("-5", "exit.");
        errorCodes = errorCodesTemp;
    }


    public static void main(String[] args) throws IOException{
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
                if (errorCodes.containsKey(serverStr)){
                    System.out.println("receive: " + errorCodes.get(serverStr));
                    if (serverStr.equals("-5")){
                        break;
                    }
                }
                else{
                    System.out.println("receive: " + serverStr);
                }


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
