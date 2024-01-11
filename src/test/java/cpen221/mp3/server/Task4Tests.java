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
public class Task4Tests {
    String csvFilePath = "data/tests/single_client_1000_events_in-order.csv";
    CSVEventReader eventReader = new CSVEventReader(csvFilePath);
    List<Event> eventList = eventReader.readEvents();

    Client client = new Client(0, "test@test.com", "1.1.1.1", 1);
    Actuator actuator1 = new Actuator(97, 0, "Switch", true);

    @Test
    public void Test1(){
        Server server = new Server(client);
        Event event1 = new ActuatorEvent(0.00010015, 0, 11,"Switch", true);
        Event event2 = new SensorEvent(0.000111818, 0, 1,"TempSensor", 1.0);
        Event event3 = new ActuatorEvent(0.00015, 0, 11,"Switch", false);
        Event event4 = new SensorEvent(0.00022, 0, 1,"TempSensor", 11.0);
        Event event5 = new ActuatorEvent(0.00027, 0, 11,"Switch", true);
        Event event6 = new ActuatorEvent(0.00047, 0, 11,"Switch", false);
        List<Event> simulatedEvents = new ArrayList<>();
        simulatedEvents.add(event1);
        simulatedEvents.add(event2);
        simulatedEvents.add(event3);
        simulatedEvents.add(event4);
        simulatedEvents.add(event5);
        simulatedEvents.add(event6);
        for (int i = 0; i < simulatedEvents.size(); i++) {
            server.processIncomingEvent(simulatedEvents.get(i));
        }
        ArrayList<Boolean> answer = new ArrayList<>();
        answer.add(true);
        answer.add(false);
        answer.add(true);
        answer.add(false);
        assertEquals(server.predictNextNValues(11, 4), answer);
    }

    @Test
    public void Test2(){
        Server server = new Server(client);
        Event event1 = new ActuatorEvent(0.00010015, 0, 11,"Switch", false);
        Event event2 = new SensorEvent(0.000111818, 0, 1,"TempSensor", 1.0);
        Event event3 = new ActuatorEvent(0.00015, 0, 11,"Switch", true);
        Event event4 = new SensorEvent(0.00022, 0, 1,"TempSensor", 11.0);
        Event event5 = new ActuatorEvent(0.00027, 0, 11,"Switch", false);
        Event event6 = new ActuatorEvent(0.00047, 0, 11,"Switch", true);
        List<Event> simulatedEvents = new ArrayList<>();
        simulatedEvents.add(event1);
        simulatedEvents.add(event2);
        simulatedEvents.add(event3);
        simulatedEvents.add(event4);
        simulatedEvents.add(event5);
        simulatedEvents.add(event6);
        for (int i = 0; i < simulatedEvents.size(); i++) {
            server.processIncomingEvent(simulatedEvents.get(i));
        }
        ArrayList<Boolean> answer = new ArrayList<>();
        answer.add(false);
        answer.add(true);
        answer.add(false);
        answer.add(true);
        assertEquals(server.predictNextNValues(11, 4), answer);
    }
    @Test
    public void Test3(){
        Server server = new Server(client);
        Event event1 = new ActuatorEvent(0.00010015, 0, 11,"Switch", false);
        Event event2 = new SensorEvent(0.000111818, 0, 1,"TempSensor", 1.0);
        Event event3 = new ActuatorEvent(0.00015, 0, 11,"Switch", false);
        Event event4 = new SensorEvent(0.00022, 0, 1,"TempSensor", 11.0);
        Event event5 = new ActuatorEvent(0.00027, 0, 11,"Switch", false);
        Event event6 = new ActuatorEvent(0.00047, 0, 11,"Switch", false);
        List<Event> simulatedEvents = new ArrayList<>();
        simulatedEvents.add(event1);
        simulatedEvents.add(event2);
        simulatedEvents.add(event3);
        simulatedEvents.add(event4);
        simulatedEvents.add(event5);
        simulatedEvents.add(event6);
        for (int i = 0; i < simulatedEvents.size(); i++) {
            server.processIncomingEvent(simulatedEvents.get(i));
        }
        ArrayList<Boolean> answer = new ArrayList<>();
        answer.add(false);
        answer.add(false);
        answer.add(false);
        answer.add(false);
        assertEquals(server.predictNextNValues(11, 4), answer);
    }

