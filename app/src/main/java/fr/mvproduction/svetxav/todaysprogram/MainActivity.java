package fr.mvproduction.svetxav.todaysprogram;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor proximity;

    private TextView textSensorX;
    private TextView textSensorY;
    private TextView textSensorZ;
    private TextView textMain;

    private EditText textInput;
    private String message;

    private static final String IP = "192.168.2.148";
    private static final int PORT = 8081;
    private InetAddress address;
    private DatagramSocket UDPSocket;

    public MainActivity(){
        this.message="";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        this.accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        this.textMain = findViewById(R.id.main_text);
        this.textSensorX = findViewById(R.id.text_sensor_output_x);
        this.textSensorY = findViewById(R.id.text_sensor_output_y);
        this.textSensorZ = findViewById(R.id.text_sensor_output_z);

        this.textInput = findViewById(R.id.main_text_input);

        findViewById(R.id.main_button).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                textMain.setText(R.string.bt_click_message);
                message = textInput.getText().toString();
                sendNetworkMessage(message);
            }
        });
    }

    protected void sendNetworkMessage(final String str){
        (new Thread(){
            public void run(){
                try {
                    byte[] data = str.getBytes();
                    DatagramPacket packet = new DatagramPacket(data, data.length, address, PORT);
                    UDPSocket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        this.sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        this.sensorManager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_UI);

        // init network
        try {
            this.UDPSocket = new DatagramSocket();
            this.address = InetAddress.getByName(IP);

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values;
        if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            this.textSensorX.setText(values[0]+"");
            this.textSensorY.setText(values[1]+"");
            this.textSensorZ.setText(values[2]+"");
        }else{
            this.textMain.setText(values[0]==0?"ALLO ?!":"oui bonjour");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

//    private class ReceiverTask extends AsyncTask<Void byte[], Void>{
//
//    }
}
