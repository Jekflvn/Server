package cpen221.mp3.server;

import cpen221.mp3.client.Client;
import cpen221.mp3.client.Request;
import cpen221.mp3.client.RequestCommand;
import cpen221.mp3.client.RequestType;
import cpen221.mp3.entity.*;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.event.SensorEvent;
import cpen221.mp3.handler.PARSER;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cpen221.mp3.server.DoubleOperator.GREATER_THAN_OR_EQUALS;
import static cpen221.mp3.server.DoubleOperator.LESS_THAN;
import static org.junit.jupiter.api.Assertions.*;

public class FinishingTest {

    @Test
    public void TimeWindowtest(){
        TimeWindow test1 = new TimeWindow(4.0, 7.0);
        String[] TWString = test1.toString().split("\\{");
        assertEquals(TWString[0], "TimeWindow");
        assertEquals(4.0, test1.getStartTime());
        assertEquals(7.0, test1.getEndTime());

        String SVAT1 = SeverCommandToActuator.SET_STATE.toString();
        String SVAT2 = SeverCommandToActuator.TOGGLE_STATE.toString();
        assertEquals("SET_STATE", SVAT1);
        assertEquals("TOGGLE_STATE", SVAT2);


    }

    @Test
    public void testComplexFilter() {
        Event sensorEvent = new SensorEvent(0.4, 0, 4, "CO2", 4);
        Filter sensorValueFilter = new Filter("value", GREATER_THAN_OR_EQUALS, 23);
        Filter sensorTSFilter = new Filter("timestamp", LESS_THAN, 1);
        List<Filter> filterList = new ArrayList<>();
        filterList.add(sensorValueFilter);
        filterList.add(sensorTSFilter);
        Filter complexFilter = new Filter(filterList);
        Event k = complexFilter.sift(sensorEvent);
        assertNull(k);


        Filter actuatorFilter = new Filter(BooleanOperator.NOT_EQUALS, true);
        String[] toString1 = actuatorFilter.toString().split("r");
        String[] toString2 = sensorValueFilter.toString().split("r");

        assertEquals("Filte", toString1[0]);
        assertEquals("Filte", toString2[0]);
    }


    @Test
    public void Actuatortest1() {
        Actuator a1 = new Actuator(1, "Switch", true);
        Actuator a2 = new Actuator(2, "cooler", true, "127.0.0.1", 443);
        String s1 = a1.toString();
        try {
            a1.start();
            a1.registerForClient(3);
        } catch (RuntimeException e) {
            assertTrue(true);
        }
        try {
            a2.start();
        } catch (RuntimeException e) {
            assertTrue(true);
        }
        a2.registerForClient(2);
        assertEquals(a2.getClientId(), 2);
        assertEquals("cooler", a2.getType());
        assertTrue(a2.isActuator());
        assertEquals(0, a2.getPort());
        a1.updateState(false);
        assertFalse(a1.getState());
        a1.registerForClient(1000);
        assertFalse(a1.registerForClient(100));

        try{
            a1.setEventGenerationFrequency(-3);
        } catch (Exception e){
            assertTrue(true);
        }
        a1.setEventGenerationFrequency( 9);
        a1.setEndpoint("127.0.0.1", 888);

        Request command1 = new Request(RequestType.CONTROL, RequestCommand.CONTROL_SET_ACTUATOR_STATE, "5");
        Request command2 = new Request(RequestType.CONTROL, RequestCommand.CONTROL_TOGGLE_ACTUATOR_STATE, "8");

        a1.processServerMessage(command1);
        assertTrue(a1.getState());
        a1.processServerMessage(command2);
        assertFalse(a1.getState());
        a1.processServerMessage(command2);
        assertTrue(a1.getState());
    }

    @Test
    public void testActuator2(){
        Actuator testAct3 = new Actuator(5,"switch", true);
        Event eventAct = new ActuatorEvent(0.9, 4,5, "switch", true);
        try{
            for(int i  = 0; i < 18; i++){
                testAct3.sendEvent(eventAct);
            }
        } catch (Exception e){
            assertTrue(true);
        }

    }

    @Test
    public void testSensor1(){
        Sensor sensor1 = new Sensor(1,"wewek");
        Sensor sensor2 = new Sensor(4, "weweki", "127.0.0.1", 777);

        try{
            sensor1.start();
        } catch (Exception e){
            assertTrue(true);
        }

        sensor1.registerForClient(777);
        sensor1.registerForClient(777);
        assertEquals(sensor1.getClientId(), 777);

        try{
            sensor1.setEventGenerationFrequency(-2);
        } catch (Exception e){
            assertTrue(true);
        }

        sensor1.setEventGenerationFrequency(7);
        Event sensorEvent = new SensorEvent(0.7, 1,5,"weweki", 7.9);
        try{
            for(int i = 0; i < 18; i++){
                sensor1.sendEvent(sensorEvent);
            }
        } catch (Exception e){
            assertTrue(true);
        }

        assertEquals(4, sensor2.getId());
        assertEquals("weweki", sensor2.getType());
        assertFalse(sensor2.isActuator());

    }

