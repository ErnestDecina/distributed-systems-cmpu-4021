package interfaces;

import java.net.Socket;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ISellerManager extends Remote {
    public Boolean addRequestQueue(Integer itemId, Integer quantity, Integer buyerNodeId) throws RemoteException;
    public Boolean acceptRequest(Integer requestId, Integer sellerNodeId) throws RemoteException;
    public Boolean updateType(Integer itemId, Integer quantity, Integer sellerNodeId) throws RemoteException;
}
