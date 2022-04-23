import java.net.Socket;
import java.net.ServerSocket;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.BufferedReader;
// Your First Program
import java.io.OutputStream;

class Calculator {

    static int serverPort; 
    static int loggerPort;

    static class CalculatorRunnable  implements Runnable {
        Socket clientSock;
        public CalculatorRunnable(Socket clientSock){
            this.clientSock = clientSock;
        }

        // these are our evaluation functions for the various operations
        float add(float a, float b){
            return a + b;
        }

        float sub(float a, float b){
            return a - b;
        }

        float mult(float a, float b){
            return a * b;
        }

        float div(float a, float b){
            // handle divide by zero above this level so we dont crash
            return a / b;
        }

        public String calculate(String expression){
            String[] tokens = expression.split("\\s");

            // We are not required to do anything super complicated so we are going to restrict this
            // to simple expressions of the form <numb> <op> <numb> where op is any of (+,-,*, \).
            // and num can be anything that becomes a float

            if (tokens.length > 3){
                return "Expression was too complicated, please use form  <numb> <op> <numb>";
            }

            try{
                // based on operation, try and evaluate, but if the numbers arent valid, emit an error message
                if(tokens[1].equals("+")){
                    return String.valueOf(add(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[2])));
                }

                if(tokens[1].equals("-")){
                    return String.valueOf(sub(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[2])));
                }

                if(tokens[1].equals("*")){
                    return String.valueOf(mult(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[2])));
                }

                if(tokens[1].equals("/")){
                    if(Float.parseFloat(tokens[2]) == 0.0){
                        return "can not divide by zero!";
                    }
                    return String.valueOf(div(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[2])));
                }

            } catch (Exception e){
                return "Failed to parse your expression";
            }
            return "invalid";
        }

        public void run(){
            try{
                // set up socket IO, to the client and the logger
                BufferedReader sockReader = new BufferedReader(new InputStreamReader( clientSock.getInputStream()));
                PrintWriter sockWriter = new PrintWriter(clientSock.getOutputStream());

                Socket loggerSocket = new Socket("127.0.0.1", 6000);
                PrintWriter logWriter = new PrintWriter(loggerSocket.getOutputStream());

                String line  = sockReader.readLine();
                // we expect a special message if the client requests the full log from the logger
                if(line.equals("PRINT_LOG_CMD")){
                    
                    OutputStream  writeToClient = clientSock.getOutputStream();
                    InputStream readFromLogger = loggerSocket.getInputStream();

                    logWriter.println(line);
                    logWriter.flush();

                    // we are simply bridging the two sockets, so we dont want to impose any sort of type
                    // on this data, so we are just going to do a memcopy over.
                    byte[] buf = new byte[8192];
                    int length;
                    while ((length = readFromLogger.read(buf)) > 0) {
                        writeToClient.write(buf, 0, length);
                    }
                    
                } else {
                    //otherwise  do a normal calcuation and log the results
                    String result = calculate(line);
                    sockWriter.println(result);
                    logWriter.println("Expression \"" + line + "\" gave results: " + result);
                    logWriter.flush();
                }
                // clean up, flush and close.
                sockWriter.flush();
                loggerSocket.close();
                clientSock.close();
            } catch (IOException e) {
                // no requirement for real exception handling
            }


        }
    }
    public static void main(String[] args) throws IOException{
        serverPort = Integer.parseInt(args[0]);
        loggerPort = Integer.parseInt(args[1]);
        ServerSocket sockServ = new ServerSocket(serverPort);

        // this side-steps some warnings, but this should run infinitely
        boolean keepRunning = true;
        while(keepRunning){
            Socket clientSocket = sockServ.accept();
            
            CalculatorRunnable calc = new CalculatorRunnable(clientSocket);
            calc.run();
        }

        sockServ.close();
    }
}