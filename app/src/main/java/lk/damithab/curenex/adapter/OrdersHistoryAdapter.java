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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;
import java.util.Locale;

import lk.damithab.curenex.R;
import lk.damithab.curenex.model.Address;
import lk.damithab.curenex.model.Booking;
import lk.damithab.curenex.model.Order;
import lk.damithab.curenex.model.Therapist;

public class OrdersHistoryAdapter extends RecyclerView.Adapter<OrdersHistoryAdapter.ViewHolder> {

    private List<Order> ordersList;

    private FirebaseFirestore db;

    private FirebaseStorage storage;

    private OnOrderItemsView listener;

    public OrdersHistoryAdapter(List<Order> ordersList, OnOrderItemsView listener) {
        this.ordersList = ordersList;
        this.listener = listener;
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_history, parent, false);
        return new OrdersHistoryAdapter.ViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = ordersList.get(position);
        holder.orderId.setText("#"+order.getOrderId());
        holder.orderDate.setText(order.getOrderDate().toDate().toString());
        holder.orderStatus.setText(order.getStatus());
        holder.orderTotal.setText(String.format(Locale.US, "LKR %,.2f",order.getTotalAmount()));

        holder.viewMore.setOnClickListener(v -> {
            Animation animation = AnimationUtils.loadAnimation(v.getContext(), R.anim.button_click);
            v.startAnimation(animation);
            if (listener != null) {
                listener.onClick(order);
            }
        });

    }

    @Override
    public int getItemCount() {
        return ordersList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView orderId, orderDate, orderStatus, orderTotal;


        MaterialButton viewMore;

        CardView statusCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.orderId = itemView.findViewById(R.id.orders_id);
            this.orderDate = itemView.findViewById(R.id.orders_date);
            this.orderTotal = itemView.findViewById(R.id.orders_total);
            this.orderStatus = itemView.findViewById(R.id.orders_status);
            this.statusCard = itemView.findViewById(R.id.orders_status_card);
            this.viewMore = itemView.findViewById(R.id.view_order_items_btn);
        }
    }

    public interface OnOrderItemsView {
        void onClick(Order order);
    }
}