    @Test
    public void DoubleTest4(){
        Server server = new Server(client);
        Event event1 = new ActuatorEvent(0.00010015, 0, 11,"Switch", false);
        Event event2 = new SensorEvent(0.000011115, 0, 1,"TempSensor", 1.0);
        Event event3 = new ActuatorEvent(0.00015, 0, 11,"Switch", false);
        Event event4 = new SensorEvent(0.000111818, 0, 1,"TempSensor", 11.0);
        Event event5 = new SensorEvent(0.000150000, 0, 1,"TempSensor", 1.0);
        Event event6 = new SensorEvent(0.00022, 0, 1,"TempSensor", 11.0);
        List<Event> simulatedEvents = new ArrayList<>();
        simulatedEvents.add(event1);
        simulatedEvents.add(event2);
        simulatedEvents.add(event3);
        simulatedEvents.add(event4);
        simulatedEvents.add(event5);
        simulatedEvents.add(event6);
        for (int i = 0; i < simulatedEvents.size(); i++) {
            server.processIncomingEvent(simulatedEvents.get(i));
        }
        ArrayList<Double> answer = new ArrayList<>();
        answer.add(1.0);
        answer.add(11.0);
        answer.add(1.0);
        answer.add(11.0);
        assertEquals(server.predictNextNValues(1, 4), answer);
    }

    @Test
    public void DoubleTest5(){
        Server server = new Server(client);
        Event event1 = new ActuatorEvent(0.00010015, 0, 11,"Switch", false);
        Event event2 = new SensorEvent(0.000011115, 0, 1,"TempSensor", 1.0);
        Event event3 = new ActuatorEvent(0.00015, 0, 11,"Switch", false);
        Event event4 = new SensorEvent(0.000111818, 0, 1,"TempSensor", 1.0);
        Event event5 = new SensorEvent(0.000150000, 0, 1,"TempSensor", 1.0);
        Event event6 = new SensorEvent(0.00022, 0, 1,"TempSensor", 1.0);
        List<Event> simulatedEvents = new ArrayList<>();
        simulatedEvents.add(event1);
        simulatedEvents.add(event2);
        simulatedEvents.add(event3);
        simulatedEvents.add(event4);
        simulatedEvents.add(event5);
        simulatedEvents.add(event6);
        for (int i = 0; i < simulatedEvents.size(); i++) {
            server.processIncomingEvent(simulatedEvents.get(i));
        }
        ArrayList<Double> answer = new ArrayList<>();
        answer.add(1.0);
        answer.add(1.0);
        answer.add(1.0);
        answer.add(1.0);
        assertEquals(server.predictNextNValues(1, 4), answer);
    }
    @Test
    public void DoubleTest6(){
        Server server = new Server(client);
        Event event1 = new ActuatorEvent(0.00010015, 0, 11,"Switch", false);
        Event event2 = new SensorEvent(0.000011115, 0, 1,"TempSensor", 11.0);
        Event event3 = new ActuatorEvent(0.00015, 0, 11,"Switch", false);
        Event event4 = new SensorEvent(0.000111818, 0, 1,"TempSensor", 1.0);
        Event event5 = new SensorEvent(0.000150000, 0, 1,"TempSensor", 11.0);
        Event event6 = new SensorEvent(0.00022, 0, 1,"TempSensor", 1.0);
        List<Event> simulatedEvents = new ArrayList<>();
        simulatedEvents.add(event1);
        simulatedEvents.add(event2);
        simulatedEvents.add(event3);
        simulatedEvents.add(event4);
        simulatedEvents.add(event5);
        simulatedEvents.add(event6);
        for (int i = 0; i < simulatedEvents.size(); i++) {
            server.processIncomingEvent(simulatedEvents.get(i));
        }
        ArrayList<Double> answer = new ArrayList<>();
        answer.add(11.0);
        answer.add(1.0);
        answer.add(11.0);
        answer.add(1.0);
        assertEquals(server.predictNextNValues(1, 4), answer);
    }
    @Test
    public void DoubleTestTimestamp4(){
        Server server = new Server(client);
        Event event1 = new ActuatorEvent(0.00010015, 0, 11,"Switch", false);
        Event event2 = new SensorEvent(0.000011115, 0, 1,"TempSensor", 1.0);
        Event event3 = new ActuatorEvent(0.00015, 0, 11,"Switch", false);
        Event event4 = new SensorEvent(0.000111818, 0, 1,"TempSensor", 11.0);
        Event event5 = new SensorEvent(0.000150000, 0, 1,"TempSensor", 1.0);
        Event event6 = new SensorEvent(0.00022, 0, 1,"TempSensor", 11.0);
        List<Event> simulatedEvents = new ArrayList<>();
        simulatedEvents.add(event1);
        simulatedEvents.add(event2);
        simulatedEvents.add(event3);
        simulatedEvents.add(event4);
        simulatedEvents.add(event5);
        simulatedEvents.add(event6);
        for (int i = 0; i < simulatedEvents.size(); i++) {
            server.processIncomingEvent(simulatedEvents.get(i));
        }
        ArrayList<Double> answer = new ArrayList<>();
        answer.add(1.0);
        answer.add(11.0);
        answer.add(1.0);
        answer.add(11.0);
        assertEquals(server.predictNextNTimeStamps(1, 4), answer);
    }
}
