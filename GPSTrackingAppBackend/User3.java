import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import com.example.myapplication.resultInfo;

public class User3 {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 600;

    public static void main(String[] args) {
        // System.out.println(args.length);

        
            // read_gpx(args[0]);

            try {
                
                
                Socket socket = new Socket(SERVER_ADDRESS, PORT);

                File file = new File(args[0]);
                FileInputStream fileInput = new FileInputStream(file);
                byte[] fileData = new byte[(int) file.length()];
                fileInput.read(fileData);
                fileInput.close();
                String fileString = new String(fileData, StandardCharsets.UTF_8);
                
                // Write the file data to the server's output stream
                OutputStream outputStream = socket.getOutputStream();
                PrintWriter out = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);
                out.println(fileString);

                


                

                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                while (true) { // keep looping until you receive the info

                    try {
                    	resultInfo route_stats = (resultInfo) in.readObject();
                    	System.out.println("\nCurrent route Activity stats: \n");
                    	
                    	System.out.println("Total distance: "+route_stats.getTotalDistance()+" km");
                    	System.out.println("Average Speed: "+route_stats.getAverageSpeed()*60*60 +" km/h");
                    	System.out.println("Total Elevation: "+route_stats.getTotalelevation()+ " m");
                    	System.out.println("Total Time: "+route_stats.getTotalTime()+ " sec");

                        ArrayList<resultInfo> avg_stats = (ArrayList<resultInfo>) in
                                .readObject(); // read results
                        // System.out.println("-- Read from Master --\n");
                        // List<resultInfo> Map_results = (List<resultInfo>) in.readObject();
                        

                        System.out.println("\nUser stats: \n");
                        System.out.println("Average Exercise Time: " + avg_stats.get(0).getTotalTime() + " sec");
                        System.out.println("Average Distance Time: " + avg_stats.get(0).getTotalDistance() + " km");
                        System.out
                                .println("Average Elevation Time: " + avg_stats.get(0).getTotalelevation()
                                        + " meters\n");

                        System.out.println("Global Average Stats: \n");
                        System.out.println("Average Exercise Time: " + avg_stats.get(1).getTotalTime() + " sec");
                        System.out.println("Average Distance: " + avg_stats.get(1).getTotalDistance() + " km");
                        System.out.println("Average Elevation: " + avg_stats.get(1).getTotalelevation() + " meters");
                        break;
                    } catch (ClassNotFoundException e) {
                        System.out.println(e);
                    }
                }

                //outputStream.close(); // close connection with master
                socket.close();

            } catch (UnknownHostException e) {
                System.err.println("Unknown host: " + SERVER_ADDRESS);
            } catch (IOException e) {
                System.err.println("Failed to connect to server: " + e.getMessage());
            }
        }
    

    
}