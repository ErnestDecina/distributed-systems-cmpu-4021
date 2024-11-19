package interfaces;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IMarket extends Remote {
    public IJoinMarketDetails joinMarket(Integer  clientType) throws RemoteException;
    public Boolean leaveMarket(Integer nodeId, Integer clientType) throws RemoteException;
    public Boolean buyItems(Integer itemId, Integer numberOfItems, Integer buyerNodeId) throws RemoteException;
    public Boolean acceptBuyRequest(Integer reuestId, Integer sellerNodeId) throws RemoteException;
    public Boolean selectItemToSell(Integer itemId, Integer quantity, Integer sellerNodeId) throws RemoteException;
}
