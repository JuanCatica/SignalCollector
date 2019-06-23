package com.example.juancamilo.signalcollector;

public class Message {
    private String message;
    private int bufferSize;
    private int counter;
    private int bodyPos;

    private String[] buffer;

    public Message(int bufferSize, int bodyPos){
        this.bufferSize = bufferSize;
        this.bodyPos = bodyPos;
        counter = 0;
        message = new String();
        buffer= new String[bufferSize];
    }

    public void addValues(int SensorID, double X, double Y, double Z){
        if (isFree()){
            buffer[SensorID] = ","+X+","+Y+","+Z;
            counter ++;
        }
    }

    public boolean isFree(){
        return counter < bufferSize;
    }

    public String getMessageStr(){
        message = ""+bodyPos;
        for (int i = 0; i < buffer.length; i++) {
            message += buffer[i];
        }
        return message;
    }
}
