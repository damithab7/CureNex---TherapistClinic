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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

import lk.damithab.curenex.R;
import lk.damithab.curenex.adapter.OrdersHistoryAdapter;
import lk.damithab.curenex.databinding.FragmentOrdersBinding;
import lk.damithab.curenex.model.Order;

public class OrdersFragment extends Fragment {

    private FragmentOrdersBinding binding;
    private FirebaseFirestore db;

    private FirebaseStorage storage;

    private FirebaseAuth auth;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        db = FirebaseFirestore.getInstance();
        storage= FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.ordersHistoryRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        db.collection("orders").whereEqualTo("userId", auth.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        if(!qds.isEmpty()){
                            List<Order> ordersList = qds.toObjects(Order.class);
                            OrdersHistoryAdapter adapter = new OrdersHistoryAdapter(ordersList, order -> {
                                OrderItemsFragment fragment = new OrderItemsFragment();
                                Bundle bundle = new Bundle();
                                bundle.putString("orderId", order.getDocId());
                                fragment.setArguments(bundle);

                                getParentFragmentManager().beginTransaction()
                                        .replace(R.id.orders_container, fragment)
                                        .addToBackStack(null)
                                        .commit();
                            });

                            binding.ordersHistoryRecycler.setAdapter(adapter);
                        }
                    }
                });
    }
}