package sensorla.watch.application;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.Strings;
import com.google.android.material.navigation.NavigationView;

import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import sensorla.watch.application.Service.ApiService;
import sensorla.watch.application.Service.ServiceGenerator.ServiceGenerator;
import sensorla.watch.application.SqliteDatabase.DbHelper;

import sensorla.watch.application.ui.DeviceID.DeviceIDFragment;
import sensorla.watch.application.ui.HeartRate.HeartRateFragment;
import sensorla.watch.application.ui.Home.HomeFragment;
import sensorla.watch.application.ui.Login.LoginFragment;
import sensorla.watch.application.ui.Login.Models.UserInfo;
import sensorla.watch.application.ui.Login.SaveSharedPreference;
import sensorla.watch.application.ui.Logout.LogoutFragment;
import sensorla.watch.application.ui.Server.ServerFragment;
import sensorla.watch.application.ui.Tracking.TrackService;
import sensorla.watch.application.ui.WaitingForJob.CardFragment;

public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {
    private AppBarConfiguration mAppBarConfiguration;
    private LocalBroadcastManager broadcaster;
    private TextView deviceusername, deviceID, serverName;
    private DbHelper dbHelper = new DbHelper(this);
    private Timer bgTimer = new Timer("Background Timer");
    boolean doubleBackToExitPressedOnce = false;
    private String serverText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //control for no internet connection status
        if (isConnected()) {
            Toast.makeText(getApplicationContext(), "Internet Connected", Toast.LENGTH_SHORT).show();
        } else {
            AlertDialog.Builder netbuilder = new AlertDialog.Builder(this);
            networkAlert(netbuilder);
            netbuilder.show();
        }
        manageMenuItem();
        updateDrawerLabels();
        manageLogin();
        //background service to send data to server
        TimerTask task = new TimerTask() {
            public void run() {
                dbHelper.UploadWorkOrderDetailToServer();
            }
        };
        bgTimer.schedule(task, 10, 10 * 1000);

        TimerTask task2 = new TimerTask() {
            public void run() {
                Intent intent = new Intent(MainActivity.this, TrackService.class);
                startService(intent);
            }
        };
        bgTimer.schedule(task2, 10, 120 * 1000);

    }

    //create service
    final ApiService service = ServiceGenerator.createService(ApiService.class);

    public FragmentTransaction getFragmentTransaction() {
        return getSupportFragmentManager().beginTransaction();
    }

    public void manageLogin() {
        Intent i = getIntent();
        String messageType = i.getStringExtra("messageType"),
                status = i.getStringExtra("type"),
                deviceId = i.getStringExtra("deviceId"),
                userId = i.getStringExtra("userId"),
                userName = i.getStringExtra("userName"),
                env = i.getStringExtra("env");

        if (messageType == "LogIn")
            popUp(status, deviceId, userId, userName, env);
    }

    public void updateDrawerLabels() {
        String deviceId = SaveSharedPreference.getDeviceID(this);
        String server_Name = SaveSharedPreference.getEnvironment(this);

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        deviceID = headerView.findViewById(R.id.device_ID);
        serverName = headerView.findViewById(R.id.server_Name);

        if (!Strings.isEmptyOrWhitespace(deviceId))
            deviceID.setText(deviceId);
        if (!Strings.isEmptyOrWhitespace(server_Name)) {
            if (server_Name.equals("1"))
                serverText = "Parkway";
            else if (server_Name.equals("3"))
                serverText = "Testing";
            else if (server_Name.equals("4"))
                serverText = "URA";
            else if (server_Name.equals("5"))
                serverText = "NUS";
            else if (server_Name.equals("6"))
                serverText = "CS";
            else if (server_Name.equals("7"))
                serverText = "Demo";

            serverName.setText(serverText);

        }
    }

    public void manageMenuItem() {
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        deviceusername = headerView.findViewById(R.id.device_username);

        if (SaveSharedPreference.getLoggedStatus(getApplicationContext())) {
            mAppBarConfiguration = new AppBarConfiguration
                    .Builder(R.id.nav_home, R.id.nav_wo_list, R.id.nav_logout)
                    .setDrawerLayout(drawer)
                    .build();

            setVisibleMenuItems(false);
            setVisibleLogoutMenuItem(true);

            UserInfo userInfo = SaveSharedPreference.getUserInfo(this);

            if (userInfo != null)
                deviceusername.setText(userInfo.getUserName());
        } else {
            mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_login, R.id.nav_server, R.id.nav_logout, R.id.nav_device_id)
                    .setDrawerLayout(drawer)
                    .build();

            setVisibleMenuItems(true);
            setVisibleLogoutMenuItem(false);

            deviceusername.setText("Username");
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = getItem(item.getItemId());
                if (fragment != null) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.nav_host_fragment, fragment)
                            .commit();
                }
                drawer.closeDrawers();
                return false;
            }
        });
    }

    private Fragment getItem(int id) {
        switch (id) {
            case R.id.nav_home:
                return new HomeFragment();
            case R.id.nav_login:
                return new LoginFragment();
            case R.id.nav_server:
                return new ServerFragment();
            case R.id.nav_device_id:
                return new DeviceIDFragment();
            case R.id.nav_wo_list:
                return new CardFragment();
            case R.id.nav_logout:
                return new LogoutFragment();
                // this is the only connection between fragment and MainActivity
            case R.id.nav_heartRate:
                return new HeartRateFragment();
            default:
                return null;
        }
    }


    private void setVisibleMenuItems(boolean isVisible) {
        NavigationView navView = findViewById(R.id.nav_view);
        navView.getMenu().findItem(R.id.nav_home).setVisible(isVisible);
        navView.getMenu().findItem(R.id.nav_login).setVisible(isVisible);
        navView.getMenu().findItem(R.id.nav_device_id).setVisible(isVisible);
        navView.getMenu().findItem(R.id.nav_server).setVisible(isVisible);
    }

    private void setVisibleLogoutMenuItem(boolean isVisible) {
        NavigationView navView = findViewById(R.id.nav_view);
        navView.getMenu().findItem(R.id.nav_wo_list).setVisible(isVisible);
        navView.getMenu().findItem(R.id.nav_logout).setVisible(isVisible);
        navView.getMenu().findItem(R.id.nav_heartRate).setVisible(isVisible);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void popUp(final String status, final String deviceId, final String userId, final String userName, final String env) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Connect Alert:")
                .setMessage(userName + " has been requested to login to this device.")
                .setCancelable(false)
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //calling api
                        final Call<String> apiCall = service.Connect(deviceId, userId, status, env);
                        apiCall.enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {
                                if (response.isSuccessful() && response.body().contains("Success")) {
                                    SaveSharedPreference.setLoggedIn(getApplicationContext(), true);
                                    SaveSharedPreference.setUser_id(getApplicationContext(),Integer.parseInt(userId));
                                    Toast.makeText(getApplicationContext().getApplicationContext(), response.body(), Toast.LENGTH_SHORT).show();
                                    manageMenuItem();
                                    FragmentTransaction transaction = getFragmentTransaction();
                                    Fragment cardFragment = new CardFragment();
                                    transaction.replace(R.id.nav_host_fragment, cardFragment, "clear").commit();
                                } else {
                                    Toast.makeText(getApplicationContext().getApplicationContext(), response.message(), Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<String> call, Throwable t) {
                                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }).create();

        alertDialog.show();
        MainActivity.applyGoogleFont(this, alertDialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        broadcaster = LocalBroadcastManager.getInstance(this);
        broadcaster.registerReceiver(mMessageReceiver, new IntentFilter("FirebaseMessage"));
    }

    @Override
    public void onStop() {
        broadcaster.unregisterReceiver(mMessageReceiver);
        super.onStop();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String messageType = intent.getStringExtra("messageType"),
                    status = intent.getStringExtra("type"),
                    deviceId = intent.getStringExtra("deviceId"),
                    userId = intent.getStringExtra("userId"),
                    userName = intent.getStringExtra("userName"),
                    env = intent.getStringExtra("env");
            if (messageType == "LogIn")

                popUp(status, deviceId, userId, userName, env);

            else if (messageType == "LogOut")
                logOut();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
//        Intent i = new Intent(this, MainActivity.class);
//        startActivity(i);
//        clear();
//        exitPopUp();
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 1000);
    }

    public static void applyGoogleFont(Context context, AlertDialog dialog) {
        Typeface font = ResourcesCompat.getFont(context, R.font.google);

        int titleId = context
                .getResources()
                .getIdentifier("alertTitle", "id", "android");

        if (font != null) {
            TextView titleView = dialog.findViewById(titleId);
            TextView messageView = dialog.findViewById(android.R.id.message);
            if (titleView != null) {
                titleView.setTypeface(font, Typeface.BOLD);
                titleView.setTextSize(25);
            }
            if (messageView != null) {
                messageView.setTypeface(font);
                messageView.setTextSize(32);
            }
        }
    }

    @Override
    public void onBackStackChanged() {
        //getSupportFragmentManager().popBackStackImmediate();
    }

    //check network status
    public boolean isConnected() {
        try {
            boolean connected;
            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return connected;
        } catch (Exception e) {
            Log.e("Connectivity exception", e.getMessage(), e);
            return false;
        }
    }

    //no internet alert box
    public void networkAlert(final AlertDialog.Builder networkbuilder) {
        networkbuilder.setTitle("There is no Internet Connection!");
        networkbuilder.setCancelable(false);
        networkbuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                WifiManager wifiManager1 = (WifiManager) MainActivity.this.getApplicationContext().getSystemService(WIFI_SERVICE);
                wifiManager1.setWifiEnabled(true);
            }
        });
    }

    public void logOut() {
        Helper.goToFragment(this, new HomeFragment());
        manageMenuItem();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //for home button disable the action
        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);

        activityManager.moveTaskToFront(getTaskId(), 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    //for exit the application
    public void exitPopUp() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle("Exit Application Alert!")
                .setMessage("Please type password to exit the application.")
                .setCancelable(false)
                .setView(input)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String value = input.getText().toString();
                        if (value.equals("123")) {
                            finish();
                        } else {
                            String message = "The password you have entered is incorrect." + " \n \n" + "Please try again!";
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Error");
                            builder.setMessage(message);
                            builder.setPositiveButton("Cancel", null);
                            builder.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    exitPopUp();
                                }
                            });
                            builder.create().show();
                        }
                    }
                })
                .create();

        alert.show();
        MainActivity.applyGoogleFont(this, alert);

    }

}
