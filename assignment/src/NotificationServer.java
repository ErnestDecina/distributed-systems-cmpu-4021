import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import interfaces.IJoinMarketDetails;
import interfaces.INotification;
import modals.ClientSocket;
import modals.JoinMarketDetails;

public class NotificationServer extends UnicastRemoteObject implements INotification {
    // Domain Configuration
    static int port = 8081;
    static String url = "//localhost:" + port + "/MarketServer/NotificationServer";

    private static NotificationServer notificationServer;
    private static ArrayList<ClientSocket> buyerClientConnections = new ArrayList<ClientSocket>();
    private static ArrayList<ClientSocket> sellerClientConnections = new ArrayList<ClientSocket>();
    
    


    protected NotificationServer() throws RemoteException { super(); }
        
    public static void main(String args[]) {
        setupSellersMarket();
        // connectSockets();
    }

    public static void setupSellersMarket() {
        try {
            notificationServer = new NotificationServer();
            Naming.rebind(url, notificationServer);
            System.err.println("Server ready @ " + url);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IJoinMarketDetails addBuyerNotification(Integer buyerNodeId) throws RemoteException {
        try {
            ServerSocket temp = new ServerSocket(0);
            ClientSocket newBuyerClient = new ClientSocket();
            newBuyerClient.clientNodeId = buyerNodeId;
            newBuyerClient.portNumber = temp.getLocalPort();
            newBuyerClient.clientHost = RemoteServer.getClientHost();
            newBuyerClient.clientSocket = new DatagramSocket();

            temp.close();
            buyerClientConnections.add(newBuyerClient);

            System.out.println("Host: " + newBuyerClient.clientHost);

            JoinMarketDetails newBuyerMarketDetails = new JoinMarketDetails();
            newBuyerMarketDetails.nodeId = buyerNodeId;
            newBuyerMarketDetails.socketNumber = newBuyerClient.portNumber;

            return newBuyerMarketDetails;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ServerNotActiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Boolean removeBuyerNotification(Integer buyerNodeId) throws RemoteException {
        return buyerClientConnections.removeIf(buyerClient -> buyerClient.clientNodeId.equals(buyerNodeId));
    }

    @Override
    public IJoinMarketDetails addSellerNotification(Integer sellerNodeId) throws RemoteException {
        try {
            ServerSocket temp = new ServerSocket(0);
            ClientSocket newSellerClient = new ClientSocket();
            newSellerClient.clientNodeId = sellerNodeId;
            newSellerClient.portNumber = temp.getLocalPort();
            newSellerClient.clientHost = RemoteServer.getClientHost();
            newSellerClient.clientSocket = new DatagramSocket();

            temp.close();
            sellerClientConnections.add(newSellerClient);

            JoinMarketDetails newSellerMarketDetails = new JoinMarketDetails();
            newSellerMarketDetails.nodeId = sellerNodeId;
            newSellerMarketDetails.socketNumber = newSellerClient.portNumber;

            return newSellerMarketDetails;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ServerNotActiveException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        return null;
    }

    @Override
    public Boolean removeSellerNotifcation(Integer sellerNodeId) throws RemoteException {
        return sellerClientConnections.removeIf(sellerClient -> sellerClient.clientNodeId.equals(sellerNodeId));
    }

    @Override
    public void notifyBuyerSuccess(Integer itemId, Integer quantity, Integer buyerNodeId, Integer sellerNodeId) throws RemoteException {
        try {
            String notifySellerNewRequestMessage = "NOTIFY_BUYER_SUCCESS_BUY " + itemId + " " + quantity + " " + buyerNodeId + " " + sellerNodeId + " END_NOTIFY";
            byte m[] = notifySellerNewRequestMessage.getBytes();
        
            ClientSocket clientConnection = null;
            for (ClientSocket clientSocketQ : buyerClientConnections) {
                    if(clientSocketQ.clientNodeId.equals(buyerNodeId)) {
                        clientConnection = clientSocketQ;
                    }
            }
    
            InetAddress clientHost = Inet4Address.getByName(clientConnection.clientHost);
    
            DatagramPacket notifyRequest = new DatagramPacket(
                m,
                notifySellerNewRequestMessage.length(),
                clientHost,
                clientConnection.portNumber
            );

            clientConnection.clientSocket.send(notifyRequest);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
    }

    @Override
    public void notifyBuyerFailure(Integer requestId, Integer itemId, Integer quantity, Integer buyerNodeId, Integer sellerNodeId) {
        try {
            String notifySellerNewRequestMessage = "NOTIFY_BUYER_FAILURE_BUY " + itemId + " " + quantity + " " + buyerNodeId + " " + sellerNodeId + " END_NOTIFY";
            byte m[] = notifySellerNewRequestMessage.getBytes();
        
            ClientSocket clientConnection = null;
            for (ClientSocket clientSocketQ : buyerClientConnections) {
                    if(clientSocketQ.clientNodeId.equals(buyerNodeId)) {
                        clientConnection = clientSocketQ;
                    }
            }

            InetAddress clientHost = Inet4Address.getByName(clientConnection.clientHost);

            DatagramPacket notifyRequest = new DatagramPacket(
                m,
                notifySellerNewRequestMessage.length(),
                clientHost,
                clientConnection.portNumber
            );

            clientConnection.clientSocket.send(notifyRequest);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
    }

    @Override
    public void notifySellerSuccess(Integer requestId, Integer itemId, Integer quantity, Integer buyerNodeId, Integer sellerNodeId) throws RemoteException {
        try {
            String notifySellerNewRequestMessage = "NOTIFY_SELLER_SUCCESS_BUY " + requestId + " " + itemId + " " + quantity + " " + buyerNodeId + " " + sellerNodeId + " END_NOTIFY";
            byte m[] = notifySellerNewRequestMessage.getBytes();
        
            ClientSocket clientConnection = null;
            for (ClientSocket clientSocket : sellerClientConnections) {
                 if(clientSocket.clientNodeId.equals(sellerNodeId)) {
                     clientConnection = clientSocket;
                 }
            }

            InetAddress clientHost = Inet4Address.getByName(clientConnection.clientHost);
    
            DatagramPacket notifyRequest = new DatagramPacket(
                m,
                notifySellerNewRequestMessage.length(),
                clientHost,
                clientConnection.portNumber
            );
            clientConnection.clientSocket.send(notifyRequest);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
    }

    @Override
    public void notifySellerFailure(Integer requestId, Integer itemId, Integer quantity, Integer buyerNodeId, Integer sellerNodeId) throws RemoteException {
        try {
            String notifySellerNewRequestMessage = "NOTIFY_SELLER_FAILURE_BUY " + requestId + " " + itemId + " " + quantity + " " + buyerNodeId + " " + sellerNodeId + " END_NOTIFY";
            byte m[] = notifySellerNewRequestMessage.getBytes();
        
            ClientSocket clientConnection = null;
            for (ClientSocket clientSocket : sellerClientConnections) {
                 if(clientSocket.clientNodeId.equals(sellerNodeId)) {
                     clientConnection = clientSocket;
                 }
            }

            InetAddress clientHost = Inet4Address.getByName(clientConnection.clientHost);
    
            DatagramPacket notifyRequest = new DatagramPacket(
                m,
                notifySellerNewRequestMessage.length(),
                clientHost,
                clientConnection.portNumber
            );
            clientConnection.clientSocket.send(notifyRequest);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
    }

    @Override
    public void notifySellerNewRequest(Integer requestId, Integer itemId, Integer quantity, Integer buyerNodeId, Integer sellerNodeId) throws RemoteException {
       try {
        String notifySellerNewRequestMessage = "NOTIFY_SELLER_NEW_REQUEST " + requestId + " " + itemId + " " + quantity + " " + buyerNodeId + " " + sellerNodeId + " END_NOTIFY";
        byte m[] = notifySellerNewRequestMessage.getBytes();
 
        ClientSocket clientConnection = null;
        for (ClientSocket clientSocket : sellerClientConnections) {
             if(clientSocket.clientNodeId.equals(sellerNodeId)) {
                 clientConnection = clientSocket;
             }
        }

        InetAddress clientHost = Inet4Address.getByName(clientConnection.clientHost);

        DatagramPacket notifyRequest = new DatagramPacket(
            m,
            notifySellerNewRequestMessage.length(),
            clientHost,
            clientConnection.portNumber
        );
        clientConnection.clientSocket.send(notifyRequest);
    } catch (UnknownHostException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
    }
}

    @Override
    public void updateAvailableStock(Integer itemAvailablitity[], Integer currentSellers[]) throws RemoteException {
        String notifyAllUpdateAvailableStockMessage = "NOTIFY_MARKET_UPDATE " + itemAvailablitity[0] + " " + currentSellers[0] 
                                                                                + " " + itemAvailablitity[1] + " " + currentSellers[1] 
                                                                                + " " + itemAvailablitity [2] + " " + currentSellers[2]
                                                                                + " " + itemAvailablitity[3] + " " + currentSellers[3]
                                                                                + " END_NOTIFY";
        byte m[] = notifyAllUpdateAvailableStockMessage.getBytes();
        
        for (ClientSocket clientSocket : buyerClientConnections) {
            try {
                InetAddress clientHost = Inet4Address.getByName(clientSocket.clientHost);

                DatagramPacket notifyRequest = new DatagramPacket(
                    m,
                    notifyAllUpdateAvailableStockMessage.length(),
                    clientHost,
                    clientSocket.portNumber
                );
                clientSocket.clientSocket.send(notifyRequest);
            } catch (IOException e) {
                
                e.printStackTrace();
            }
        }
        for (ClientSocket clientSocket : sellerClientConnections) {
            try {
                InetAddress clientHost = Inet4Address.getByName(clientSocket.clientHost);

                DatagramPacket notifyRequest = new DatagramPacket(
                    m,
                    notifyAllUpdateAvailableStockMessage.length(),
                    clientHost,
                    clientSocket.portNumber
                );
                clientSocket.clientSocket.send(notifyRequest);
            } catch (IOException e) {
                
                e.printStackTrace();
            }
        }
    }

    @Override
    public void notifySellerNoStock(Integer sellerNodeId, Integer itemId) throws RemoteException {
        try {
            String notifySellerNewRequestMessage = "NOTIFY_SELLER_NO_STOCK " + itemId +  " END_NOTIFY";
            byte m[] = notifySellerNewRequestMessage.getBytes();
     
            ClientSocket clientConnection = null;
            for (ClientSocket clientSocket : sellerClientConnections) {
                 if(clientSocket.clientNodeId.equals(sellerNodeId)) {
                     clientConnection = clientSocket;
                 }
            }
    
            InetAddress clientHost = Inet4Address.getByName(clientConnection.clientHost);
    
            DatagramPacket notifyRequest = new DatagramPacket(
                m,
                notifySellerNewRequestMessage.length(),
                clientHost,
                clientConnection.portNumber
            );
            clientConnection.clientSocket.send(notifyRequest);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
    }

    @Override
    public void notifySellerRanOutOfTime(Integer sellerNodeId) throws RemoteException {
        try {
            String notifySellerRanOutOfTimeMessage = "NOTIFY_SELLER_NO_TIME END_NOTIFY";
            byte m[] = notifySellerRanOutOfTimeMessage.getBytes();
     
            ClientSocket clientConnection = null;
            for (ClientSocket clientSocket : sellerClientConnections) {
                 if(clientSocket.clientNodeId.equals(sellerNodeId)) {
                     clientConnection = clientSocket;
                 }
            }
    
            InetAddress clientHost = Inet4Address.getByName(clientConnection.clientHost);
    
            DatagramPacket notifyRequest = new DatagramPacket(
                m,
                notifySellerRanOutOfTimeMessage.length(),
                clientHost,
                clientConnection.portNumber
            );
            clientConnection.clientSocket.send(notifyRequest);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
    }

    


}
