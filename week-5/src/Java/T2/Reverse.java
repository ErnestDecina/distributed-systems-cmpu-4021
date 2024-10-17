import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.*;


// Class with remote methods

public class Reverse
  extends UnicastRemoteObject
  implements ReverseInterface {

    private int a;

    public Reverse() throws RemoteException {
	System.out.println("Reverse created (new instance)");
	a = 1;
    }

    public String InvertStr(String m) throws RemoteException {
        // return the message with characters reversed
        System.out.println("Invert string("+m+") a=" + a);
        return new StringBuffer(m).reverse().toString();
    }
}
