package sensorla.watch.application.ui.Logout;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sensorla.watch.application.MainActivity;
import sensorla.watch.application.R;
import sensorla.watch.application.Service.ApiService;
import sensorla.watch.application.Service.ServiceGenerator.ServiceGenerator;
import sensorla.watch.application.ui.Home.HomeFragment;
import sensorla.watch.application.ui.Login.Models.UserInfo;
import sensorla.watch.application.ui.Login.SaveSharedPreference;

public class LogoutFragment extends Fragment {
    Button logoutbtn;
    final ApiService service= ServiceGenerator.createService(ApiService.class);
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_logout, container, false);

        logoutbtn=root.findViewById(R.id.logoutBT);
        logoutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Logout
                logout();
            }
        });

        return root;
    }

    private void logout() {
        UserInfo userInfo = SaveSharedPreference.getUserInfo(getActivity());

        //call api
        final Call<String> apiCall = service.Disconnect(
                userInfo.getDeviceId(),
                userInfo.getUserId(),
                "disconnect",
                "3"
        );

        apiCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful() && response.body().contains("Success")) {
                    // Set LoggedIn status to false
                    SaveSharedPreference.setLoggedIn(getActivity(), false);
                    Toast.makeText(getActivity(), response.body(), Toast.LENGTH_SHORT).show();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.nav_host_fragment, new HomeFragment(),"clear");
                    ((MainActivity) getActivity()).manageMenuItem();
                    transaction.commit();
                }
                else{
                    Toast.makeText(getActivity(), "Internet connection is required!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) { }
        });
    }
}
