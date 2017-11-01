package repo;

import schema.Campus;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.logging.Logger;

public class CentralRepositoryOps {
    private Hashtable<String, Campus> campusList;
    private Logger logs;
    private static final Object campusLock = new Object();

    CentralRepositoryOps(Logger logs) {
        this.campusList = new Hashtable<String, Campus>();
        this.logs = logs;
    }

    boolean addCampus(HashMap<String, Object> campus) {
        synchronized (campusLock) {
            String code = (String) campus.get(ADD_CAMPUS.BODY_CODE), namingReference, name;
            int udpPort;
            // add the campus. but check for the duplicate value
            if (!this.campusList.containsKey(code)) {
                namingReference = (String) campus.get(ADD_CAMPUS.BODY_NAMING_REFERENCE);
                name = (String) campus.get(ADD_CAMPUS.BODY_NAME);
                udpPort = (Integer) campus.get(ADD_CAMPUS.BODY_UDP_PORT);

                // make object and insert to the hash table
                Campus item = new Campus(udpPort, code, namingReference, name);
                campusList.put(code, item);

                // mark the operation as successful
                this.logs.info("The connection details of the " + name + " server has been recorded successfully.");
                return true;
            }

            // mark the operation as unsuccessful
            this.logs.warning("Couldn't add the campus with code, " + code + ". It already exists");
            return false;
        }
    }

    public static abstract class ADD_CAMPUS {
        public static final int OP_CODE = 0;
        public static final String BODY_UDP_PORT = "up";
        public static final String BODY_NAME = "n";
        public static final String BODY_NAMING_REFERENCE = "nf";
        public static final String BODY_CODE = "c";
    }
}
