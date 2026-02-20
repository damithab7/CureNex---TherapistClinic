package lk.damithab.curenex.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import lk.damithab.curenex.R;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);

            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            }
        } else {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }

        setContentView(R.layout.activity_splash_screen);

        ImageView logoView = findViewById(R.id.splashLogo);
//        Picasso.get()
//                .load(R.drawable.ic_launcher_foreground)
//                .resize(300, 300)
//                .centerCrop()
//                .into(logoView);

        Glide.with(this)
                .asBitmap()
                .load(R.drawable.curenexlogo)
                .override(300)
                .circleCrop()
                .into(logoView);

        new Handler(Looper.getMainLooper())
                .postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.splashProgressBar).setVisibility(View.VISIBLE);
                    }
                }, 1000);
        new Handler(Looper.getMainLooper())
                .postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.splashProgressBar).setVisibility(View.INVISIBLE);
                        Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, 5000);
    }
}
