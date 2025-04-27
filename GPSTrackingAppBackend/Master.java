import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

// import javax.swing.text.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import com.example.myapplication.resultInfo;

public class Master {
    // -----------------------------------------------------------------------------------//
    /*
     * //
     * CREATING AND INITIALIZING THE VARSIABLES //
     */ //
    // -----------------------------------------------------------------------------------//
    private static final int PORT = 600; // contains the first port number for the server
    private static final int PORT2 = 200;// contains the second port number for the server
    private static final int THREAD_COUNT = 3;// the number of the threads that will be used
    private static SynchronizedArrayList<Chunk> chunks = new SynchronizedArrayList<Chunk>();// an arrayList which
    // contains the chunks
    private static SynchronizedArrayList<Socket> workers = new SynchronizedArrayList<Socket>(); // an ArrayList filled
                                                                                                // with the worker
                                                                                                // objects
    public static HashMap<Integer, List<resultInfo>> results_for_each_route = new HashMap<Integer, List<resultInfo>>();// an
                                                                                                                       // ArrayList
                                                                                                                       // that
                                                                                                                       // has
                                                                                                                       // a
                                                                                                                       // ResultInfo
                                                                                                                       // object
                                                                                                                       // List
                                                                                                                       // for
                                                                                                                       // each
                                                                                                                       // route
    public static HashMap<String, SynchronizedArrayList<resultInfo>> results_of_user_routes = new HashMap<String, SynchronizedArrayList<resultInfo>>();// creating
    // a
    // HashMap
    // for
    // each
    // user
    // along
    // with
    // his
    // ArrayList
    // for
    // the
    // results
    // of
    // his
    // routes

