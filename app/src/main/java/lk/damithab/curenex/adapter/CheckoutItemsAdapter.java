package lk.damithab.curenex.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
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
import lk.damithab.curenex.model.CartItem;
import lk.damithab.curenex.model.Product;
import lk.damithab.curenex.module.GlideApp;

public class CheckoutItemsAdapter extends RecyclerView.Adapter<CheckoutItemsAdapter.ViewHolder> {

    private List<CartItem> cartItems;

    public CheckoutItemsAdapter(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    private FirebaseStorage storage;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        storage = FirebaseStorage.getInstance();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkout_products, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        CartItem cartItem = cartItems.get(position);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products").whereEqualTo("productId", cartItem.getProductId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot qds) {
                if (!qds.isEmpty()) {

                    Product product = qds.getDocuments().get(0).toObject(Product.class);

                    holder.productTitle.setText(product.getTitle());
                    holder.productPrice.setText(String.format(Locale.US, "LKR %,.2f", product.getPrice() * cartItem.getQuantity()));
//                    holder.productPrice.setText("LKR " + product.getPrice());
                    StringBuilder attrBuilder = getStringBuilder();
                    holder.productAttr.setText(attrBuilder.toString());
                    holder.productQuantity.setText("Quantity: "+String.valueOf(cartItem.getQuantity()));

                    StorageReference ref = storage.getReference(product.getImages().get(0));

                    GlideApp.with(holder.itemView.getContext())
                            .load(ref)
                            .centerCrop()
                            .placeholder(R.drawable.imageplaceholder2)
                            .into(holder.productImage);

//                holder.itemView.setOnClickListener(v -> {
//
//                    Animation animation = AnimationUtils.loadAnimation(v.getContext(), R.anim.click_animation);
//                    v.startAnimation(animation);
//                    if(listener != null){
//                        listener.onCartItemClick(cartItem);
//                    }
//                });
                }
            }

            @NonNull
            private StringBuilder getStringBuilder() {
                StringBuilder attrBuilder = new StringBuilder();

                if(!cartItem.getAttributes().isEmpty()){
                    for(CartItem.Attribute attribute: cartItem.getAttributes()){
                        attrBuilder.append(attribute.getName().substring(0, 1));
                        attrBuilder.append("-");
                        attrBuilder.append(attribute.getValue());
                        attrBuilder.append(" ");
                    }
                }
                return attrBuilder;
            }
        });

    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productTitle, productPrice, productQuantity, productAttr;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.productImage = itemView.findViewById(R.id.checkout_item_image);
            this.productTitle = itemView.findViewById(R.id.checkout_item_title);
            this.productPrice = itemView.findViewById(R.id.checkout_item_price);
            this.productQuantity = itemView.findViewById(R.id.checkout_item_qty);
            this.productAttr = itemView.findViewById(R.id.checkout_item_attributes);
        }
    }

}
