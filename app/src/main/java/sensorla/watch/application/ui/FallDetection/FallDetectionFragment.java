package sensorla.watch.application.ui.FallDetection;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import sensorla.watch.application.MainActivity;
import sensorla.watch.application.R;
import sensorla.watch.application.ui.Login.Models.UserInfo;
import sensorla.watch.application.ui.Login.SaveSharedPreference;

import static android.content.Context.SENSOR_SERVICE;

public class FallDetectionFragment extends Fragment implements SensorEventListener {

    private DecimalFormat df = new DecimalFormat("#.###");
    private String boundary_key;
    private TextView last_x_textView;
    private TextView last_y_textView;
    private TextView last_z_textView;
    private TextView x_textView;
    private TextView y_textView;
    private TextView z_textView;
    private TextView current_change_amount_textView;
    private TextView current_boundary_textView;

    private Sensor sensor;
    private SensorManager sensorManager;
    private SharedPreferences sharedPreferences;

    private double[] last_gravity = new double[3];
    private double[] gravity = new double[3];
    private boolean firstChange = true;
    private int warningBoundary = 10;
    private double changeAmount = 0;

    final String username = "smartband@sensorla.co";
    final String password = "Sensorla123";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_fall_detection, container, false);
        last_x_textView = root.findViewById(R.id.last_x_value);
        last_y_textView = root.findViewById(R.id.last_y_value);
        last_z_textView = root.findViewById(R.id.last_z_value);
        x_textView = root.findViewById(R.id.x_value);
        y_textView = root.findViewById(R.id.y_value);
        z_textView = root.findViewById(R.id.z_value);
        current_change_amount_textView = root.findViewById(R.id.current_change_amount);
        current_boundary_textView = root.findViewById(R.id.current_boundary);
        initialSensor();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return root;
    }

    private void initialSensor() {
        sensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, sensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        last_gravity[0] = gravity[0];
        last_gravity[1] = gravity[1];
        last_gravity[2] = gravity[2];

        gravity[0] = event.values[0];
        gravity[1] = event.values[1];
        gravity[2] = event.values[2];

        changeAmount = Math.pow((gravity[0] - last_gravity[0]), 2) +
                Math.pow((gravity[1] - last_gravity[1]), 2) +
                Math.pow((gravity[2] - last_gravity[2]), 2);

        updateSensorView();

        warningBoundary = Integer.parseInt(sharedPreferences.getString(boundary_key, "50"));
        if (changeAmount >= warningBoundary) {
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                    .setTitle("Safety Alert")
                    .setMessage("Are you ok?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                new AsyncTask<Void, Void, Void>() {
                                    @Override
                                    protected Void doInBackground(Void... voids) {
                                        eMail();
                                        return null;
                                    }
                                }.execute();
                            } catch (Exception e) {
                                Log.d("error", e.getMessage());
                            }
                        }
                    }).create();
            alertDialog.show();
            MainActivity.applyGoogleFont(getActivity(), alertDialog);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int displayWidth = displayMetrics.widthPixels;
            int displayHeight = displayMetrics.heightPixels;
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(alertDialog.getWindow().getAttributes());

            int dialogWindowWidth = (int) (displayWidth * 1.0f);
            int dialogWindowHeight = (int) (displayHeight * 1.0f);
            layoutParams.width = dialogWindowWidth;
            layoutParams.height = dialogWindowHeight;
            alertDialog.getWindow().setAttributes(layoutParams);
        }
    }

    @Override
    public void onResume() {
        sensorManager.registerListener(this, sensor, sensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
    }

    @Override
    public void onPause() {
        sensorManager.unregisterListener(this);
        super.onPause();
    }

    private void updateSensorView() {
        last_x_textView.setText("Last X = " + last_gravity[0]);
        last_y_textView.setText("Last Y = " + last_gravity[1]);
        last_z_textView.setText("Last Z = " + last_gravity[2]);
        x_textView.setText("X = " + gravity[0]);
        y_textView.setText("Y = " + gravity[1]);
        z_textView.setText("Z = " + gravity[2]);
        current_change_amount_textView.setText("Current change amount = " + df.format(changeAmount));
        current_boundary_textView.setText("Current boundary = " + warningBoundary);
    }


    public void eMail() {
        UserInfo userInfo = SaveSharedPreference.getUserInfo(getActivity());
        //String userName = userInfo.getUserName();

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("smartband@sensorla.co"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse("smartband@sensorla.co"));
            message.setSubject("Safety Alert!");
            message.setText("Dear Supervisor,"
                    + "\n\n This the fall detection alert."
                    + "\n\n Attendant:"
                    + "\n\n Heart Rate: ");

            Transport.send(message);

            System.out.println("Done");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

}