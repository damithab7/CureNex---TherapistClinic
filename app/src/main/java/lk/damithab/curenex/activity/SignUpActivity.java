package lk.damithab.curenex.activity;

import static lk.damithab.curenex.util.RegexUtil.isCharacterValid;
import static lk.damithab.curenex.util.RegexUtil.isEmailValid;
import static lk.damithab.curenex.util.RegexUtil.isPasswordValid;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

import lk.damithab.curenex.R;
import lk.damithab.curenex.databinding.ActivitySignUpBinding;
import lk.damithab.curenex.model.User;
import lk.damithab.curenex.util.RegexUtil;

public class SignUpActivity extends AppCompatActivity {
    private Button sign_up_back;

    private ActivitySignUpBinding binding;

    private FirebaseAuth firebaseAuth;

    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        this.sign_up_back = binding.signUpBackBtn;
        sign_up_back.setOnClickListener(v -> {
            finish();
        });

        binding.signUpContinueBtn.setOnClickListener(view -> {

            String firstName = binding.firstNameSignUpInput.getText().toString().trim();
            String lastName = binding.lastNameSignUpInput.getText().toString().trim();
            String email = binding.emailSignUpInput.getText().toString().trim();
            String password = binding.passwordSignUpInput.getText().toString().trim();
            String retypePassword = binding.retypePasswordSignUpInput.getText().toString().trim();

            /// Input Listeners
            binding.firstNameSignUpInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {

                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    binding.signInFirstNameLayout.setErrorEnabled(false);
                }
            });

            binding.lastNameSignUpInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {

                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    binding.signInLastNameLayout.setErrorEnabled(false);
                }
            });
            binding.emailSignUpInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {

                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    binding.signInEmailLayout.setErrorEnabled(false);
                }
            });
            binding.passwordSignUpInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {

                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    binding.signInPasswordLayout.setErrorEnabled(false);
                }
            });
            binding.retypePasswordSignUpInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {

                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    binding.signInRetypePasswordLayout.setErrorEnabled(false);
                }
            });

            if (firstName.isEmpty() && lastName.isEmpty() && email.isEmpty() && password.isEmpty() && retypePassword.isEmpty()) {
                binding.signInFirstNameLayout.setErrorEnabled(true);
                binding.signInFirstNameLayout.setError("Firstname is required!");

                binding.signInLastNameLayout.setErrorEnabled(true);
                binding.signInLastNameLayout.setError("Lastname is required!");

                binding.signInEmailLayout.setErrorEnabled(true);
                binding.signInEmailLayout.setError("Email is required!");

                binding.signInPasswordLayout.setErrorEnabled(true);
                binding.signInPasswordLayout.setError("Password is required!");

                binding.signInRetypePasswordLayout.setErrorEnabled(true);
                binding.signInRetypePasswordLayout.setError("Retype password is required!");

                return;

            }

            if (firstName.isEmpty()) {
                binding.signInFirstNameLayout.setErrorEnabled(true);
                binding.signInFirstNameLayout.setError("Firstname is required!");
                binding.firstNameSignUpInput.requestFocus();
                return;
            }
            if (!isCharacterValid(firstName)) {
                binding.signInFirstNameLayout.setErrorEnabled(true);
                binding.firstNameSignUpInput.setError("Invalid firstname!");
                binding.firstNameSignUpInput.requestFocus();
                return;
            }
            if (lastName.isEmpty()) {
                binding.signInLastNameLayout.setErrorEnabled(true);
                binding.signInLastNameLayout.setError("Lastname is required!");
                binding.lastNameSignUpInput.requestFocus();
                return;
            }
            if (!isCharacterValid(lastName)) {
                binding.signInLastNameLayout.setErrorEnabled(true);
                binding.signInLastNameLayout.setError("Invalid lastname!");
                binding.lastNameSignUpInput.requestFocus();
                return;
            }
            if (email.isEmpty()) {
                binding.signInEmailLayout.setErrorEnabled(true);
                binding.signInEmailLayout.setError("Email address is required!");
                binding.emailSignUpInput.requestFocus();
                return;
            }
            if (!isEmailValid(email)) {
                binding.signInEmailLayout.setErrorEnabled(true);
                binding.signInEmailLayout.setError("Invalid email address!");
                binding.emailSignUpInput.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                binding.signInPasswordLayout.setErrorEnabled(true);
                binding.signInPasswordLayout.setError("Password is required!");
                binding.passwordSignUpInput.requestFocus();
                return;
            }

            if (!isPasswordValid(password)) {
                binding.signInPasswordLayout.setErrorEnabled(true);
                binding.signInPasswordLayout.setError("Password must include at least one uppercase letter, one number, one special character & at least 6 characters long.");
                binding.passwordSignUpInput.requestFocus();
                return;
            }

            if (!password.equals(retypePassword)) {
                binding.signInRetypePasswordLayout.setErrorEnabled(true);
                binding.signInRetypePasswordLayout.setError("Passwords do not match. Please re-enter.");
                binding.retypePasswordSignUpInput.requestFocus();
                return;
            }

            binding.signUpProgress.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            binding.firstNameSignUpInput.clearFocus();
            binding.lastNameSignUpInput.clearFocus();
            binding.emailSignUpInput.clearFocus();
            binding.passwordSignUpInput.clearFocus();
            binding.retypePasswordSignUpInput.clearFocus();

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String uid = task.getResult().getUser().getUid();
                                User user = User.builder().uid(uid).firstName(firstName)
                                        .lastName(lastName)
                                        .profileUrl("https://ui-avatars.com/api/" + firstName + "+" + lastName)
                                        .email(email)
                                        .userStatus(true).build();

                                firebaseFirestore.collection("users").document(uid)
                                        .set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                binding.signUpProgress.setVisibility(View.INVISIBLE);
                                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//                                                Toast.makeText(SignUpActivity.this, "Sign-Up success!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                            }
                                        });
                            } else {
                                binding.signUpProgress.setVisibility(View.INVISIBLE);
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    binding.signInEmailLayout.setErrorEnabled(true);
                                    binding.signInEmailLayout.setError("This email is already registered. Please login.");
                                } else {
                                    Log.e("SignUp", "Error: " + task.getException().getMessage());
                                    Toast.makeText(SignUpActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }

                            }
                        }
                    });

        });
    }
}