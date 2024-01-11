package cpen221.mp3.server;

import cpen221.mp3.client.RequestCommand;
import cpen221.mp3.client.RequestType;
import cpen221.mp3.entity.Actuator;
import cpen221.mp3.client.Client;
import cpen221.mp3.entity.Entity;
import cpen221.mp3.entity.Sensor;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.client.Request;
import cpen221.mp3.handler.PARSER;

import java.awt.event.HierarchyEvent;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

import java.io.*;
import java.net.Socket;
import java.security.KeyStore;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Server {
    private Client client;
    private int clientId;
    private double maxWaitTime = 2; // in seconds

    private Socket socketForServer;
    private PrintWriter serverWriter;
    private BufferedReader serverReader;
    private List<Event> historyOfEvent = new ArrayList<>();
    private ConcurrentHashMap<Integer, ArrayList<Event>> mostActiveEntity = new ConcurrentHashMap<>();
    private Queue<Event> currentEventsbyTimeStamp = new PriorityQueue<>();
    private Queue<Event> currentEventsbyDeadline = new PriorityQueue<>();
    private ArrayList<Socket> sensorSockets;
    private ArrayList<Socket> actuatorSockets;
    private ArrayList<Integer> log = new ArrayList<>();
    private double logTime;

    private BlockingQueue<String> clientNotifications = new LinkedBlockingQueue<>();
    private  Map<Integer, Entity> entityIDs;


    private  Map<Integer, Actuator> ActuatorIDs;

    private  Map<Integer, Sensor> SensorIDs;

    private Socket clientSocket;
    private List<BlockingQueue> actuatorQueues = new ArrayList<>();

    private Filter logIfCurrentFilter;
    private Filter logIfPrevFilter;
    private Filter toggleIfCurrentFilter;
    private Filter toggleIfPrevFilter;
    private Filter setIfCurrentFilter;
    private Filter setIfPrevFilter;
    private Integer toggleIfCurrActuator;
    private Integer toggleIfPrevActuator;
    private Integer setIfCurrActuator;
    private Integer setIfPrevActuator;
    private Double setIfCurrTime =0.0;
    private Double setIfPrevTime = 0.0;
    private Double toggleIfCurrTime = 0.0;
    private Double toggleIfPrevTime = 0.0;
    private Double  logIfCurrTime = 0.0;
    private Double logIfPrevTime = 0.0;
    private List<Event> toggleSetList = Collections.synchronizedList(new ArrayList<Event>());

    public ConcurrentHashMap<Integer, BlockingQueue<String>> entityQueues  = new ConcurrentHashMap<>(); //Entity ID, list of commands
    public ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1); // Shared executor

    // you may need to add additional private fields

    public Server(Client client){
        this.clientId = client.getClientId();
        // implement the Server constructor
        this.client = client;
        this.logIfCurrTime = 0.0;
        this.setIfCurrActuator = 0;
        this.toggleIfCurrActuator = 0;

        runEventQueueThread();
    }
    public Server(int clientId){
        this.clientId = clientId;
        this.logIfCurrTime = 0.0;
        this.setIfCurrActuator = 0;
        this.toggleIfCurrActuator = 0;
        runEventQueueThread();
    }
    public void runEventQueueThread() {
        new Thread(() -> {
            while (true) {

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                if (!toggleSetList.isEmpty() && toggleSetList.get(0).getDeadline() < System.currentTimeMillis() + 100) {

                    double time = toggleSetList.get(0).getTimeStamp();
                    ArrayList<Event> result = new ArrayList<>();
                    result.add(toggleSetList.get(0));

                    synchronized (toggleSetList) {
                        for (int i = toggleSetList.size() - 1; i >= 0; i--) {
                            if (toggleSetList.get(i).getTimeStamp() < time) {
                                for (int j = result.size() - 1; j >= 0; j--) {
                                    boolean found = false;
                                    if (toggleSetList.get(i).getTimeStamp() < result.get(j).getTimeStamp()) {
                                        result.add(j, toggleSetList.get(i));
                                        found = true;
                                    }
                                    if (!found) {
                                        result.add(0, toggleSetList.get(i));
                                    }
                                }
                                result.add(toggleSetList.get(i));

                                toggleSetList.remove(i);
                            }
                        }
                    }

                    for (int i = 0; i < result.size(); i++) {
                        processToggleSetEventStuff(result.get(i));
                    }

                }
            }

        }).start();

    }

    public void addActuator(int actuatorID) {
        entityQueues.put(actuatorID, new LinkedBlockingQueue<String>());
    }

    /**
     * Update the max wait time for the client.
     * The max wait time is the maximum amount of time
     * that the server can wait for before starting to process each event of the client:
     * It is the difference between the time the message was received on the server
     * (not the event timeStamp from above) and the time it started to be processed.
     *
     * @param maxWaitTime the new max wait time
     */
    public void updateMaxWaitTime(double maxWaitTime) {
        // implement this method
        this.maxWaitTime = maxWaitTime;

        // Important note: updating maxWaitTime may not be as simple as
        // just updating the field. You may need to do some additional
        // work to ensure that events currently being processed are not
        // dropped or ignored by the change in maxWaitTime.
    }

    /**
     * Set the actuator state if the given filter is satisfied by the latest event.
     * Here the latest event is the event with the latest timestamp not the event
     * that was received by the server the latest.
     *
     * If the actuator is not registered for the client, then this method should do nothing.
     *
     * @param filter the filter to check
     * @param actuator the actuator to set the state of as true
     */
    public void setActuatorStateIf(Filter filter, Actuator actuator) {
        setIfPrevActuator = setIfCurrActuator;
        setIfCurrActuator = actuator.getId();
        setIfPrevFilter = setIfCurrentFilter;
        setIfCurrentFilter = filter;

    }

    public void setActuatorStateIf(Filter filter, int ActuatorID) throws InterruptedException {
        setIfPrevActuator = setIfCurrActuator;
        setIfCurrActuator = ActuatorID;
        setIfPrevFilter = setIfCurrentFilter;
        setIfCurrentFilter = filter;

    }

    /**
     * Toggle the actuator state if the given filter is satisfied by the latest event.
     * Here the latest event is the event with the latest timestamp not the event
     * that was received by the server the latest.
     *
     * If the actuator has never sent an event to the server, then this method should do nothing.
     * If the actuator is not registered for the client, then this method should do nothing.
     *
     * @param filter the filter to check
     * @param actuator the actuator to toggle the state of (true -> false, false -> true)
     */
    public void toggleActuatorStateIf(Filter filter, Actuator actuator) {
        toggleIfPrevActuator = toggleIfCurrActuator;
        toggleIfCurrActuator = actuator.getId();
        toggleIfPrevFilter = toggleIfCurrentFilter;
        toggleIfCurrentFilter = filter;
        toggleIfPrevTime = toggleIfCurrTime;
        toggleIfCurrTime = (double) System.currentTimeMillis();

    }

    public void toggleActuatorStateIf(Filter filter, int actuatorID) throws InterruptedException {
        toggleIfPrevActuator = toggleIfCurrActuator;
        toggleIfCurrActuator = actuatorID;
        toggleIfPrevFilter = toggleIfCurrentFilter;
        toggleIfCurrentFilter = filter;
        toggleIfPrevTime = toggleIfCurrTime;
        toggleIfCurrTime = (double) System.currentTimeMillis();

    }

    /**
     * Log the event ID for which a given filter was satisfied.
     * This method is checked for every event received by the server.
     *
     * @param filter the filter to check
     */
    public void logIf(Filter filter) {
        logIfPrevTime = logIfCurrTime;
        logIfCurrTime = (double) System.currentTimeMillis();

        logIfPrevFilter = logIfCurrentFilter;
        logIfCurrentFilter = filter;
        for(int i = historyOfEvent.size() - 1; i >= 0; i--){
            if (historyOfEvent.get(i).getTimeStamp() > logIfCurrTime){
                if(filter.satisfies((historyOfEvent.get(i)))) {
                    log.add(historyOfEvent.get(i).getEntityId());
                }
            } else {
                return;
            }
        }
    }

    /**
     * Return all the logs made by the "logIf" method so far.
     * If no logs have been made, then this method should return an empty list.
     * The list should be sorted in the order of event timestamps.
     * After the logs are read, they should be cleared from the server.
     *
     * @return list of entity IDs
     */
    public List<Integer> readLogs() {
        ArrayList<Integer> clone = (ArrayList<Integer>) log.clone();
        log.clear();
        return clone;
    }

    /**
     * List all the events of the client that occurred in the given time window.
     * Here the timestamp of an event is the time at which the event occurred, not
     * the time at which the event was received by the server.
     * If no events occurred in the given time window, then this method should return an empty list.
     *
     * @param timeWindow the time window of events, inclusive of the start and end times
     * @return list of the events for the client in the given time window
     */
    public List<Event> eventsInTimeWindow(TimeWindow timeWindow) {
        List<Event> eventsInWindow = historyOfEvent.stream()
                .filter(event -> event.getTimeStamp() >= timeWindow.startTime && event.getTimeStamp() <= timeWindow.endTime)
                .toList();
        return eventsInWindow;
    }

    /**
     * Returns a set of IDs for all the entities of the client for which
     * we have received events so far.
     * Returns an empty list if no events have been received for the client.
     *
     * @return list of all the entities of the client for which we have received events so far
     */
    public List<Integer> getAllEntities() {
        List<Integer> uniqueIds = historyOfEvent.stream()
                .map(Event::getEntityId)
                .distinct()
                .collect(Collectors.toList());
        return uniqueIds;
    }

    /**
     * List the latest n events of the client.
     * Here the order is based on the original timestamp of the events, not the time at which the events were received by the server.
     * If the client has fewer than n events, then this method should return all the events of the client.
     * If no events exist for the client, then this method should return an empty list.
     * If there are multiple events with the same timestamp in the boundary,
     * the ones with largest EntityId should be included in the list.
     *
     * @param n the max number of events to list
     * @return list of the latest n events of the client
     */
    public List<Event> lastNEvents(int n) {
        return historyOfEvent.subList(historyOfEvent.size() - n, historyOfEvent.size());
    }

    /**
     * returns the ID corresponding to the most active entity of the client
     * in terms of the number of events it has generated.
     *
     * If there was a tie, then this method should return the largest ID.
     *
     * @return the most active entity ID of the client
     */
    public int mostActiveEntity() {
        int max = 0;
        int id = 0;
        for(Integer key : mostActiveEntity.keySet()){
            if(mostActiveEntity.get(key).size() > max){
                max = mostActiveEntity.get(key).size();
                id = key;
            }
            else if (mostActiveEntity.get(key).size() == max){
                if (key > id){
                    max = mostActiveEntity.get(key).size();
                    id = key;
                }
            }
        }
        return id;
    }

    /**
     * the client can ask the server to predict what will be
     * the next n timestamps for the next n events
     * of the given entity of the client (the entity is identified by its ID).
     *
     * If the server has not received any events for an entity with that ID,
     * or if that Entity is not registered for the client, then this method should return an empty list.
     *
     * @param entityId the ID of the entity
     * @param n the number of timestamps to predict
     * @return list of the predicted timestamps
     */
    public List<Double> predictNextNTimeStamps(int entityId, int n) {
        List<Event> events = mostActiveEntity.get(entityId);
        List<Double> answer = new ArrayList<>();
        if(events.get(0).getValueDouble() == events.get(1).getValueDouble()){
            for(int i = 0; i < n; i++){
                answer.add(events.get(0).getValueDouble());
            }
        } else if (events.get(0).getValueDouble() == events.get(events.size() - 1).getValueDouble()){
            for(int i = 0; i < n; i ++){
                if (i % 2 == 1){
                    answer.add(events.get(0).getValueDouble());
                } else {
                    answer.add(events.get(1).getValueDouble());
                }
            }
        } else {
            for(int i = 0; i < n; i ++){
                if (i % 2 == 1){
                    answer.add(events.get(1).getValueDouble());
                } else {
                    answer.add(events.get(0).getValueDouble());
                }
            }
        }
        return answer;
    }

    /**
     * the client can ask the server to predict what will be
     * the next n values of the timestamps for the next n events
     * of the given entity of the client (the entity is identified by its ID).
     * The values correspond to Event.getValueDouble() or Event.getValueBoolean()
     * based on the type of the entity. That is why the return type is List<Object>.
     *
     * If the server has not received any events for an entity with that ID,
     * or if that Entity is not registered for the client, then this method should return an empty list.
     *
     * @param entityId the ID of the entity
     * @param n the number of double value to predict
     * @return list of the predicted timestamps
     */

    public List<Object> predictNextNValues(int entityId, int n) {
        List<Event> events = mostActiveEntity.get(entityId);
        Collections.sort(events, Comparator.comparing(Event::getTimeStamp));
        if (events.get(0).getValueDouble() == -1){
            return predictHelperBoolean(events, n);
        } else {
            return predictHelperDouble(events, n);
        }
    }

    public List<Object> predictHelperBoolean(List<Event> events, int n){
        List<Object> answer = new ArrayList<>();
        if(events.get(0).getValueBoolean() == events.get(1).getValueBoolean()){
            for(int i = 0; i < n; i++){
                answer.add(events.get(0).getValueBoolean());
            }
        } else if (events.get(0).getValueBoolean() == events.get(events.size() - 1).getValueBoolean()){
            for(int i = 0; i < n; i ++){
                if (i % 2 == 1){
                    answer.add(events.get(0).getValueBoolean());
                } else {
                    answer.add(events.get(1).getValueBoolean());
                }
            }
        } else {
            for(int i = 0; i < n; i ++){
                if (i % 2 == 1){
                    answer.add(events.get(1).getValueBoolean());
                } else {
                    answer.add(events.get(0).getValueBoolean());
                }
            }
        }
        return answer;
    }

    public List<Object> predictHelperDouble (List<Event> events, int n){
        List<Object> answer = new ArrayList<>();
        if(events.get(0).getValueDouble() == events.get(1).getValueDouble()){
            for(int i = 0; i < n; i++){
                answer.add(events.get(0).getValueDouble());
            }
        } else if (events.get(0).getValueDouble() == events.get(events.size() - 1).getValueDouble()){
            for(int i = 0; i < n; i ++){
                if (i % 2 == 1){
                    answer.add(events.get(0).getValueDouble());
                } else {
                    answer.add(events.get(1).getValueDouble());
                }
            }
        } else {
            for(int i = 0; i < n; i ++){
                if (i % 2 == 1){
                    answer.add(events.get(1).getValueDouble());
                } else {
                    answer.add(events.get(0).getValueDouble());
                }
            }
        }
        return answer;
    }

    synchronized public void processIncomingEvent(Event event) {
        event.setDeadline(System.currentTimeMillis() + maxWaitTime * 1000);
        toggleSetList.add(event);
        if (historyOfEvent.isEmpty()) {
            historyOfEvent.add(event);
        } else {
            boolean found = false;
            for(int i = historyOfEvent.size() - 1; i >= 0; i--){
                if (historyOfEvent.get(i).getTimeStamp() < event.getTimeStamp()){
                    found = true;
                    historyOfEvent.add(i + 1, event);
                    break;
                }
            }
            if (!found) {
                historyOfEvent.add(0, event);
            }
        }
        if(mostActiveEntity.containsKey(event.getEntityId())){
            mostActiveEntity.get(event.getEntityId()).add(event);
        }
        else {
            mostActiveEntity.put(event.getEntityId(), new ArrayList<>());
            mostActiveEntity.get(event.getEntityId()).add(event);
        }
        if(logIfCurrTime != 0.0){
            if(logIfPrevTime != 0.0 && logIfPrevTime <= event.getTimeStamp()  && event.getTimeStamp() < logIfCurrTime){
                if(logIfPrevFilter.satisfies(event)){
                    log.add(event.getEntityId());
                }
            } else if (event.getTimeStamp() >= logIfCurrTime){
                if(logIfCurrentFilter.satisfies(event)){
                    log.add((event.getEntityId()));
                }
            }
        }
    }

    synchronized public void processToggleSetEventStuff(Event event) throws InterruptedException {

        if(toggleIfCurrActuator != 0){
            if(toggleIfPrevTime != 0.0 && toggleIfPrevTime <= event.getTimeStamp()  && event.getTimeStamp() < toggleIfCurrTime){
                if(toggleIfPrevFilter.satisfies(event)){
                    Request x = new Request(RequestType.CONTROL, RequestCommand.CONTROL_TOGGLE_ACTUATOR_STATE, "TOGGLE");
                    entityQueues.get(event.getEntityId()).put(x.toString());
                }
            } else if (event.getTimeStamp() >= toggleIfCurrTime){
                if(toggleIfCurrentFilter.satisfies(event)){
                    Request x = new Request(RequestType.CONTROL, RequestCommand.CONTROL_TOGGLE_ACTUATOR_STATE, "TOGGLE");
                    entityQueues.get(event.getEntityId()).put(x.toString());
                }
            }
        }
        if(setIfCurrActuator != 0){
            if(setIfPrevTime != 0.0 && setIfPrevTime <= event.getTimeStamp()  && event.getTimeStamp() < setIfCurrTime){
                if(setIfPrevFilter.satisfies(event)){
                    Request x = new Request(RequestType.CONTROL, RequestCommand.CONTROL_SET_ACTUATOR_STATE, "SET");
                    entityQueues.get(event.getEntityId()).put(x.toString());
                }
            } else if (event.getTimeStamp() >= setIfCurrTime){
                if(setIfCurrentFilter.satisfies(event)){
                    Request x = new Request(RequestType.CONTROL, RequestCommand.CONTROL_SET_ACTUATOR_STATE, "SET");
                    entityQueues.get(event.getEntityId()).put(x.toString());
                }
            }
        }
    }

    synchronized public void processIncomingRequest(Request request) {
        System.out.println("SERVER RECEIVED REQUEST");
        String data = request.getRequestData();
        String[] parts = data.split("\\|");
        switch (request.getRequestCommand()) {
            case ANALYSIS_READ_LOG:
                ArrayList<Integer> currentLog = (ArrayList<Integer>) readLogs();
                clientNotifications.add(PARSER.ListofIntegers(currentLog));
                break;
            case CONTROL_LOG_IF:
                Filter LOGIF;
                logIfPrevTime = logIfCurrTime;
                logIfCurrTime = request.getTimeStamp();
                if(parts.length == 2){
                    LOGIF = new Filter(BooleanOperator.valueOf(parts[0]), Boolean.valueOf(parts[1]));
                }
                else {
                    LOGIF = new Filter(parts[0], DoubleOperator.valueOf(parts[1]), Double.valueOf(parts[2]));
                }
                logIf(LOGIF);
                logTime = request.getTimeStamp();
                break;

            case PREDICT_NEXT_N_VALUES:
                clientNotifications.add(PARSER.ListofObjects(predictNextNValues(Integer.valueOf(parts[0]), Integer.valueOf(parts[1]))));
                break;

            case ANALYSIS_GET_LATEST_EVENTS:
                clientNotifications.add(PARSER.ParsingListOfEvent(lastNEvents(Integer.parseInt(parts[0]))));
                break;

            case ANALYSIS_GET_ALL_ENTITIES:
                String Combined = "";
                for(Integer ID : getAllEntities()){
                    Combined += ID + ", ";
                }
                try {
                    clientNotifications.put(Combined);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                break;

            case PREDICT_NEXT_N_TIMESTAMPS:
                predictNextNTimeStamps(Integer.valueOf(parts[0]), Integer.valueOf(parts[1]));
                break;

            case CONTROL_SET_ACTUATOR_STATE:
                setIfPrevActuator = setIfCurrActuator;
                setIfCurrActuator = Integer.valueOf(parts[0]);
                setIfPrevTime = setIfCurrTime;
                setIfCurrTime = request.getTimeStamp();
                Filter SETFILTER;
                if(parts.length == 3){
                    SETFILTER = new Filter(BooleanOperator.valueOf(parts[1]), Boolean.valueOf(parts[2]));
                }
                else {
                    SETFILTER = new Filter(parts[1], DoubleOperator.valueOf(parts[2]), Double.valueOf(parts[3]));
                }
                try {
                    toggleActuatorStateIf(SETFILTER, setIfCurrActuator);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                break;

            case CONFIG_UPDATE_MAX_WAIT_TIME:
                updateMaxWaitTime(Double.valueOf(parts[0]));
                break;

            case ANALYSIS_GET_EVENTS_IN_WINDOW:
                TimeWindow window = new TimeWindow(Double.valueOf(parts[0]), Double.valueOf(parts[1]));
                clientNotifications.add(PARSER.ParsingListOfEvent(eventsInTimeWindow(window)));
                break;

            case CONTROL_TOGGLE_ACTUATOR_STATE:
                setIfPrevActuator = setIfCurrActuator;
                setIfCurrActuator = Integer.valueOf(parts[0]);
                toggleIfPrevTime = toggleIfCurrTime;
                toggleIfCurrTime = request.getTimeStamp();
                Filter TOGGLEFILTER;
                if(parts.length == 3){
                    TOGGLEFILTER = new Filter(BooleanOperator.valueOf(parts[1]), Boolean.valueOf(parts[2]));
                }
                else {
                    TOGGLEFILTER = new Filter(parts[1], DoubleOperator.valueOf(parts[2]), Double.valueOf(parts[3]));
                }
                try {
                    setActuatorStateIf(TOGGLEFILTER, toggleIfCurrActuator);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                break;

            case ANALYSIS_GET_MOST_ACTIVE_ENTITY:
                try {
                    clientNotifications.put(Integer.toString(mostActiveEntity()));
                    System.out.println("Put in client notifs");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                break;

        }



    }
    public double getMaxWaitTime() {
        return maxWaitTime;
    }

    public BlockingQueue<String> getClientNotifications() {
        return clientNotifications;
    }

//    public static void main(String[] args){
//        List<Double> x = List.of(1.2,7.9);
//        System.out.println(x.stream().max(Comparator.comparingDouble(p -> p)).orElse(100.9));
//    }
}