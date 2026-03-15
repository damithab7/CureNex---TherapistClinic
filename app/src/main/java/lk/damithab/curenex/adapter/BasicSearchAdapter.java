package lk.damithab.curenex.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import lk.damithab.curenex.R;
import lk.damithab.curenex.model.Category;
import lk.damithab.curenex.model.Service;
import lk.damithab.curenex.module.GlideApp;

public class BasicSearchAdapter extends RecyclerView.Adapter<BasicSearchAdapter.ViewHolder> {
    private List<Object> result;

    private OnListingClickListener listener;

    private FirebaseStorage storage;

    public BasicSearchAdapter(List<Object> result, OnListingClickListener listener){
        this.result = result;
        this.listener = listener;
        storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_basic, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Object item = result.get(position);
        if (item instanceof Service) {
            Service service = (Service) item;
            holder.itemTitle.setText(service.getName());
            StorageReference ref = storage.getReference(service.getImageUrl());

            GlideApp.with(holder.itemView.getContext())
                    .load(ref)
                    .centerCrop()
                    .placeholder(R.drawable.imageplaceholder2)
                    .into(holder.itemImage);

        } else if (item instanceof Category) {
            Category category = (Category) item;
            holder.itemTitle.setText(category.getCategoryName());
            StorageReference ref = storage.getReference(category.getImageUrl());

            GlideApp.with(holder.itemView.getContext())
                    .load(ref)
                    .centerCrop()
                    .placeholder(R.drawable.imageplaceholder2)
                    .into(holder.itemImage);
        }

        holder.itemView.setOnClickListener(v -> {
            Animation animation = AnimationUtils.loadAnimation(v.getContext(), R.anim.button_click);
            v.startAnimation(animation);
            if(listener != null){
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return result.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView itemImage;
        TextView itemTitle;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemImage = itemView.findViewById(R.id.home_basic_search_item_image);
            this.itemTitle = itemView.findViewById(R.id.home_basic_search_item_title);
        }
    }

    public interface OnListingClickListener{
        void onItemClick(Object object);
    }
}