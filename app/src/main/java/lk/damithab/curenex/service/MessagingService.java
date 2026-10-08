package lk.damithab.curenex.service;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import lk.damithab.curenex.R;
import lk.damithab.curenex.activity.MainActivity;
import lk.damithab.curenex.model.Notification;

public class MessagingService extends FirebaseMessagingService {

    private static final String TAG = "MessagingService";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "FCM Token: " + token);
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        if (u != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("fcmToken", token);
            db.collection("users").document(u.getUid()).set(data, SetOptions.merge());
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        Log.d(TAG, "FCM message received");

        String title = "CureNex";
        String messageBody = "Empty Body";
        String imageUrl = null;

        if (message.getNotification() != null) {
            if (message.getNotification().getTitle() != null) {
                title = message.getNotification().getTitle();
            }
            if (message.getNotification().getBody() != null) {
                messageBody = message.getNotification().getBody();
            }
            if (message.getNotification().getImageUrl() != null) {
                imageUrl = message.getNotification().getImageUrl().toString();
            }
        }

        if (message.getData().containsKey("title")) {
            title = message.getData().get("title");
        }
        if (message.getData().containsKey("body")) {
            messageBody = message.getData().get("body");
        }
        if (message.getData().containsKey("image")) {
            imageUrl = message.getData().get("image");
        }

        Log.d(TAG, "Title: " + title);
        Log.d(TAG, "Body: " + messageBody);

        saveNotification(title, messageBody, imageUrl);
        sendNotification(this, title, messageBody);
    }

    private void saveNotification(String title, String messageBody, String imageUrl) {
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            Log.d(TAG, "No authenticated user. Notification will not be saved.");
            return;
        }

        Notification notification = new Notification();
        notification.setImage(imageUrl);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        notification.setDate(dbFormat.format(calendar.getTime()));
        notification.setTitle(title);
        notification.setMessage(messageBody);
        notification.setUid(auth.getUid());

        DocumentReference notificationReference = db.collection("notifications").document();
        notification.setNotificationId(notificationReference.getId());

        notificationReference.set(notification)
                .addOnSuccessListener(unused -> Log.d(TAG, "Notification saved to Firestore"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save notification", e));
    }

    private void sendNotification(Context context, String title, String messageBody) {
        String channelId = getString(R.string.default_notification_channel_id);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "POST_NOTIFICATIONS permission not granted");
                return;
            }
        }

        NotificationManagerCompat.from(context).notify((int) System.currentTimeMillis(), notificationBuilder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = getString(R.string.default_notification_channel_id);

            NotificationChannel channel = new NotificationChannel(channelId, "Promotions", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notifications for CureNex");

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}