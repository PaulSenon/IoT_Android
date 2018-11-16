package fr.mvproduction.svetxav.todaysprogram;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor proximity;

    private TextView textSensorX;
    private TextView textSensorY;
    private TextView textSensorZ;

    private TextView textMain;

    public MainActivity(){

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

        findViewById(R.id.main_button).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                textMain.setText(R.string.bt_click_message);
            }
        });
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
}
