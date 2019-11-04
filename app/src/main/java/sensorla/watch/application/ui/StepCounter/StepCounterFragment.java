package sensorla.watch.application.ui.StepCounter;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import sensorla.watch.application.R;

public class StepCounterFragment extends Fragment implements SensorEventListener {
    TextView tv_steps;
    SensorManager sensorManager;
    Sensor sensor;
    boolean running = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_step_counter, container, false);
        tv_steps = root.findViewById ( R.id.tv_steps );

        sensorManager = (SensorManager) getActivity().getSystemService ( Context.SENSOR_SERVICE);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        running = true;
        Sensor countSensor = sensorManager.getDefaultSensor ( sensor.TYPE_STEP_COUNTER );
        if(countSensor!= null){
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }else {
            Toast.makeText ( getActivity(),"SENSOR NOT FOUND", Toast.LENGTH_SHORT ).show ();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        running = false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (running){
            tv_steps.setText ( String.valueOf ( event.values[0] ) );
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}
