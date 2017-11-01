package server;

import RoomReservationApp.AvailTimeSlot;
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
        students = new Hashtable<>();
        admins = new ArrayList<>();
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
        int total, noOfCampuses;
        AvailTimeSlot ts;
        List<AvailTimeSlot> availTimeSlotList = new ArrayList<>();

        ts = new AvailTimeSlot(campus.getCode(), this.totalAvailableTimeSlots(date));
        availTimeSlotList.add(ts);

        List<Campus> campuses = this.getListOfCampuses();

        if (campuses == null)
            logs.warning("No other campus(es) found!");
        else {
            for (Campus item : campuses) {
                if (item.getCode().equalsIgnoreCase(campus.getCode()))
                    continue;
                total = this.fetchTotalTimeSlots(date, item.getUdpPort());
                ts = new AvailTimeSlot(item.getCode(), total);
                availTimeSlotList.add(ts);
            }
        }

        noOfCampuses = availTimeSlotList.size();
        AvailTimeSlot[] slots = new AvailTimeSlot[noOfCampuses];
        for (int i = 0; i < noOfCampuses; i++) {
            slots[i] = availTimeSlotList.get(i);
        }

        availTimeSlots.value = slots;
        return true;
    }

    // gets total number of available time slots for a particular campus server.
    int totalAvailableTimeSlots(String date) {
        int total = 0;

        if (!this.roomRecords.containsKey(date))
            return 0;

        HashMap<Integer, List<TimeSlot>> rooms = this.roomRecords.get(date);

        for (Map.Entry<Integer, List<TimeSlot>> entry : rooms.entrySet()) {
            List<TimeSlot> slots = entry.getValue();

            for (TimeSlot item : slots) {
                // already booked ? it's not available
                total += ((item.bookingId == null) ? 1 : 0);
            }
        }

        return total;
    }

    private List<Campus> getListOfCampuses() {
        // connect to auth server
        try {
            DatagramSocket socket = new DatagramSocket();

            // make data object
            UdpPacket udpPacket = new UdpPacket(CentralRepositoryOps.LIST_CAMPUSES.OP_CODE, null);

            // make packet and send
            byte[] outgoing = serialize(udpPacket);
            DatagramPacket outgoingPacket = new DatagramPacket(outgoing, outgoing.length, InetAddress.getByName("localhost"), 8009);
            socket.send(outgoingPacket);

            // incoming
            byte[] incoming = new byte[1000];
            DatagramPacket incomingPacket = new DatagramPacket(incoming, incoming.length);
            socket.receive(incomingPacket);

            @SuppressWarnings("unchecked")
            List<Campus> response = (List<Campus>) deserialize(incomingPacket.getData());

            return response;
        } catch (SocketException se) {
            logs.warning("Error creating a client socket for connection to authentication server.\nMessage: " + se.getMessage());
        } catch (IOException ioe) {
            logs.warning("Error creating serialized object.\nMessage: " + ioe.getMessage());
        } catch (ClassNotFoundException e) {
            logs.warning("Error parsing the response from auth server.\nMessage: " + e.getMessage());
        }

        return null;
    }

    private int fetchTotalTimeSlots(String date, int udpPort) {
        int total = 0;
        // connect to campus server
        try {
            DatagramSocket socket = new DatagramSocket();

            // make data object
            HashMap<String, Object> body = new HashMap<>();
            body.put(TOTAL_TIMESLOT.BODY_DATE, date);
            UdpPacket udpPacket = new UdpPacket(TOTAL_TIMESLOT.OP_CODE, body);

            // make packet and send
            byte[] outgoing = serialize(udpPacket);
            DatagramPacket outgoingPacket = new DatagramPacket(outgoing, outgoing.length, InetAddress.getByName("localhost"), udpPort);
            socket.send(outgoingPacket);

            // incoming
            byte[] incoming = new byte[1000];
            DatagramPacket incomingPacket = new DatagramPacket(incoming, incoming.length);
            socket.receive(incomingPacket);

            total = (int) deserialize(incomingPacket.getData());
        } catch (SocketException se) {
            logs.warning("Error creating a client socket for connection to authentication server.\nMessage: " + se.getMessage());
        } catch (IOException ioe) {
            logs.warning("Error creating serialized object.\nMessage: " + ioe.getMessage());
        } catch (ClassNotFoundException e) {
            logs.warning("Error parsing the response from auth server.\nMessage: " + e.getMessage());
        }

        return total;
    }

    @Override
    public String bookRoom(String studentId, String code, String date, int roomNumber, TimeSlot timeSlot) {
        String bookingId;
        Student student;

        // no student. no booking.
        if (!this.students.containsKey(studentId))
            return "No student found!";

        student = this.students.get(studentId);

        // super active student. no booking.
        if (student.bookingIds.size() > 3)
            return "Maximum booking limit has been exceeded.";

        if (code.equalsIgnoreCase(campus.getCode())) {
            // make sure others don't book it.
            synchronized (roomLock) {
                // no date. no booking.
                if (!this.roomRecords.containsKey(date))
                    return "Incorrect date provided!";
                HashMap<Integer, List<TimeSlot>> room = this.roomRecords.get(date);
                // no room. no booking
                if (!room.containsKey(roomNumber))
                    return "Incorrect room number provided!";
                List<TimeSlot> timeSlots = room.get(roomNumber);
                // get the time slot to book
                TimeSlot slot = null;
                int index = -1;
                for (TimeSlot item : timeSlots) {
                    if (item.startTime.equalsIgnoreCase(timeSlot.startTime) && item.endTime.equalsIgnoreCase(timeSlot.endTime)) {
                        slot = item;
                        index = timeSlots.indexOf(item);
                        break;
                    }
                }
                // no time slot. no booking.
                if (slot == null)
                    return "Time slot does not exist!";
                // already booked ? no booking.
                if (slot.bookedBy != null)
                    return "Time slot has already been booked by other student.";

                // generate booking id.
                Random random = new Random();
                int num = random.nextInt(10000);
                bookingId = "BKG" + campus.getCode().toUpperCase() + String.format("%04d", num);

                // book it.
                slot.bookingId = bookingId;
                slot.bookedBy = studentId;

                // update room records
                timeSlots.set(index, slot);
                room.put(roomNumber, timeSlots);
                this.roomRecords.put(date, room);
            }

            synchronized (studentLock) {
                student.bookingIds.add(bookingId);
                this.students.put(studentId, student);
            }

            logs.info("New booking has been created under " + studentId + " with id, " + bookingId);
        } else {
            // get the port of the other campus
            int port = getUdpPort(code);
            // book on the other campus
            bookingId = bookRoomOnOtherCampus(studentId, roomNumber, date, timeSlot, port);
            // update the count
            if (bookingId != null) {
                synchronized (studentLock) {
                    student.bookingIds.add(bookingId);
                    this.students.put(studentId, student);
                }
                logs.info("New booking has been created under " + studentId + " with id, " + bookingId);
            }
        }

        return bookingId;
    }

    private int getUdpPort(String code) {
        int port = -1;

        // connect to auth server
        try {
            DatagramSocket socket = new DatagramSocket();

            // make data object
            HashMap<String, Object> body = new HashMap<>();
            body.put(CentralRepositoryOps.UDP_PORT.BODY_CODE, code);
            UdpPacket udpPacket = new UdpPacket(CentralRepositoryOps.UDP_PORT.OP_CODE, body);

            // make packet and send
            byte[] outgoing = serialize(udpPacket);
            DatagramPacket outgoingPacket = new DatagramPacket(outgoing, outgoing.length, InetAddress.getByName("localhost"), 8009);
            socket.send(outgoingPacket);

            // incoming
            byte[] incoming = new byte[1000];
            DatagramPacket incomingPacket = new DatagramPacket(incoming, incoming.length);
            socket.receive(incomingPacket);

            port = (int) deserialize(incomingPacket.getData());
        } catch (SocketException se) {
            logs.warning("Error creating a client socket for connection to authentication server.\nMessage: " + se.getMessage());
        } catch (IOException ioe) {
            logs.warning("Error creating serialized object.\nMessage: " + ioe.getMessage());
        } catch (ClassNotFoundException e) {
            logs.warning("Error parsing the response from auth server.\nMessage: " + e.getMessage());
        }

        return port;
    }

    private String bookRoomOnOtherCampus(String studentId, int roomNo, String date, TimeSlot slot, int udpPort) {
        String bookingId = null;

        // connect to auth server
        try {
            DatagramSocket socket = new DatagramSocket();

            // make data object
            HashMap<String, Object> body = new HashMap<>();
            body.put(BOOK_OTHER_SERVER.BODY_STUDENT_ID, studentId);
            body.put(BOOK_OTHER_SERVER.BODY_ROOM_NO, roomNo);
            body.put(BOOK_OTHER_SERVER.BODY_DATE, date);
            body.put(BOOK_OTHER_SERVER.BODY_TIME_SLOT, slot);
            UdpPacket udpPacket = new UdpPacket(BOOK_OTHER_SERVER.OP_CODE, body);

            // make packet and send
            byte[] outgoing = serialize(udpPacket);
            DatagramPacket outgoingPacket = new DatagramPacket(outgoing, outgoing.length, InetAddress.getByName("localhost"), udpPort);
            socket.send(outgoingPacket);

            // incoming
            byte[] incoming = new byte[1000];
            DatagramPacket incomingPacket = new DatagramPacket(incoming, incoming.length);
            socket.receive(incomingPacket);

            bookingId = (String) deserialize(incomingPacket.getData());

        } catch (SocketException se) {
            logs.warning("Error creating a client socket for connection to authentication server.\nMessage: " + se.getMessage());
        } catch (IOException ioe) {
            logs.warning("Error creating serialized object.\nMessage: " + ioe.getMessage());
        } catch (ClassNotFoundException e) {
            logs.warning("Error parsing the response from auth server.\nMessage: " + e.getMessage());
        }

        return bookingId;
    }

    String bookRoomFromOtherCampus(String studentId, int roomNumber, String date, TimeSlot timeSlot) {
        String bookingId;
        // make sure others don't book it.
        synchronized (roomLock) {
            // no date. no booking.
            if (!this.roomRecords.containsKey(date))
                return "Incorrect date provided!";
            HashMap<Integer, List<TimeSlot>> room = this.roomRecords.get(date);
            // no room. no booking
            if (!room.containsKey(roomNumber))
                return "Incorrect room number provided!";
            List<TimeSlot> timeSlots = room.get(roomNumber);
            // get the time slot to book
            TimeSlot slot = null;
            int index = -1;
            for (TimeSlot item : timeSlots) {
                if (item.startTime.equalsIgnoreCase(timeSlot.startTime) && item.endTime.equalsIgnoreCase(timeSlot.endTime)) {
                    slot = item;
                    index = timeSlots.indexOf(item);
                    break;
                }
            }
            // no time slot. no booking.
            if (slot == null)
                return "Time slot does not exist!";
            // already booked ? no booking.
            if (slot.bookedBy != null)
                return "Time slot has already been booked by other student.";

            // generate booking id.
            Random random = new Random();
            int num = random.nextInt(10000);
            bookingId = "BKG" + campus.getCode().toUpperCase() + String.format("%04d", num);

            // book it.
            slot.bookingId = bookingId;
            slot.bookedBy = studentId;

            // update room records
            timeSlots.set(index, slot);
            room.put(roomNumber, timeSlots);
            this.roomRecords.put(date, room);

            logs.info("New booking has been created under " + studentId + " with id, " + bookingId);
        }

        return bookingId;
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

    static abstract class TOTAL_TIMESLOT {
        static final int OP_CODE = 0;
        static final String BODY_DATE = "date";
    }

    static abstract class BOOK_OTHER_SERVER {
        static final int OP_CODE = 1;
        static final String BODY_STUDENT_ID = "stdId";
        static final String BODY_ROOM_NO = "rNo";
        static final String BODY_DATE = "date";
        static final String BODY_TIME_SLOT = "ts";
    }
}
