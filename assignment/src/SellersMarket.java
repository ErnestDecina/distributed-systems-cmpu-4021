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
import interfaces.ISellerMarket;
import modals.JoinMarketDetails;

public class SellersMarket extends UnicastRemoteObject implements ISellerMarket {
   // Buyers Market
    private static ArrayList<JoinMarketDetails> activeSellerMarketList;
    private static Integer sellerMarketIdCounter = 0;


    // Domain Configuration
    static int port = 8081;
    static String url = "//localhost:" + port + "/MarketServer/SellersMarket";

    private static SellersMarket sellersMarketServer;

    //
    static String urlSellerManagerServer = "//localhost:" + port + "/MarketServer/SellerManager";
    static ISellerManager sellerManagerServer;

    //
    static String urlNotificationServer = "//localhost:" + port + "/MarketServer/NotificationServer";
    static INotification notificationServer;

    protected SellersMarket() throws RemoteException { super(); }

    public static void main(String args[]) {
        setupSellersMarket();
        setUpRegistery();
    }

    public static void setupSellersMarket() {
        try {
            activeSellerMarketList = new ArrayList<JoinMarketDetails>();
            sellersMarketServer = new SellersMarket();
            Naming.rebind(url, sellersMarketServer);
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
    public IJoinMarketDetails joinSellersMarket() throws RemoteException {
        try {
            sellerMarketIdCounter = sellerMarketIdCounter + 1;
            IJoinMarketDetails sellerDetailsI = notificationServer.addSellerNotification(sellerMarketIdCounter);

            JoinMarketDetails sellerDetails = new JoinMarketDetails();
            sellerDetails.nodeId = sellerDetailsI.getNodeId();
            sellerDetails.socketNumber = sellerDetailsI.getSocketNumber();
            activeSellerMarketList.add(sellerDetails);

            return sellerDetails;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Boolean leaveSellersMarket(Integer sellersNodeId) throws RemoteException {
        notificationServer.removeSellerNotifcation(sellersNodeId);
        return activeSellerMarketList.removeIf(buyer -> buyer.nodeId.equals(sellersNodeId));
    }

    @Override
    public Boolean acceptRequest(Integer reuestId, Integer sellerNodeId) throws RemoteException {
        return sellerManagerServer.acceptRequest(reuestId, sellerNodeId);
    }

    @Override
    public Boolean updateItemToSell(Integer itemId, Integer quantity, Integer sellerNodeId) throws RemoteException {
        if(sellerNodeId < 0) {
            return false;
        }

        return sellerManagerServer.updateType(itemId, quantity, sellerNodeId);
    }

}
