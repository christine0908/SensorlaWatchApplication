package sensorla.watch.application.ui.DeviceID;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sensorla.watch.application.MainActivity;
import sensorla.watch.application.R;
import sensorla.watch.application.Service.ApiService;
import sensorla.watch.application.Service.ServiceGenerator.ServiceGenerator;
import sensorla.watch.application.ui.Home.HomeFragment;
import sensorla.watch.application.ui.Login.SaveSharedPreference;

import static android.content.ContentValues.TAG;
import static sensorla.watch.application.Constants.WATCH_ID;

public class DeviceIDFragment extends Fragment {
    Button btn_register;
    EditText edit_deviceID;
    String mToken;
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //////
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( getActivity(),
                new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        mToken = instanceIdResult.getToken();
                        Log.e("Token",mToken);
                    }
                });
        ///////

        View root = inflater.inflate(R.layout.fragment_device_id, container, false);
        btn_register=root.findViewById(R.id.register);
        edit_deviceID=root.findViewById(R.id.deviceID_Text);

        final String serverName = SaveSharedPreference.getEnvironment(getActivity());
        final String deviceID = SaveSharedPreference.getDeviceID(getActivity());
        edit_deviceID.setText(deviceID, TextView.BufferType.EDITABLE);

        //create service
        final ApiService service= ServiceGenerator.createService(ApiService.class);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldExternalId=WATCH_ID;
                final String deviceID_text=edit_deviceID.getText().toString();
                WATCH_ID=deviceID_text;

                //calling api
                final Call<String> apiCall=service.UpdateDevice(oldExternalId,deviceID_text,mToken,serverName);
                Log.d(TAG, "TOKEN: " + mToken);
                apiCall.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if(response.isSuccessful() && response.body().contains("Success")){
                            Toast.makeText(getActivity().getApplicationContext(), response.body(), Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(getActivity().getApplicationContext(), response.message(), Toast.LENGTH_SHORT).show();
                        }

                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.replace(R.id.nav_host_fragment, new HomeFragment());
                        getFragmentManager().popBackStack();
                        transaction.commit();
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Toast.makeText(getActivity().getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                SaveSharedPreference.setDeviceId(getActivity(), WATCH_ID);
                ((MainActivity) getActivity()).updateDrawerLabels();
            }
        });
        return root;
    }
}