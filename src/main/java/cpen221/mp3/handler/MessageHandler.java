package cpen221.mp3.handler;

import cpen221.mp3.client.Request;
import cpen221.mp3.client.RequestType;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.event.SensorEvent;
import cpen221.mp3.server.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MessageHandler {
    private ServerSocket serverSocket;
    private int port;
    private ArrayList<Socket> clients;
    private ArrayList<Socket> entities;
    private Map<Integer, Server> servers;  //Client ID: Server


    // you may need to add additional private fields and methods to this class

    public MessageHandler(int port) {
        this.port = port;
        clients = new ArrayList<>();
        entities = new ArrayList<>();
        servers = new HashMap<>();
    }

    public void start() {
        // the following is just to get you started
        // you may need to change it to fit your implementation
        new Thread(() -> {

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            while (true) {
                Socket incomingSocket = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(incomingSocket.getInputStream()));
                String introMessage = reader.readLine();

                if (introMessage != null) {
                    // Parse the intro message
                    String[] parts = introMessage.split("\\|");

                    // String introMessage = "Client|" + this.clientId;
                    String type = parts[0]; //Client, Sensor, Actuator
                    int clientID = Integer.parseInt(parts[1]);

                    if (!servers.containsKey(clientID)) {
                        servers.put(clientID, new Server(clientID));
                    }
                    Server thisServer = servers.get(clientID);
                    if (type.equals("Client")) {
                        PrintWriter printer = new PrintWriter(new OutputStreamWriter(incomingSocket.getOutputStream()), true);
                        Thread clientReaderThread = new Thread(new ClientReadThread(reader, clientID, thisServer, -1));
                        Thread clientWriterThread = new Thread(new ClientWriteThread(printer, clientID, thisServer, -1));
                        clientWriterThread.start();
                        clientReaderThread.start();
                    }

                    else if (type.equals("Actuator")) {

                        thisServer.addActuator(Integer.parseInt(parts[2]));

                        PrintWriter printer = new PrintWriter(new OutputStreamWriter(incomingSocket.getOutputStream()), true);

                        Thread actuatorReaderThread = new Thread(new ActuatorReadThread(reader, clientID, thisServer, Integer.parseInt(parts[2])));
                        Thread actuatorWriterThread = new Thread(new ActuatorWriteThread(printer, clientID, thisServer, Integer.parseInt(parts[2])));
                        actuatorWriterThread.start();
                        actuatorReaderThread.start();
                    }
                    else if (type.equals("Sensor")) {
                        Thread sensorReaderThread = new Thread(new SensorReadThread(reader, clientID, thisServer, Integer.parseInt(parts[2])));
                        sensorReaderThread.start();
                    }
                    System.out.println("Created Threads for " + type);

                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }).start();
    }

    public static void main(String[] args) {
        MessageHandler handler = new MessageHandler(8080);
        handler.start();

        
    }
}


abstract class ReaderThreadBase implements Runnable {
    protected BufferedReader reader;
    protected int clientID;
    protected Server server;
    protected int entityID;

    public ReaderThreadBase(BufferedReader reader, int clientID, Server server, int entityID) {
        this.reader = reader;
        this.clientID = clientID;
        this.server = server;
        this.entityID = entityID;
    }
}
abstract class WriterThreadBase implements Runnable {
    protected PrintWriter writer;
    protected int clientID;
    protected Server server;
    protected int entityID;

    public WriterThreadBase(PrintWriter writer, int clientID, Server server, int entityID) {
        this.writer = writer;
        this.clientID = clientID;
        this.server = server;
        this.entityID = entityID;
    }
}

class ClientReadThread extends ReaderThreadBase {
    public ClientReadThread(BufferedReader reader, int clientID, Server server, int entityID) {
        super(reader, clientID, server, entityID);
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                Request newRequest = PARSER.clientRequest(message);
                if (!newRequest.getRequestType().equals(RequestType.CONFIG)) {

                    server.executorService.schedule(() -> server.processIncomingRequest(newRequest), (long)server.getMaxWaitTime(), TimeUnit.SECONDS);
                } else {
                    server.processIncomingRequest(newRequest);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientWriteThread extends WriterThreadBase {
    private BlockingQueue<String> messageQueue = server.getClientNotifications();

    public ClientWriteThread(PrintWriter printer, int clientID, Server server, int entityID) {
        super(printer, clientID, server, entityID);
    }
    @Override
    public void run() {
        try {
            while (true) {
                System.out.println("Just before read");

                String message = messageQueue.take();
                System.out.println("Message: " + message);
                writer.println(message);
                writer.flush();
                System.out.println("Sent res back to client");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class SensorReadThread extends ReaderThreadBase {
    public SensorReadThread(BufferedReader reader, int clientID, Server server, int entityID) {
        super(reader, clientID, server, entityID);
    }

    @Override
    public void run() {

        try {
            String message;
            while ((message = reader.readLine()) != null) {
                SensorEvent newEvent = PARSER.SensorEvent(message);
                server.processIncomingEvent(newEvent);



            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ActuatorReadThread extends ReaderThreadBase {
    public ActuatorReadThread(BufferedReader reader, int clientID, Server server, int entityID) {
        super(reader, clientID, server, entityID);
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                ActuatorEvent newEvent = PARSER.actuatorEvent(message);

                server.processIncomingEvent(newEvent);

               // toggleIFQueue.add(newEvent);
                //  OPTION:: Make 2 priority queues, one for waitTimeLimit, one for eventTimeStamp
                //
                //   check the first element of the waitTimeLimit, and when removing one that's timedOut, check all with less time stamp in eventTimeStamp queue

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ActuatorWriteThread extends WriterThreadBase {
    private BlockingQueue<String> messageQueue;

    public ActuatorWriteThread(PrintWriter writer, int clientID, Server server, int entityID) {
        super(writer, clientID, server, entityID);
        while (!server.entityQueues.containsKey(entityID)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        messageQueue = server.entityQueues.get(entityID);

    }

    @Override
    public void run() {
        try {
            while (true) {

                String message = messageQueue.take();
                writer.println(message);
                writer.flush();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}