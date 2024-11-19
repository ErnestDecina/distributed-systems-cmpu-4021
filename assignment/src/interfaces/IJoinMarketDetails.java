package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IJoinMarketDetails extends Remote {
    public Integer getNodeId() throws RemoteException;
    public Integer getSocketNumber() throws RemoteException;
}