    public static void main(String[] args) {
        // -----------------------------------------------------------------------------------//
        /*
         * //
         * CREATING AND INITIALIZING THE SERVER AND //
         * ITS SOCKETS //
         */ //
        // -----------------------------------------------------------------------------------//
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);
            // Starting server on the socket with port 600
            for (int i = 0; i < THREAD_COUNT; i++) {
                Thread t = new Thread(new ConnectionHandler(serverSocket));
                t.start();
            } // creating and starting the threads
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        } // error in case server doesn't start

        try {
            ArrayList<ServerSocket> socket_list = new ArrayList<>(); // creating and ArrayList which contains the
                                                                     // ServerSockets for the second port

            System.out.println("created sockets for each worker to join to");
            // -----------------------------------------------------------------------------------//
            /*
             * //
             * READING THE CONFIGURATION FILE AND CREATING //
             * HE WORKERS //
             */ //
            // -----------------------------------------------------------------------------------//
            File config = new File("config.txt");// creating the file
            Scanner r = new Scanner(config); // creating a scanner for it
            String line = r.nextLine();// read the line
            String[] parts = line.split(" ");// split the spaces
            int num_of_workers = Integer.parseInt(parts[2]);// save the number of the workers
            // System.out.println("number of workers: ");
            // System.out.print(num_of_workers);// print the number of the workers
            r.close();// close the scanner

            for (int i = 0; i < num_of_workers; i++) {

                ServerSocket temp = new ServerSocket(i + PORT2);
                socket_list.add(temp);
                temp = null;
            } // creating these sockets and adding them to the ArrayList
              // -----------------------------------------------------------------------------------//
            /*
             * //
             * CREATING AND INITIALIZING THE SERVER CONNECTION //
             * FOR THE WORKERS //
             */ //
            // -----------------------------------------------------------------------------------//

            for (int i = 0; i < num_of_workers; i++) {
                workers.add(socket_list.get(i).accept());
                System.out.println("worker joined");

                // Worker worker = new Worker(socket_list.get(i), i, i + PORT2);
                // workers.add(worker);
                // worker.start();
                // System.out.println(worker.getId());
                // socket_clients.add(socket_list.get(i).accept());

            } // for each worker create a socket and start the communication with a client

            System.out.println("created workers that are connected");
            // -----------------------------------------------------------------------------------//
            /*
             * //
             * CREATING THE RUNNABLE PART OF THE CODE WHICH IS //
             * GOING TO CALL ALL THE METHODS //
             */ //
            // -----------------------------------------------------------------------------------//

            ObjectOutput outt;
            ObjectInput inn;
            synchronized (Master.class) {
                while (true) { // send any data to the worker that was received from the clients

                    // System.out.println(results_of_user_routes.size());

                    if (chunks.size() != 0) {

                        for (int j = 0; j < workers.size(); j++) {

                            // workers.get(i).start();

                            if (chunks.size() == 0) {
                                break;
                            } // if there isn't any chunks to work on break out of the while loop

                            outt = new ObjectOutputStream(workers.get(j).getOutputStream());// create an output Stream
                            System.out.println("About to send----");
                            outt.writeObject(chunks.get(0));// write to it the first chunk from the list
                            chunks.remove(0);// and remove it from it

                            outt.flush();

                            inn = new ObjectInputStream(workers.get(j).getInputStream());// create an Input Stream
                            Pair<Integer, resultInfo> p = (Pair<Integer, resultInfo>) inn.readObject();// create a pair
                                                                                                       // where the
                                                                                                       // object
                                                                                                       // is saved
                            resultInfo chunk_result = p.getValue();// save the value from the pair as result of the
                                                                   // chunk
                            // result.setUser(p.getKey());

                            if (!results_for_each_route.containsKey(p.getKey())) {
                                results_for_each_route.put(p.getKey(), new ArrayList<resultInfo>());

                            } // if the route isn't already in the list add it as a key with its array

                            results_for_each_route.get(p.getKey()).add(chunk_result);// add the result of the chunk to
                                                                                     // the
                                                                                     // array of the route

                            outt = null;
                            if (chunk_result.get_last_one() == true) {

                                Reduce(p.getKey(), results_for_each_route);
                                // System.out.println("Reduced");
                            } // if the last chunk was calculated call the reduce function

                            // System.out.println(results_of_user_routes.size());

                        } // searching through the workers to get an active one

                    } // while there are still some chunks to work on keep looking for workers

                }
            }

        } catch (IOException e2) {
            System.err.println(e2);
            System.out.println("Connection error");

        } catch (ClassNotFoundException e3) {
            System.err.println(e3);
        } // error codes

    }// end of the main class where methods are called
     // -----------------------------------------------------------------------------------//
    /*
     * / //
     * CREATING THE REDUCE FUNCTION WHICH COLLECTS //
     * THE CHUNKS OF A ROUTE AND CALCULATES //
     * THE RESULTS OF THE WHOLE ROUTE //
     */ //
    // ------------------------------------------------------------------------------------//

    public static void Reduce(int route, HashMap<Integer, List<resultInfo>> results_of_routes_chunks) {
        List<resultInfo> route_chunks_results = results_of_routes_chunks.get(route);
        // initializing the things we want to calculate to 0
        double TotalDistance = 0.0;
        double TotalAverageSpeed = 0.0;
        double Totalelevation = 0.0;
        double TotalTime = 0.0;

        for (resultInfo value : route_chunks_results) {
            TotalDistance += value.getTotalDistance();
            TotalAverageSpeed += value.getAverageSpeed();
            Totalelevation += value.getTotalelevation();
            TotalTime += value.getTotalTime();
            // System.out.println(value.getTotalDistance());
            // System.out.println(value.getTotalTime());
            // System.out.println("Total Distance: " + TotalDistance);

        } // calculating the totals of the route

        TotalAverageSpeed = TotalAverageSpeed / route_chunks_results.size(); // calculate the avg speed of the route
        // System.out.println(route_chunks_results.size());
        // System.out.println(TotalDistance);
        // System.out.println(TotalAverageSpeed); // oxi auto
        // System.out.println(Totalelevation);
        // System.out.println(TotalTime);
        // prints
        resultInfo route_results = new resultInfo();// creating an object with the results of the route
        route_results.setUser(route_chunks_results.get(0).getUser());
        route_results.setAverageSpeed(TotalAverageSpeed);
        route_results.setTotalDistance(TotalDistance);
        route_results.setTotalelevation(Totalelevation);
        route_results.setTotalTime(TotalTime);
        route_results.set_route(route);
        // and setting its attributes

        if (!results_of_user_routes.containsKey(route_results.getUser())) {
            results_of_user_routes.put(route_results.getUser(), new SynchronizedArrayList<resultInfo>());
        } // if the user isn't already set as a key inside the HashMap create it along
          // with his ArrayList
        results_of_user_routes.get(route_results.getUser()).add(route_results); // and add it

    }

    // -----------------------------------------------------------------------------------//
    /*
     * //
     * FUNCTIONS OF THE MASTER // //
     */ //
    // -----------------------------------------------------------------------------------//

    public static SynchronizedArrayList<Chunk> getChunks() {
        return chunks;
    }// return the List with the chunks

    public static void addChunk(Chunk i) {
        chunks.add(i);
    }// add a chunk to the list

    public static SynchronizedArrayList<resultInfo> getPersonalStats(String user) {

        // SynchronizedArrayList<resultInfo> user_stats =
        // results_of_user_routes.get(user);
        SynchronizedArrayList<resultInfo> user_stats = results_of_user_routes.get(user);

        return user_stats;
    }// return the results of the user for each route

    public static boolean check_final_rslts_of_routes(String user, int route) {

        if (results_of_user_routes.containsKey(user)) {
            return true;
        } else {
            return false;
        }

    } // check if specific user has been processed already

    public static SynchronizedArrayList<resultInfo> get_processed_rslts(String user, int route) {
        return results_of_user_routes.get(user);
    } // return list of hashamp

    public static HashMap<String, SynchronizedArrayList<resultInfo>> get_procesed_rslts2() {
        return results_of_user_routes;
    } // return hashmap

}
// -----------------------------------------------------------------------------------//
/*
 * //
 * CREATING HANDLERS THAT CONTROL THE RUNNABLE //
 */ //
