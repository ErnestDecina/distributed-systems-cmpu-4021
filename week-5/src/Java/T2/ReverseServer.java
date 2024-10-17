import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


public class ReverseServer {
    public static void main(String args[]) {
        if (args.length != 1) {
            System.err.println("usage: java ReverseServer port");
            System.exit(1);
        }
        try {
            // get port of the rmiregistry
            int port = Integer.parseInt(args[0]);

            // create the URL to contact the rmiregistry
            String url = "//localhost:" + port + "/Reverse";
            System.out.println("binding " + url);

            // register it with rmiregistry
            Naming.rebind(url, new Reverse());

       
            System.out.println("server " + url + " is running...");
        }
        catch (Exception e) {
            System.out.println("Reverse server error:" + e.getMessage());
        }
    }
}
