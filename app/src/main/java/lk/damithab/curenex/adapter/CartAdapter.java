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

import java.util.List;
import java.util.Locale;

import lk.damithab.curenex.R;
import lk.damithab.curenex.model.CartItem;
import lk.damithab.curenex.model.Product;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private List<CartItem> cartItems;

    private OnQuantityChangeListener changeListener;
    private OnRemoveListener removeListener;

    private FirebaseStorage storage;

    public CartAdapter(List<CartItem> cartItems) {
        storage = FirebaseStorage.getInstance();
        this.cartItems = cartItems;
    }

    public void setOnQuantityChangeListener(OnQuantityChangeListener listener) {
        this.changeListener = listener;
    }

    public void setOnRemoveListener(OnRemoveListener listener) {
        this.removeListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
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

                    int currentPosition = holder.getAbsoluteAdapterPosition();
                    if(currentPosition == RecyclerView.NO_POSITION){
                        return;
                    }

                    Product product = qds.getDocuments().get(0).toObject(Product.class);

                    holder.productTitle.setText(product.getTitle());
                    holder.productPrice.setText(String.format(Locale.US, "LKR %,.2f", product.getPrice()));
                    holder.productQuantity.setText(String.valueOf(cartItem.getQuantity()));
                    StringBuilder attrBuilder = getStringBuilder();

                    holder.productAttr.setText(attrBuilder.toString());

                    storage.getReference(product.getImages().get(0))
                            .getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                Glide.with(holder.itemView.getContext())
                                        .load(uri)
                                        .centerCrop()
                                        .into(holder.productImage);
                            });

//                holder.itemView.setOnClickListener(v -> {
//
//                    Animation animation = AnimationUtils.loadAnimation(v.getContext(), R.anim.click_animation);
//                    v.startAnimation(animation);
//                    if(listener != null){
//                        listener.onCartItemClick(cartItem);
//                    }
//                });

                    holder.btnPlus.setOnClickListener(v -> {
                        if(cartItem.getQuantity() < product.getStockCount()) {
                            cartItem.setQuantity(cartItem.getQuantity() + 1);
                            notifyItemChanged(currentPosition);
                            if (changeListener != null) {
                                changeListener.onChanged(cartItem);
                            }
                        }
                    });
                    holder.btnMinus.setOnClickListener(v -> {
                        if(cartItem.getQuantity() > 1) {
                            cartItem.setQuantity(cartItem.getQuantity() - 1);
                            notifyItemChanged(currentPosition);
                            if (changeListener != null) {
                                changeListener.onChanged(cartItem);
                            }
                        }
                    });

                    holder.btnRemove.setOnClickListener(v -> {
                        int pos = holder.getAbsoluteAdapterPosition();
                        Log.i("Position", String.valueOf(pos));
                        if (pos != RecyclerView.NO_POSITION && removeListener != null) {
                            removeListener.onRemoved(currentPosition);
                        }
                    });
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

        AppCompatButton btnPlus, btnMinus;
        MaterialButton btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.productImage = itemView.findViewById(R.id.item_cart_image);
            this.productTitle = itemView.findViewById(R.id.item_cart_title);
            this.productPrice = itemView.findViewById(R.id.item_cart_price);
            this.productQuantity = itemView.findViewById(R.id.item_cart_quantity);
            this.productAttr = itemView.findViewById(R.id.item_cart_attributes);
            this.btnPlus = itemView.findViewById(R.id.item_cart_btn_plus);
            this.btnMinus = itemView.findViewById(R.id.item_cart_btn_minus);
            this.btnRemove = itemView.findViewById(R.id.item_cart_remove);
        }
    }

    public interface OnQuantityChangeListener {
        void onChanged(CartItem cartItem);
    }

    public interface OnRemoveListener {
        void onRemoved(int position);
    }
}
