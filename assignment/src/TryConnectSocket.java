import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;

import modals.ClientSocket;

public class TryConnectSocket implements Runnable {
        final long duration = 300 * 1_000_000; // duration in nanoseconds (300 milliseconds)
        private ClientSocket clientMarketDetails;
        

        public TryConnectSocket(ClientSocket joinMarketDetails) {
            clientMarketDetails = joinMarketDetails;
        }

        @Override
        public void run() {
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < duration) {
                try {
                    clientMarketDetails.clientSocket = new DatagramSocket(clientMarketDetails.portNumber);
                    System.out.println("Socket connected to nodeId: " + clientMarketDetails.clientNodeId);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
}
