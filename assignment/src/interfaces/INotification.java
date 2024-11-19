package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface INotification extends Remote {
    public IJoinMarketDetails addBuyerNotification(Integer buyerNodeId) throws RemoteException;
    public Boolean removeBuyerNotification(Integer buyerNodeId) throws RemoteException;
    public IJoinMarketDetails addSellerNotification(Integer sellerNodeId) throws RemoteException;
    public Boolean removeSellerNotifcation(Integer sellerNodeId) throws RemoteException;

    public void notifyBuyerSuccess(Integer itemId, Integer quantity, Integer buyerNodeId, Integer sellerNodeId) throws RemoteException;
    public void notifyBuyerFailure(Integer requestId, Integer itemId, Integer quantity, Integer buyerNodeId, Integer sellerNodeId) throws RemoteException;
    public void notifySellerSuccess(Integer requestId, Integer itemId, Integer quantity, Integer buyerNodeId, Integer sellerNodeId) throws RemoteException;
    public void notifySellerFailure(Integer requestId, Integer itemId, Integer quantity, Integer buyerNodeId, Integer sellerNodeId) throws RemoteException;
    public void notifySellerNoStock(Integer sellerNodeId, Integer itemId) throws RemoteException;
    public void notifySellerRanOutOfTime(Integer sellerNodeId) throws RemoteException;

    public void notifySellerNewRequest(Integer requestId, Integer itemId, Integer quantity, Integer buyerNodeId, Integer sellerNodeId) throws RemoteException;

    public void updateAvailableStock(Integer itemAvailablitity[], Integer currentSellers[]) throws RemoteException;
}
