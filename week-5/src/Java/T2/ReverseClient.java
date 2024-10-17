import java.rmi.Naming;
import java.rmi.RemoteException;


public class ReverseClient  {
    public static void main(String args[]) {
        try {
            if (args.length < 3) {
                System.err.println("usage: java ReverseClient host port string... \n");
                System.exit(1);
            }

            int port = Integer.parseInt(args[1]);
            String url = "//" + args[0] + ":" + port + "/Reverse";
            System.out.println("looking up at:" + url);

            ReverseInterface Reverse = (ReverseInterface)Naming.lookup(url);
			System.out.println("Reversed entered string is:");
				
            // args[2] onward are the strings to reverse
            for (int i=2; i < args.length; ++i)
                // remote method call; print the return

                System.out.println(Reverse.InvertStr(args[i]));
        } catch(Exception e) {
            System.out.println("ReverseClient exception: " + e);
        }
    }
}
