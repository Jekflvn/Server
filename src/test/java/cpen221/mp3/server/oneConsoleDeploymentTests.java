package cpen221.mp3.server;

import cpen221.mp3.client.Client;
import cpen221.mp3.client.Request;
import cpen221.mp3.client.RequestCommand;
import cpen221.mp3.client.RequestType;
import cpen221.mp3.entity.Actuator;
import cpen221.mp3.entity.Sensor;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.event.SensorEvent;
import cpen221.mp3.CSVEventReader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cpen221.mp3.handler.MessageHandler;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class oneConsoleDeploymentTests {

    String csvFilePath = "data/tests/single_client_1000_events_in-order.csv";
    CSVEventReader eventReader = new CSVEventReader(csvFilePath);
    List<Event> eventList = eventReader.readEvents();
    String ip = "127.0.0.1";


    @Test
    public void TestLogIfandReadLog() {
        MessageHandler messageHandler = new MessageHandler(8080);
        messageHandler.start();

        Client client = new Client(1, "test@test.com", ip, 8080);
        Sensor sensor = new Sensor(1, 1, "Sensor", ip, 8080);
        Actuator actuator = new Actuator(2, 1, "Actuator", false, ip, 8080);

        client.start();
        sensor.start();
        actuator.start();
        Request newRequest = new Request(RequestType.CONTROL, RequestCommand.CONTROL_LOG_IF, "NOT_EQUALS|true");
        client.sendRequest(newRequest);
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Request newRequest2 = new Request(RequestType.ANALYSIS, RequestCommand.ANALYSIS_READ_LOG, " ");
        client.sendRequest(newRequest2);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    public void initialConnectionTest() {
        MessageHandler messageHandler = new MessageHandler(8080);
        messageHandler.start();

        Client client = new Client(1, "test@test.com", ip, 8080);
        Sensor sensor = new Sensor(1, 1, "Sensor", ip, 8080);
        Actuator actuator = new Actuator(2, 1, "Actuator", false, ip, 8080);

        client.start();
        sensor.start();
        actuator.start();

        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
    @Test
    public void sendRequestGetAllEntities() {
        MessageHandler messageHandler = new MessageHandler(8080);
        messageHandler.start();

        Client client = new Client(2, "test@test.com", ip, 8080);
        Sensor sensor = new Sensor(5, 2, "Sensor", ip, 8080);
        Actuator actuator = new Actuator(6, 2, "Actuator", false, ip, 8080);

        client.start();
        sensor.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        actuator.start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Request newRequest = new Request(RequestType.ANALYSIS, RequestCommand.ANALYSIS_GET_ALL_ENTITIES, " ");
        client.sendRequest(newRequest);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertEquals("6, 5, ", client.getResult());

    }

    @Test
    public void sendRequestMostActiveEntity() {
        MessageHandler messageHandler = new MessageHandler(8080);
        messageHandler.start();

        Client client = new Client(3, "test@test.com", ip, 8080);
        Sensor sensor = new Sensor(3, 3, "Sensor", ip, 8080);
        Actuator actuator = new Actuator(4, 3, "Actuator", false, ip, 8080);

        client.start();

        actuator.start();
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        sensor.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Request newRequest = new Request(RequestType.ANALYSIS, RequestCommand.ANALYSIS_GET_MOST_ACTIVE_ENTITY, " ");
        client.sendRequest(newRequest);

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertEquals("4", client.getResult());

    }

    @Test
    public void sendRequestGetAllEventsInWindow() {
        MessageHandler messageHandler = new MessageHandler(8080);
        messageHandler.start();

        Client client = new Client(4, "test@test.com", ip, 8080);
        Sensor sensor = new Sensor(7, 4, "Sensor", ip, 8080);
        Actuator actuator = new Actuator(8, 4, "Actuator", false, ip, 8080);

        client.start();
        sensor.start();
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        actuator.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Request newRequest = new Request(RequestType.ANALYSIS, RequestCommand.ANALYSIS_GET_EVENTS_IN_WINDOW, "0|10000");
        client.sendRequest(newRequest);

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void sendRequestGetLatestEvents() {
        MessageHandler messageHandler = new MessageHandler(8080);
        messageHandler.start();

        Client client = new Client(1, "test@test.com", ip, 8080);
        Sensor sensor = new Sensor(1, 1, "Sensor", ip, 8080);
        Actuator actuator = new Actuator(2, 1, "Actuator", false, ip, 8080);

        client.start();
        sensor.start();
        actuator.start();

        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Request newRequest = new Request(RequestType.ANALYSIS, RequestCommand.ANALYSIS_GET_LATEST_EVENTS, "2");
        client.sendRequest(newRequest);

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertEquals("7", client.getResult());
    }

    @Test
    public void logIfreadIfTest() {
        MessageHandler messageHandler = new MessageHandler(8080);
        messageHandler.start();

        Client client = new Client(1, "test@test.com", ip, 8080);
        Sensor sensor = new Sensor(1, 1, "Sensor", ip, 8080);
        Actuator actuator = new Actuator(2, 1, "Actuator", false, ip, 8080);

        client.start();
        sensor.start();
        actuator.start();
        Request newRequest = new Request(RequestType.CONTROL,
                RequestCommand.CONTROL_LOG_IF, "value|LESS_THAN|0.0");
        client.sendRequest(newRequest);

        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Request newRequest2 = new Request(RequestType.ANALYSIS,
                RequestCommand.ANALYSIS_READ_LOG, " ");
        client.sendRequest(newRequest2);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertEquals("7", client.getResult());
    }

    @Test
    public void toggleIfTest() {
        MessageHandler messageHandler = new MessageHandler(8080);
        messageHandler.start();

        Client client = new Client(1, "test@test.com", ip, 8080);
        Sensor sensor = new Sensor(1, 1, "Sensor", ip, 8080);
        Actuator actuator = new Actuator(2, 1, "Actuator", false, ip, 8080);

        client.start();
        sensor.start();
        actuator.start();
        Request newRequest = new Request(RequestType.CONTROL,
                RequestCommand.CONTROL_TOGGLE_ACTUATOR_STATE, "2|value|LESS_THAN|0.0");
        client.sendRequest(newRequest);

        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertEquals(true, actuator.getState());
        sensor.setState(1);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertEquals(true, actuator.getState());
    }
}