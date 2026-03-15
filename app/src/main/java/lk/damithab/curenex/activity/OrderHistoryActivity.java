package lk.damithab.curenex.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

import lk.damithab.curenex.R;
import lk.damithab.curenex.adapter.OrdersHistoryAdapter;
import lk.damithab.curenex.databinding.ActivityOrderHistoryBinding;
import lk.damithab.curenex.fragment.HomeFragment;
import lk.damithab.curenex.fragment.OrderItemsFragment;
import lk.damithab.curenex.fragment.OrdersFragment;
import lk.damithab.curenex.model.Order;

public class OrderHistoryActivity extends AppCompatActivity {

    private ActivityOrderHistoryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String orderId = getIntent().getStringExtra("orderId");
        MaterialToolbar toolbar = binding.ordersToolbar;

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        Log.d("OrderHistoryActivity", "onCreate: "+ orderId);
        if (orderId != null) {
            OrderItemsFragment oif = new OrderItemsFragment();
            Bundle bundle = new Bundle();
            bundle.putString("orderId", orderId);
            oif.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.orders_container, oif)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.orders_container, new OrdersFragment())
                    .commit();
        }


    }
}