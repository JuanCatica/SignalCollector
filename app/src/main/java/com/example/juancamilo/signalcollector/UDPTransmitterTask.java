package com.example.juancamilo.signalcollector;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.Queue;

public class UDPTransmitterTask extends AsyncTask<Void, Void, Void> {

    public static final boolean ACTIVED_STATE = true;
    public static final boolean KILLED_STATE = false;

    private DatagramSocket socket;
    private DatagramPacket packet;
    private InetAddress IP;
    private int ServerPort;

    private Queue messageBuffer;
    private boolean state;
    private Context appContext;

    public UDPTransmitterTask(Context nAppContext, InetAddress nIP, int nPORT) throws Exception {
        IP = nIP;
        ServerPort = nPORT;
        messageBuffer = new LinkedList();
        socket = new DatagramSocket();
        state = ACTIVED_STATE;
        appContext = nAppContext;
    }

    private void sendMessageStr(Message message) throws IOException {
        String messageStr = message.getMessageStr();
        int msg_length = messageStr.length();
        byte[] byte_message = messageStr.getBytes();
        packet = new DatagramPacket(byte_message, msg_length, IP, ServerPort);
        socket.send(packet);
    }

    public void enqueueMessage(Message message){
        messageBuffer.add(message);
    }

    public void kill(){
        state = KILLED_STATE;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //this method will be running on UI thread
        Toast.makeText(appContext,"Hilo de comunicación Inicializado",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        while(state){
            try {
                while(messageBuffer.size()>0 && state){
                    Message message = (Message) messageBuffer.poll();
                    sendMessageStr(message);
                }
            }catch (Exception e) {

            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        //this method will be running on UI thread
        Toast.makeText(appContext,"Hilo de comunicación Finalizado",Toast.LENGTH_SHORT).show();
    }
}
