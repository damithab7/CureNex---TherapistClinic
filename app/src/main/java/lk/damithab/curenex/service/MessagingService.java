package lk.damithab.curenex.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import lk.damithab.curenex.R;
import lk.damithab.curenex.activity.MainActivity;
import lk.damithab.curenex.model.Notification;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessagingService extends FirebaseMessagingService {
    private static final String TAG = MessagingService.class.getSimpleName();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirebaseAuth auth;

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Token: " + token);

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {

        Log.d(TAG, "onMessageReceived: Message Received!");

        String title = "CureNex";
        String messageBody = "Empty Body";
        String imageUrl = null;

        if (message.getNotification() != null) {
            title = message.getNotification().getTitle();
            messageBody = message.getNotification().getBody();

            if (message.getNotification().getImageUrl() != null) {
                imageUrl = message.getNotification().getImageUrl().toString();
            }
        }

        /// Save notification to DB
        auth = FirebaseAuth.getInstance();

        Notification notification = new Notification();

        notification.setImage(imageUrl);

        Calendar calendar = Calendar.getInstance(); ///Today date
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()); /// to Store bookings date

        notification.setDate(dbFormat.format(calendar.getTime()));
        notification.setTitle(title);
        notification.setMessage(messageBody);
        notification.setUid(auth.getCurrentUser() != null ? auth.getUid() : "");

        if(auth.getCurrentUser() != null) {
            DocumentReference notifiRef = db.collection("notifications").document();
            String generatedId = notifiRef.getId();
            notification.setNotificationId(generatedId);
            notifiRef.set(notification).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {

                }
            });
        }

        sendNotification(title, messageBody);
    }

    private void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int requestCode = 0;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        String channel_id = "promotion_channel";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channel_id)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channel_id, "Promotions", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notifications for promotions");
            notificationManager.createNotificationChannel(channel);
        }
        int notificationId = 1;
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}
