package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IBuyerMarket extends Remote {
    public IJoinMarketDetails joinBuyersMarket() throws RemoteException;
    public Boolean            leaveBuyersMarket(Integer buyersNodeId) throws RemoteException;
    public Boolean            buyItems(Integer itemId, Integer quantity, Integer buyerNodeId) throws RemoteException;
}
