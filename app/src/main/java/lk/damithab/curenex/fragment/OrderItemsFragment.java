package lk.damithab.curenex.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import lk.damithab.curenex.R;
import lk.damithab.curenex.adapter.OrderItemsAdapter;
import lk.damithab.curenex.databinding.FragmentOrderItemsBinding;
import lk.damithab.curenex.model.Order;

public class OrderItemsFragment extends Fragment {

    private FragmentOrderItemsBinding binding;

    private FirebaseFirestore db;

    private String orderId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        if (getArguments() != null) {
            this.orderId = getArguments().getString("orderId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       binding = FragmentOrderItemsBinding.inflate(inflater, container, false);
       return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.ordersItemHistoryRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        db.collection("orders").document(orderId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot ds) {
                        if(ds.exists()){
                            Order order = ds.toObject(Order.class);
                            OrderItemsAdapter adapter = new OrderItemsAdapter(order.getOrderItems());
                            binding.ordersItemHistoryRecycler.setAdapter(adapter);
                        }
                    }
                });
    }
}