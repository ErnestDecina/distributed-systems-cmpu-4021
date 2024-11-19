import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
import modals.JoinMarketDetails;

public class BuyerClient {
    static IMarket buyerClient;
    static DatagramSocket socket;

    // Market Config
    static String domain = "localhost";
    static int port = 8080;
    static String url = "//" + domain + ":" + port + "/MarketServer";
    

    static Integer buyerClientNodeId = -1;
    static Integer socketNumber = -1;

    static ArrayList<String> notificationQueue = new ArrayList<String>();
    static volatile Integer onlineStock[] = {0, 0, 0, 0};
    static volatile Integer currentSellersStock[] = {-1, -1, -1, -1};
    private static final AtomicBoolean running = new AtomicBoolean(false);

    public static void main(String args[]) {
        setUpRegistery();
        runMainMenuUI();
    }

    private static void setUpRegistery() {
        try {
            buyerClient = (IMarket)Naming.lookup(url);

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
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
                        Integer sellerNodeId;

                        switch(splitMessage[0]) {
                            case "NOTIFY_BUYER_SUCCESS_BUY":
                                itemId = Integer.parseInt(splitMessage[1]);
                                quantity = Integer.parseInt(splitMessage[2]);
                                buyerNodeId = Integer.parseInt(splitMessage[3]);
                                sellerNodeId = Integer.parseInt(splitMessage[4]);
                                
                                buyerSuccess(itemId, quantity, sellerNodeId);
                                break;
                            
                            case "NOTIFY_BUYER_FAILURE_BUY":
                                itemId = Integer.parseInt(splitMessage[1]);
                                quantity = Integer.parseInt(splitMessage[2]);
                                buyerNodeId = Integer.parseInt(splitMessage[3]);
                                sellerNodeId = Integer.parseInt(splitMessage[4]);

                                buyerFailer(itemId, quantity, sellerNodeId);
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

    private static void buyerSuccess(Integer itemId, Integer quantity, Integer sellerNodeId) {
        String successMessage = "buyerNodeId: " + buyerClientNodeId + " successfully bought \"" + quantity + "\" of itemId: " + itemId + " from sellerNodeId: " + sellerNodeId;
        notificationQueue.add(successMessage);
        System.out.println(successMessage);
    }

    private static void buyerFailer(Integer itemId, Integer quantity, Integer sellerNodeId) {
        String failureMessage = "buyerNodeId: " + buyerClientNodeId + " failed to buy \"" + quantity + "\" of itemId: " + itemId + " from sellerNodeId: " + sellerNodeId;
        notificationQueue.add(failureMessage);
        System.out.println(failureMessage);
    }

    private static void joinBuyerMarket() {
        try {
            IJoinMarketDetails buyerClientDetails = buyerClient.joinMarket(0); 
            buyerClientNodeId = buyerClientDetails.getNodeId();
            socketNumber =  buyerClientDetails.getSocketNumber();
            socket = new DatagramSocket();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void buyItems(Integer itemId, Integer quantity) {
        try {
            buyerClient.buyItems(itemId, quantity, buyerClientNodeId);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void printMainMenu() {
        String status = "Disconnected";
        if(!buyerClientNodeId.equals(-1)) {
            status = "Connected";
        }

        System.out.println("\n====== Market UI Menu ======");
        System.out.println("Market Status: " + status);
        System.out.println("1. Join Market");
        System.out.println("2. Buy Items");
        System.out.println("3. View Market");
        System.out.println("4. View Notifications");
        System.out.println("5. Leave Market");
        System.out.println("0. Close Program");
        System.out.println("================================");
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
                    joinBuyerMarket();
                    setupSocketReceiveMessages();
                    break;
                case 2:
                    buyItemsUI(scanner);
                    break;
                case 3:
                    viewMarket(scanner);
                    break;
                case 4:
                    viewNotifications(scanner);
                    break;
                case 5:
                       
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

    public static void buyItemsUI(Scanner scanner) {
        Integer tempBuyItemId = -1;
        Integer tempQuantity = -1;

        System.out.println("1: Flower");
        System.out.println("2: Sugar");
        System.out.println("3: Potato");
        System.out.println("4: Oil");

        System.out.print("Enter itemId to buy: ");
        tempBuyItemId = scanner.nextInt() - 1;

        System.out.print("Enter itemId to buy: ");
        tempQuantity = scanner.nextInt();

        // Try and get selling rights
        try {
            buyerClient.buyItems(tempBuyItemId, tempQuantity, buyerClientNodeId);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void printBuyItemsMenu() {
        System.out.print("\033[2J\033[H");
        System.out.println("\n====== Buyers Market UI Menu ======");
        System.out.println("1. Flower    (" + currentSellersStock[0] + ") : " + onlineStock[0]);
        System.out.println("2. Sugar     (" + currentSellersStock[1] + "): " + onlineStock[1]);
        System.out.println("3. Potato    (" + currentSellersStock[2] + "): " + onlineStock[2]);
        System.out.println("4. Oil       (" + currentSellersStock[3] + "): " + onlineStock[3]);
        System.out.println("======================================");
    }

    private static void printNotificationsMenu() {
        System.out.print("\033[2J\033[H");
        System.out.println("\n====== Buyers Notifications UI Menu ======");
        for (String notification : notificationQueue) {
            System.out.println(notification);
        }
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

    private static void viewNotifications(Scanner scanner) {
        running.set(true);
        Thread uiThread = new Thread(() -> {
            while (running.get()) {
                printNotificationsMenu();
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
}