// -----------------------------------------------------------------------------------//

class ConnectionHandler implements Runnable {
    private ServerSocket serverSocket;

    public ConnectionHandler(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void run() {
        // System.out.println(Master.getChunks().size());

        while (true) {
            try {
                // System.out.println(Master.getChunks().size());
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();

            } catch (IOException e) {
                System.err.println("Failed to accept client connection: " + e.getMessage());
            }
        } // handling the connection of the client
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private static String user = "";
    private static ArrayList<String> lat = new ArrayList<String>();
    private static ArrayList<String> lon = new ArrayList<String>();
    private static ArrayList<String> ele = new ArrayList<String>();
    private static ArrayList<String> time = new ArrayList<String>();
    private static ArrayList<Chunk> chunks = new ArrayList<Chunk>();

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        try {

            Route.route++;
            // ObjectInputStream inputStream = new
            // ObjectInputStream(clientSocket.getInputStream());
            ObjectOutputStream outputstream = new ObjectOutputStream(clientSocket.getOutputStream());

            // Receive the GPX file from the client
            InputStream inputStream = clientSocket.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                // System.out.println(line);
                sb.append(line);
                sb.append(System.lineSeparator());
                if (line.contains("</gpx>")) { // check for the </gpx> tag
                    break;
                }
            }
            String fileString = sb.toString();

            // Write the string to a new file
            String filepath = String.format("processed/_%d.gpx", Route.route);
            BufferedWriter writer = new BufferedWriter(new FileWriter(filepath));
            writer.write(fileString);
            writer.close();

            read_gpx(filepath);

            int waypoint = 0;
            int counter = 0;
            Information temp = new Information();
            // System.out.println(lat.size());
            // System.out.println(lon.size());
            // System.out.println(ele.size());
            // System.out.println(time.size());

            while (lat.size() != 0) {
                if (counter == 0) {
                    Chunk chunk = new Chunk();

                    temp = new Information(Float.parseFloat(lat.get(waypoint)),
                            Float.parseFloat(lon.get(waypoint)), Float.parseFloat(ele.get(waypoint)),
                            time.get(waypoint));
                    lat.remove(waypoint);
                    lon.remove(waypoint);
                    ele.remove(waypoint);
                    time.remove(waypoint);
                    chunk.set_info1(temp);

                    // System.out.println(lat.size());

                    if (lat.size() == 0) {
                        chunk.set_User(user);
                        chunks.add(chunk);

                        break;
                    }

                    temp = new Information(Float.parseFloat(lat.get(waypoint)), Float.parseFloat(lon.get(waypoint)),
                            Float.parseFloat(ele.get(waypoint)), time.get(waypoint));
                    lat.remove(waypoint);
                    lon.remove(waypoint);
                    ele.remove(waypoint);
                    time.remove(waypoint);
                    chunk.set_info2(temp);

                    // System.out.println(lat.size());

                    if (lat.size() == 0) {
                        chunk.set_User(user);
                        chunks.add(chunk);
                        break;
                    }

                    temp = new Information(Float.parseFloat(lat.get(waypoint)), Float.parseFloat(lon.get(waypoint)),
                            Float.parseFloat(ele.get(waypoint)), time.get(waypoint));
                    lat.remove(waypoint);
                    lon.remove(waypoint);
                    ele.remove(waypoint);
                    time.remove(waypoint);
                    chunk.set_info3(temp);

                    // System.out.println(lat.size());

                    chunk.set_User(user);
                    chunks.add(chunk);
                    chunk = null;
                    // temp = null;
                    counter++;
                } else if (counter != 0) {
                    Chunk chunk = new Chunk();
                    chunk.set_info1(temp);

                    if (lat.size() == 0) {
                        chunk.set_User(user);
                        chunks.add(chunk);

                        break;
                    }

                    temp = new Information(Float.parseFloat(lat.get(waypoint)), Float.parseFloat(lon.get(waypoint)),
                            Float.parseFloat(ele.get(waypoint)), time.get(waypoint));
                    lat.remove(waypoint);
                    lon.remove(waypoint);
                    ele.remove(waypoint);
                    time.remove(waypoint);
                    chunk.set_info2(temp);

                    if (lat.size() == 0) {
                        chunk.set_User(user);
                        chunks.add(chunk);
                        break;
                    }

                    temp = new Information(Float.parseFloat(lat.get(waypoint)), Float.parseFloat(lon.get(waypoint)),
                            Float.parseFloat(ele.get(waypoint)), time.get(waypoint));
                    lat.remove(waypoint);
                    lon.remove(waypoint);
                    ele.remove(waypoint);
                    time.remove(waypoint);
                    chunk.set_info3(temp);

                    counter++;
                    chunk.set_User(user);
                    chunks.add(chunk);
                    chunk = null;
                }

            }

            for (int i = 0; i < chunks.size(); i++) {
                System.out.println("Chunk " + i + " Info 1: " + chunks.get(i).get_info1());
                System.out.println("Chunk " + i + " Info 2: " + chunks.get(i).get_info2());
                System.out.println("Chunk " + i + " Info 3: " + chunks.get(i).get_info3());
            }

            chunks.get(chunks.size() - 1).set_last_one(true);

            System.out.println(chunks.size());

            int num_of_chunks = chunks.size();
            String temp1 = " ";
            int temp2 = Route.route;

            for (int i = 0; i < num_of_chunks; i++) {

                chunks.get(i).setRouteNumber(Route.route);
                // System.out.println("Route is: " + chunk.getRouteNumber());

                Master.addChunk(chunks.get(i));
                if (i == num_of_chunks - 1) {

                    temp1 = chunks.get(i).get_User();
                }

            } // adding chunks to the list

            chunks.clear();

            // wait for workers

            java.util.concurrent.TimeUnit.SECONDS.sleep(10);
            // outputstream.writeObject(Master.get_procesed_rslts2()); // send all the
            // results to the user who is connected
            resultInfo route_stats = null;
            SynchronizedArrayList<resultInfo> user_routes = Master.results_of_user_routes.get(temp1);

            for (int i = 0; i < user_routes.size(); i++) {
                if (user_routes.get(i).get_route() == temp2) {
                    route_stats = user_routes.get(i);
                }
            }
            System.out.println("\n Activity Results: \n");
            System.out.println("Total Distance: " + route_stats.getTotalDistance());
            System.out.println("Average Speed: " + route_stats.getAverageSpeed());
            System.out.println("Total Elevation: " + route_stats.getTotalelevation());
            System.out.println("Total Time: " + route_stats.getTotalTime());
            outputstream.writeObject(route_stats);
            outputstream.flush();

            double total_user_exercise_time = 0;
            double total_user_distance = 0;
            double total_user_elevation = 0;

            SynchronizedArrayList<resultInfo> temp5 = new SynchronizedArrayList<>();
            // List<resultInfo> Map_results = new List<>();
            temp5 = Master.get_procesed_rslts2().get(temp1);
            double user_avg_exercise_time = 0;
            double user_avg_distance = 0;
            double user_avg_elevation = 0;

            // List<resultInfo> Map_results = Master.results_for_each_route.get(temp2);

            if (temp5 == null) {
                // System.out.println("User stats: ");
                // System.out.println("Average Exercise Time: " + avg_exercise_time + " sec");
                // System.out.println("Average Distance Time: " + avg_distance + " km");
                // System.out.println("Average Elevation Time: " + avg_elevation + " meters");
            } else {

                for (int i = 0; i < temp5.size(); i++) {
                    total_user_distance += temp5.get(i).getTotalDistance();
                    total_user_exercise_time += temp5.get(i).getTotalTime();
                    total_user_elevation += temp5.get(i).getTotalelevation();
                }

                user_avg_exercise_time = total_user_exercise_time / temp5.size();
                user_avg_distance = total_user_distance / temp5.size();
                user_avg_elevation = total_user_elevation / temp5.size();

                System.out.println("User stats: ");
                System.out.println("Average Exercise Time: " + user_avg_exercise_time + " sec");
                System.out.println("Average Distance Time: " + user_avg_distance + " km");
                System.out.println("Average Elevation Time: " + user_avg_elevation + " meters");

            }

            ArrayList<resultInfo> stats = new ArrayList<>();

            resultInfo user_total = new resultInfo();

            user_total.setTotalDistance(total_user_distance);
            user_total.setTotalTime(total_user_exercise_time);
            user_total.setTotalelevation(total_user_elevation);

            stats.add(user_total);// 0 user total

            resultInfo user_avg = new resultInfo();
            user_avg.setTotalDistance(user_avg_distance);
            user_avg.setTotalTime(user_avg_exercise_time);
            user_avg.setTotalelevation(user_avg_elevation);

            stats.add(user_avg); // 1 user avg

            double sum_exercise_time = 0;
            double sum_distance = 0;
            double sum_elevation = 0;

            HashMap<String, SynchronizedArrayList<resultInfo>> h = Master.get_procesed_rslts2();

            for (int j = 0; j < 3; j++) {
                if (j == 0 && h.get("user1") != null) {
                    for (int z = 0; z < h.get("user1").size(); z++) {
                        sum_distance += h.get("user1").get(z).getTotalDistance();
                        sum_exercise_time += h.get("user1").get(z).getTotalTime();
                        sum_elevation += h.get("user1").get(z).getTotalelevation();
                    }
                } else if (j == 1 && h.get("user2") != null) {
                    for (int z = 0; z < h.get("user2").size(); z++) {
                        sum_distance += h.get("user2").get(z).getTotalDistance();
                        sum_exercise_time += h.get("user2").get(z).getTotalTime();
                        sum_elevation += h.get("user2").get(z).getTotalelevation();
                    }
                } else if (j == 2 && h.get("user3") != null) {
                    for (int z = 0; z < h.get("user3").size(); z++) {
                        sum_distance += h.get("user3").get(z).getTotalDistance();
                        sum_exercise_time += h.get("user3").get(z).getTotalTime();
                        sum_elevation += h.get("user3").get(z).getTotalelevation();
                    }
                }
            }

            int num = 0;
            int num2 = 0;
            if (h.containsKey("user1")) {
                num += h.get("user1").size();
                num2++;
            }

            if (h.containsKey("user2")) {
                num += h.get("user2").size();
                num2++;
            }

            if (h.containsKey("user3")) {
                num += h.get("user3").size();
                num2++;
            }

            double sum_avg_route_exercise_time = sum_exercise_time / (num);
            double sum_avg_route_distance = sum_distance / (num);
            double sum_avg_route_elevation = sum_elevation / (num);

            double sum_avg_users_exercise_time = sum_exercise_time / (num2);
            double sum_avg_users_distance = sum_distance / (num2);
            double sum_avg_users_elevation = sum_elevation / (num2);
            System.out.println(num2);
            System.out.println("Average All User Stats: \n");
            System.out.println("Average Exercise Time: " + sum_avg_users_exercise_time + " sec");
            System.out.println("Average Distance: " + sum_avg_users_distance + " km");
            System.out.println("Average Elevation: " + sum_avg_users_elevation + " meters");

            System.out.println("Average Route Stats: \n");
            System.out.println("Average Exercise Time: " + sum_avg_route_exercise_time + " sec");
            System.out.println("Average Distance: " + sum_avg_route_distance + " km");
            System.out.println("Average Elevation: " + sum_avg_route_elevation + " meters");

            double percentAvgTimeWork = 0;
            double percentAvgdistance = 0;
            double percentAvgTelevation = 0;
            percentAvgTimeWork = ((total_user_exercise_time - sum_avg_users_exercise_time)
                    / sum_avg_users_exercise_time)*100;
            percentAvgdistance = ((total_user_distance - sum_avg_users_distance) / sum_avg_users_distance)*100;
            percentAvgTelevation = ((total_user_elevation - sum_avg_users_elevation) / sum_avg_users_elevation)*100;

            resultInfo percent = new resultInfo();
            percent.setTotalTime(percentAvgTimeWork);
            percent.setTotalDistance(percentAvgdistance);
            percent.setTotalelevation(percentAvgTelevation);

            System.out.println("time: " + percentAvgTimeWork + " %");
            System.out.println("distance: " + percentAvgdistance + " %");
            System.out.println("Elevation: " + percentAvgTelevation + " %");

            resultInfo all_users_avg_route = new resultInfo();
            all_users_avg_route.setTotalDistance(sum_avg_route_distance);
            all_users_avg_route.setTotalTime(sum_avg_route_exercise_time);
            all_users_avg_route.setTotalelevation(sum_avg_route_elevation);

            stats.add(all_users_avg_route); // 2 global average route

            resultInfo all_users_avg = new resultInfo();
            all_users_avg.setTotalDistance(sum_avg_users_distance);
            all_users_avg.setTotalTime(sum_avg_users_exercise_time);
            all_users_avg.setTotalelevation(sum_avg_users_elevation);

            stats.add(all_users_avg); // 3 global average users

            stats.add(percent); // 4 percent for each user

            outputstream.writeObject(stats);

            // inputStream.close(); // close connection with user
            clientSocket.close();
            outputstream.close();

        } catch (IOException e) {
            System.err.println("Failed to handle client connection: " + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void read_gpx(String filepath) {
        try {
            File inputFile = new File(filepath);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            // System.out.println("Root element :" +
            // doc.getDocumentElement().getNodeName());
            user = doc.getDocumentElement().getAttribute("creator"); // save the user who is sending the file for future
                                                                     // use

            NodeList nList = doc.getElementsByTagName("wpt");

            // System.out.println("----------------------------");

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);

                // System.out.println("\nCurrent Element :" + nNode.getNodeName());

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;
                    lat.add(eElement.getAttribute("lat"));
                    // System.out.println("lat : " + eElement.getAttribute("lat"));
                    lon.add(eElement.getAttribute("lon"));
                    // System.out.println("lon : " + eElement.getAttribute("lon"));

                    ele.add(eElement.getElementsByTagName("ele").item(0).getTextContent());

                    // System.out.println("Ele : " +
                    // eElement.getElementsByTagName("ele").item(0).getTextContent());
                    time.add(eElement.getElementsByTagName("time").item(0).getTextContent());

                    // System.out.println("Time : " +
                    // eElement.getElementsByTagName("time").item(0).getTextContent());

                    // System.out.println("--------------------------");

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
