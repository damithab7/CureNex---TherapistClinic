package lk.damithab.curenex.fragment;

import static lk.damithab.curenex.util.RegexUtil.isPasswordValid;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import lk.damithab.curenex.R;
import lk.damithab.curenex.activity.SignInActivity;
import lk.damithab.curenex.databinding.FragmentSignInPasswordBinding;
import lk.damithab.curenex.model.SignInViewModel;
import lk.damithab.curenex.util.RegexUtil;


public class SignInPasswordFragment extends Fragment {

    private EditText passwordInput;

    private Button continueToHome;

    private FragmentSignInPasswordBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      binding = FragmentSignInPasswordBinding.inflate(inflater);
      return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.passwordInput = binding.passwordSignInInput;

        this.continueToHome = binding.passwordContinueBtn;

        passwordInput.addTextChangedListener(new TextWatcher() {
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
        continueToHome.setOnClickListener(v -> {
            Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.button_click);
            v.startAnimation(anim);
            v.postDelayed(()->{
                String password = passwordInput.getText().toString().trim();
                validatePassword(password);
            },100);

        });

    }

    private void validatePassword(String password) {
        if (password.isEmpty()) {
            binding.signInPasswordLayout.setErrorEnabled(true);
            binding.signInPasswordLayout.setError("Password is required.");
        } else if (password.length() < 6) {
            binding.signInPasswordLayout.setErrorEnabled(true);
            binding.signInPasswordLayout.setError("Password must be at least 6 characters long.");
        } else{
            SignInViewModel viewModel = new ViewModelProvider(requireActivity()).get(SignInViewModel.class);
            String email = viewModel.getEmail();
            ((SignInActivity) getActivity()).login(email, password);
        }
    }
}