package sensorla.watch.application.BackgroundService;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;
import androidx.annotation.Nullable;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import sensorla.watch.application.ui.Login.Models.UserInfo;
import sensorla.watch.application.ui.Login.SaveSharedPreference;

public class BackgroundService extends Service implements SensorEventListener {
    private Sensor sensor;
    private SensorManager sensorManager;
    private SharedPreferences sharedPreferences;
    private String boundary_key;

    private double[] last_gravity = new double[3];
    private double[] gravity = new double[3];
    private boolean firstChange = true;
    private int warningBoundary = 10;
    private double changeAmount = 0;

    final String username = "smartband@sensorla.co";
    final String password = "Sensorla123";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Start Detecting...", Toast.LENGTH_LONG).show();
        initialSensor();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service destroyed by user.", Toast.LENGTH_LONG).show();
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

        warningBoundary = Integer.parseInt(sharedPreferences.getString(boundary_key, "30"));
        if (changeAmount >= warningBoundary) {
            Toast.makeText(getApplicationContext(),"Are you ok?",Toast.LENGTH_LONG).show();
//            Intent intent = new Intent(BackgroundService.this,Dialog.class);
//            startActivity(intent);
        }
    }

    private void initialSensor() {
        sensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, sensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void eMail() {
        UserInfo userInfo = SaveSharedPreference.getUserInfo(getApplicationContext());
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
                    + "\n\n Heart Rate: 133 bpm");

            Transport.send(message);

            System.out.println("Done");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}