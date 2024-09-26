

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Date;
import java.net.*;
import java.util.*;


public class MulticastServer {

    public MulticastServer(int port) {
        System.out.println("Multicast Time Server Started");
        try {
         
			InetAddress mcastaddr = InetAddress.getByName("228.5.6.7");
            InetSocketAddress group = new InetSocketAddress(mcastaddr, port);
            // NetworkInterface networkInterface = NetworkInterface.getByName("ethernet_1");
			NetworkInterface netIf = NetworkInterface.getByName("lo");
			
			try{
				netIf = NetworkInterface.getByInetAddress(mcastaddr);
			}
			catch (SocketException ex){
				ex.printStackTrace();
			}
			
            MulticastSocket multicastSocket = new MulticastSocket(port);
 
            multicastSocket.joinGroup(group, netIf);
 
            byte[] data;
            DatagramPacket packet;        
            while (true) {
                Thread.sleep(1000);
                String message = (new Date()).toString();
                System.out.println("Sending: [" + message + "]");
                data = message.getBytes();
                packet = new DatagramPacket(data, message.length(), 
                        mcastaddr, port);
                
                multicastSocket.send(packet);
            }
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
        System.out.println("Multicast Time Server Terminated");
    }
    
    public static void main(String[] args) {
		new MulticastServer(Integer.parseInt(args[0]));
	}
}
