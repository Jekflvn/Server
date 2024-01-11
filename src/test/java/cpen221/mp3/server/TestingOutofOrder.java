package cpen221.mp3.server;

import cpen221.mp3.client.Client;
import cpen221.mp3.entity.Actuator;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.event.SensorEvent;
import cpen221.mp3.CSVEventReader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestingOutofOrder {
    String csvFilePath = "data/tests/single_client_1000_events_in-order.csv";
    CSVEventReader eventReader = new CSVEventReader(csvFilePath);
    List<Event> eventList = eventReader.readEvents();

    Client client = new Client(0, "test@test.com", "1.1.1.1", 1);
    Actuator actuator1 = new Actuator(97, 0, "Switch", true);

    @Test
    public void outOfOrderMostRecent(){
        Server server = new Server(client);
        Event event1 = new ActuatorEvent(0.00010015, 0, 11,"Switch", true);
        Event event2 = new SensorEvent(0.000111818, 0, 1,"TempSensor", 1.0);
        Event event3 = new ActuatorEvent(0.00015, 0, 5,"Switch", false);
        Event event4 = new SensorEvent(0.00022, 0, 1,"TempSensor", 11.0);
        Event event5 = new ActuatorEvent(0.00027, 0, 11,"Switch", false);
        Event event6 = new ActuatorEvent(0.00047, 0, 11,"Switch", true);
        server.processIncomingEvent(event1);
        server.processIncomingEvent(event4);
        server.processIncomingEvent(event2);
        server.processIncomingEvent(event6);
        server.processIncomingEvent(event5);
        server.processIncomingEvent(event3);
        ArrayList<Event> answerNEvents = new ArrayList<>();
        answerNEvents.add(event5);
        answerNEvents.add(event6);
        assertEquals(answerNEvents, server.lastNEvents(2));
        assertEquals(11, server.mostActiveEntity());

        ArrayList<Event> eventsInWindow = new ArrayList<>();
        eventsInWindow.add(event1);
        eventsInWindow.add(event2);
        eventsInWindow.add(event3);
        assertEquals(eventsInWindow, server.eventsInTimeWindow(new TimeWindow(0,0.00020)));

        ArrayList<Integer> getAllEntities = new ArrayList<>();
        getAllEntities.add(11);
        getAllEntities.add(1);
        getAllEntities.add(5);
        assertEquals( getAllEntities,server.getAllEntities());

        ArrayList<Boolean> predictNevents = new ArrayList<>();
        predictNevents.add(false);
        predictNevents.add(true);
        predictNevents.add(false);
        assertEquals(predictNevents, server.predictNextNValues(11,3));

        }
    }

