package sensorla.watch.application.ui.Login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import sensorla.watch.application.MainActivity;
import sensorla.watch.application.R;
import sensorla.watch.application.SplashScreenActivity;
import sensorla.watch.application.ui.FaceRecognition.FaceRecognition_activity;

public class LoginOptionFragment extends Fragment {

    Button btn_facerecog;
    Button btn_login;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_login_option, container, false);

        btn_facerecog = root.findViewById(R.id.faceRecognitionBtn);
        btn_login = root.findViewById(R.id.emailAndpasswordBtn);

        btn_facerecog.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.e("face clicked!","true");
                getActivity().getSupportFragmentManager().popBackStackImmediate();
                Intent myIntent = new Intent(getActivity(), FaceRecognition_activity.class);
                getActivity().startActivity(myIntent);

            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("clicked!","true");
                MainActivity main = (MainActivity) getActivity();
                Fragment loginFragment = new LoginFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.nav_host_fragment, loginFragment);
                // Commit the transaction
                transaction.commit();

            }
        });

        return root;
    }
}
