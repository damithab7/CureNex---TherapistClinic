package lk.damithab.curenex.adapter;

import android.content.Context;
import android.graphics.BitmapRegionDecoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
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

public class HomeProductAdapter extends RecyclerView.Adapter<HomeProductAdapter.ViewHolder> {
    private List<Product> productList;

    private Context context;

    private HomeProductAdapter.OnHomeProductItemClickListener listener;
    private HomeProductAdapter.OnHomeProductBuyNowClickListener buyNowListener;

    private FirebaseFirestore db;

    private FirebaseStorage storage;

    public void setBuyNowListener(OnHomeProductBuyNowClickListener listener){
        this.buyNowListener = listener;
    }

    public HomeProductAdapter(List<Product> productList, HomeProductAdapter.OnHomeProductItemClickListener listener) {
        this.productList = productList;
        this.listener = listener;
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public HomeProductAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_product,parent, false);
        context = parent.getContext();
        return new HomeProductAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeProductAdapter.ViewHolder holder, int position) {

        Product product = productList.get(position);
        holder.productTitle.setText(product.getTitle());

        db.collection("categories").whereEqualTo("categoryId", product.getCategoryId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot qds) {
                if (!qds.isEmpty()) {
                    Category category = qds.getDocuments().get(0).toObject(Category.class);
                    holder.productCategory.setText(category.getCategoryName());
                }
            }
        });

        StringBuilder rateText = new StringBuilder();
        rateText.append(String.format(Locale.US, "LKR %,.2f",product.getPrice()));

        holder.productPrice.setText(rateText);

        holder.productStars.setRating(product.getRating());

        StringBuilder starRateText = new StringBuilder();
        starRateText.append(" (")
                .append(product.getRating())
                .append(")");
        holder.productRateText.setText(starRateText);

        StorageReference ref = storage.getReference(product.getImages().get(0));

        GlideApp.with(holder.itemView.getContext())
                .load(ref)
                .centerCrop()
                .placeholder(R.drawable.imageplaceholder2)
                .into(holder.productImage);

        holder.itemView.setOnClickListener(v -> {
            Animation animation = AnimationUtils.loadAnimation(v.getContext(), R.anim.button_click);
            v.startAnimation(animation);
            if (listener != null) {
                listener.onHomeProductItemClick(product);
            }
        });

        if(buyNowListener != null){
            holder.buyNow.setOnClickListener(v->{
                buyNowListener.onHomeProductBuyNowClick(product);
            });
        }

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productTitle, productCategory, productPrice, productRateText;
        RatingBar productStars;

        MaterialButton buyNow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.productImage = itemView.findViewById(R.id.home_item_product_image);
            this.productTitle = itemView.findViewById(R.id.home_item_product_title);
            this.productCategory = itemView.findViewById(R.id.home_item_product_category);
            this.productPrice = itemView.findViewById(R.id.home_item_product_price);
            this.productStars = itemView.findViewById(R.id.home_item_product_stars);
            this.productRateText = itemView.findViewById(R.id.home_item_product_rate_text);
            this.buyNow = itemView.findViewById(R.id.home_item_product_buy_now);
        }

    }

    public interface OnHomeProductItemClickListener {
        void onHomeProductItemClick(Product product);
    }
    public interface OnHomeProductBuyNowClickListener {
        void onHomeProductBuyNowClick(Product product);
    }
}
