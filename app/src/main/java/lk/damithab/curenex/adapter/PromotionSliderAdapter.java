package lk.damithab.curenex.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

import lk.damithab.curenex.R;
import lk.damithab.curenex.model.Promotion;

public class PromotionSliderAdapter extends RecyclerView.Adapter<PromotionSliderAdapter.ProductSliderViewHolder> {

    private List<Promotion> promotions;

    private FirebaseStorage storage;

    public PromotionSliderAdapter(List<Promotion> promotions){
        this.promotions = promotions;
        storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public ProductSliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.promotion_slider_item, parent, false);
        return new ProductSliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductSliderViewHolder holder, int position) {
        Promotion promotion = promotions.get(position);

        storage.getReference(promotion.getImageUrl())
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Glide.with(holder.itemView.getContext())
                            .load(uri)
                            .centerCrop()
                            .into(holder.imageView);
                });
    }

    @Override
    public int getItemCount() {
        return promotions.size();
    }

    public static class ProductSliderViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        public ProductSliderViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.promotion_slider_item_image);
        }
    }
}
