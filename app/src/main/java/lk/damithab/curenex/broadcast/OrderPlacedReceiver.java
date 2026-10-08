package lk.damithab.curenex.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class OrderPlacedReceiver extends BroadcastReceiver {
    public static final String ACTION_ORDER_PLACED = "lk.damithab.curenex.ORDER_PLACED";

    @Override
    public void onReceive(Context context, Intent intent) {
        String orderId = intent.getStringExtra("orderId");
        Toast.makeText(context, "Payment complete. Order #" + orderId + " placed",
                Toast.LENGTH_LONG).show();
    }
}