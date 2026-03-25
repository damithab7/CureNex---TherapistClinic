package lk.damithab.curenex.activity;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import lk.damithab.curenex.R;
import lk.damithab.curenex.adapter.NotificationsAdapter;
import lk.damithab.curenex.databinding.ActivityNotificationBinding;
import lk.damithab.curenex.dialog.SpinnerDialog;
import lk.damithab.curenex.fragment.EmptyNotificationsFragment;
import lk.damithab.curenex.model.Notification;

public class NotificationActivity extends AppCompatActivity {

    private ActivityNotificationBinding binding;

    private FirebaseFirestore db;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        setContentView(binding.getRoot());

        MaterialToolbar toolbar = binding.notificationToolbar;

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        SpinnerDialog spinner = SpinnerDialog.show(getSupportFragmentManager());
        if(auth.getCurrentUser() != null) {
            binding.notificationsRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            db.collection("notifications").whereEqualTo("uid", auth.getUid())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot qds) {
                            spinner.dismiss();
                            if(!qds.isEmpty()){
                                binding.notificationsRecycler.setVisibility(View.VISIBLE);
                                binding.notificationFragmentContainer.setVisibility(View.GONE);
                                List<Notification> notificationList = qds.toObjects(Notification.class);
                                NotificationsAdapter adapter = new NotificationsAdapter(notificationList);
                                binding.notificationsRecycler.setAdapter(adapter);
                            }else {
                                if(savedInstanceState == null){
                                    binding.notificationsRecycler.setVisibility(View.GONE);
                                    binding.notificationFragmentContainer.setVisibility(View.VISIBLE);
                                    getSupportFragmentManager().beginTransaction()
                                            .replace(R.id.notification_fragment_container, new EmptyNotificationsFragment())
                                            .commit();
                                }
                            }
                        }
                    });

        }
    }
}