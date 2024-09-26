
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Enumeration;

public class MulticastNetworkInterfaces {

    public static void main(String[] args) {
        try {
            Enumeration<NetworkInterface> networkInterfaces;
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface networkInterface : Collections.list(networkInterfaces)) {
                displayNetworkInterfaceInformation(networkInterface);
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }

    }
    static void displayNetworkInterfaceInformation(NetworkInterface networkInterface) {
        try {
            System.out.printf("Display name: %s\n", networkInterface.getDisplayName());
            System.out.printf("Name: %s\n", networkInterface.getName());
            System.out.printf("Supports Multicast: %s\n", networkInterface.supportsMulticast());
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                System.out.printf("InetAddress: %s\n", inetAddress);
            }
            System.out.println();
        } catch (SocketException ex) {
            ex.printStackTrace();
        }

    }
}
