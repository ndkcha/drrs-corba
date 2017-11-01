package schema;

import java.io.Serializable;

// holds the data representation that can be passed amongst the servers.

public class Campus implements Serializable {
    private int udpPort;
    private String namingReference, code;
    public String name;

    public Campus(int udpPort, String code, String namingReference, String name) {
        this.udpPort = udpPort;
        this.code = code;
        this.namingReference = namingReference;
        this.name = name;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public String getCode() {
        return code;
    }

    public String getNamingReference() {
        return namingReference;
    }
}
