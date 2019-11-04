package sensorla.watch.application.ui.Login;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sensorla.watch.application.MainActivity;
import sensorla.watch.application.R;
import sensorla.watch.application.Service.ApiService;
import sensorla.watch.application.Service.ServiceGenerator.ServiceGenerator;
import sensorla.watch.application.ui.Server.ServerFragment;
import sensorla.watch.application.ui.WaitingForJob.CardFragment;

import static sensorla.watch.application.Constants.WATCH_ID;

public class LoginFragment extends Fragment {
   EditText username,password;
    Button btn_signin;
    private OnFragmentInteractionListener mListener;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_login, container, false);
        username=root.findViewById(R.id.username);
        password=root.findViewById(R.id.password);
        btn_signin=root.findViewById(R.id.signin);

        //Check if User is Already Logged In
        if(SaveSharedPreference.getLoggedStatus(getActivity())){
            Fragment cardFragment=new CardFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.nav_host_fragment, cardFragment).commit();
        }
        //create service
        final ApiService service= ServiceGenerator.createService(ApiService.class);
        final String deviceID = SaveSharedPreference.getDeviceID(getActivity());

        btn_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name=username.getText().toString();
                String pwd=password.getText().toString();

                if(deviceID == "000000" && deviceID == " ")
                {
                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                            .setTitle("ERROR:")
                            .setMessage("Please register device Id first!")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Fragment serverFragment=new ServerFragment();
                                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                    transaction.replace(R.id.nav_host_fragment, serverFragment).commit();
                                }
                            }).show();
                }
                else
                {
                    final String serverName = SaveSharedPreference.getEnvironment(getActivity());
                    //calling api
                    final Call<String> apiCall=service.UserLogin(name,pwd,deviceID,serverName);
                    apiCall.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            if(response.isSuccessful()) {
                                if(response.body().contains("Success")) {
                                    SaveSharedPreference.saveUserInfo(
                                            getActivity(),
                                            SaveSharedPreference.loggedInUserInfo(response.body())
                                    );

                                    SaveSharedPreference.setLoggedIn(getActivity(), true);
                                    Toast.makeText(getActivity().getApplicationContext(), response.body(), Toast.LENGTH_SHORT).show();
                                    ((MainActivity) getActivity()).manageMenuItem();

                                    redirectToCardFragment();
                                }
                                else {
                                    Toast.makeText(getActivity(), response.body(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            else{
                                Toast.makeText(getActivity().getApplicationContext(), response.message(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(getActivity().getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        return root;
    }

    public void redirectToCardFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, new CardFragment()).commit();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
}