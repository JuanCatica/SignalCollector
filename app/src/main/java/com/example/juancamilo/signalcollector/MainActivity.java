package com.example.juancamilo.signalcollector;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;

import java.net.InetAddress;
import java.net.UnknownHostException;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity extends AppCompatActivity implements SensorEventListener,
        View.OnClickListener, ZXingScannerView.ResultHandler {

    private static final String TAG = "MainActivity";
    public static final int ACCELEROMETER_ID = 0;
    public static final int GYROSCOPE_ID = 1;
    public static final int MAGNETOMETER_ID = 2;
    public static final int SENS_COUNT = 3;

    public static final int posR1 = 0;
    public static final int posR2 = 1;
    public static final int posL1 = 2;
    public static final int posL2 = 3;
    public static final int posNull = -1;
    public static final int PORT = 6565;
    
    private SensorManager sensorManager;
    private Sensor acelerometro;
    private Sensor giroscopio;
    private Sensor magnetometro;

    private TextView AX, AY, AZ, GX, GY, GZ, MX, MY, MZ;
    private Button START, STOP;
    private ImageView QR;
    private RadioButton R1, R2, L1, L2;
    private TextView IP_PORT, RATE_VIEW, STATUS;

    private Context appContext;

    private UDPTransmitterTask UDPTransmitter;
    private Message mensaje;
    private InetAddress IP;
    private int ServerPort;
    private int RATE;

    private ZXingScannerView mScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AX = findViewById(R.id.ax);
        AY = findViewById(R.id.ay);
        AZ = findViewById(R.id.az);

        GX = findViewById(R.id.gx);
        GY = findViewById(R.id.gy);
        GZ = findViewById(R.id.gz);

        MX = findViewById(R.id.mx);
        MY = findViewById(R.id.my);
        MZ = findViewById(R.id.mz);

        START = findViewById(R.id.start);
        START.setOnClickListener(this);

        STOP = findViewById(R.id.stop);
        STOP.setOnClickListener(this);

        QR = findViewById(R.id.qr);
        QR.setOnClickListener(this);

        R1 = findViewById(R.id.r1);
        R2 = findViewById(R.id.r2);
        L1 = findViewById(R.id.l1);
        L2 = findViewById(R.id.l2);


        IP_PORT = findViewById(R.id.ip_port);
        RATE_VIEW = findViewById(R.id.rate);
        STATUS = findViewById(R.id.socket_status);


        UDPTransmitter = null;
        appContext = getApplicationContext();

        try {
            IP = InetAddress.getByName("192.168.0.255");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        ServerPort = PORT;
        RATE = SensorManager.SENSOR_DELAY_GAME;

        Bundle extras = getIntent().getExtras();
        if(extras!=null) {
            try {
                String newString = extras.getString("URL");
                Toast.makeText(getApplicationContext(), newString, Toast.LENGTH_LONG).show();

                if (newString != null) {
                    String[] ip_port = newString.split(":");
                    IP = InetAddress.getByName(ip_port[0]);
                    ServerPort = Integer.parseInt(ip_port[1]);
                    int pos = Integer.parseInt(ip_port[2]);
                    setPosition(pos);
                    setViewsText();
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

        }else{
            R1.setChecked(true);
        }

        Log.d(TAG, "OnCreate: Inicializando Sensor Service");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        giroscopio = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magnetometro = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(MainActivity.this, acelerometro, RATE);
        sensorManager.registerListener(MainActivity.this, giroscopio, RATE);
        sensorManager.registerListener(MainActivity.this, magnetometro, RATE);
        Log.d(TAG, "OnCreate: Aceletometro Registrado (Listener)");

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // here, Permission is not granted
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 50);
        }
        mScannerView = new ZXingScannerView(this);
    }

    private String getShortNumber(double num){
        return ""+(((int)(num*10000))/(10000.0));
    }

    private int setSensorIDAndUpdate(SensorEvent sensorEvent, double x, double y, double z){
        switch (sensorEvent.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                AX.setText(String.format("X : %s", getShortNumber(x)));
                AY.setText(String.format("Y : %s", getShortNumber(y)));
                AZ.setText(String.format("Z : %s", getShortNumber(z)));
                return ACCELEROMETER_ID;

            case Sensor.TYPE_GYROSCOPE:
                GX.setText(String.format("X : %s", getShortNumber(x)));
                GY.setText(String.format("Y : %s", getShortNumber(y)));
                GZ.setText(String.format("Z : %s", getShortNumber(z)));
                return GYROSCOPE_ID;

            case Sensor.TYPE_MAGNETIC_FIELD:
                MX.setText(String.format("X : %s", getShortNumber(x)));
                MY.setText(String.format("Y : %s", getShortNumber(y)));
                MZ.setText(String.format("Z : %s", getShortNumber(z)));
                return MAGNETOMETER_ID;
            default:
                return -1;
        }
    }

    private int getPositionID(){
        if(R1.isChecked())
            return posR1;
        if(R2.isChecked())
            return posR2;
        if(L1.isChecked())
            return posL1;
        if(L2.isChecked())
            return posL2;
        return posNull;
    }

    public void setPosition(int pos){
        R1.setChecked(false);
        R2.setChecked(false);
        L1.setChecked(false);
        L2.setChecked(false);

        switch (pos){
            case posR1:
                R1.setChecked(true);
                break;
            case posR2:
                R2.setChecked(true);
                break;
            case posL1:
                L1.setChecked(true);
                break;
            case posL2:
                L2.setChecked(true);
                break;
        }
    }

    private void enableViews(boolean bool){
        R1.setEnabled(bool);
        R2.setEnabled(bool);
        L1.setEnabled(bool);
        L2.setEnabled(bool);
        START.setEnabled(bool);
        QR.setEnabled(bool);
    }

    private void setViewsText(){
        IP_PORT.setText(String.format("URL  %s:%d ", IP.getHostAddress(), ServerPort));
        String sRate = "";
        switch (RATE){
            case SensorManager.SENSOR_DELAY_FASTEST:
                sRate = "DELAY FASTEST (?Hz)";
                break;
            case SensorManager.SENSOR_DELAY_GAME:
                sRate = "DELAY GAME (50Hz)";
                break;
            case SensorManager.SENSOR_DELAY_UI:
                sRate = "DELAY UI (16.6Hz)";
                break;
            case SensorManager.SENSOR_DELAY_NORMAL:
                sRate = "DELAY NORMAL (5Hz)";
                break;
        }
        RATE_VIEW.setText(String.format("RATE : %s", sRate));
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double x = sensorEvent.values[0];
        double y = sensorEvent.values[1];
        double z = sensorEvent.values[2];
        int sensorID = setSensorIDAndUpdate(sensorEvent,x,y,z);

        if(UDPTransmitter != null){
            if (!mensaje.isFree()){
                UDPTransmitter.enqueueMessage(mensaje);
                mensaje = new Message(SENS_COUNT,getPositionID());
            }
            mensaje.addValues(sensorID,x,y,z);

            STATUS.setText("Sending...");
            STATUS.setTextColor(Color.GREEN);
        }else{
            STATUS.setText("No Active Socket");
            STATUS.setTextColor(Color.GRAY);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onResume() {
        super.onResume();
        // Register ourselves as a handler for scan results.
        mScannerView.setResultHandler(this);
        // Start camera on resume
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop camera on pause
        mScannerView.stopCamera();
    }


    @Override
    public void onClick(View view) {
        if(STOP == view){
            if(UDPTransmitter != null)
                UDPTransmitter.kill();
            UDPTransmitter = null;
            enableViews(true);
        }else if(START == view){
            enableViews(false);
            try {
                mensaje = new Message(SENS_COUNT,getPositionID());
                UDPTransmitter = new UDPTransmitterTask(appContext, IP, ServerPort);
                UDPTransmitter.execute();
            } catch (UnknownHostException uhe){
                uhe.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(QR == view){
            try {
                setContentView(mScannerView);
            } catch (Exception e) {

            }
        }
    }

    @Override
    public void handleResult(Result result) {
        String res = result.getText();
        Intent i = new Intent(this,MainActivity.class);
        sensorManager.unregisterListener(MainActivity.this, acelerometro);
        sensorManager.unregisterListener(MainActivity.this, giroscopio);
        sensorManager.unregisterListener(MainActivity.this, magnetometro);
        i.putExtra("URL",res+":"+getPositionID());
        setResult(RESULT_OK, i);
        startActivity(i);
        this.finish();
    }
}
