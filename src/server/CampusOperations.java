package server;

import RoomReservationApp.AvailTimeSlotsHolder;
import RoomReservationApp.CampusPOA;
import RoomReservationApp.TimeSlot;
import repo.CentralRepositoryOps;
import schema.Campus;
import schema.Student;
import schema.UdpPacket;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;
import java.util.logging.Logger;

public class CampusOperations extends CampusPOA {
    private Logger logs;
    private Campus campus;
    private Hashtable<String, Student> students;
    private List<String> admins;
    private HashMap<String, HashMap<Integer, List<TimeSlot>>> roomRecords = new HashMap<>();
    private static final Object adminLock = new Object();
    private static final Object studentLock = new Object();
    private static final Object roomLock = new Object();

    CampusOperations(Logger logs) {
        this.logs = logs;
    }

    Campus setUpCampus(String name, Scanner scan) {
        String namingReference, code;
        int port;

        System.out.println("Enter the naming reference value (For NS lookup):");
        namingReference = scan.nextLine();

        System.out.println("Enter the campus code (e.g. DVL, WMT):");
        code = scan.nextLine();

        System.out.println("Enter the port number for UDP server:");
        port = scan.nextInt();

        campus = new Campus(port, code, namingReference, name);
        return campus;
    }

    void registerCampus() {
        // connect to auth server and register campus
        try {
            String message;
            DatagramSocket socket = new DatagramSocket();

            // make data object
            HashMap<String, Object> body = new HashMap<>();
            body.put(CentralRepositoryOps.ADD_CAMPUS.BODY_CODE, campus.getCode());
            body.put(CentralRepositoryOps.ADD_CAMPUS.BODY_NAME, campus.name);
            body.put(CentralRepositoryOps.ADD_CAMPUS.BODY_NAMING_REFERENCE, campus.getNamingReference());
            body.put(CentralRepositoryOps.ADD_CAMPUS.BODY_UDP_PORT, campus.getUdpPort());
            UdpPacket udpPacket = new UdpPacket(CentralRepositoryOps.ADD_CAMPUS.OP_CODE, body);

            // make packet and send
            byte[] outgoing = serialize(udpPacket);
            DatagramPacket outgoingPacket = new DatagramPacket(outgoing, outgoing.length, InetAddress.getByName("localhost"), 8009);
            socket.send(outgoingPacket);

            // incoming
            byte[] incoming = new byte[1000];
            DatagramPacket incomingPacket = new DatagramPacket(incoming, incoming.length);
            socket.receive(incomingPacket);

            // parse response and reflect back to user
            boolean response = (boolean) deserialize(incomingPacket.getData());
            message = response ? "The campus has been registered to the authentication server successfully." : "The campus already exists at the server.";

            if (response)
                logs.info(message);
            else
                logs.warning(message);
        } catch (SocketException se) {
            logs.warning("Error creating a client socket for connection to authentication server.\nMessage: " + se.getMessage());
        } catch (IOException ioe) {
            logs.warning("Error creating serialized object.\nMessage: " + ioe.getMessage());
        } catch (ClassNotFoundException e) {
            logs.warning("Error parsing the response from auth server.\nMessage: " + e.getMessage());
        }
    }

    @Override
    public boolean lookupAdmin(String id) {
        boolean success = (this.admins.indexOf(id) > -1);
        if (success)
            this.logs.info("Admin with id " + id + "has logged into the system.");
        else
            this.logs.warning("Unknown login detected. Id: " + id);
        return success;
    }

    @Override
    public String generateAdminId() {
        synchronized (adminLock) {
            String adminId;

            Random random = new Random();
            int num = random.nextInt(10000);
            adminId = campus.getCode().toUpperCase() + "A" + String.format("%04d", num);

            int index = this.admins.indexOf(adminId);
            if (index < 0)
                this.admins.add(adminId);

            return adminId;
        }
    }

    @Override
    public boolean createRoom(String date, int roomNo, TimeSlot[] timeSlots) {
        synchronized (roomLock) {
            // date already exists ? update the room : new date
            boolean isDateExists = roomRecords.containsKey(date);
            // get the map of rooms of given date
            HashMap<Integer, List<TimeSlot>> room = isDateExists ? roomRecords.get(date) : new HashMap<>();
            // room already exists ? update time slots : new room
            boolean isRoomExists = room.containsKey(roomNo);
            List<TimeSlot> slots = (isRoomExists) ? room.get(roomNo) : new ArrayList<>();
            for (TimeSlot inSlot : timeSlots) {
                // room doesn't exit ? add anyways : avoid duplicates
                if (!isRoomExists || (this.indexOfTimeSlot(inSlot.startTime, inSlot.endTime, slots) < 0))
                    slots.add(inSlot);
            }
            // update room
            room.put(roomNo, slots);
            // update date
            roomRecords.put(date, room);

            this.logs.info(isDateExists ? (isRoomExists ? "Time slots have been added to the room." : "New room has been created along with time slots") : "New date has been recorded along with the room and time slots");

            return true;
        }
    }

    // find the duplicate time slots and return their indexes. no duplicates ? return negative.
    private int indexOfTimeSlot(String start, String end, List<TimeSlot> list) {
        for (TimeSlot item : list) {
            if (item.startTime.equalsIgnoreCase(start) || item.endTime.equalsIgnoreCase(end))
                return list.indexOf(item);
        }
        return -1;
    }

    @Override
    public boolean deleteRoom(String date, int roomNo, TimeSlot[] timeSlots) {
        return false;
    }

    @Override
    public boolean lookupStudent(String id) {
        boolean success = this.students.containsKey(id);
        if (success)
            this.logs.info("Student with id " + id + " has logged into the system.");
        else
            this.logs.warning("Unknown login detected. Id: " + id);
        return success;
    }

    @Override
    public String generateStudentId() {
        synchronized (studentLock) {
            String studentId;

            Random random = new Random();
            int num = random.nextInt(10000);
            studentId = campus.getCode().toUpperCase() + "S" + String.format("%04d", num);

            Student student = new Student(studentId);
            this.students.put(studentId, student);

            return studentId;
        }
    }

    @Override
    public boolean getAvailableTimeSlots(String date, AvailTimeSlotsHolder availTimeSlots) {
        return false;
    }

    @Override
    public String bookRoom(String studentId, String date, int roomNumber, TimeSlot timeSlot) {
        return null;
    }

    @Override
    public boolean cancelBooking(String studentId, String bookingId) {
        return false;
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
