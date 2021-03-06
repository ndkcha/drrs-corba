package admin;

import RoomReservationApp.Campus;
import RoomReservationApp.TimeSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

class AdminOperations {
    private Campus campus;

    AdminOperations(Campus campus) {
        this.campus = campus;
    }

    boolean createRoom(Scanner scan) {
        List<TimeSlot> timeSlots = new ArrayList<>();
        int roomNo, noOfTimeSlots;
        String date, start, end, response;
        boolean isMoreSlots, success = false;

        // ask necessary things
        System.out.print("Enter the date (format: dd-MM-yyyy, e.g. 11-01-2018)\n : ");
        date = scan.nextLine();

        System.out.print("Enter the room number (integer values only)\n : ");
        roomNo = scan.nextInt();
        scan.nextLine();

        System.out.println("\nEntries for the time-slots:");

        do {
            System.out.print("\nEnter the start time (hh:mm): ");
            start = scan.nextLine();

            System.out.print("Enter the end time (hh:mm): ");
            end = scan.nextLine();

            TimeSlot slot = new TimeSlot(start, end, "", "");
            timeSlots.add(slot);

            System.out.print("Add another time-slot? (y/n):");
            response = scan.nextLine();

            isMoreSlots = response.equalsIgnoreCase("y");
        } while (isMoreSlots);

        // because of the marshalling in ORB architecture
        noOfTimeSlots = timeSlots.size();
        TimeSlot slots[] = new TimeSlot[noOfTimeSlots];

        for (int i = 0; i < slots.length; i++) {
            slots[i] = timeSlots.get(i);
        }

        // hit
        success = campus.createRoom(date, roomNo, slots);

        // respond
        return success;
    }

    boolean deleteRoom(Scanner scan) {
        List<TimeSlot> timeSlots = new ArrayList<>();
        int roomNo, noOfTimeSlots;
        String date, start, end, response;
        boolean isMoreSlots, success = false;

        // ask necessary things
        System.out.print("Enter the date (format: dd-MM-yyyy, e.g. 11-01-2018)\n : ");
        date = scan.nextLine();

        System.out.print("Enter the room number (integer values only)\n : ");
        roomNo = scan.nextInt();
        scan.nextLine();

        System.out.println("\nEntries for the time-slots:");

        do {
            System.out.print("\nEnter the start time (hh:mm): ");
            start = scan.nextLine();

            System.out.print("Enter the end time (hh:mm): ");
            end = scan.nextLine();

            TimeSlot slot = new TimeSlot(start, end, "", "");
            timeSlots.add(slot);

            System.out.print("Add another time-slot? (y/n): ");
            response = scan.nextLine();

            isMoreSlots = response.equalsIgnoreCase("y");
        } while (isMoreSlots);

        // because of the marshalling in ORB architecture
        noOfTimeSlots = timeSlots.size();
        TimeSlot slots[] = new TimeSlot[noOfTimeSlots];

        for (int i = 0; i < slots.length; i++) {
            slots[i] = timeSlots.get(i);
        }

        // perform the operation
        success = campus.deleteRoom(date, roomNo, slots);

        return success;
    }
}
