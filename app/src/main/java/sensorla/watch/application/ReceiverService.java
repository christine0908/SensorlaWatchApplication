package sensorla.watch.application;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.Toast;

public class ReceiverService extends Service {

    private static BroadcastReceiver m_ScreenOffReceiver;

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        registerScreenOffReceiver();
    }

    @Override
    public void onDestroy()
    {
        unregisterReceiver(m_ScreenOffReceiver);
        m_ScreenOffReceiver = null;
    }

    private void registerScreenOffReceiver()
    {
        m_ScreenOffReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                Toast.makeText(context, "Hi", Toast.LENGTH_LONG).show();
            }
        };
        IntentFilter filter = new IntentFilter("FirebaseMessage");
        registerReceiver(m_ScreenOffReceiver, filter);
    }
}
