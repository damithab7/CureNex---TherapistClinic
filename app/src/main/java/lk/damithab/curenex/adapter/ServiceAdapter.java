package lk.damithab.curenex.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

import lk.damithab.curenex.R;
import lk.damithab.curenex.dto.ServiceDTO;
import lk.damithab.curenex.model.Service;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ViewHolder> {
    private List<Service> serviceList;

    private Context context;

    private OnServiceItemClickListener listener;

    private FirebaseStorage storage;

    public ServiceAdapter(List<Service> serviceList, OnServiceItemClickListener listener) {
        this.serviceList = serviceList;
        this.listener = listener;
        storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Service service = serviceList.get(position);
        holder.serviceName.setText(service.getName());

        storage.getReference(service.getImageUrl())
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Glide.with(holder.itemView.getContext())
                            .load(uri)
                            .centerCrop()
                            .into(holder.serviceImage);
                });

        holder.itemView.setOnClickListener(v -> {
            Animation animation = AnimationUtils.loadAnimation(v.getContext(), R.anim.button_click);
            v.startAnimation(animation);
            if (listener != null) {
                listener.onServiceItemClick(service);
            }
        });
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView serviceImage;
        TextView serviceName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.serviceImage = itemView.findViewById(R.id.item_service_image);
            this.serviceName = itemView.findViewById(R.id.item_service_text);
        }

    }

    public interface OnServiceItemClickListener {
        void onServiceItemClick(Service service);
    }
}
