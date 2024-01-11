package cpen221.mp3.client;

import cpen221.mp3.entity.Entity;

import java.io.*;
import java.net.Socket;
import java.util.*;


public class Client {

    private final int clientId;
    private String email;
    private String serverIP;
    private int serverPort;
    private Socket clientSocket;
    private PrintWriter clientPrint;
    private BufferedReader clientReader;
    private String mostRecentResultOfRequest; //Stores the result from the most recent request reply

    private static Map<Client, Entity> listOfEntity = new HashMap<>();

    public Client(int clientId, String email, String serverIP, int serverPort) {
        this.clientId = clientId;
        this.email = email;
        this.serverIP = serverIP;
        this.serverPort = serverPort;

    }

    public void start() {
        try {
            clientSocket = new Socket(serverIP, serverPort);
            clientPrint = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);
            clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            sendIntroMessage(); // Send intro message after making the connection
        } catch (IOException e) {
            System.out.println("Client Cannot be initialized: " + e.getMessage());
        }
    }


    private void sendIntroMessage() {
        // Format: "Client|<clientId>"
        String introMessage = "Client|" + this.clientId;
        clientPrint.println(introMessage);
    }

    public int getClientId() {
        return clientId;
    }

    /**
     * Registers an entity for the client
     *
     * @return true if the entity is new and gets successfully registered, false if the Entity is already registered
     */
    public boolean addEntity(Entity entity) {
        if(listOfEntity.containsValue(entity)){
            return false;
        }
        else {
            listOfEntity.put(this, entity);
            return true;
        }
    }

    public void sendRequest(Request request) {
        if(serverIP == null){
            System.out.println("message cannot be initialized, check serverIP");
        } else {
            String stringRequest = request.toString();
            clientPrint.println(stringRequest);
            clientPrint.flush();

            if (request.getRequestType().equals(RequestType.ANALYSIS) || request.getRequestType().equals(RequestType.PREDICT)) {

                new Thread(() -> {
                    try {
                        System.out.println("Just before read in client");
                        String result = clientReader.readLine();
                        System.out.println("Result of Request: " + result);
                        mostRecentResultOfRequest = result;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).start();


            }

        }
    }
    public String getResult() {
        return mostRecentResultOfRequest;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Client){
            Client k = (Client) obj;
            return k.getClientId() == this.getClientId();
        }
        return super.equals(obj);
    }
}