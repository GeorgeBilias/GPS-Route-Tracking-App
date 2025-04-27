import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;

public class Worker2 {

    final static String SERVER_ADDRESS = "localhost";
    final static int PORT = 201;

    public static void main(String[] args) throws UnknownHostException, IOException {

        Socket socket = new Socket(SERVER_ADDRESS, PORT);

        System.out.println("Worker: 2 has started");

        WorkerHandler wb = new WorkerHandler(socket);
        wb.start();
    }

}