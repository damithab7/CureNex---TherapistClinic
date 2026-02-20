package lk.damithab.curenex.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import lk.damithab.curenex.R;
import lk.damithab.curenex.api.AuthAPI;
import lk.damithab.curenex.client.RetrofitClient;
import lk.damithab.curenex.databinding.ActivitySignInBinding;
import lk.damithab.curenex.dialog.WelcomeDialog;
import lk.damithab.curenex.dto.LoginRequestDTO;
import lk.damithab.curenex.dto.TokenDTO;
import lk.damithab.curenex.fragment.SignInEmailFragment;
import lk.damithab.curenex.manager.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        String accessToken = TokenManager.retrieveAccessToken(this);
        if(accessToken != null && !accessToken.isEmpty()){
            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }else{
            /// Load fragments by fragment transactions
            loadFragments(new SignInEmailFragment(), false);
        }

    }

    public void login(String email, String password) {

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    updateUI(user);
                }else{
                    Toast.makeText(SignInActivity.this, "Authentication failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUI(FirebaseUser user){
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void loadFragments(Fragment fragment, boolean addToStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                        R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.fragment_container, fragment);

        if (addToStack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }


}