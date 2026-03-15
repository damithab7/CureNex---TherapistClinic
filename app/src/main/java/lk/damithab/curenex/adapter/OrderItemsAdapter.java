package lk.damithab.curenex.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Locale;

import lk.damithab.curenex.R;
import lk.damithab.curenex.model.Order;
import lk.damithab.curenex.model.Product;
import lk.damithab.curenex.module.GlideApp;

public class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.ViewHolder> {

    private List<Order.OrderItem> orderItems;

    private FirebaseFirestore db;

    private FirebaseStorage storage;

    private OnOrderItemsView listener;

    public OrderItemsAdapter(List<Order.OrderItem> orderItems) {
        this.orderItems = orderItems;
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_item_history, parent, false);
        return new OrderItemsAdapter.ViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order.OrderItem order = orderItems.get(position);
        holder.orderId.setText("#"+order.getProductId());

        db.collection("products").whereEqualTo("productId", order.getProductId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot qds) {
                if (!qds.isEmpty()) {
                    Product product = qds.getDocuments().get(0).toObject(Product.class);
                    holder.orderTitle.setText(product.getTitle());
                    StorageReference ref = storage.getReference(product.getImages().get(0));

                    GlideApp.with(holder.itemView.getContext())
                            .load(ref)
                            .centerCrop()
                            .placeholder(R.drawable.imageplaceholder2)
                            .into(holder.orderImage);
                }
            }
        });

        holder.orderTotal.setText(String.format(Locale.US, "LKR %,.2f",order.getUnitPrice()));
        holder.orderQty.setText("Quantity :"+ String.valueOf(order.getQuantity()));

    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView orderId, orderTitle, orderQty, orderTotal;

        ImageView orderImage;

        Chip orderStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.orderId = itemView.findViewById(R.id.order_item_id);
            this.orderTotal = itemView.findViewById(R.id.order_item_price);
            this.orderTitle = itemView.findViewById(R.id.order_item_title);
            this.orderQty = itemView.findViewById(R.id.order_item_qty);
            this.orderImage = itemView.findViewById(R.id.order_item_image);
            this.orderStatus = itemView.findViewById(R.id.order_item_status_chip);
        }
    }

    public interface OnOrderItemsView {
        void onClick(Order order);
    }
}