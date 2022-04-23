import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.BufferedReader;



class Logger {


    static public class LogManager{
        // An array list will stand in for a real database or other massive storage system
        // this does mean that if we stop and restart the logger, we lose the history, however
        // data retention was not a requirement of the implementation
        ArrayList<String> log = new ArrayList<String>();

        // add an entry to the log
        void logMessage(String message){
            log.add(message);
        }
    
        // serialize the log and transmit it on the requesting socket. 
        void transmitLog(Socket sock){
            try {
            ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
            oos.writeObject(log);
            } catch(Exception e) {

            }
        }
    }


    static class LoggerRunnable implements Runnable{
        LogManager log;
        Socket calcSocket;

        public LoggerRunnable(LogManager log, Socket calcSocket){
            this.log = log;
            this.calcSocket = calcSocket;
        }

        public void run(){
            try{
                BufferedReader sockReader = new BufferedReader(new InputStreamReader( calcSocket.getInputStream()));

                // we can assume that valid input will be one line
                String line = sockReader.readLine();
                
                System.out.println("LOGGING MESSAGE - " + line);
                
                // determine action, log or send log
                if("PRINT_LOG_CMD".equals(line)){
                    this.log.transmitLog(this.calcSocket);
                } else {
                    this.log.logMessage(line);
                }

                calcSocket.close();
        
            } catch (Exception e){
                // no requirement for comprehensive error handiling
            }
        }
    }

    public static void main(String[] args) throws IOException {
        int loggerPort = Integer.parseInt(args[0]);
        ServerSocket sockServ = new ServerSocket(loggerPort);
        
        LogManager log = new LogManager();

        boolean keepRunning = true;
        while(keepRunning){
            Socket sock = sockServ.accept();
            LoggerRunnable task = new LoggerRunnable(log, sock);
            task.run();
        }
        
        sockServ.close();
        
        
    }
}