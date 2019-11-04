package sensorla.watch.application;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import java.util.List;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.text.SimpleDateFormat;
import java.util.List;

import sensorla.watch.application.SqliteDatabase.WorkOrder;
import sensorla.watch.application.ui.WaitingForJob.CardInfo;
import static android.content.Context.ACTIVITY_SERVICE;

public class Helper {

    final static SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yy, HH:mm a");

    //check the app is opened or not
    public static boolean isOpened(Context ctx, String myPackage){
        ActivityManager manager = (ActivityManager) ctx.getSystemService(ACTIVITY_SERVICE);
        List< ActivityManager.RunningTaskInfo > runningTaskInfo = manager.getRunningTasks(1);

        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        if(componentInfo.getPackageName().equals(myPackage)) {
            return true;
        }
        return false;
    }

    //for logout fragment direction
    public static void goToFragment(AppCompatActivity context, Fragment fragment) {
        FragmentTransaction transaction = context.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);
        context.getSupportFragmentManager().popBackStack();
        transaction.commit();
    }

    //card convert to View Model
    public static CardInfo ConvertToViewModel(WorkOrder workOrder) {
        CardInfo ci = new CardInfo();
        ci.setId(workOrder.work_order_id);
        ci.setName(workOrder.work_order_name);
        ci.setInstruction(workOrder.work_order_instruction);
        ci.setType(workOrder.work_order_type);
        ci.setLocation(workOrder.work_order_location_name);
        ci.setStatus(workOrder.work_order_status);
        ci.setCreatedDate(displayFormat.format(workOrder.lastStatusTime));
        return ci;
    }
}
