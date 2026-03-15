package lk.damithab.curenex.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;
import java.util.Locale;

import lk.damithab.curenex.R;
import lk.damithab.curenex.model.CartItem;
import lk.damithab.curenex.model.Notification;
import lk.damithab.curenex.model.Product;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {
    private List<Notification> notificationsList;

    public NotificationsAdapter(List<Notification> notificationsList) {
        this.notificationsList = notificationsList;
    }

    private FirebaseStorage storage;

    @NonNull
    @Override
    public NotificationsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        storage = FirebaseStorage.getInstance();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationsAdapter.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull NotificationsAdapter.ViewHolder holder, int position) {
        Notification notification = notificationsList.get(position);
        holder.notificationTitle.setText(notification.getTitle());
        holder.notificationMessage.setText(notification.getMessage());
        holder.notificationDate.setText(notification.getDate());

    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView notificationTitle, notificationMessage, notificationDate;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.notificationTitle = itemView.findViewById(R.id.notification_item_title);
            this.notificationMessage = itemView.findViewById(R.id.notification_item_message);
            this.notificationDate = itemView.findViewById(R.id.notification_item_date);
        }
    }
}