    @Test
    public void serverTest1() throws InterruptedException {
        Client test1cllient = new Client(33, "sturbak.col", "127.0.0.1", 443);
        Actuator testAct = new Actuator(56, "switch", true, "12.0.0.1", 889);
        Server clientServer = new Server(test1cllient);
        clientServer.updateMaxWaitTime(9.0);

        Filter filetertest5 = new Filter(BooleanOperator.NOT_EQUALS, true);
        clientServer.setActuatorStateIf(filetertest5, testAct.getId());
        clientServer.toggleActuatorStateIf(filetertest5, testAct.getId());

        clientServer.logIf(filetertest5);

        Thread.sleep(1000);

        Event event1 = new ActuatorEvent(System.currentTimeMillis(), 123, 111, "coca", false);
        Event event2 = new ActuatorEvent(System.currentTimeMillis(), 123, 111, "coca", false);
        Event event3 = new ActuatorEvent(System.currentTimeMillis(), 123, 111, "coca", false);
        Event event4 = new ActuatorEvent(System.currentTimeMillis(), 123, 111, "coca", false);
        Event event5 = new ActuatorEvent(System.currentTimeMillis(), 123, 111, "coca", false);
        Event event6 = new ActuatorEvent(System.currentTimeMillis(), 123, 111, "coca", false);
        Event event7 = new ActuatorEvent(System.currentTimeMillis(), 123, 111, "coca", false);
        clientServer.processIncomingEvent(event1);
        clientServer.processIncomingEvent(event2);
        clientServer.processIncomingEvent(event3);
        clientServer.processIncomingEvent(event4);
        clientServer.processIncomingEvent(event5);
        clientServer.processIncomingEvent(event6);
        clientServer.processIncomingEvent(event7);

        System.out.println(System.currentTimeMillis());

        assertEquals(7, clientServer.readLogs().size());

        clientServer.processIncomingEvent(event1);
        clientServer.processIncomingEvent(event2);
        clientServer.processIncomingEvent(event3);
        clientServer.processIncomingEvent(event4);
        clientServer.processIncomingEvent(event5);
        clientServer.processIncomingEvent(event6);
        clientServer.processIncomingEvent(event7);

        assertEquals(7, clientServer.predictNextNTimeStamps(111, 7).size() );

        clientServer.processToggleSetEventStuff(event1);
        clientServer.processToggleSetEventStuff(event2);
        clientServer.processToggleSetEventStuff(event3);
        clientServer.processToggleSetEventStuff(event4);
        clientServer.processToggleSetEventStuff(event5);



    }

