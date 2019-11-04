package sensorla.watch.application;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Map;
import java.util.regex.Pattern;

import sensorla.watch.application.SqliteDatabase.DbHelper;
import sensorla.watch.application.SqliteDatabase.WorkOrder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sensorla.watch.application.Service.ApiService;
import sensorla.watch.application.Service.ServiceGenerator.ServiceGenerator;
import sensorla.watch.application.ui.Login.Models.UserInfo;
import sensorla.watch.application.ui.Login.SaveSharedPreference;
import sensorla.watch.application.ui.Tracking.TrackService;

import static android.content.ContentValues.TAG;

public class MyFirebaseInstanceIDService extends FirebaseMessagingService {

    private LocalBroadcastManager broadcaster;
    private DbHelper dbHelper;

    @Override
    public void onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this);
        dbHelper = new DbHelper(this);
    }

    @Override
    public void onNewToken(String mtoken) {
        super.onNewToken(mtoken);
        Log.e("TOKEN---",mtoken);
    }

    //FCM noti onMessageReceived
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        String messageBody = data.get("body");
        String[] resultList = messageBody.split(Pattern.quote("*"));

        String type = "", device_id = "", user_id = "", userName = "";
        if (resultList[0].equals("connect") || resultList[0].equals("disconnect")) {
            Constants.resultList = resultList;

            int i = 0;
            for (String s : Constants.resultList) {
                i++;
                if (i == 1) {
                    type = s;
                    Log.d(TAG, "TYPE:" + type);
                } else if (i == 2) {
                    device_id = s;
                    Log.d(TAG, "deviceID:" + device_id);
                } else if (i == 3) {
                    user_id = s;
                    Log.d(TAG, "USERID:" + user_id);
                } else if (i == 4) {
                    userName = s;
                    Log.d(TAG, "USERNAME:" + userName);
                    Constants.CONNECT_USER_MSG = userName + " has been requested to login to this device.";
                }
            }
        }
        final String serverName = SaveSharedPreference.getEnvironment(getApplicationContext());

        switch (resultList[0]) {
            case "connect":
                DisplayConnectView(device_id, user_id, type, serverName, userName);
                break;
            case "disconnect":
                DisConnectView();
                break;
            case "Ping user location":
                PingUserLocation(resultList);
                break;
            default:
                Log.d(TAG, "TYPE: " + messageBody);
                for (String res : resultList) {
                    Log.d(TAG, "DEBUG >>>>> " + res);
                }
                ReceiveWorkOrder(resultList);
                break;
        }
        //showNotification(remoteMessage.getData().get("message"));
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getData());
    }

    //create service
    final ApiService service= ServiceGenerator.createService(ApiService.class);

    private void PingUserLocation(String[] resultList) {
        vibrate();
        Intent intent = new Intent(MyFirebaseInstanceIDService.this, TrackService.class);
        startService(intent);
        Log.d(TAG, "Service Start " );
        stopService(intent);
        Log.d(TAG, "Service Stop " );
    }

    private void DisplayConnectView(String deviceId, String userId,String type,String env,String userName) {
        vibrate();
        if (!Helper.isOpened(getApplicationContext(), "sensorla.watch.application")) {

            Intent main = new Intent(getApplicationContext(), MainActivity.class);
            main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            main.putExtra("messageType", "LogIn");
            main.putExtra("type", type);
            main.putExtra("userId", userId);
            main.putExtra("deviceId", deviceId);
            main.putExtra("env", env);
            main.putExtra("userName", userName);

            startActivity(main);
        }
        else {
            Intent intent = new Intent("FirebaseMessage");
            intent.putExtra("messageType", "LogIn");
            intent.putExtra("type", type);
            intent.putExtra("userId", userId);
            intent.putExtra("deviceId", deviceId);
            intent.putExtra("env", env);
            intent.putExtra("userName", userName);

            SaveSharedPreference.saveUserInfo(getApplicationContext(), new UserInfo(userId, userName, deviceId));

            broadcaster.sendBroadcast(intent);
        }
    }

    private void DisConnectView() {
        vibrate();
        SaveSharedPreference.setLoggedIn(getApplicationContext(), false);
        UserInfo userInfo = SaveSharedPreference.getUserInfo(getApplicationContext());
        final String serverName = SaveSharedPreference.getEnvironment(getApplicationContext());
        //call api
        final Call<String> apiCall = service.Disconnect(
                userInfo.getDeviceId(),
                userInfo.getUserId(),
                "disconnect",
                serverName
        );
        apiCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful() && response.body().contains("Success")) {
                    Toast.makeText(getApplicationContext(), response.body(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent("FirebaseMessage");
                    intent.putExtra("messageType", "LogOut");
                    broadcaster.sendBroadcast(intent);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    public void ReceiveWorkOrder(String[] resultList){
        String priority = "false";
        if(resultList.length == 6) {
            priority = resultList[4];
        }
        WorkOrder wo = new WorkOrder();
        wo.work_order_id = resultList[0];
        wo.work_order_name = resultList[1];
        wo.work_order_location_name = resultList[2];
        wo.work_order_instruction = resultList[3];
        wo.work_order_snooze = false;
        wo.work_order_priority = priority;
        wo.work_order_status = "reported";
        //wo.snooze_datetime = currentdateTime;
        //wo.work_order_type = resultList[5];

        String user_id = SaveSharedPreference.getUserInfo(this).getUserId();

        dbHelper.InsertWorkOrder(
                wo.work_order_id,
                wo.work_order_name,
                wo.work_order_location_name,
                wo.work_order_instruction,
                wo.work_order_snooze.toString(),
                wo.work_order_priority,
                wo.work_order_status,
                user_id
        );

        Intent intent = new Intent("FirebaseMessage");
        intent.putExtra("messageType", "dbUpdated");
        broadcaster.sendBroadcast(intent);
        vibrate();
    }

    public void vibrate(){
        final MediaPlayer mp = MediaPlayer.create(this,R.raw.app_alert_tone_ringtone_003 );
        mp.start();
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 1 seconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
           v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
           v.vibrate(1000);
        }
    }
}
