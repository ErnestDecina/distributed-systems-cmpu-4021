package interfaces;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IBuyer extends Remote {
    public Integer joinMarketAsBuyer() throws RemoteException;
    public void    leaveMarketAsBuyer() throws RemoteException;
    public void    buyItems(Integer itemId, Integer numberOfItems) throws RemoteException;
    public void    listAvailableItemsOnSale() throws RemoteException;
}
