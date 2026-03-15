package lk.damithab.curenex.activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import lk.damithab.curenex.R;
import lk.damithab.curenex.databinding.ActivitySupportBinding;
import lk.damithab.curenex.helper.SQLiteHelper;
import lk.damithab.curenex.model.Clinic;

public class SupportActivity extends AppCompatActivity {

    private ActivitySupportBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySupportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MaterialToolbar toolbar = binding.supportToolbar;

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });


        Clinic clinic = getLocalClinicInfo();
        binding.supportPhoneNo.setText(clinic.getPhone());
        binding.supportEmailAddress.setText(clinic.getEmail());
        binding.supportEmergencyNo.setText(clinic.getEmergency());

        Log.i("Tessst", clinic.getName()+" "+clinic.getEmail()+" "+clinic.getAddress());

        binding.supportMapButton.setOnClickListener(v->{
            Intent intent = new Intent(SupportActivity.this, ClinicLocation.class);
            startActivity(intent);
        });
    }

    public Clinic getLocalClinicInfo() {
        SQLiteHelper sqLiteHelper = SQLiteHelper.getInstance(this);
        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM clinic WHERE id = 1", null);

        Clinic clinic = null;
        if (cursor.moveToFirst()) {
            clinic = new Clinic();
            clinic.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            clinic.setAddress(cursor.getString(cursor.getColumnIndexOrThrow("address")));
            clinic.setEmergency(cursor.getString(cursor.getColumnIndexOrThrow("emergency")));
            clinic.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            clinic.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
        }
        cursor.close();
        return clinic;
    }
}