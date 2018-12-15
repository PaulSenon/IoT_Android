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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    private TextView textTemp;
    private TextView textHum;
    private TextView textlum;

    private EditText textInput;
    private EditText ipInput;
    private EditText portInput;
    private String message;

    String ip;
    int port;

    private static final String IP = "192.168.2.208";
    private static final int PORT = 3000;
    private static final int SALT = 1567464;
    private InetAddress address;
    private DatagramSocket UDPSocket;

    public MainActivity(){
        this.message="";
        this.ip = IP;
        this.port = PORT;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.textTemp = findViewById(R.id.text_temperature);
        this.textHum = findViewById(R.id.text_humidity);
        this.textlum = findViewById(R.id.text_luminosity);

        this.textInput = findViewById(R.id.main_text_input);
        this.ipInput = findViewById(R.id.text_input_ip);
        this.ipInput.setText(this.ip);
        this.portInput = findViewById(R.id.text_input_port);
        this.portInput.setText(this.port+"");

        findViewById(R.id.main_button).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                message = textInput.getText().toString();
                sendNetworkMessage(message);
            }
        });

        findViewById(R.id.button_valid_host).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ip = ipInput.getText().toString();
                port = Integer.parseInt(portInput.getText().toString());
                initNetwork(ip, port);
            }
        });

    }

    protected void sendNetworkMessage(final String str){
        (new Thread(){
            public void run(){
                try {
                    byte[] data = str.getBytes();
                    DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
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
    }

    @Override
    protected void onResume() {
        super.onResume();

        // init network
        this.initNetwork(ip, port);
    }

    private void initNetwork(String ip, int port){
        try {
            this.UDPSocket = new DatagramSocket(port);
            this.address = InetAddress.getByName(ip);
            (new MessageReceiver()).execute();

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void notif(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private class MessageReceiver extends AsyncTask<Void, byte[], Void>{
        protected Void doInBackground(Void... rien){
            while(true){
                byte[] data = new byte[1024];
                DatagramPacket packet = new DatagramPacket(data, data.length);
                try {
                    UDPSocket.receive(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int size = packet.getLength();
                publishProgress(java.util.Arrays.copyOf(data, size));
            }
        }

        protected void onProgressUpdate(byte[]... data){
            String message = new String(data[0], StandardCharsets.UTF_8);
            System.out.println("LOL BONJOUR DEBUG : "+message);
            JSONObject jsonData = null;
            try {
                // TODO dégueulasse
                jsonData = new JSONObject(message);
                int temp = Integer.parseInt(jsonData.get("temperature").toString());
                int hum = Integer.parseInt(jsonData.get("humidity").toString());
                int lum = Integer.parseInt(jsonData.get("luminosity").toString());
                textTemp.setText("temp : "+(temp/10)+","+(temp%10)+"°C");
                textHum.setText("hum : "+(hum/10)+","+(hum%10)+"%");
                textlum.setText("hum : "+lum+" lux");
            } catch (JSONException e) {
                notif("invalid response");
                e.printStackTrace();
            }
        }
    }

}
