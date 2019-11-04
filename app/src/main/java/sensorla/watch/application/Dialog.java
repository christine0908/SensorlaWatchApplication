package sensorla.watch.application;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

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

public class Dialog extends AppCompatActivity {
    final String username = "smartband@sensorla.co";
    final String password = "Sensorla123";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alert();

    }
    public void alert(){
        AlertDialog alertDialog = new AlertDialog.Builder(Dialog.this)
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
        finish();
        MainActivity.applyGoogleFont(this, alertDialog);
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