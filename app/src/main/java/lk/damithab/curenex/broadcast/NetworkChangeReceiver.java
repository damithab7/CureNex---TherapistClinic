package lk.damithab.curenex.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.widget.Toast;

public class NetworkChangeReceiver extends BroadcastReceiver {
    public static final String ACTION_NETWORK_CHANGED = "lk.damithab.curenex.NETWORK_CHANGED";
    private static boolean wasOffline = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean connected = intent.getBooleanExtra("connected", false);
        if (!connected) {
            wasOffline = true;
            Toast.makeText(context, "No internet connection", Toast.LENGTH_LONG).show();
        } else if (wasOffline) {
            wasOffline = false;
            Toast.makeText(context, "Back online", Toast.LENGTH_SHORT).show();
        }
    }
}