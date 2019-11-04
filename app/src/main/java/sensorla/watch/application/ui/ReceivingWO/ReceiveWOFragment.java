package sensorla.watch.application.ui.ReceivingWO;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import sensorla.watch.application.Helper;
import sensorla.watch.application.R;
import sensorla.watch.application.Service.ApiService;
import sensorla.watch.application.Service.ServiceGenerator.ServiceGenerator;
import sensorla.watch.application.SqliteDatabase.DbHelper;
import sensorla.watch.application.SqliteDatabase.WorkOrder;
import sensorla.watch.application.ui.Login.SaveSharedPreference;
import sensorla.watch.application.ui.WaitingForJob.CardFragment;
import sensorla.watch.application.ui.WaitingForJob.CardInfo;

public class ReceiveWOFragment extends Fragment {
    private DbHelper dbHelper;
    private ViewGroup container;
    private ImageButton accept,reject,pause;
    private TextView txtWoStatus,sec;
    private ApiService api;
    private long mLastClickTime = 0;
    private DateFormat format;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
      dbHelper = new DbHelper(getActivity());
        api = ServiceGenerator.createService(ApiService.class);
        this.container = container;

        final View root = inflater.inflate(R.layout.fragment_receive_wo, container, false);

        String cardInfoString = getArguments().getString("cardInfo");
        final CardInfo cardInfo = new Gson().fromJson(cardInfoString, CardInfo.class);
        setTexts(root, cardInfo);

        accept = root.findViewById(R.id.btnAccept);
        reject = root.findViewById(R.id.btnReject);
        pause = root.findViewById(R.id.btnPause);
        sec = root.findViewById(R.id.sec);

        if(!cardInfo.getStatus().contains("reported"))
        {
            reject.setVisibility((View.GONE));
            pause.setVisibility(View.VISIBLE);
        }
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-mLastClickTime < 15000){
                    return;
                }
                vibrate();
                mLastClickTime = SystemClock.elapsedRealtime();
                new CountDownTimer(15000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        sec.setVisibility(View.VISIBLE);
                        sec.setText("Please wait " + millisUntilFinished / 1000 + " seconds for next button click");
                    }

                    public void onFinish() {
                        sec.setText("done!");
                    }

                }.start();

                pause.setVisibility(View.VISIBLE);
                InsertWorkOrderDetail("accepted", root, cardInfo);
            }
        });

        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrate();
                InsertWorkOrderDetail("rejected", root, cardInfo);
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectToWOList();
            }
        });

        return root;
    }

    public void customToast(String message) {
        LayoutInflater inflater = getLayoutInflater();
        View parent = inflater.inflate(R.layout.fragment_receive_wo, container , false);
        View layout = inflater.inflate(R.layout.custom_toast,(ViewGroup) parent.findViewById(R.id.custom_toast_container));

        TextView text = layout.findViewById(R.id.text);
        text.setText(message);

        Toast toast = new Toast(getActivity().getApplicationContext());
        toast.setGravity(Gravity.FILL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    private void setTexts(View v, CardInfo cardInfo) {

        Animation animation = AnimationUtils.loadAnimation(getActivity(),R.anim.marquee);
        animation.reset();
        TextView textWOName = v.findViewById(R.id.txtWOName);
        textWOName.setText(cardInfo.getName());
        textWOName.startAnimation(animation);
        TextView txtLocName = v.findViewById(R.id.txtLocName);
        txtLocName.setText(cardInfo.getLocation());
        txtLocName.startAnimation(animation);
        ((TextView)v.findViewById(R.id.txtWOStatus)).setText(cardInfo.getStatus());
        ((TextView)v.findViewById(R.id.txtInstruction)).setText(cardInfo.getInstruction());
        ((TextView)v.findViewById(R.id.txtWOTime)).setText(cardInfo.getCreatedDate());
    }

    private String updatedStatus(String current) {
        switch (current) {
            case "reported":
                return "acknowledged";
            case "acknowledged":
                return "started";
            case "started":
                return "completed";
            default:
                return "reported";
        }
    }

    private void InsertWorkOrderDetail(String type, final View root, CardInfo cardInfo)
    {
        switch(root.getId())
        {
            case R.id.btnPause:
                redirectToWOList();
                break;
        }
        final String serverName = SaveSharedPreference.getEnvironment(getActivity());
        final String deviceID = SaveSharedPreference.getDeviceID(getActivity());
        final WorkOrder workOrder = dbHelper.GetWorkOrderByWorkOrderId(cardInfo.getId());

        workOrder.work_order_status = (type == "accepted") ? updatedStatus(workOrder.work_order_status) : type;
        workOrder.lastStatusTime = dbHelper.toDate(dbHelper.now());
        if(workOrder.work_order_status == null){redirectToWOList();}
        dbHelper.UpdateWorkOrder(workOrder);
        dbHelper.InsertWorkOrderDetail(workOrder.work_order_id, workOrder.work_order_status, deviceID, serverName, now());

        //update wo status
        CardInfo ci = Helper.ConvertToViewModel(workOrder);
        setTexts(root, ci);

        customToast(workOrder.work_order_status);
        List<String> statusList = Arrays.asList("acknowledged", "started", "completed");

        if(statusList.contains(workOrder.work_order_status))
        {
            clear();
            reject.setVisibility(View.GONE);
        }
        if (workOrder.work_order_status == "completed" || workOrder.work_order_status == "rejected")
        {
            clear();

            if(workOrder.work_order_status == null){redirectToWOList();}
            else {
                dbHelper.DeleteWorkOrder(workOrder.work_order_id);
                dbHelper.DeleteWorkOrderDetail(workOrder.work_order_id);
                redirectToWOList();
            }
        }
    }
    private void redirectToWOList() {
        Fragment cardFragment = new CardFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, cardFragment,"clear");
        transaction.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void vibrate(){
        final MediaPlayer mp = MediaPlayer.create(getActivity(),R.raw.app_alert_tone_ringtone_003 );
        mp.start();
        Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 4 seconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(1000);
        }
    }

    public String now() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public void clear(){
        ((AppCompatActivity)getContext()).getSupportFragmentManager()
                .popBackStack("clear", FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}
