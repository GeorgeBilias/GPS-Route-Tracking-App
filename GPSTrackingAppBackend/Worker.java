
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.time.Duration;
import java.time.Instant;
import com.example.myapplication.resultInfo;

public class Worker extends Thread {
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private List<resultInfo> results;
    private Socket socket;
    private int worker_id;
    private int active = 0;
    private int sending = 0;

    public ObjectOutputStream getOut() {
        return this.out;
    }

    public void setOut(ObjectOutputStream out) {
        this.out = out;
    }

    public ObjectInputStream getIn() {
        return this.in;
    }

    public void setIn(ObjectInputStream in) {
        this.in = in;
    }

    public List<resultInfo> getResults() {
        return this.results;
    }

    public void setResults(List<resultInfo> results) {
        this.results = results;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void Activate() {
        this.active = 1;
    }

    public void Deactivate() {
        this.active = 0;
    }

    public int get_active() {
        return this.active;
    }

    public int get_send() {
        return this.sending;
    }

    public void set_send(int n) {
        this.sending = n;
    }

    public Worker(ServerSocket server, int id, int p) throws UnknownHostException, IOException {

        Socket socket = new Socket("localhost", p);
        // this.socket = server.accept();
        // System.out.println("Socket:" + socket);
        this.socket = socket;
        this.worker_id = id;

    }

    public Worker(ObjectOutputStream out, ObjectInputStream in, List<resultInfo> results) {
        this.out = out;
        this.in = in;
        this.results = results;
    }

    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371e3; // metres
        double f1 = Math.toRadians(lat1);
        double f2 = Math.toRadians(lat2);
        double df = Math.toRadians(lat2 - lat1);
        double dl = Math.toRadians(lon2 - lon1);

        double a = Math.sin(df / 2) * Math.sin(df / 2)
                + Math.cos(f1) * Math.cos(f2)
                        * Math.sin(dl / 2) * Math.sin(dl / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double d = R * c; // in metres
        return d / 1000;
    }

    public Pair<Integer, resultInfo> Map(int route, Chunk ch) {

        double sum_dist = 0;
        double avg_speed = 0;
        long sum_time = 0;
        double sum_elevation = 0;

        if (ch.getInfo2() != null && ch.getInfo3() == null) {
            sum_dist = distance(ch.getInfo1().getLat(), ch.getInfo1().getLon(), ch.getInfo2().getLat(),
                    ch.getInfo2().getLon());

            Instant instant1 = Instant.parse(ch.getInfo1().getTime());
            Instant instant2 = Instant.parse(ch.getInfo2().getTime());
            Duration time1 = Duration.between(instant1, instant2);
            long secondsPassed = time1.getSeconds();

            double speed1 = sum_dist / secondsPassed;

            avg_speed = speed1;

            sum_time = secondsPassed;

            if (ch.getInfo1().getEle() >= 0) {
                sum_elevation += ch.getInfo1().getEle();
            }

            if (ch.getInfo2().getEle() >= 0) {
                sum_elevation += ch.getInfo2().getEle();
            }

        } else if (ch.getInfo2() != null && ch.getInfo3() != null) {
            double dist1 = distance(ch.getInfo1().getLat(), ch.getInfo1().getLon(), ch.getInfo2().getLat(),
                    ch.getInfo2().getLon());
            double dist2 = distance(ch.getInfo2().getLat(), ch.getInfo2().getLon(), ch.getInfo3().getLat(),
                    ch.getInfo3().getLon());
            sum_dist = dist1 + dist2;

            Instant instant1 = Instant.parse(ch.getInfo1().getTime());
            Instant instant2 = Instant.parse(ch.getInfo2().getTime());
            Duration time1 = Duration.between(instant1, instant2);
            long secondsPassed1 = time1.getSeconds();

            instant1 = Instant.parse(ch.getInfo2().getTime());
            instant2 = Instant.parse(ch.getInfo3().getTime());

            Duration time2 = Duration.between(instant1, instant2);
            long secondsPassed2 = time2.getSeconds();

            double speed1 = dist1 / secondsPassed1;
            double speed2 = dist2 / secondsPassed2;

            avg_speed = (speed1 + speed2) / 2;

            sum_time = secondsPassed1 + secondsPassed2;

            if (ch.getInfo1().getEle() >= 0) {
                sum_elevation += ch.getInfo1().getEle();
            }

            if (ch.getInfo2().getEle() >= 0) {
                sum_elevation += ch.getInfo2().getEle();
            }

            if (ch.getInfo3().getEle() >= 0) {
                sum_elevation += ch.getInfo3().getEle();
            }

        } else if (ch.getInfo2() == null && ch.getInfo3() == null) {
            sum_dist = 0;
            avg_speed = 0;
            sum_time = 0;
            sum_elevation = 0;
        }

        // System.out.println("Calculated distance is " + sum_dist);
        // System.out.println("Speed is: "+avg_speed);
        // System.out.println("Time spent: "+sum_time);
        // System.out.println("Total elevation: "+sum_elevation);

        resultInfo i = new resultInfo();
        if (ch.get_last_one() == true) {
            i.set_last_one(true);
        } else {
            i.set_last_one(false);
        }
        i.setAverageSpeed(avg_speed);
        i.setTotalDistance(sum_dist);
        i.setTotalTime(sum_time);
        i.setTotalelevation(sum_elevation);
        i.setUser(ch.get_User());

        return new Pair<Integer, resultInfo>(ch.getRouteNumber(), i);

    }

    @Override
    public void run() {
        try {

            while (true) {

                System.out.println("Worker: " + this.worker_id + " has started");

                // System.out.println("Socket is " + this.socket);

                Deactivate();
                ObjectInputStream inn = new ObjectInputStream(this.socket.getInputStream());
                Activate();
                // System.out.println("created stream");

                Chunk temp = (Chunk) inn.readObject();

                Pair<Integer, resultInfo> info = Map(temp.getRouteNumber(), temp);

                // this.sending++;

                System.out.println("calculated and ready to get  sent");

                ObjectOutputStream outt = new ObjectOutputStream(this.socket.getOutputStream());

                outt.writeObject(info);
                outt.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e2) {
            e2.printStackTrace();
        }
    }
}

// class resultInfo implements Serializable {
// private double TotalDistance;
// private double AverageSpeed;
// private double Totalelevation;
// private double TotalTime;

// public double getTotalDistance() {
// return this.TotalDistance;
// }

// public void setTotalDistance(double TotalDistance) {
// this.TotalDistance = TotalDistance;
// }

// public double getAverageSpeed() {
// return this.AverageSpeed;
// }

// public void setAverageSpeed(double AverageSpeed) {
// this.AverageSpeed = AverageSpeed;
// }

// public double getTotalelevation() {
// return this.Totalelevation;
// }

// public void setTotalelevation(double Totalelevation) {
// this.Totalelevation = Totalelevation;
// }

// public double getTotalTime() {
// return this.TotalTime;
// }

// public void setTotalTime(double TotalTime) {
// this.TotalTime = TotalTime;
// }

// }