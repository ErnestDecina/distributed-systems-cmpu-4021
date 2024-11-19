import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import interfaces.IJoinMarketDetails;
import interfaces.IMarket;
import modals.ClientSocket;
import modals.JoinMarketDetails;

public class SellerClient {
    static Registry registry;
    static IMarket sellerClient;
    static DatagramSocket socket;

    // Market Config
    static String domain = "localhost";
    static int port = 8080;
    static String url = "//" + domain + ":" + port + "/MarketServer";

    // SellerClientDetails
    static Integer sellerClientID = -1;
    static Integer socketNumber = -1;
    static volatile Integer currentSelling = -1;
    static volatile Integer onlineStock[] = {0, 0, 0, 0};
    static volatile Integer localStock[] = {10, 10, 10, 10};
    static volatile Integer currentSellersStock[] = {-1, -1, -1, -1};

    //
    static ArrayList<Request> requestArrayList = new ArrayList<Request>();

    // UI
    private static final AtomicBoolean running = new AtomicBoolean(false);

    public static void main(String args[]) {
        setUpRegistery();
        runMainMenuUI();
    }

    private static void setUpRegistery() {
        try {
            sellerClient = (IMarket)Naming.lookup(url);

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void setupSocketReceiveMessages() {
        try {
            socket = new DatagramSocket(socketNumber);

            Thread infiniteThreadSocketReceiveMessages = new Thread(() -> {
                while (true) {
                    try {
                        byte buffer[] = new byte[1000];
                        DatagramPacket receiveMessagePacket = new DatagramPacket(buffer, buffer.length);

                        socket.receive(receiveMessagePacket);

                        String receiveMessage = new String(receiveMessagePacket.getData());
                        String splitMessage[] = receiveMessage.split(" ");

                        Integer requestId;
                        Integer itemId;
                        Integer quantity;
                        Integer buyerNodeId;

                        switch(splitMessage[0]) {
                            case "NOTIFY_SELLER_NEW_REQUEST":
                                requestId = Integer.parseInt(splitMessage[1]);
                                itemId = Integer.parseInt(splitMessage[2]);
                                quantity = Integer.parseInt(splitMessage[3]);
                                buyerNodeId = Integer.parseInt(splitMessage[4]);

                                addToRequestQueue(requestId, itemId, quantity, buyerNodeId);
                                break;
                            
                            case "NOTIFY_SELLER_SUCCESS_BUY":
                                requestId = Integer.parseInt(splitMessage[1]);
                                itemId = Integer.parseInt(splitMessage[2]);
                                quantity = Integer.parseInt(splitMessage[3]);
                                buyerNodeId = Integer.parseInt(splitMessage[4]);

                                localStock[itemId] = localStock[itemId] - quantity; 

                                removeFromRequestQueue(requestId, itemId, quantity, buyerNodeId);
                                break;
                            
                            case "NOTIFY_SELLER_FAILURE_BUY":
                                requestId = Integer.parseInt(splitMessage[1]);
                                itemId = Integer.parseInt(splitMessage[2]);
                                quantity = Integer.parseInt(splitMessage[3]);
                                buyerNodeId = Integer.parseInt(splitMessage[4]);

                                removeFromRequestQueue(requestId, itemId, quantity, buyerNodeId);
                                break;

                            case "NOTIFY_SELLER_NO_STOCK":
                                itemId = Integer.parseInt(splitMessage[1]);
                                localStock[itemId] = 0;

                                autoChangeSell();
                                break;
                            
                            case "NOTIFY_SELLER_NO_TIME":
                                autoChangeSell();
                                break;
                            
                            case "NOTIFY_MARKET_UPDATE":
                                Integer flowerQuantity = Integer.parseInt(splitMessage[1]);
                                Integer flowerSellerId = Integer.parseInt(splitMessage[2]);
                                Integer sugarQuantity = Integer.parseInt(splitMessage[3]);
                                Integer sugarSellerId = Integer.parseInt(splitMessage[4]);
                                Integer potatoQuantity = Integer.parseInt(splitMessage[5]);
                                Integer potatoSellerId = Integer.parseInt(splitMessage[6]);
                                Integer oilQuantity = Integer.parseInt(splitMessage[7]);
                                Integer oilSellerId = Integer.parseInt(splitMessage[8]);

                                updateMarket(flowerQuantity, flowerSellerId, sugarQuantity, sugarSellerId, potatoQuantity, potatoSellerId, oilQuantity, oilSellerId);
                                break;

                            default:
                                break;
                        }
                    } catch (IOException e) {
                        System.out.println("Thread was interrupted");
                        break;
                    }
                }
            });
    
            // Start the thread
            infiniteThreadSocketReceiveMessages.start();
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void autoChangeSell() {
            int i = 0;
            int previousFailureSellingRights = currentSelling;

            while(true) {
                // Try and get selling rights
                Integer tempCurrentSelling = -1;

                
                for(i = 0; i < 4; i++) {
                    if(localStock[i] > 0 && i != previousFailureSellingRights) {
                        tempCurrentSelling = i;
                        break;
                    }
                }

                if(i == 4) {
                    return;
                }


                try { 
                    previousFailureSellingRights = tempCurrentSelling;
                    Boolean successSellingRights = sellerClient.selectItemToSell(tempCurrentSelling, localStock[tempCurrentSelling], sellerClientID); 
                    if(successSellingRights) {
                        currentSelling = tempCurrentSelling;
                        onlineStock[currentSelling] = localStock[tempCurrentSelling];
                        currentSellersStock[currentSelling] = sellerClientID;
                        return;
                    }
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

    }

    private static void updateMarket(
        Integer flowerQuantity,
        Integer flowerSellerId,
        Integer sugarQuantity,
        Integer sugarSellerId,
        Integer potatoQuantity,
        Integer potatoSellerId,
        Integer oilQuantity,
        Integer oilSellerId
    ) {
        	onlineStock[0] = flowerQuantity;
            onlineStock[1] = sugarQuantity;
            onlineStock[2] = potatoQuantity;
            onlineStock[3] = oilQuantity;

            currentSellersStock[0] = flowerSellerId;
            currentSellersStock[1] = sugarSellerId;
            currentSellersStock[2] = potatoSellerId;
            currentSellersStock[3] = oilQuantity;
    }

    private static void removeFromRequestQueue(Integer requestId, Integer itemId, Integer quantity, Integer buyerNodeId) {
        requestArrayList.removeIf(request -> request.requestId.equals(requestId));
    }

    private static void addToRequestQueue(Integer requestId, Integer itemId, Integer quantity, Integer buyerNodeId) {
        Request newRequest = new Request();
        newRequest.requestId = requestId;
        newRequest.itemId = itemId;
        newRequest.quantity = quantity;
        newRequest.buyerNodeId = buyerNodeId;
        requestArrayList.add(newRequest);
    }

    private static void joinSellerMarket() {
        try {
            IJoinMarketDetails sellerClientDetails = sellerClient.joinMarket(1); 
            sellerClientID = sellerClientDetails.getNodeId();
            socketNumber =  sellerClientDetails.getSocketNumber();   

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private static void leaveSellerMarket() {
        try {
            onlineStock[currentSelling] = 0;
            sellerClient.leaveMarket(sellerClientID, 1);
            sellerClientID = -1;
            currentSelling = -1;
            
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static class Request {
        Integer requestId;
        Integer itemId;
        Integer quantity;
        Integer buyerNodeId;
    }
    
    private static void printMainMenu() {
        String status = "Disconnected";
        if(!sellerClientID.equals(-1)) {
            status = "Connected";
        }

        System.out.println("\n====== Market UI Menu ======");
        System.out.println("Market Status: " + status);
        System.out.println("1. Join Market");
        System.out.println("2. Sell Items");
        System.out.println("3. Accept Requests (" + requestArrayList.size() + ")");
        System.out.println("4. View Market");
        System.out.println("5. Leave Market");
        System.out.println("0. Close Program");
        System.out.println("================================");
    }

    private static void printBuyItemsMenu() {
        String selling = "Nothing";
         if (currentSelling.equals(0)) {
            selling = "Flower";
        } else if (currentSelling.equals(1)) {
            selling = "Sugar";
        } else if (currentSelling.equals(2)) {
            selling = "Potato";
        } else if (currentSelling.equals(3)) {
            selling = "Oil";
        } else {
            selling = "Nothing";
        }

        System.out.print("\033[2J\033[H");
        System.out.println("\n====== Sellers Market UI Menu ======");
        System.out.println("Currently Selling: " + selling);
        System.out.println("1. Flower    (" + currentSellersStock[0] + ") : " + onlineStock[0]);
        System.out.println("2. Sugar     (" + currentSellersStock[1] + "): " + onlineStock[1]);
        System.out.println("3. Potato    (" + currentSellersStock[2] + "): " + onlineStock[2]);
        System.out.println("4. Oil       (" + currentSellersStock[3] + "): " + onlineStock[3]);
        System.out.println("======================================");
    }

    private static void viewMarket(Scanner scanner) {
        running.set(true);
        Thread uiThread = new Thread(() -> {
            while (running.get()) {
                printBuyItemsMenu();
                try {
                    Thread.sleep(100); // Update the display every 500ms
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        uiThread.start();
        if(scanner.next().contains("q")) {
            try {
                running.set(false);;
                uiThread.join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private static void runSellItemsUI(Scanner scanner) {
            Integer tempCurrentSelling = -1;

            System.out.println("1: Flower");
            System.out.println("2: Sugar");
            System.out.println("3: Potato");
            System.out.println("4: Oil");

            System.out.print("Enter itemId to sell: ");
            tempCurrentSelling = scanner.nextInt() - 1;

            // Try and get selling rights
            try {
                Boolean successSellingRights = sellerClient.selectItemToSell(tempCurrentSelling, localStock[tempCurrentSelling], sellerClientID);
                if(successSellingRights) {
                    currentSelling = tempCurrentSelling;
                    onlineStock[currentSelling] = localStock[tempCurrentSelling];
                    currentSellersStock[currentSelling] = sellerClientID;
                    System.out.print("You now have selling rights for 60 seconds or till stock is 0");
                }
                else {
                    System.out.println("item is currently being sold by another seller");
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }

    private static void acceptBuyRequestUI(Scanner scanner) {
        scanner.nextLine();
        ArrayList<Request> copy = new ArrayList<Request>(requestArrayList);
        System.out.println("Requests Available: " + copy.size());
        for(int i = 0; i < copy.size(); i++) {
            Request request = copy.get(i);
            System.out.println((i+1) + ".  quantity: " + request.quantity + " from buyer " + request.buyerNodeId);
        }
        System.out.print("Enter requests to accept eg- 1 2 3: "); 
        String line = scanner.nextLine();
        String splitLine[] = line.split(" ");

        ArrayList<Integer> temp = new ArrayList<Integer>();
        for (String integer : splitLine) {
            System.out.println(integer);
            temp.add(Integer.parseInt(integer));
        }
        
        
        for(int i = 0; i < temp.size(); i++) {
            Request request = copy.get(temp.get(i) - 1);
            try {
                sellerClient.acceptBuyRequest(request.requestId, sellerClientID);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
    



    private static void runMainMenuUI() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            
            printMainMenu();

            System.out.print("Enter Option: ");
            int choice = -1;
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
            } else {
                scanner.next();
                continue;
            }

            switch (choice) {
                case 1:
                    joinSellerMarket();
                    setupSocketReceiveMessages();
                    break;
                case 2:
                    runSellItemsUI(scanner);
                    break;
                case 3:
                    acceptBuyRequestUI(scanner);
                    break;
                case 4:
                    viewMarket(scanner);
                    break;
                case 5:
                    leaveSellerMarket();    
                    break;
                case 0:
                    running = false;
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    break;
            }
        }

        scanner.close();
    }
}
