package fr.mvproduction.svetxav.todaysprogram;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor proximity;

    private TextView textSensorX;
    private TextView textSensorY;
    private TextView textSensorZ;
    private TextView textMain;
    private ImageView feedBackSit;
    private Switch switchPlayer;

    private EditText textInput;
    private String message;

    private static final String IP = "192.168.2.208";
    private static final int PORT = 10000;
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
        this.feedBackSit = findViewById(R.id.green_feedback);
        this.feedBackSit.setImageResource(R.color.feedback_default);
        this.switchPlayer = findViewById(R.id.switch_player);

        this.textInput = findViewById(R.id.main_text_input);

        findViewById(R.id.main_button).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                textMain.setText(R.string.bt_click_message);
                message = textInput.getText().toString();
                sendNetworkMessage(message);
                vibrate();
            }
        });

//        (new MessageReceiver()).execute();
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
        this.sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        this.sensorManager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_UI);

        // init network
        try {
            this.UDPSocket = new DatagramSocket(PORT);
            this.address = InetAddress.getByName(IP);

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }


    private static final int SHAKE_THRESHOLD = 800;
    float last_x;
    float last_y;
    float last_z;
    float x;
    float y;
    float z;
    long lastUpdate;
    long diffTime;


    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values;
        if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            this.textSensorX.setText(values[0]+"");
            this.textSensorY.setText(values[1]+"");
            this.textSensorZ.setText(values[2]+"");

            long curTime = System.currentTimeMillis();
            // only allow one update every 100ms.
            if ((curTime - lastUpdate) > 100) {
                diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                x = values[SensorManager.DATA_X];
                y = values[SensorManager.DATA_Y];
                z = values[SensorManager.DATA_Z];

                float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    this.feedBackSit.setImageResource(R.color.feedback_accent);
                    sendNetworkMessage(this.switchPlayer.isChecked()?"(2)":"(1)");
//                    Log.d("sensor", "shake detected w/ speed: " + speed);
//                    Toast.makeText(this, "shake detected w/ speed: " + speed, Toast.LENGTH_SHORT).show();
                }else{
                    this.feedBackSit.setImageResource(R.color.feedback_default);
                }
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }else{
            this.textMain.setText(values[0]==0?"ALLO ?!":"oui bonjour");
        }
    }

    private void vibrate(){
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(1000);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

//    private class MessageReceiver extends AsyncTask<Void, byte[], Void>{
//        protected Void doInBackground(Void... rien){
//            while(true){
//                byte[] data = new byte[1024];
//                DatagramPacket packet = new DatagramPacket(data, data.length);
//                try {
//                    UDPSocket.receive(packet);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                int size = packet.getLength();
//                publishProgress(java.util.Arrays.copyOf(data, size));
//            }
//        }
//
//        protected void onProgressUpdate(byte[]... data){
//            System.out.println("LOL BONJOUR DEBUG : "+data);
////            String message = new String(data[0], StandardCharsets.UTF_8);
//            if(data[0].equals("(1)")){
//                vibrate();
//            }else if(data[0].equals("(0)")){
//
//            }
//        }
//    }


//    private class ReceiverTask extends AsyncTask<Void byte[], Void>{
//
//    }
}
