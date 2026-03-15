package lk.damithab.curenex.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;

import lk.damithab.curenex.R;
import lk.damithab.curenex.databinding.DialogWelcomeBinding;
import lombok.Setter;

public class WelcomeDialog extends DialogFragment {

    private DialogWelcomeBinding binding;

    @Setter
    private View.OnClickListener onContinueClickListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogWelcomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (onContinueClickListener != null) {
            binding.WelcomeScreenContinueBtn.setOnClickListener(v -> {
                onContinueClickListener.onClick(v);
                dismiss();
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                window.setDimAmount(0.7f);

                /// Adjust the dialogFragment width programmatically
                int width = (int)(getResources().getDisplayMetrics().widthPixels * 0.90);
                window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
                window.setBackgroundDrawableResource(android.R.color.transparent);

            }
            dialog.setCanceledOnTouchOutside(false);
        }
    }


}
