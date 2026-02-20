package lk.damithab.curenex.fragment;

import static lk.damithab.curenex.util.RegexUtil.isEmailValid;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import lk.damithab.curenex.R;
import lk.damithab.curenex.activity.SignInActivity;
import lk.damithab.curenex.activity.SignUpActivity;
import lk.damithab.curenex.databinding.FragmentSignInEmailBinding;
import lk.damithab.curenex.model.SignInViewModel;
import lk.damithab.curenex.util.RegexUtil;

public class SignInEmailFragment extends Fragment {
    private Button continueToPassword;
    private EditText emailInput;

    private FragmentSignInEmailBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSignInEmailBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView signUpLink = binding.signUpLinkView;
        this.continueToPassword = binding.emailContinueBtn;
        this.emailInput = binding.emailSignInInput;

        signUpLink.setOnClickListener(v->{
            Intent intent = new Intent(view.getContext(), SignUpActivity.class);
            startActivity(intent);
        });

        continueToPassword.setOnClickListener(v -> {
            Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.button_click);
            v.startAnimation(anim);
            v.postDelayed(()->{
                String email = emailInput.getText().toString().trim();
                validateEmail(email);
            },100);

        });

    }
    private void validateEmail(String email) {
        if (email.isEmpty()) {
            emailInput.setError("Email address is required.");
        } else if (!isEmailValid(email)) {
            emailInput.setError("Invalid email format. Please use the format: name@example.com.");
        } else {
            SignInViewModel viewModel = new ViewModelProvider(requireActivity()).get(SignInViewModel.class);
            viewModel.setEmail(email);
            if(getActivity() != null){
                ((SignInActivity) getActivity()).loadFragments(new SignInPasswordFragment(), true);
            }
        }
    }
}