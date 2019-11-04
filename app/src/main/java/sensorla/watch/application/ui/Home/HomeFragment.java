package sensorla.watch.application.ui.Home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import sensorla.watch.application.R;
import sensorla.watch.application.ui.Login.SaveSharedPreference;
import sensorla.watch.application.ui.WaitingForJob.CardFragment;

public class HomeFragment extends Fragment {
    ViewGroup container;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.container = container;

        if (SaveSharedPreference.getLoggedStatus(getActivity()))
            redirectToCardFragment();

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        return root;
    }

    public void redirectToCardFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, new CardFragment(),"clear").commit();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}