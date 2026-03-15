package lk.damithab.curenex.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import lk.damithab.curenex.R;
import lk.damithab.curenex.module.GlideApp;

public class ProductSliderAdapter extends RecyclerView.Adapter<ProductSliderAdapter.ProductSliderViewHolder> {

    private List<String> images;

    private FirebaseStorage storage;

    public ProductSliderAdapter(List<String> images){
        this.images = images;
        storage = FirebaseStorage.getInstance();
    }


    @NonNull
    @Override
    public ProductSliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_slider_item, parent, false);
        return new ProductSliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductSliderViewHolder holder, int position) {
        StorageReference ref = storage.getReference(images.get(position));

        GlideApp.with(holder.itemView.getContext())
                .load(ref)
                .centerCrop()
                .placeholder(R.drawable.imageplaceholder2)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public static class ProductSliderViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        public ProductSliderViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.product_slider_item_image);
        }
    }
}
