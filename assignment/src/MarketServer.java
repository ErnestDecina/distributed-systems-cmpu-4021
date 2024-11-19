import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import interfaces.IBuyerMarket;
import interfaces.IJoinMarketDetails;
import interfaces.IMarket;
import interfaces.ISellerMarket;
import modals.JoinMarketDetails;

public class MarketServer extends UnicastRemoteObject implements IMarket {
    static MarketServer server;
    static IMarket      marketInterface;
    
    
    // Domain Configuration
    static int port = 8080;
    static String urlMarketServer = "//localhost:" + port + "/MarketServer";
    
    // Buyer Market Configuration
    static IBuyerMarket buyerMarketServer;
    static String urlBuyerMarketServer = "//localhost:8081/MarketServer/BuyersMarket";

    // Seller Market Configuration
    static ISellerMarket sellerMarketServer;
    static String urlSellerMarketServer = "//localhost:8081/MarketServer/SellersMarket";

    protected MarketServer() throws RemoteException { super(); }

    public static void main(String args[]) {
        setUpRegistery();
        setUpMarketServer();
    }

    private static void setUpMarketServer() {
        try {
            server = new MarketServer();
            Naming.rebind(urlMarketServer, server);

            System.err.println("Server ready @ " + urlMarketServer);
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

    private static void setUpRegistery() {
        try {
            buyerMarketServer = (IBuyerMarket) Naming.lookup(urlBuyerMarketServer);
            sellerMarketServer = (ISellerMarket) Naming.lookup(urlSellerMarketServer);
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
    public IJoinMarketDetails joinMarket(Integer clientType) throws RemoteException {
        // Buyer Client
        if (clientType == 0) {
            IJoinMarketDetails buyerClientDetailsI = buyerMarketServer.joinBuyersMarket();
            JoinMarketDetails buyerClientDetails = new JoinMarketDetails();
            buyerClientDetails.nodeId = buyerClientDetailsI.getNodeId();
            buyerClientDetails.socketNumber = buyerClientDetailsI.getSocketNumber();

            return buyerClientDetails;
        }

        // Seller Client
        else {
            IJoinMarketDetails sellerClientDetailsI = sellerMarketServer.joinSellersMarket();
            JoinMarketDetails sellerClientDetails = new JoinMarketDetails();
            sellerClientDetails.nodeId = sellerClientDetailsI.getNodeId();
            sellerClientDetails.socketNumber = sellerClientDetailsI.getSocketNumber();

            return sellerClientDetails;
        }
    }

    @Override
    public Boolean leaveMarket(Integer nodeId, Integer clientType) throws RemoteException {
        if(clientType == 0) {
            return buyerMarketServer.leaveBuyersMarket(nodeId);
        }

        else {
            return false;
        }
        
    }

    @Override
    public Boolean buyItems(Integer itemId, Integer numberOfItems, Integer buyerNodeId) throws RemoteException {
        System.out.println("Buyer Node: " + buyerNodeId + " trying to buy ItemID: " + itemId + " Quantity: " + numberOfItems);
        return buyerMarketServer.buyItems(itemId, numberOfItems, buyerNodeId);
    }

    @Override
    public Boolean selectItemToSell(Integer itemId, Integer quantity, Integer sellerNodeId) throws RemoteException {
        System.out.println("Seller Node: " + sellerNodeId + " trying to sell ItemID: " + itemId + " Quantity: " + quantity);
        return sellerMarketServer.updateItemToSell(itemId, quantity, sellerNodeId);
    }

    @Override
    public Boolean acceptBuyRequest(Integer reuestId, Integer sellerNodeId) throws RemoteException {
        System.out.println("Seller Node: " + sellerNodeId + " accepts BuyRequestID: " + reuestId);
        sellerMarketServer.acceptRequest(reuestId, sellerNodeId);
        return true;
    }

}