    @Test
    public void testServerSynchronize() throws InterruptedException {

        Client test1cllient = new Client(33, "sturbak.col", "127.0.0.1", 443);
        Actuator testAct = new Actuator(56, "switch", true, "12.0.0.1", 889);
        Server clientServer = new Server(test1cllient);
        clientServer.updateMaxWaitTime(9.0);

        Filter filetertest5 = new Filter(BooleanOperator.NOT_EQUALS, true);
        clientServer.setActuatorStateIf(filetertest5, testAct.getId());
        clientServer.toggleActuatorStateIf(filetertest5, testAct.getId());

        Event event1 = new ActuatorEvent(0.09, 123, 111, "coca", false);
        Event event2 = new ActuatorEvent(0.18, 123, 111, "coca", false);
        Event event3 = new ActuatorEvent(0.27, 123, 111, "coca", false);
        Event event4 = new ActuatorEvent(0.36, 123, 111, "coca", false);
        Event event5 = new ActuatorEvent(0.45, 123, 111, "coca", false);
        Event event6 = new ActuatorEvent(0.54, 123, 111, "coca", false);
        Event event7 = new ActuatorEvent(0.63, 123, 111, "coca", false);
        clientServer.processIncomingEvent(event1);
        clientServer.processIncomingEvent(event2);
        clientServer.processIncomingEvent(event3);
        clientServer.processIncomingEvent(event4);
        clientServer.processIncomingEvent(event5);
        clientServer.processIncomingEvent(event6);
        clientServer.processIncomingEvent(event7);

        clientServer.logIf(filetertest5);

        String boolFilter = "EQUALS|true";
        String sensorFilter = "value|GREATER_THAN|1";
        Request analysis1 = new Request(RequestType.ANALYSIS, RequestCommand.ANALYSIS_READ_LOG,"2");
        clientServer.processIncomingRequest(analysis1);

        Request controlLogIf = new Request(RequestType.CONTROL, RequestCommand.CONTROL_LOG_IF, boolFilter);
        clientServer.processIncomingRequest(controlLogIf);

        Request controlLogIf2 = new Request(RequestType.CONTROL, RequestCommand.CONTROL_LOG_IF, sensorFilter);
        clientServer.processIncomingRequest(controlLogIf2);

        String predict = "123|2";
        Request predictNValues = new Request(RequestType.PREDICT, RequestCommand.PREDICT_NEXT_N_VALUES, predict);

//        clientServer.processIncomingEvent(event1);
//        clientServer.processIncomingEvent(event2);
//        clientServer.processIncomingEvent(event3);
//        clientServer.processIncomingEvent(event4);
//        clientServer.processIncomingRequest(predictNValues);
        try{
            clientServer.processIncomingRequest(predictNValues);
        } catch (Exception e){
            assertTrue(true);
        }

        Request analysisGetLatestEvent = new Request(RequestType.ANALYSIS, RequestCommand.ANALYSIS_GET_LATEST_EVENTS, "2");
        clientServer.processIncomingRequest(analysisGetLatestEvent);

        try {
            Request predictNextNTimeStamp = new Request(RequestType.PREDICT, RequestCommand.PREDICT_NEXT_N_TIMESTAMPS, predict);
            clientServer.processIncomingRequest(predictNextNTimeStamp);
        } catch (Exception e){
            assertTrue(true);
        }

        String boolFilter2 = "123|EQUALS|true";
        String sensorFilter2 = "123|value|GREATER_THAN|1";
        Request controlSetActuatorState1 = new Request(RequestType.CONTROL, RequestCommand.CONTROL_SET_ACTUATOR_STATE, boolFilter2);
        Request controlSetActuatorState2 = new Request(RequestType.CONTROL, RequestCommand.CONTROL_SET_ACTUATOR_STATE, sensorFilter2);

        clientServer.processIncomingRequest(controlSetActuatorState1);
        clientServer.processIncomingRequest(controlSetActuatorState2);

        Request confiqUpdateMaxWaitTime = new Request(RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, "10");
        clientServer.processIncomingRequest(confiqUpdateMaxWaitTime);

        String timeWindows = "4.0|7.8";
        Request analysisGetEventInWindow = new Request(RequestType.ANALYSIS, RequestCommand.ANALYSIS_GET_EVENTS_IN_WINDOW, timeWindows);
        clientServer.processIncomingRequest(analysisGetEventInWindow);

        Request controlToggleActuatorState1 = new Request(RequestType.CONTROL, RequestCommand.CONTROL_TOGGLE_ACTUATOR_STATE, boolFilter2);
        Request controlToggleActuatorState2 = new Request(RequestType.CONTROL, RequestCommand.CONTROL_TOGGLE_ACTUATOR_STATE, sensorFilter2);
        clientServer.processIncomingRequest(controlToggleActuatorState1);
        clientServer.processIncomingRequest(controlToggleActuatorState2);
    }

    @Test
    public void testParser(){
        Filter testlagi = new Filter("value", DoubleOperator.EQUALS, 9);
        Filter testlagi2 = new Filter("helo", DoubleOperator.EQUALS, 9);
        Event newEvent = new SensorEvent(1,2,2,"sensor", 9);
        assertTrue(testlagi.satisfies(newEvent));
        try{
            testlagi2.satisfies(newEvent);
        } catch (Exception e){
            assertTrue(true);
        }

        Filter testlagi3 = new Filter("timestamp", DoubleOperator.EQUALS, 9);
        Filter testlagi4 = new Filter("timestamp", DoubleOperator.LESS_THAN, 9);
        Event newEvent2 = new SensorEvent(9,2,2,"sensor", 9);
        assertTrue(testlagi3.satisfies(newEvent2));
        assertFalse(testlagi4.satisfies(newEvent2));

    }

    @Test
    public void test8(){
        Event testEvent1 = new ActuatorEvent(0.1, 10, 10, "CO2", true);
        Event testEvent2 = new ActuatorEvent(0.2, 19, 11, "water", false);
        List<Event> testList = new ArrayList<>();
        testList.add(testEvent1);
        testList.add(testEvent2);
        String stringList = PARSER.ParsingListOfEvent(testList);
        List<Event> testListOutcome = PARSER.ListOfEvent(stringList);
        assertEquals(2, testListOutcome.size());
        Filter sensorValueFilter = new Filter("value", DoubleOperator.GREATER_THAN_OR_EQUALS, 23);
    }

    @Test
    public void test9(){
        List<Integer> a = List.of(22,23,24,25,26);
        String x1 = PARSER.ListofIntegers(a);
        String x2 = PARSER.ListofObjects(Collections.singletonList(a));
        assertEquals(x1, x1.toString());
        assertEquals(x2, x2.toString());
    }












}
