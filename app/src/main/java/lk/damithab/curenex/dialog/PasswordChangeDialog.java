package lk.damithab.curenex.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import lk.damithab.curenex.R;

public class PasswordChangeDialog extends DialogFragment {
    public Button continueBtn;

    public PasswordChangeDialog() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_password_change, container, false);

        Button continueBtn = v.findViewById(R.id.WelcomeScreenContinueBtn);
        continueBtn.setOnClickListener(view -> dismiss());

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setGravity(Gravity.BOTTOM);
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                window.setDimAmount(0.7f);

                /// Adjust the dialogFragment width programmatically
//                int width = (int)(getResources().getDisplayMetrics().widthPixels * 0.90);
//                window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
                window.setBackgroundDrawableResource(android.R.color.transparent);

            }
            dialog.setCanceledOnTouchOutside(false);
        }
    }


}
