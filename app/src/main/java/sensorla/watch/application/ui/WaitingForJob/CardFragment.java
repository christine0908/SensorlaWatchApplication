package sensorla.watch.application.ui.WaitingForJob;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import sensorla.watch.application.Helper;
import sensorla.watch.application.R;
import sensorla.watch.application.SqliteDatabase.DbHelper;
import sensorla.watch.application.SqliteDatabase.WorkOrder;
import sensorla.watch.application.ui.Login.SaveSharedPreference;


public class CardFragment extends Fragment {
    View root;
    private LocalBroadcastManager broadcaster;
    private DbHelper dbHelper;
    private RecyclerView recyclerView;
    private CardAdapter ca;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_card, container, false);

        dbHelper = new DbHelper(getContext());
        recyclerView = root.findViewById(R.id.cardList);
        recyclerView.setHasFixedSize(true);
        ca = new CardAdapter(getActivity(), createList());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(ca);

        manageViewVisibilities();

        return root;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        broadcaster = LocalBroadcastManager.getInstance(getActivity());
        broadcaster.registerReceiver(mMessageReceiver, new IntentFilter("FirebaseMessage"));
    }

    @Override
    public void onDetach() {
        broadcaster.unregisterReceiver(mMessageReceiver);
        super.onDetach();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String  messageType = intent.getStringExtra("messageType");

            if (messageType == "dbUpdated")
                updateCardView();
        }
    };

    private List<CardInfo> createList() {
        List<CardInfo> result = new ArrayList<>();
        String userId = SaveSharedPreference.getUserInfo(getActivity()).getUserId();

        List<WorkOrder> woList = dbHelper.GetWorkOrderListByUserId(userId);
        for (WorkOrder workOrder: woList)
        {
            CardInfo ci = Helper.ConvertToViewModel(workOrder);
            Log.d("TAG", "Status >> " + ci.status);
            result.add(ci);
        }

        return result;
    }

    private void manageViewVisibilities() {
        if (createList().size() > 0)
            root.findViewById(R.id.empty_tempalte).setVisibility(View.GONE);
        else
            root.findViewById(R.id.empty_tempalte).setVisibility(View.VISIBLE);
    }

    public void updateCardView() {
        ca.updateCardList(createList());
        manageViewVisibilities();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
