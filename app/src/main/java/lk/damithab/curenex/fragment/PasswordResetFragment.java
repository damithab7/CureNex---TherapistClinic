package lk.damithab.curenex.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import lk.damithab.curenex.R;
import lk.damithab.curenex.databinding.FragmentPasswordResetBinding;

public class PasswordResetFragment extends Fragment {

    private FragmentPasswordResetBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPasswordResetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.forgotPasswordBack.setOnClickListener(v->{
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.navContainerView, new SignInEmailFragment())
                    .commit();
        });
    }
}