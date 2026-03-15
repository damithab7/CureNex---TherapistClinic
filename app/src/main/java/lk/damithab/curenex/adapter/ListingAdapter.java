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

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import lk.damithab.curenex.R;
import lk.damithab.curenex.model.Product;
import lk.damithab.curenex.module.GlideApp;


public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ViewHolder> {
    private List<Product> products;

    private OnListingClickListener listener;

    private FirebaseStorage storage;

    public ListingAdapter(List<Product> products, OnListingClickListener listener){
        this.products = products;
        this.listener = listener;
        storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_listing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.productTitle.setText(product.getTitle());
        holder.productPrice.setText("LKR "+product.getPrice());

        if(product.getStockCount() > 0){
            holder.productStock.setText("In Stock");
        }else{
            holder.productStock.setText("Out of Stock");
            holder.productStock.setTextColor(Color.RED);
        }
        StorageReference ref = storage.getReference(product.getImages().get(0));

        GlideApp.with(holder.itemView.getContext())
                .load(ref)
                .centerCrop()
                .placeholder(R.drawable.imageplaceholder2)
                .into(holder.productImage);

        holder.itemView.setOnClickListener(v -> {

            Animation animation = AnimationUtils.loadAnimation(v.getContext(), R.anim.button_click);
            v.startAnimation(animation);
            if(listener != null){
                listener.onListingItemClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView productImage;
        TextView productTitle, productPrice, productStock;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.productImage = itemView.findViewById(R.id.listing_item_image);
            this.productTitle = itemView.findViewById(R.id.list_item_name);
            this.productPrice = itemView.findViewById(R.id.list_item_price);
            this.productStock = itemView.findViewById(R.id.list_item_stock);
        }
    }

    public interface OnListingClickListener{
        void onListingItemClick(Product product);
    }
}
