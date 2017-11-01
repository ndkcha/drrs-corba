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

    String askStudentId(Scanner scan) {
        String response, studentId;

        System.out.println("Do you have a studentId? (y/n):");
        response = scan.nextLine();

        if (response.equalsIgnoreCase("y")) {
            System.out.println("Enter your studentId:");
            studentId = scan.nextLine();
        } else {
            studentId = campus.generateStudentId();
            System.out.println("\nYour new studentId is " + studentId + ".\nUse it for the logging into the system next time.");
        }

        return studentId;
    }

    boolean bookRoom(Scanner scan, String studentId) {
        boolean success = false, isCodeFound = false;
        String code, start, end, date, bookingId;
        int roomNo;
        TimeSlot slot;
        AvailTimeSlotsHolder availTimeSlotsHolder = new AvailTimeSlotsHolder();

        System.out.println("Enter the date for which you want to book the room (format: dd-MM-yyyy, e.g. 11-01-2018):");
        date = scan.nextLine();

        campus.getAvailableTimeSlots(date, availTimeSlotsHolder);

        System.out.println("Available time-slot(s):");

        for (AvailTimeSlot availTimeSlot : availTimeSlotsHolder.value) {
            System.out.println(availTimeSlot.campusCode + " " + availTimeSlot.noOfSlots);
        }

        System.out.println("Enter the campus code:");
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

        System.out.println("Enter the room number:");
        roomNo = scan.nextInt();
        scan.nextLine();

        System.out.println("Enter the 'start time' for time slot (format: hh:mm):");
        start = scan.nextLine();

        System.out.println("Enter the 'end time' for time slot (format: hh:mm):");
        end = scan.nextLine();

        slot = new TimeSlot();
        slot.startTime = start;
        slot.endTime = end;

        bookingId = campus.bookRoom(studentId, code, date, roomNo, slot);

        if (bookingId != null) {
            if (bookingId.startsWith("BKG")) {
                success = true;
                this.logs.info("The booking has been made successfully.");
                System.out.println("\nYour booking id: " + bookingId + "\n");
            } else {
                success = false;
                this.logs.warning(bookingId);
            }
        }

        return success;
    }

    public boolean cancelBooking(Scanner scan, String studentId) {
        boolean success;
        String bookingId;

        System.out.println("Enter the bookingId:");
        bookingId = scan.nextLine();

        success = campus.cancelBooking(studentId, bookingId);

        return success;
    }
}
