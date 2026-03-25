package lk.damithab.curenex.broadcast;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

public class NetworkObserver {

    private final ConnectivityManager connectivityManager;
    private final ConnectivityManager.NetworkCallback networkCallback;

    public NetworkObserver(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                //network is available
                Log.d("NetworkStatus", "Connected");
            }

            @Override
            public void onLost(Network network) {
                // network is lost
                Log.d("NetworkStatus", "Disconnected - Show error message here");
            }
        };
    }

    public void startListening() {
        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();
        connectivityManager.registerNetworkCallback(request, networkCallback);
    }

    public void stopListening() {
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }
}
