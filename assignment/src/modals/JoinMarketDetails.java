package modals;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import interfaces.IJoinMarketDetails;

public class JoinMarketDetails extends UnicastRemoteObject implements IJoinMarketDetails {
    public Integer      nodeId;
    public Integer      socketNumber;

    public JoinMarketDetails() throws RemoteException { super(); }

    @Override
    public String toString() {
        return "NodeId: " + nodeId + " SockerNumber: " + socketNumber;
    }

    @Override
    public Integer getNodeId() throws RemoteException {
        return nodeId;
    }

    @Override
    public Integer getSocketNumber() throws RemoteException {
        return socketNumber;
    }
}
