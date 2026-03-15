package lk.damithab.curenex.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import lk.damithab.curenex.R;
import lk.damithab.curenex.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;

    private Switch themeChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("CureNexPrefs", MODE_PRIVATE);
        boolean followOs = prefs.getBoolean("follow_os", true);
        boolean isDark = prefs.getBoolean("is_dark", false);

        if (followOs) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else {
            AppCompatDelegate.setDefaultNightMode(isDark ?
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.settingsDefaultOsTheme.setChecked(followOs);
        binding.settingsSwitchTheme.setEnabled(!followOs);
        binding.settingsSwitchTheme.setChecked(isDark);

        MaterialToolbar toolbar = findViewById(R.id.settings_toolbar);

        toolbar.setNavigationOnClickListener(v -> finish());

        // 3. Attach Listeners AFTER the UI is set
        setupListeners();
    }

    private void setupListeners() {
        SharedPreferences.Editor editor = getSharedPreferences("CureNexPrefs", MODE_PRIVATE).edit();

        binding.settingsDefaultOsTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("follow_os", isChecked).apply();

            if (isChecked) {
                binding.settingsSwitchTheme.setEnabled(false);
                applyTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            } else {
                binding.settingsSwitchTheme.setEnabled(true);
                // Apply current state of the manual switch
                applyTheme(binding.settingsSwitchTheme.isChecked() ?
                        AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        binding.settingsSwitchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("is_dark", isChecked).apply();

            if (!binding.settingsDefaultOsTheme.isChecked()) {
                applyTheme(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
    }

    private void applyTheme(int mode) {
        if (AppCompatDelegate.getDefaultNightMode() != mode) {
            AppCompatDelegate.setDefaultNightMode(mode);
        }
    }
}