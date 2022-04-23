import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

class InputOutput {
    public static void main(String[] args) throws Exception{
        // doing this all in the main method probably violates some java style rules but the input/output is 
        // really too simple to justify building a much bigger structure
        boolean run = true;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while(run){
            // get input
            System.out.print("> ");
            String input = br.readLine();

            Socket sock  = new Socket("127.0.0.1", Integer.parseInt(args[0]));
            PrintWriter sockWriter = new PrintWriter(sock.getOutputStream());
            BufferedReader sockReader = new BufferedReader(new InputStreamReader( sock.getInputStream()));

            if( "List contents of the log".equals(input) || "PRINT_LOG_CMD".equals(input)){
                // if the user requests the log, send special request
                sockWriter.println("PRINT_LOG_CMD");
                sockWriter.flush();

                // deserialize the arraylist of log entries
                ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
                @SuppressWarnings("unchecked")  
                ArrayList<String> logEntries = (ArrayList<String>) ois.readObject();
                sock.close();

                // print the log
                for (String entry : logEntries) {
                    System.out.println("[LOG] " + entry);
                }
            } else {
                // send normal request to the calculator, get evaluated response, print
                sockWriter.println(input);
                sockWriter.flush();
                System.out.println( "\t--> " + sockReader.readLine());
                sock.close();
            }


        }

    }
}