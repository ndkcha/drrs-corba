package schema;

import java.io.Serializable;
import java.util.HashMap;

// holds the data representation that can be passed amongst the servers.

public class UdpPacket implements Serializable {
    private static final long serialVersionUID = 1L;
    public int operationName;
    public HashMap<String, Object> body;

    public UdpPacket(int operationName, HashMap<String, Object> body) {
        this.operationName = operationName;
        this.body = body;
    }
}
