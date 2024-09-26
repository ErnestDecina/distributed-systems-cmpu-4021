

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.*;
import java.util.*;


public class MulticastClient {

    public MulticastClient(int port) {
        System.out.println("Multicast Time Client Started");
	
		
        try {
  	        InetAddress mcastaddr = InetAddress.getByName("228.5.6.7");
            InetSocketAddress group = new InetSocketAddress(mcastaddr, port);
           
  	        NetworkInterface netIf = NetworkInterface.getByName("lo");
	
			
			try{
			   netIf = NetworkInterface.getByInetAddress(mcastaddr);
			}
			catch (SocketException ex){
				ex.printStackTrace();
			}
			
			
            MulticastSocket multicastSocket = new MulticastSocket(port);
 
            multicastSocket.joinGroup(group, netIf);
			
            byte[] data = new byte[256];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            
            while (true) {
                multicastSocket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Message from: " + packet.getAddress() 
                        + " Message: [" + message + "]");        
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        System.out.println("UDP Multicast Time Client Terminated");
    }
    
  public static void main(String[] args) {
		new MulticastClient(Integer.parseInt(args[0]));
	}
}

