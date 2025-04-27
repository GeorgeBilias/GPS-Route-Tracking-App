package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MyThread extends Thread {

    String filepath;
    Handler handler;
    TextView label;
    TextView label2;
    Context context;


    private static final String SERVER_ADDRESS = "192.168.1.4"; //INSERT YOUR IP HERE
    private static final int PORT = 600;

    public MyThread(String filepath, Handler handler, TextView label, TextView label2, Context context) {
        this.filepath = filepath;
        this.handler = handler;
        this.label = label;
        this.label2 = label2;
        this.context = context;
    }

    @Override
    public void run() {
        try {
            try {
                Log.d("myTag", "about to connect");
                Socket socket = null;
                try {
                    socket = new Socket(SERVER_ADDRESS, PORT);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Log.d("myTag", "connected");

                File file = new File(this.filepath);
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Log.e("PRINTING,", " LINE: " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.e("path trim", "trimmed: " + this.filepath.trim());
                Log.e("existence", "does it exist: " + file.exists());
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
                        resultInfo route_stats;
                        route_stats = (resultInfo) in.readObject();

                        String route_stats_string;
                        route_stats_string = "CURRENT ROUTE ACTIVITY STATS: \n";

                        route_stats_string += "\nTotal distance: " + String.format("%.2f",route_stats.getTotalDistance()) + " km\n";
                        route_stats_string += "Average Speed: " + String.format("%.2f",route_stats.getAverageSpeed() * 60 * 60) + " km/h\n";
                        route_stats_string += "Total Elevation: " + String.format("%.2f",route_stats.getTotalelevation()) + " m\n";
                        route_stats_string += ("Total Time: " + String.format("%.2f",route_stats.getTotalTime()/60) + " mins");
                        Intent intent = new Intent(context, MainActivity2.class);

                        intent.putExtra("route_stats", route_stats_string);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);


                        ArrayList<resultInfo> avg_stats = (ArrayList<resultInfo>) in.readObject();

                        String user_stats;

                        user_stats = "\n\nUSER TOTAL STATS: \n";
                        user_stats += "Total Exercise Time: " + String.format("%.2f",avg_stats.get(0).getTotalTime()/60) + " mins\n";
                        user_stats += "Total Distance Time: " + String.format("%.2f",avg_stats.get(0).getTotalDistance()) + " km\n";
                        user_stats += "Total Elevation Time: " + String.format("%.2f",avg_stats.get(0).getTotalelevation()) + " meters\n\n";

                        user_stats += "USER AVERAGE STATS: \n";
                        user_stats += "\nAverage Exercise Time: " + String.format("%.2f",avg_stats.get(1).getTotalTime()/60) + " mins\n";
                        user_stats += "Average Distance Time: " + String.format("%.2f",avg_stats.get(1).getTotalDistance()) + " km\n";
                        user_stats += "Average Elevation Time: " + String.format("%.2f",avg_stats.get(1).getTotalelevation()) + " meters\n\n";

                        user_stats += "GLOBAL AVERAGE ROUTE STATS: \n";
                        user_stats += "\nAverage Exercise Time: " + String.format("%.2f",avg_stats.get(2).getTotalTime()/60) + " mins\n";
                        user_stats += "Average Distance: " + String.format("%.2f",avg_stats.get(2).getTotalDistance()) + " km\n";
                        user_stats += "Average Elevation: " + String.format("%.2f",avg_stats.get(2).getTotalelevation()) + " meters\n";

                        user_stats += "\nGLOBAL AVERAGE USER STATS: \n";
                        user_stats += "\nAverage Exercise Time: " + String.format("%.2f",avg_stats.get(3).getTotalTime()/60) + " mins\n";
                        user_stats += "Average Distance: " + String.format("%.2f",avg_stats.get(3).getTotalDistance()) + " km\n";
                        user_stats += "Average Elevation: " + String.format("%.2f",avg_stats.get(3).getTotalelevation()) + " meters\n";

                        if (avg_stats.get(4).getTotalTime()<0){
                            user_stats += "\nYou have exercised "+String.format("%.2f", Math.abs(avg_stats.get(4).getTotalTime()))+"% less than the Average";
                        }else{
                            user_stats += "\nYou have exercised "+String.format("%.2f", avg_stats.get(4).getTotalTime())+"% more than the Average";
                        }
                        if (avg_stats.get(4).getTotalDistance()<0){
                            user_stats += "\nYou have travelled "+String.format("%.2f", Math.abs(avg_stats.get(4).getTotalDistance()))+"% less than the Average";
                        }else{
                            user_stats += "\nYou have travelled "+String.format("%.2f", avg_stats.get(4).getTotalDistance())+"% more than the Average";
                        }
                        if (avg_stats.get(4).getTotalelevation()<0){
                            user_stats += "\nYou have elevated "+String.format("%.2f", Math.abs(avg_stats.get(4).getTotalelevation()))+"% less than the Average";
                        }else{
                            user_stats += "\nYou have elevated "+String.format("%.2f", avg_stats.get(4).getTotalelevation())+"% more than the Average";
                        }

                        MainActivity.results = user_stats;

                        Log.e("user stats", user_stats);
                        file.delete();

                        break;
                    } catch (ClassNotFoundException e) {
                        System.out.println(e);
                    }
                }

                socket.close();
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
