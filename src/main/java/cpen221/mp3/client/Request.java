package cpen221.mp3.client;

import java.text.DecimalFormat;

public class Request {

    /**
     * timeStamp : double more than 0.0, representation the timeStamp of the request are sent
     * requestType : requestType (enum), nonnull. Representing the assigned request type
     * requestCommand : requestCommand (enum), representing the command
     * requestData : string non-null, representing the data.
     */

    private final double timeStamp;
    private final RequestType requestType;
    private final RequestCommand requestCommand;
    private final String requestData;

    public Request(RequestType requestType, RequestCommand requestCommand, String requestData) {
        double timestamping = System.currentTimeMillis();
        String timing = String.valueOf(timestamping);
        this.timeStamp = Double.parseDouble(timing.substring(0, timing.length() - 4));
        this.requestType = requestType;
        this.requestCommand = requestCommand;
        this.requestData = requestData;
        if(requestType.equals(RequestType.CONFIG)){
            if(!(requestCommand.equals(RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME))){
                throw new IllegalArgumentException("Invalid request Command");
            }
        }
        else if(requestType.equals(RequestType.PREDICT)){
            if(!(requestCommand.equals(RequestCommand.PREDICT_NEXT_N_VALUES) ||
                    requestCommand.equals(RequestCommand.PREDICT_NEXT_N_TIMESTAMPS))){
                throw new IllegalArgumentException("Invalid request Command");
            }
        }
        else if(requestType.equals(RequestType.CONTROL)){
            if(!(requestCommand.equals(RequestCommand.CONTROL_LOG_IF) ||
                    requestCommand.equals(RequestCommand.CONTROL_SET_ACTUATOR_STATE) ||
                    requestCommand.equals(RequestCommand.CONTROL_TOGGLE_ACTUATOR_STATE))){
                throw new IllegalArgumentException("Invalid request");
            }
        }
        else{
            if(!(requestCommand.equals(RequestCommand.ANALYSIS_GET_MOST_ACTIVE_ENTITY) ||
                    requestCommand.equals(RequestCommand.ANALYSIS_GET_LATEST_EVENTS) ||
                    requestCommand.equals(RequestCommand.ANALYSIS_GET_ALL_ENTITIES) ||
                    requestCommand.equals(RequestCommand.ANALYSIS_GET_EVENTS_IN_WINDOW) ||
                    requestCommand.equals(RequestCommand.ANALYSIS_READ_LOG))){
                throw new IllegalArgumentException("Invalid request");
            }
        }
    }

    public double getTimeStamp() {
        return timeStamp;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public RequestCommand getRequestCommand() {
        return requestCommand;
    }

    public String getRequestData() {
        return requestData;
    }

    @Override
    public String toString() {
        return "Request{" +
                "timeStamp=" +  this.getTimeStamp() +
                ",RequestType=" + this.getRequestType() +
                ",RequestCommand=" + this.getRequestCommand() +
                ",Requestdata=" + this.getRequestData();
    }



}