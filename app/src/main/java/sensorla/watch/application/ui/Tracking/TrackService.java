package sensorla.watch.application.ui.Tracking;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sensorla.watch.application.Service.ApiService;
import sensorla.watch.application.Service.ServiceGenerator.ServiceGenerator;
import sensorla.watch.application.ui.Login.SaveSharedPreference;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class TrackService extends Service {
    private ArrayList permissionsToRequest;
    private ArrayList permissionsRejected = new ArrayList();
    private ArrayList permissions = new ArrayList();

    private final static int ALL_PERMISSIONS_RESULT = 101;
    LocationTrack locationTrack;
    public double latitude,longitude,altitude;
    public TrackService() { }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        track();
        return super.onStartCommand(intent, flags, startId);
    }

    public void track(){

        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);

        permissionsToRequest = findUnAskedPermissions(permissions);

        locationTrack = new LocationTrack(TrackService.this);

        if (locationTrack.canGetLocation()) {

            latitude = locationTrack.getLatitude();
            longitude = locationTrack.getLongitude();
            altitude = locationTrack.getAltitude();

        } else {

            locationTrack.showSettingsAlert();
        }
//        final ApiService service= ServiceGenerator.createService(ApiService.class);
//        final String serverName = SaveSharedPreference.getEnvironment(getApplicationContext());
//        final int userId = SaveSharedPreference.getUser_id(getApplicationContext());
//        final Call<String> apiCall = service.GetUserLocation(userId,latitude,longitude,serverName);
//        apiCall.enqueue(new Callback<String>() {
//            @Override
//            public void onResponse(Call<String> call, Response<String> response) { }
//
//            @Override
//            public void onFailure(Call<String> call, Throwable t) { }
//        });
        final String serverName = SaveSharedPreference.getEnvironment(getApplicationContext());
        final int userId = SaveSharedPreference.getUser_id(getApplicationContext());
        //create service
        final ApiService service= ServiceGenerator.createIndoorService(ApiService.class);
        final Call<String> apiCall = service.Indoor(userId,latitude,longitude,altitude,serverName);
        apiCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) { }

            @Override
            public void onFailure(Call<String> call, Throwable t) { }
        });

    }

    private ArrayList findUnAskedPermissions(ArrayList wanted) {
        ArrayList result = new ArrayList();

        for (Object perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(Object permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission((String) permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

}
