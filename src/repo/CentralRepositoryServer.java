package repo;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class CentralRepositoryServer {
    private static Logger logs = Logger.getLogger("Central Repository");

    public static void main(String args[]) {
        // setup file handler for logs
        try {
            FileHandler handler = new FileHandler("central-repo.log", true);
            logs.addHandler(handler);
        } catch (IOException ioException) {
            logs.warning("Couldn't initialize the file for the logger. \nMessage: " + ioException.getMessage());
        }

        // initialize the operations and data
        CentralRepositoryOps ops = new CentralRepositoryOps(logs);

        // setup udp server
        try {
            DatagramSocket udpSocket = new DatagramSocket(8009);
            logs.info("The UDP server for authentication is up and running on port 8009");

            // prepare for incoming data
            byte[] incoming = new byte[10000];
            while (true) {
                // capture the packet
                DatagramPacket packet = new DatagramPacket(incoming, incoming.length);

                // fork a thread and let the thread do the job
                try {
                    udpSocket.receive(packet);
                    UdpThread thread= new UdpThread(udpSocket, packet, ops, logs);
                    thread.start();
                } catch (IOException ioe) {
                    logs.warning("Error receiving packet.\nMessage: " + ioe.getMessage());
                }
            }
        } catch (SocketException e) {
            logs.warning("Exception thrown while server was running/trying to start.\nMessage: " + e.getMessage());
        }
    }
}
