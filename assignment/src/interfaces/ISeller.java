package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ISeller extends Remote {
    public Integer joinMarketAsSeller()  throws RemoteException;
    public void    leaveMarketAsSeller() throws RemoteException;
    public void    acceptRequest(Integer reuestIds[])  throws RemoteException;
    public void    selectItemToSell(Integer itemId) throws RemoteException;
}
