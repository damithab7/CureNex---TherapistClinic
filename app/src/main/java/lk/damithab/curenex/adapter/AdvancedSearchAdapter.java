package lk.damithab.curenex.adapter;

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
import java.util.Locale;

import lk.damithab.curenex.R;
import lk.damithab.curenex.model.Category;
import lk.damithab.curenex.model.Product;
import lk.damithab.curenex.model.Service;
import lk.damithab.curenex.model.Therapist;
import lk.damithab.curenex.module.GlideApp;

public class AdvancedSearchAdapter extends RecyclerView.Adapter<AdvancedSearchAdapter.ViewHolder> {
    private List<Object> result;

    private OnListingClickListener listener;

    private FirebaseStorage storage;

    public AdvancedSearchAdapter(List<Object> result, OnListingClickListener listener){
        this.result = result;
        this.listener = listener;
        storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_advanced, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Object item = result.get(position);
        if (item instanceof Therapist) {
            Therapist therapist = (Therapist) item;
            holder.itemTitle.setText(therapist.getName());
            StorageReference ref = storage.getReference(therapist.getTherapistImage());
            holder.itemSubText.setText(String.format(Locale.US, "LKR %,.2f", therapist.getRate()) + "/h");

            GlideApp.with(holder.itemView.getContext())
                    .load(ref)
                    .centerCrop()
                    .placeholder(R.drawable.imageplaceholder2)
                    .into(holder.itemImage);

        } else if (item instanceof Product) {
            Product product = (Product) item;
            holder.itemTitle.setText(product.getTitle());
            StorageReference ref = storage.getReference(product.getImages().get(0));
            holder.itemSubText.setText(String.format(Locale.US, "LKR %,.2f", product.getPrice()));

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
        TextView itemTitle, itemSubText;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemImage = itemView.findViewById(R.id.home_search_item_image);
            this.itemTitle = itemView.findViewById(R.id.home_search_item_title);
            this.itemSubText = itemView.findViewById(R.id.home_search_item_last_text);
        }
    }

    public interface OnListingClickListener{
        void onItemClick(Object object);
    }
}
