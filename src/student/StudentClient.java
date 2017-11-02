package student;

import RoomReservationApp.Campus;
import RoomReservationApp.CampusHelper;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.Object;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class StudentClient {
    public static void main(String args[]) {
        boolean isExitRequested = false, success;
        int choice;
        String message, code, response, studentId = null;
        Campus campus;
        StudentOperations studentOps;

        Logger logs = Logger.getLogger("Student Client");
        Scanner scan = new Scanner(System.in);

        System.out.print("Do you have a studentId? (y/n): ");
        response = scan.nextLine();

        if (response.equalsIgnoreCase("y")) {
            System.out.print("\nEnter your studentId: ");
            studentId = scan.nextLine();
            code = studentId.substring(0, 3).toUpperCase();
        } else {
            System.out.print("\nEnter the campus code you're in: ");
            code = scan.nextLine();
        }

        // start orb client
        try {
            // create and initialize orb
            ORB orb = ORB.init(args, null);

            // get the root naming context
            Object objectReference = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objectReference);

            // get the remote interface
            campus = CampusHelper.narrow(ncRef.resolve_str(code.toLowerCase()));
        } catch (InvalidName invalidName) {
            logs.severe("Invalid reference to Name Service. \nMessage: " + invalidName.getMessage());
            return;
        } catch (CannotProceed cannotProceed) {
            logs.severe("CannotProceed exception thrown. \nMessage: " + cannotProceed.getMessage());
            return;
        } catch (org.omg.CosNaming.NamingContextPackage.InvalidName invalidName) {
            logs.severe("Invalid reference to the server. Please check the name. \n Message:" + invalidName.getMessage());
            return;
        } catch (NotFound notFound) {
            logs.severe("Server not found.\nMessage: " + notFound.getMessage());
            return;
        }

        // no remote. no work.
        if (campus == null) {
            logs.severe("Error initializing ORB object. Try again later!");
            return;
        }

        // initialize implementation class for the client
        studentOps = new StudentOperations(campus, logs);

        if (studentId == null) {
            studentId = campus.generateStudentId();
            System.out.println("\nYour new studentId is " + studentId + ".\n");
        }

        // look up studentId at server
        if (!campus.lookupStudent(studentId)) {
            logs.warning("No entry found for the studentId, " + studentId);
            return;
        }

        // set up file handler for logging mechanism.
        try {
            FileHandler handler = new FileHandler(studentId + ".log", true);
            logs.addHandler(handler);
        } catch (IOException ioe) {
            logs.warning("Error initializing log file.\n Message: " + ioe.getMessage());
        }

        // be nice
        System.out.println("\tWelcome to the campus!\n");

        do {
            // ask what to do
            System.out.print("What do you want to do?\n\t1. Book a room\n\t2. Cancel booking\n\t3. Change booking\nAny other number to exit\n : ");
            choice = scan.nextInt();
            scan.nextLine();

            switch (choice) {
                // create a room
                case 1:
                    success = studentOps.bookRoom(scan, studentId);
                    message = success ? "A room has successfully been booked." : "An unexpected error thrown while booking a room.";
                    break;
                // cancel booking
                case 2:
                    success = studentOps.cancelBooking(scan, studentId);
                    message = success ? "The booking has been cancelled successfully" : "An unexpected error thrown while cancelling the booking.";
                    break;
                // change booking
                case 3:
                    success = studentOps.changeBooking(scan);
                    message = success ? "The booking has been changed successfully" : "An unexpected error thrown while changing the booking.";
                    break;
                // exit
                default:
                    message = "Exit requested! Have a nice day!";
                    success = true;
                    isExitRequested = true;
                    break;
            }

            if (success)
                logs.info(message);
            else
                logs.warning(message);
        } while (!isExitRequested);

        scan.close();
    }
}
