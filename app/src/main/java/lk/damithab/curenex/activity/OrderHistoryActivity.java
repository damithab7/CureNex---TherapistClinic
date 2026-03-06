package lk.damithab.curenex.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

import lk.damithab.curenex.R;
import lk.damithab.curenex.adapter.OrdersHistoryAdapter;
import lk.damithab.curenex.databinding.ActivityOrderHistoryBinding;
import lk.damithab.curenex.model.Order;

public class OrderHistoryActivity extends AppCompatActivity {

    private ActivityOrderHistoryBinding binding;

    private FirebaseFirestore db;

    private FirebaseStorage storage;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        storage= FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        binding.ordersHistoryRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        db.collection("orders").whereEqualTo("userId", auth.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        if(!qds.isEmpty()){
                            List<Order> ordersList = qds.toObjects(Order.class);
                            OrdersHistoryAdapter adapter = new OrdersHistoryAdapter(ordersList, order -> {

                            });

                            binding.ordersHistoryRecycler.setAdapter(adapter);
                        }
                    }
                });

    }
}