package lk.damithab.curenex.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Objects;

import lk.damithab.curenex.R;
import lk.damithab.curenex.activity.AddressActivity;
import lk.damithab.curenex.activity.ChangePasswordActivity;
import lk.damithab.curenex.activity.OrderHistoryActivity;
import lk.damithab.curenex.activity.SettingsActivity;

public class AccountFragment extends Fragment {

    Button addressBtn, ordersBtn, changePasswordBtn, settingsBtn;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.accountAddressBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), AddressActivity.class);
                startActivity(intent);
            }
        });        view.findViewById(R.id.accountOrdersBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), OrderHistoryActivity.class);
                startActivity(intent);
            }
        });        view.findViewById(R.id.accountPasswordBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ChangePasswordActivity.class);
                startActivity(intent);
            }
        });        view.findViewById(R.id.accountSettingsBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(((AppCompatActivity)getActivity()).getSupportActionBar()).hide();
    }

    @Override
    public void onStop() {
        super.onStop();
        Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).show();
    }
}