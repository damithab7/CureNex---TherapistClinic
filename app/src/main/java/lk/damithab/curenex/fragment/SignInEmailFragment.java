package lk.damithab.curenex.fragment;

import static lk.damithab.curenex.util.RegexUtil.isEmailValid;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import lk.damithab.curenex.R;
import lk.damithab.curenex.activity.MainActivity;
import lk.damithab.curenex.activity.SignInActivity;
import lk.damithab.curenex.activity.SignUpActivity;
import lk.damithab.curenex.databinding.FragmentSignInEmailBinding;
import lk.damithab.curenex.dialog.SpinnerDialog;
import lk.damithab.curenex.dialog.ToastDialog;
import lk.damithab.curenex.model.SignInViewModel;
import lk.damithab.curenex.model.User;
import lk.damithab.curenex.util.RegexUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SignInEmailFragment extends Fragment {
    private Button continueToPassword;
    private EditText emailInput;

    private FragmentSignInEmailBinding binding;

    private FirebaseAuth firebaseAuth;

    private FirebaseFirestore db;

    private SpinnerDialog spinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSignInEmailBinding.inflate(inflater);
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView signUpLink = binding.signUpLinkView;
        this.continueToPassword = binding.emailContinueBtn;
        this.emailInput = binding.emailSignInInput;

        signUpLink.setOnClickListener(v -> {
            Intent intent = new Intent(view.getContext(), SignUpActivity.class);
            startActivity(intent);
        });

        emailInput.addTextChangedListener(new TextWatcher() {
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

        continueToPassword.setOnClickListener(v -> {
            Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.button_click);
            v.startAnimation(anim);
            v.postDelayed(() -> {
                String email = emailInput.getText().toString().trim();
                validateEmail(email);
            }, 100);

        });

        /// Google Sign in
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();

        CredentialManager credentialManager = CredentialManager.create(getContext());

        binding.googleContinueBtn.setOnClickListener(v -> {
//            spinner = SpinnerDialog.show(getParentFragmentManager());

            SpinnerDialog spinnerDialog = SpinnerDialog.show(getParentFragmentManager());

            GetCredentialRequest request = new GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build();

            credentialManager.getCredentialAsync(
                    requireContext(),
                    request,
                    null,
                    requireActivity().getMainExecutor(),
                    new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                        @Override
                        public void onResult(GetCredentialResponse result) {
                            spinnerDialog.dismiss();
                            handleSignIn(result.getCredential());
                        }

                        @Override
                        public void onError(GetCredentialException e) {
                            spinnerDialog.dismiss();
//                            if (spinner.isAdded()) {
//                                spinner.dismissAllowingStateLoss();
//                            }
                            Log.e("CureNexAuth", "Sign-in error: " + e.getMessage());
                            Toast.makeText(requireContext(), "Sign-in failed", Toast.LENGTH_SHORT).show();
                        }
                    }
            );


        });
    }

    private void handleSignIn(Credential credential) {
        if (credential instanceof CustomCredential
                && credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {

            GoogleIdTokenCredential googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(((CustomCredential) credential).getData());

            firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());

        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    if(user == null) return;

                    if (authResult.getAdditionalUserInfo() != null &&
                            authResult.getAdditionalUserInfo().isNewUser()) {
                        saveNewUserToFirestore(user);
                    }else{
                        navigateToMain();
                    }

                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthUserCollisionException) {
                        new ToastDialog(getParentFragmentManager(), "This email already has an account. Please login with your password to link the google account");
                    } else {
                        Log.d("SignInEmailFragment", "firebaseAuthWithGoogle: "+e.getMessage());
                    }
                });
    }

    private void saveNewUserToFirestore(FirebaseUser firebaseUser) {
        String fullName = firebaseUser.getDisplayName();
        String firstName = "User";
        String lastName = "";

        if (fullName != null && !fullName.isEmpty()) {
            String[] nameParts = fullName.split(" ", 2);
            firstName = nameParts[0];
            lastName = (nameParts.length > 1) ? nameParts[1] : "";
        }

        User newUser = User.builder()
                .uid(firebaseUser.getUid())
                .firstName(firstName)
                .lastName(lastName)
                .email(firebaseUser.getEmail())
                .profileUrl(firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : "https://ui-avatars.com/api/?name=" + firstName + "+" + lastName)
                .userStatus(true)
                .build();

        db.collection("users").document(newUser.getUid())
                .set(newUser)
                .addOnSuccessListener(aVoid -> {
                    Log.d("CureNex", "User profile created for: " + newUser.getEmail());
                    navigateToMain();
                })
                .addOnFailureListener(e -> {
                    Log.e("CureNex", "Failed to save user", e);
                });
    }

    private void navigateToMain() {
        if (isAdded()) { // Check if fragment is still attached to avoid crashes
            Intent intent = new Intent(requireActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        }
    }

    private void validateEmail(String email) {
        if (email.isEmpty()) {
            binding.signInEmailLayout.setErrorEnabled(true);
            binding.signInEmailLayout.setError("Email address is required.");
        } else if (!isEmailValid(email)) {
            binding.signInEmailLayout.setErrorEnabled(true);
            binding.signInEmailLayout.setError("Invalid email format. Please use the format: name@example.com.");
        } else {
            SignInViewModel viewModel = new ViewModelProvider(requireActivity()).get(SignInViewModel.class);
            viewModel.setEmail(email);
            if (getActivity() != null) {
                ((SignInActivity) getActivity()).loadFragments(new SignInPasswordFragment(), true);
            }
        }
    }
}