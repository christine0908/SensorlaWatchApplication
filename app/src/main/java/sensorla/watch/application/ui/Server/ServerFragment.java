package sensorla.watch.application.ui.Server;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sensorla.watch.application.R;
import sensorla.watch.application.Service.ApiService;
import sensorla.watch.application.Service.ServiceGenerator.ServiceGenerator;
import sensorla.watch.application.ui.DeviceID.DeviceIDFragment;
import sensorla.watch.application.ui.Login.SaveSharedPreference;

public class ServerFragment extends Fragment  {
    private EditText server_input , server_pwd;
    private Button connect;
    String servertype , servergetName;

    //create service
    final ApiService service= ServiceGenerator.createService(ApiService.class);

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_server, container, false);
        server_input = root.findViewById(R.id.server_input);
        server_pwd=root.findViewById(R.id.server_pwd);

        final String serverName = SaveSharedPreference.getEnvironment(getActivity());
        if(serverName.equals("1")) servergetName = "Parkway";
        else if(serverName.equals("3")) servergetName = "Testing";
        else if(serverName.equals("4")) servergetName = "URA";
        else if(serverName.equals("5")) servergetName = "NUS";
        else if(serverName.equals("6")) servergetName = "CS";
        else if(serverName.equals("7")) servergetName = "Demo";

        server_input.setText(servergetName, TextView.BufferType.EDITABLE);
        connect=root.findViewById(R.id.connect);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                servertype = server_input.getText().toString();
                String serverpwd = server_pwd.getText().toString();

                if(servertype.equals("Parkway") || servertype.equals("Testing") || servertype.equals("URA")
                        || servertype.equals("NUS") || servertype.equals("CS") || servertype.equals("Demo")) {
                    if (servertype.equals("Parkway"))
                        servertype = "1";
                    else if (servertype.equals("Testing"))
                        servertype = "3";
                    else if (servertype.equals("URA"))
                        servertype = "4";
                    else if (servertype.equals("NUS"))
                        servertype = "5";
                    else if (servertype.equals("CS"))
                        servertype = "6";
                    else if (servertype.equals("Demo"))
                        servertype = "7";

                    //calling api
                    final Call<String> apiCall = service.GetSuperUserPwd(serverpwd);
                    apiCall.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            if (response.isSuccessful() && response.body().contains("Success")) {
                                SaveSharedPreference.setEnvironment(getActivity(), servertype);
                                Toast.makeText(getActivity(), response.body(), Toast.LENGTH_SHORT).show();
                                redirectToDeviceId(servertype);
                            } else {
                                Toast.makeText(getActivity(), "Your server name or password is wrong", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(getActivity().getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else{
                    Toast.makeText(getActivity(),"Your server name or password is wrong!",Toast.LENGTH_LONG).show();
                }
            }
        });

        return root;
    }

    private void redirectToDeviceId(String servertype) {
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("ENV",servertype);
        editor.apply();
        editor.commit();

        //send intent from fragment to fragment
        Fragment deviceIdFragment = new DeviceIDFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, deviceIdFragment,"tag").commit();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}