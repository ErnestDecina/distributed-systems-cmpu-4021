import java.net.MalformedURLException;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import interfaces.INotification;
import interfaces.ISellerManager;

public class SellerManager extends UnicastRemoteObject implements ISellerManager {
    // Domain Configuration
    static int port = 8081;
    static String url = "//localhost:" + port + "/MarketServer/SellerManager";

    private static SellerManager sellersManagerServer;

    private static Integer currentItemSeller[] = {-1, -1, -1, -1};
    private static Integer itemSellerCountDown[] = {-1, -1, -1, -1};
    private static ArrayList<RequestItem> itemRequestQueue;
    private static Integer requestIdCounter = 0;

    //
    private static ArrayList<SellerStockStatus> sellerStockStatusArrayList = new ArrayList<SellerStockStatus>();
    

    //
    static String urlNotificationServer = "//localhost:" + port + "/MarketServer/NotificationServer";
    static INotification notificationServer;


    protected SellerManager() throws RemoteException { super(); }

    public static void main(String args[]) {
        setupSellersMarket();
        setUpRegistery();
        setupItemSellerCountdown();
        
    }

    @SuppressWarnings("unchecked")
    public static void setupSellersMarket() {
        try {
            itemRequestQueue = new ArrayList<RequestItem>();

            sellersManagerServer = new SellerManager();
            Naming.rebind(url, sellersManagerServer);
            System.err.println("Server ready @ " + url);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private static void setUpRegistery() {
        try {
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

    public static void setupItemSellerCountdown() {
        Thread infiniteThread = new Thread(() -> {
            while (true) {
                try {                    
                    for(int i = 0; i < 4; i ++) {
                        if(itemSellerCountDown[i] <= 0) {
                            if(currentItemSeller[i] == -1) continue;

                            // Notify seller has lost seller priveleges
                            System.out.println("Seller: " + currentItemSeller[i] + " Lost selling rights to itemID: " + i);
                            notificationServer.notifySellerRanOutOfTime(currentItemSeller[i]);
                            currentItemSeller[i] = -1;
                            continue;
                        }
                        itemSellerCountDown[i] = itemSellerCountDown[i] - 1;
                    }
                    Thread.sleep(1000);
                    updateStock();
                } catch (InterruptedException e) {
                    System.out.println("Thread was interrupted");
                    break;
                } catch (RemoteException e) {
                   e.printStackTrace();
                }
            }
        });

        // Start the thread
        infiniteThread.start();
    }

    
    public static void checkAvailablity() {
        // After accept request check if other request can be fufilled eg: check item quantity
        for(RequestItem requestItem: itemRequestQueue) {
            // Check if seller is still valid
            if(!requestItem.sellerNodeId.equals(currentItemSeller[requestItem.itemId])) {
                // Notify Buyer and Seller
                try {
                    notificationServer.notifyBuyerFailure(requestItem.reuqestId, requestItem.itemId, requestItem.quantity, requestItem.buyerNodeId, requestItem.sellerNodeId);
                    notificationServer.notifySellerFailure(requestItem.reuqestId, requestItem.itemId, requestItem.quantity, requestItem.buyerNodeId, requestItem.sellerNodeId);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                
                // Remove from requestQueue
                itemRequestQueue.removeIf(request -> request.quantity.equals(requestItem.reuqestId));
            }
        }
        updateStock();
    }

    @Override
    public Boolean addRequestQueue(Integer itemId, Integer quantity, Integer buyerNodeId) throws RemoteException {
        requestIdCounter = requestIdCounter + 1;
        RequestItem newRequest = new RequestItem();
        newRequest.reuqestId = requestIdCounter;
        newRequest.buyerNodeId = buyerNodeId;
        newRequest.itemId = itemId;
        newRequest.quantity = quantity;

        if(currentItemSeller[itemId] == -1) {
            notificationServer.notifyBuyerFailure(requestIdCounter, itemId, quantity, buyerNodeId, null);
            return false;
        }

        // Check if seller has stock
        SellerStockStatus currentSellerStockStatus = null;
        for (SellerStockStatus sellerStockStatus : sellerStockStatusArrayList)
            if(sellerStockStatus.sellerNodeId.equals(currentItemSeller[itemId]))
                currentSellerStockStatus = sellerStockStatus;
        
        newRequest.sellerNodeId = currentSellerStockStatus.sellerNodeId;

        if(currentSellerStockStatus.itemStock[itemId] <= 0) {
            notificationServer.notifyBuyerFailure(requestIdCounter, itemId, quantity, buyerNodeId, currentSellerStockStatus.sellerNodeId);
            return false;
        }

        itemRequestQueue.add(newRequest);
        notificationServer.notifySellerNewRequest(newRequest.reuqestId, itemId, quantity, buyerNodeId, currentItemSeller[itemId]);
        return true;
    }

    @Override
    public Boolean acceptRequest(Integer requestId, Integer sellerNodeId) throws RemoteException {

        // Find requestId details
        RequestItem request = null;
        for (RequestItem requestItem : itemRequestQueue) {
            if(requestItem.reuqestId.equals(requestId)) {
                request = requestItem;
                break;
            }
        }

        // Check if request cann be done
        // Check if valid seller
        if(currentItemSeller[request.itemId] == -1) {
            System.out.println("Invalid Seller");
            notificationServer.notifyBuyerFailure(requestId, request.itemId, request.quantity, request.buyerNodeId, sellerNodeId);
            notificationServer.notifySellerFailure(requestId, request.itemId, request.quantity, request.buyerNodeId, sellerNodeId);
            itemRequestQueue.removeIf(requestQ -> requestQ.reuqestId.equals(requestId));
            return false;
        }

        // Check if seller has stock
        SellerStockStatus currentSellerStockStatus = null;
        for (SellerStockStatus sellerStockStatus : sellerStockStatusArrayList) {
            if(sellerStockStatus.sellerNodeId.equals(sellerNodeId)) {
                currentSellerStockStatus = sellerStockStatus;
                break;
            }
                
        }

        if(currentSellerStockStatus.itemStock[request.itemId] <= 0) {
            notificationServer.notifyBuyerFailure(requestId, request.itemId, request.quantity, request.buyerNodeId, sellerNodeId);
            notificationServer.notifySellerFailure(requestId, request.itemId, request.quantity, request.buyerNodeId, sellerNodeId);
            notificationServer.notifySellerNoStock(sellerNodeId, request.itemId);
            itemRequestQueue.removeIf(requestQ -> requestQ.reuqestId.equals(requestId));
            return false;
        }

        if(!sellerNodeId.equals(currentItemSeller[request.itemId])) {
            notificationServer.notifyBuyerFailure(requestId, request.itemId, request.quantity, request.buyerNodeId, sellerNodeId);
            notificationServer.notifySellerFailure(requestId, request.itemId, request.quantity, request.buyerNodeId, sellerNodeId);
            itemRequestQueue.removeIf(requestQ -> requestQ.reuqestId.equals(requestId));
            return false;
        }

        // Check if the stock is enough
        if (currentSellerStockStatus.itemStock[request.itemId] < request.quantity) {
            System.out.println("Low Stock");
            notificationServer.notifyBuyerFailure(requestId, request.itemId, request.quantity, request.buyerNodeId, sellerNodeId);
            notificationServer.notifySellerFailure(requestId, request.itemId, request.quantity, request.buyerNodeId, sellerNodeId);
            itemRequestQueue.removeIf(requestQ -> requestQ.reuqestId.equals(requestId));
            return false;
        }
        
        // Tell buyer & seller request has been accepted
        notificationServer.notifyBuyerSuccess(request.itemId, request.quantity, request.buyerNodeId, sellerNodeId);
        notificationServer.notifySellerSuccess(request.reuqestId, request.itemId, request.quantity, request.buyerNodeId, request.sellerNodeId);
    
        // Decrement Sellers 
        currentSellerStockStatus.itemStock[request.itemId] = currentSellerStockStatus.itemStock[request.itemId] - request.quantity;

        // Check if stock reached 0
        if(currentSellerStockStatus.itemStock[request.itemId] <= 0) {
            currentItemSeller[request.itemId] = -1;
            itemSellerCountDown[request.itemId] = -1;            
            notificationServer.notifySellerNoStock(sellerNodeId, request.itemId);
        }

        itemRequestQueue.removeIf(requestQ -> requestQ.reuqestId.equals(requestId));

        // Recheck availability of other items
        checkAvailablity();
        // Remove Request
        return true;
    }

    @Override
    public Boolean updateType(Integer itemId, Integer quantity, Integer sellerNodeId) throws RemoteException {
        System.out.println("sellerNodeId: " + sellerNodeId + " trying to sell itemID: " + itemId + " Quantity: " + quantity);
        
        // Check if item is not being held by another seller
        if(currentItemSeller[itemId] != -1) {
            return false;
        }

        // look if sellerStock Exists in arraylist
        SellerStockStatus sellerStockStatusA = null;
        for (SellerStockStatus sellerStockStatus : sellerStockStatusArrayList) {
            if(sellerStockStatus.sellerNodeId.equals(sellerNodeId))
                sellerStockStatusA = sellerStockStatus;
        }

        // If seller stock exists
        if (sellerStockStatusA != null) {
            sellerStockStatusA.itemStock[itemId] = quantity;
        }

        // Doesnt exist
        else {
            SellerStockStatus newSellerStockStatus = new SellerStockStatus();
            newSellerStockStatus.sellerNodeId = sellerNodeId;
            newSellerStockStatus.itemStock = new Integer[4];
            newSellerStockStatus.itemStock[itemId] = quantity;
            sellerStockStatusArrayList.add(newSellerStockStatus);
        }

        // DeMark Old selling item
        for(int i = 0; i < 4; i++ ) {
            if(currentItemSeller[i].equals(sellerNodeId)) {
                currentItemSeller[i] = -1;
            }
        }

        // Mark New Item selling
        currentItemSeller[itemId] = sellerNodeId;
        itemSellerCountDown[itemId] = 60;

        // Recheck availability of other items
        checkAvailablity();

        return true;
    }

    public static void updateStock() {        
        try {
            Integer stock[] = {-1, -1, -1, -1};

            for(int i = 0; i < 4; i++) {
                if(currentItemSeller[i] == -1) continue;
                SellerStockStatus status = null;
                for (SellerStockStatus s : sellerStockStatusArrayList) {
                    if(s.sellerNodeId.equals(currentItemSeller[i])) {
                        status = s;
                        break;
                    }
                        
                }
                ;
                stock[i] = status.itemStock[i];
            }

            notificationServer.updateAvailableStock(stock, currentItemSeller);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static class SellerStockStatus {
        public Integer sellerNodeId;
        public Integer itemStock[]; 
    } 

    public static class RequestItem {
        public Integer reuqestId;
        public Integer itemId;
        public Integer quantity;
        public Integer buyerNodeId;
        public Integer sellerNodeId;
    }
}
