package server;

import RoomReservationApp.CampusHelper;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.Object;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import schema.Campus;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class CampusServer {

    public static void main(String args[]) {
        Logger logs;
        CampusOperations campusOps;
        Campus campus;
        String orbDetails[] = new String[4];

        if (args.length != 7) {
            System.out.println("Usage: java CampusServer <campus-name> <campus-code> <campus-udp-port> -ORBInitialPort <orb-port> -ORBInitialHost <orb-host>");
            return;
        }

        String campusName = args[0];
        for (int i = 3, j = 0; i < args.length; i++, j++) {
            orbDetails[j] = args[i];
        }

        // set up the logging mechanism
        logs = Logger.getLogger(campusName + " Server");
        try {
            FileHandler fileHandler = new FileHandler(campusName.replace(" ", "-").toLowerCase() + ".log", true);
            logs.addHandler(fileHandler);
        } catch(IOException ioe) {
            logs.warning("Failed to create handler for log file.\n Message: " + ioe.getMessage());
        }

        // initialize the implementation class (servant)
        campusOps = new CampusOperations(logs);

        // get the campus details from the user (for dynamic server initialization)
        campus = campusOps.setUpCampus(campusName, args[1], Integer.parseInt(args[2]));

        // register the connection details to central repository (so that other servers can communicate using udp)
        campusOps.registerCampus();

        // start the object request broker server
        try {
            // initialize the orb
            ORB orb = ORB.init(orbDetails, null);

            // get the reference to portable object adapter and activate the POA manager
            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootPOA.the_POAManager().activate();

            // get object reference from the implementation class (servant)
            Object ref = rootPOA.servant_to_reference(campusOps);
            RoomReservationApp.Campus href = CampusHelper.narrow(ref);

            // get the root naming context
            Object objectReference = orb.resolve_initial_references("NameService");
            // Name Service specification
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objectReference);

            // bing the object reference in naming
            NameComponent path[] = ncRef.to_name(campus.getCode().toLowerCase());
            ncRef.rebind(path, href);

            // mark the operation successful
            logs.info("The campus server is up and running! Reference: " + campus.getCode().toLowerCase());
        } catch (InvalidName invalidName) {
            logs.severe("Invalid reference to the Portable Object Adapter. The server can not initialize POA. \nMessage: " + invalidName.getMessage());
            return;
        } catch (AdapterInactive adapterInactive) {
            logs.severe("The Portable Object Adapter is inactive. \nMessage: " + adapterInactive.getMessage());
            return;
        } catch (ServantNotActive servantNotActive) {
            logs.severe("The implementation class (servant) is either not initialized or inactive. \nMessage: " + servantNotActive.getMessage());
            return;
        } catch (WrongPolicy wrongPolicy) {
            logs.severe("The implementation class (servant) is initialized with wrong policy. \nMessage: " + wrongPolicy.getMessage());
            return;
        } catch (org.omg.CosNaming.NamingContextPackage.InvalidName invalidName) {
            logs.severe("Invalid name for the NameService. \nMessage: " + invalidName.getMessage());
            return;
        } catch (CannotProceed cannotProceed) {
            logs.severe("CannotProceed Exception thrown. \nMessage: " + cannotProceed.getMessage());
            return;
        } catch (NotFound notFound) {
            logs.severe("Naming context not found. \nMessage: " + notFound.getMessage());
            return;
        }

        // start the udp server
        try {
            DatagramSocket udpSocket = new DatagramSocket(campus.getUdpPort());
            byte[] incoming = new byte[10000];
            logs.info("The UDP server for " + campus.name + " is up and running on port " + campus.getUdpPort());
            while (true) {
                DatagramPacket packet = new DatagramPacket(incoming, incoming.length);
                try {
                    udpSocket.receive(packet);
                    UdpThread thread = new UdpThread(udpSocket, packet, campusOps, logs);
                    thread.start();
                } catch (IOException ioe) {
                    logs.warning("Exception thrown while receiving packet.\nMessage: " + ioe.getMessage());
                }
                if (udpSocket.isClosed())
                    break;
            }
        } catch (SocketException e) {
            logs.warning("Exception thrown while server was running/trying to start.\nMessage: " + e.getMessage());
        }
    }
}
