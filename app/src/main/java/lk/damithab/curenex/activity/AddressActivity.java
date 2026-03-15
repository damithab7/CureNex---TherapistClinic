package lk.damithab.curenex.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import lk.damithab.curenex.R;
import lk.damithab.curenex.fragment.AddressFragment;

public class AddressActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        MaterialToolbar toolbar = findViewById(R.id.address_toolbar);

        toolbar.setNavigationOnClickListener(v -> finish());

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.address_fragment_container, new AddressFragment())
                    .commit();
        }

    }
}