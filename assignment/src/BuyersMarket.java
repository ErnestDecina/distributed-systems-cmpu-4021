import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import interfaces.IBuyerMarket;
import interfaces.IJoinMarketDetails;
import interfaces.INotification;
import interfaces.ISellerManager;
import modals.JoinMarketDetails;

public class BuyersMarket extends UnicastRemoteObject implements IBuyerMarket {
    // Buyers Market
    private static ArrayList<JoinMarketDetails> activeBuyerMarketList;
    private static Integer buyerMarketIdCounter = 0;


    // Domain Configuration
    static int port = 8081;
    static String url = "//localhost:" + port + "/MarketServer/BuyersMarket";

    private static BuyersMarket buyersMarketServer;

    //
    static String urlSellerManagerServer = "//localhost:" + port + "/MarketServer/SellerManager";
    static ISellerManager sellerManagerServer;

    //
    static String urlNotificationServer = "//localhost:" + port + "/MarketServer/NotificationServer";
    static INotification notificationServer;

    public BuyersMarket() throws RemoteException { super(); }

    public static void main(String args[]) {
        setupBuyersMarket();
        setUpRegistery();
    }

    public static void setupBuyersMarket() {
        try {
            activeBuyerMarketList = new ArrayList<JoinMarketDetails>();
            buyersMarketServer = new BuyersMarket();
            Naming.rebind(url, buyersMarketServer);
            System.err.println("Server ready @ " + url);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private static void setUpRegistery() {
        try {
            sellerManagerServer = (ISellerManager) Naming.lookup(urlSellerManagerServer);
            notificationServer = (INotification) Naming.lookup(urlNotificationServer);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public IJoinMarketDetails joinBuyersMarket() throws RemoteException {
        try {
            buyerMarketIdCounter = buyerMarketIdCounter + 1;
            IJoinMarketDetails buyerDetailsI = notificationServer.addBuyerNotification(buyerMarketIdCounter);

            JoinMarketDetails buyerDetails = new JoinMarketDetails();
            buyerDetails.nodeId = buyerDetailsI.getNodeId();
            buyerDetails.socketNumber = buyerDetailsI.getSocketNumber();
            activeBuyerMarketList.add(buyerDetails);

            return buyerDetails;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Boolean leaveBuyersMarket(Integer buyersNodeId) throws RemoteException {
        notificationServer.removeBuyerNotification(buyersNodeId);
        return activeBuyerMarketList.removeIf(buyer -> buyer.nodeId.equals(buyersNodeId));
    }

    @Override
    public Boolean buyItems(Integer itemId, Integer quantity, Integer buyerNodeId) throws RemoteException {
        return sellerManagerServer.addRequestQueue(itemId, quantity, buyerNodeId);
    }

}
