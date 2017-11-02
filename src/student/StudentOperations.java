package student;

import RoomReservationApp.AvailTimeSlot;
import RoomReservationApp.AvailTimeSlotsHolder;
import RoomReservationApp.Campus;
import RoomReservationApp.TimeSlot;

import java.util.Scanner;
import java.util.logging.Logger;

class StudentOperations {
    private Campus campus;
    private Logger logs;

    StudentOperations(Campus campus, Logger logs) {
        this.campus = campus;
        this.logs = logs;
    }

    boolean bookRoom(Scanner scan, String studentId) {
        boolean success = false, isCodeFound = false;
        String code, start, end, date, bookingId;
        int roomNo;
        TimeSlot slot;
        AvailTimeSlotsHolder availTimeSlotsHolder = new AvailTimeSlotsHolder();

        System.out.print("Enter the date for which you want to book the room (format: dd-MM-yyyy, e.g. 11-01-2018)\n :");
        date = scan.nextLine();

        campus.getAvailableTimeSlots(date, availTimeSlotsHolder);

        System.out.println("\nAvailable time-slot(s):");

        for (AvailTimeSlot availTimeSlot : availTimeSlotsHolder.value) {
            System.out.println(availTimeSlot.campusCode + " " + availTimeSlot.noOfSlots);
        }

        System.out.print("\nEnter the campus code: ");
        code = scan.nextLine();

        // user gave wrong campus code. no booking.
        for (AvailTimeSlot availTimeSlot : availTimeSlotsHolder.value) {
            if (availTimeSlot.campusCode.equalsIgnoreCase(code)) {
                isCodeFound = true;
                break;
            }
        }
        if (!isCodeFound) {
            this.logs.warning("The campus does not exist!");
            return false;
        }

        System.out.print("Enter the room number: ");
        roomNo = scan.nextInt();
        scan.nextLine();

        System.out.print("Enter the 'start time' for time slot (format: hh:mm): ");
        start = scan.nextLine();

        System.out.print("Enter the 'end time' for time slot (format: hh:mm): ");
        end = scan.nextLine();

        slot = new TimeSlot(start, end, "", "");

        bookingId = campus.bookRoom(studentId, code, date, roomNo, slot);

        if (bookingId != null) {
            if (bookingId.startsWith("BKG")) {
                success = true;
                System.out.println("\nYour booking id: " + bookingId + "\n");
            } else {
                success = false;
                this.logs.warning(bookingId);
            }
        }

        return success;
    }

    boolean cancelBooking(Scanner scan, String studentId) {
        boolean success;
        String bookingId;

        System.out.print("Enter the bookingId: ");
        bookingId = scan.nextLine();

        success = campus.cancelBooking(studentId, bookingId);

        return success;
    }

    boolean changeBooking(Scanner scan) {
        boolean success = false, isCodeFound = false;
        String code, start, end, date, bookingId, newBookingId;
        int roomNo;
        TimeSlot slot;
        AvailTimeSlotsHolder availTimeSlotsHolder = new AvailTimeSlotsHolder();

        System.out.print("Enter the bookingId: ");
        bookingId = scan.nextLine();

        System.out.println("New booking details -");
        System.out.print("Enter the date for which you want to book the room (format: dd-MM-yyyy, e.g. 11-01-2018)\n :");
        date = scan.nextLine();

        campus.getAvailableTimeSlots(date, availTimeSlotsHolder);

        System.out.println("\nAvailable time-slot(s):");

        for (AvailTimeSlot availTimeSlot : availTimeSlotsHolder.value) {
            System.out.println(availTimeSlot.campusCode + " " + availTimeSlot.noOfSlots);
        }

        System.out.print("\nEnter the campus code: ");
        code = scan.nextLine();

        // user gave wrong campus code. no booking.
        for (AvailTimeSlot availTimeSlot : availTimeSlotsHolder.value) {
            if (availTimeSlot.campusCode.equalsIgnoreCase(code)) {
                isCodeFound = true;
                break;
            }
        }
        if (!isCodeFound) {
            this.logs.warning("The campus does not exist!");
            return false;
        }

        System.out.print("Enter the room number: ");
        roomNo = scan.nextInt();
        scan.nextLine();

        System.out.print("Enter the 'start time' for time slot (format: hh:mm): ");
        start = scan.nextLine();

        System.out.print("Enter the 'end time' for time slot (format: hh:mm): ");
        end = scan.nextLine();

        slot = new TimeSlot(start, end, "", "");

        newBookingId = campus.changeBooking(bookingId, code, date, roomNo, slot);

        if (newBookingId != null) {
            if (newBookingId.startsWith("BKG")) {
                success = true;
                System.out.println("\nYour new booking id: " + newBookingId + ".\nDiscard the old booking id.");
            } else {
                success = false;
                this.logs.warning(newBookingId);
            }
        }

        return success;
    }
}
