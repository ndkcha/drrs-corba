package server;

import schema.UdpPacket;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Logger;

public class UdpThread implements Runnable {
    private Thread thread;
    private DatagramSocket server;
    private DatagramPacket packet;
    private CampusOperations campusOps;
    private Logger logs;

    UdpThread(DatagramSocket server, DatagramPacket packet, CampusOperations campusOps, Logger logs) {
        this.server = server;
        this.packet = packet;
        this.campusOps = campusOps;
        this.logs = logs;
    }

    @Override
    public void run() {
        try {
            // parse the packet
            UdpPacket udpPacket = (UdpPacket) deserialize(this.packet.getData());

            // prepare for the response
            byte[] outgoing;
            DatagramPacket res;

            // perform actions
            switch (udpPacket.operationName) {
                // implement actions
                default:
                    outgoing = serialize("Error");
                    logs.warning("Operation not found!");
                    break;
            }

            // make response and send
            res = new DatagramPacket(outgoing, outgoing.length, this.packet.getAddress(), this.packet.getPort());
            this.server.send(res);
        } catch (IOException ioe) {
            logs.warning("Error reading the packet.\nMessage: " + ioe.getMessage());
        } catch (ClassNotFoundException e) {
            logs.warning("Error parsing the packet.\nMessage: " + e.getMessage());
        }
    }

    void start() {
        logs.info("One in coming connection. Forking a thread.");
        if (thread == null) {
            thread = new Thread(this, "Udp Process");
            thread.start();
        }
    }

    private static byte[] serialize(Object obj) throws IOException {
        try(ByteArrayOutputStream b = new ByteArrayOutputStream()){
            try(ObjectOutputStream o = new ObjectOutputStream(b)){
                o.writeObject(obj);
            }
            return b.toByteArray();
        }
    }

    private static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try(ByteArrayInputStream b = new ByteArrayInputStream(bytes)){
            try(ObjectInputStream o = new ObjectInputStream(b)){
                return o.readObject();
            }
        }
    }
}
