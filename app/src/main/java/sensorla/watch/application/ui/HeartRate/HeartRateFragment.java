package sensorla.watch.application.ui.HeartRate;

import android.app.Service;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sensorla.watch.application.Constants;
import sensorla.watch.application.Model.HeartRateModel;
import sensorla.watch.application.R;
import sensorla.watch.application.Service.ApiService;
import sensorla.watch.application.Service.ServiceGenerator.ServiceGenerator;
import sensorla.watch.application.SqliteDatabase.HeartRate_DBHelper;
import sensorla.watch.application.ui.Login.SaveSharedPreference;

public class HeartRateFragment extends Fragment implements SensorEventListener {
        private TextView mTextView;
        private Button btnStart;
        private Button btnPause;
        private SensorManager mSensorManager;
        private Sensor mHeartRateSensor;
        public int mHeartRate;
        public float mHeartRateFloat;

    private long lastRecordTime = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_heart_rate, container, false);
        btnStart = root.findViewById(R.id.btnStart);
        btnPause = root.findViewById(R.id.btnPause);
        mTextView=root.findViewById(R.id.heartRateText);



        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnStart.setVisibility(ImageButton.GONE);
                btnPause.setVisibility(ImageButton.VISIBLE);
                mTextView.setText("Please wait...");
                startMeasure();
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPause.setVisibility(ImageButton.GONE);
                btnStart.setVisibility(ImageButton.VISIBLE);
                mTextView.setText("--");
                stopMeasure();
//                serviceApiCall();
            }
        });
        mSensorManager = (SensorManager) getActivity().getSystemService(Service.SENSOR_SERVICE);
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        btnStart.performClick();
        return root;
    }

    private void serviceApiCall()
    {
        final String userId = String.valueOf(SaveSharedPreference.getUser_id(getActivity()));
        final String serverName = SaveSharedPreference.getEnvironment(getActivity());
        //create service
        final ApiService service = ServiceGenerator.createHeartRateService(ApiService.class);

        final Call<String> apiCall = service.uploadOneHeartRateData(userId,Integer.toString(mHeartRate),now(),serverName);
        apiCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful() && response.body().contains("Success")){
                    Toast.makeText(getActivity().getApplicationContext(), response.body(), Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getActivity().getApplicationContext(), response.body(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(getActivity().getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



//    private void newApiCall()
//    {
//        final String userId = String.valueOf(SaveSharedPreference.getUser_id(getActivity()));
//        final String serverName = SaveSharedPreference.getEnvironment(getActivity());
//        //create service
//        // change "createHeartRateService" as per new service created in ServiceGenerator class
//        final ApiService service = ServiceGenerator.createNewService(ApiService.class);
//
//        // this is preparing the object to send to API
//        // change "HeartRate" to your new method created in ApiService and pass corresponding data for POST method over here
//        HeartRateModel model = new HeartRateModel();
//        model.setUserId(userId);
//        model.setServerName(serverName);
//        model.setHeartRate(mHeartRate);
//        // this line is requesting the API / calling the API
//        final Call<String> apiCall = service.NewHeartRateApi(model);
//// this is to get result from API
//        apiCall.enqueue(new Callback<String>() {
//            @Override
//            public void onResponse(Call<String> call, Response<String> response) {
//                if(response.isSuccessful() && response.body() != null && response.body().contains("Success")){
//                    Toast.makeText(getActivity().getApplicationContext(), response.body(), Toast.LENGTH_SHORT).show();
//                }
//                else{
//                    Toast.makeText(getActivity().getApplicationContext(), response.body(), Toast.LENGTH_SHORT).show();
//                }
//            }
//            @Override
//            public void onFailure(Call<String> call, Throwable t) {
//                Toast.makeText(getActivity().getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }


    private void startMeasure() {
      boolean sensorRegistered = mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        Log.d("Sensor Status:", " Sensor registered: " + (sensorRegistered ? "yes" : "no"));
    }
    private void stopMeasure() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(event.sensor.getType()==Sensor.TYPE_HEART_RATE){
            long waitTime = 1000 * 60 * 5;
            long currentTime = System.currentTimeMillis();
            if((currentTime - lastRecordTime) > waitTime) {
                lastRecordTime = currentTime;

                mHeartRateFloat = event.values[0];

                mHeartRate = Math.round(mHeartRateFloat);

                mTextView.setText(Integer.toString(mHeartRate));

                Log.d("New Heart Rate :", mTextView.getText().toString());

                if(mHeartRate > Constants.TRESHOLD_LIMIT) {
//                    newApiCall();
                    uploadOneHeartRateData();
                    Toast.makeText(getActivity(), "Reached Threshold", Toast.LENGTH_SHORT).show();
                }
                else{
                    String userId = String.valueOf(SaveSharedPreference.getUser_id(getActivity()));
                    HeartRate_DBHelper db = new HeartRate_DBHelper(getActivity());
                    db.InsertHeartRate(mHeartRate + "", userId);
                }

            }
        }
    }

    private String now() {
        Date date = new Date();
        return fromDate(date);
    }
    public String fromDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return dateFormat.format(date);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    @Override
    public void onResume() {
        super.onResume();
        //Print all the sensors
        if (mHeartRateSensor == null) {
            List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
            for (Sensor sensor1 : sensors) {
                Log.i("Sensor list", sensor1.getName() + ": " + sensor1.getType());
            }
        }
    }
    private void uploadOneHeartRateData()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String date = dateFormat.format(new Date());

        final int userId = SaveSharedPreference.getUser_id(getActivity());
        String userId1 = String.valueOf(userId);
        final String serverName = SaveSharedPreference.getEnvironment(getActivity());
        //create service
        final ApiService service = ServiceGenerator.createHeartRateService(ApiService.class);


        final Call<String> apiCall = service.uploadOneHeartRateData(userId1,String.valueOf(mHeartRate),date,serverName);
        apiCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                // this shows api is working.
                if(response.isSuccessful() && response.body().contains("Success")){
                    Toast.makeText(getActivity(), response.body(), Toast.LENGTH_SHORT).show();

                }
                else{
                    Toast.makeText(getActivity(), response.body(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}

