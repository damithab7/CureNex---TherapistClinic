package lk.damithab.curenex.fragment;

import static lk.damithab.curenex.util.RegexUtil.isPasswordValid;

import android.os.Bundle;
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
            passwordInput.setError("Password is required.");
        } else if (password.length() < 8) {
            passwordInput.setError("Password must be at least 8 characters long.");
        } else if (!isPasswordValid(password)) {
            passwordInput.setError("Password must include at least one uppercase letter, one number, and one special character.");
        }else{
            SignInViewModel viewModel = new ViewModelProvider(requireActivity()).get(SignInViewModel.class);
            String email = viewModel.getEmail();
            ((SignInActivity) getActivity()).login(email, password);
        }
    }
}