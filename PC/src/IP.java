import java.net.*;
/*IP.java
IP address accessing
    - getAddress()
*/

public class IP {
    public static String getAddress() {
        try {
            for (NetworkInterface ni : java.util.Collections.list(
                    NetworkInterface.getNetworkInterfaces())) {

                for (InetAddress addr : java.util.Collections.list(ni.getInetAddresses())) {
                    if (!addr.isLoopbackAddress()
                            && addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }
}
