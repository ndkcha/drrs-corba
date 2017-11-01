package admin;

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

public class AdminClient {
    public static void main(String args[]) {
        boolean isExitRequested = false, success;
        int choice;
        String message;
        Campus campus;
        String campusNamingReference, adminId;
        AdminOperations adminOps;
        Logger logs = Logger.getLogger("Admin Client");
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the (reference) name of the server: ");
        campusNamingReference = scanner.nextLine();

        // start orb client
        try {
            // create and initialize orb
            ORB orb = ORB.init(args, null);

            // get the root naming context
            Object objectReference = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objectReference);

            // get the remote interface
            campus = CampusHelper.narrow(ncRef.resolve_str(campusNamingReference));
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
        adminOps = new AdminOperations(campus, logs);

        // get adminId from the user
        adminId = adminOps.askAdminId(scanner);

        // lookup the adminId at server
        if (!campus.lookupAdmin(adminId)) {
            logs.warning("No entry found for the adminId, " + adminId);
            return;
        }

        // set up file handler for logging mechanism.
        try {
            FileHandler handler = new FileHandler(adminId + ".log", true);
            logs.addHandler(handler);
        } catch (IOException ioe) {
            logs.warning("Error initializing log file.\n Message: " + ioe.getMessage());
        }

        // be nice
        System.out.print("\n\n\tWelcome to the campus!\n");

        do {
            // ask what to do
            System.out.print("What do you want to do?\n\t1. Create a room\n\t2. Delete a room\nAny other number to exit");
            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                // create a room
                case 1:
                    success = adminOps.createRoom(scanner);
                    message = success ? "A room has successfully been created." : "An unexpected error thrown while creating a room.";
                    break;
                case 2:
                    success = adminOps.deleteRoom(scanner);
                    message = success ? "The room has successfully been deleted." : "An unexpected error thrown while deleting a room.";
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

        scanner.close();
    }
}
