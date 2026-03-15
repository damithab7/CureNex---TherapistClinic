package lk.damithab.curenex.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

import lk.damithab.curenex.R;
import lk.damithab.curenex.databinding.ActivityVerificationBinding;
import lk.damithab.curenex.dialog.SpinnerDialog;
import lk.damithab.curenex.dialog.ToastDialog;

public class VerificationActivity extends AppCompatActivity {

    private ActivityVerificationBinding binding;
    FirebaseAuth auth;

    FirebaseUser firebaseUser;

    private CountDownTimer countDownTimer;

    private final Handler pollingHandler = new Handler(Looper.getMainLooper());
    private Runnable pollingRunnable;

    private SpinnerDialog spinnerDialog;

    private int completedTasks = 0;
    private final int TOTAL_TASKS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();

        firebaseUser = auth.getCurrentUser();

        spinnerDialog = SpinnerDialog.show(getSupportFragmentManager());
        sendVerificationEmail();

        binding.verificationResendEmailBtn.setOnClickListener(v->{
                sendVerificationEmail();
        });

        binding.verificationBackToHomeBtn.setOnClickListener(v->{
            Intent intent = new Intent(VerificationActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void startResendTimer() {
        binding.verificationResendEmailBtn.setEnabled(false);

        countDownTimer = new CountDownTimer(120000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;

                String timeLeft = String.format(Locale.getDefault(), "Resend in %02d:%02d", minutes, seconds);
                binding.verificationResendEmailBtn.setText(timeLeft);
            }

            @Override
            public void onFinish() {
                binding.verificationResendEmailBtn.setEnabled(true);
                binding.verificationResendEmailBtn.setText("Resend Email");
            }
        }.start();
    }

    private void checkAllTasksFinished() {
        completedTasks++;
        Log.d("HomeFragment", "checkAllTasksFinished: "+completedTasks);
        if (completedTasks >= TOTAL_TASKS) {
            spinnerDialog.dismiss();
            completedTasks = 0;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startPolling();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPolling();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopPolling();
    }

    private void startPolling() {
        stopPolling();
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                if (firebaseUser != null) {
                    firebaseUser.reload().addOnCompleteListener(task -> {
                        if (firebaseUser.isEmailVerified()) {
                            stopPolling();
                            Intent intent = new Intent(VerificationActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP);;
                            startActivity(intent);
                            finish();
                        } else {
                            pollingHandler.postDelayed(this, 3000);
                        }
                    });
                }
            }
        };
        pollingHandler.post(pollingRunnable);
    }

    private void stopPolling() {
        if (pollingHandler != null && pollingRunnable != null) {
            pollingHandler.removeCallbacks(pollingRunnable);
        }
    }

    private void sendVerificationEmail(){
        if (firebaseUser != null) {
            firebaseUser.sendEmailVerification()
                    .addOnSuccessListener(aVoid -> {
                        checkAllTasksFinished();
                        new ToastDialog(getSupportFragmentManager(), "Verification email sent!");
                        startResendTimer();
                    })
                    .addOnFailureListener(e -> {
                        new ToastDialog(getSupportFragmentManager(), "Error: " + e.getMessage());
                    });
        }
    }

//    private void checkEmailVerificationStatus() {
//        if (firebaseUser != null) {
//            firebaseUser.reload().addOnCompleteListener(task -> {
//                if (firebaseUser.isEmailVerified()) {
//                    Intent intent = new Intent(VerificationActivity.this, MainActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(intent);
//                    finish();
//                } else {
//                    Log.d("Verify", "Email not yet verified.");
//                }
//            });
//        }
//    }
}