import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Worker3 {

    final static String SERVER_ADDRESS = "localhost";
    final static int PORT = 202;

    public static void main(String[] args) throws UnknownHostException, IOException {

        Socket socket = new Socket(SERVER_ADDRESS, PORT);

        System.out.println("Worker: 3 has started");

        WorkerHandler wb = new WorkerHandler(socket);
        wb.start();
    }

}