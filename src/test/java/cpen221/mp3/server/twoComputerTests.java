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

public class twoComputerTests {

    String csvFilePath = "data/tests/single_client_1000_events_in-order.csv";
    CSVEventReader eventReader = new CSVEventReader(csvFilePath);
    List<Event> eventList = eventReader.readEvents();
    String ip = "127.0.0.1";




    @Test
    public void initialConnectionTestforTwoComputers() {
        MessageHandler messageHandler = new MessageHandler(8080);
        messageHandler.start();


        try {
            Thread.sleep(1000); //do longer when actually testing it
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //A client and sensor are initialized in another computer
        //Check that "RECEIVED AN EVENT" is printed on this computer
        //IT WORKS
        //Worked for on same network
    }



}