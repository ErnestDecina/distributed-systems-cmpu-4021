package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ISellerMarket extends Remote {
    public IJoinMarketDetails joinSellersMarket() throws RemoteException;
    public Boolean            leaveSellersMarket(Integer sellersNodeId) throws RemoteException;
    public Boolean            acceptRequest(Integer reuestId, Integer sellerNodeId) throws RemoteException;
    public Boolean            updateItemToSell(Integer itemId, Integer quantity, Integer sellerNodeId) throws RemoteException;
}
