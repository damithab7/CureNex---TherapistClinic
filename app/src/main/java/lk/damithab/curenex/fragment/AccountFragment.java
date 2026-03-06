package lk.damithab.curenex.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import lk.damithab.curenex.R;
import lk.damithab.curenex.activity.MainActivity;
import lk.damithab.curenex.activity.OrderHistoryActivity;
import lk.damithab.curenex.activity.SettingsActivity;
import lk.damithab.curenex.activity.SignInActivity;
import lk.damithab.curenex.databinding.FragmentAccountBinding;
import lk.damithab.curenex.dialog.PasswordChangeDialog;
import lk.damithab.curenex.dialog.SpinnerDialog;
import lk.damithab.curenex.model.User;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    Button addressBtn, ordersBtn, changePasswordBtn, settingsBtn;

    FirebaseAuth auth;

    FirebaseFirestore db;

    private FirebaseStorage storage;

    private FirebaseAuth firebaseAuth;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        SpinnerDialog spinner = SpinnerDialog.show(getParentFragmentManager());

        if (firebaseAuth.getCurrentUser() != null) {
            binding.accountBtnSignOut.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), SignInActivity.class);
                startActivity(intent);

                firebaseAuth.signOut();
                ((MainActivity) getActivity()).loadFragment(new HomeFragment());
                ((MainActivity) getActivity()).clearNavigationHeader();

            });
        }

        db.collection("users").whereEqualTo("uid", auth.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        if (!qds.isEmpty()) {
                            User user = qds.toObjects(User.class).get(0);
                            binding.accountUserName.setText(user.getFirstName() + " " + user.getLastName());

                            if (user.getProfileUrl().startsWith("https")) {
                                Glide.with(binding.getRoot())
                                        .load(user.getProfileUrl())
                                        .centerCrop()
                                        .into(binding.accountUserImage);
                            } else {
                                storage.getReference(user.getProfileUrl())
                                        .getDownloadUrl()
                                        .addOnSuccessListener(uri -> {
                                            Glide.with(binding.getRoot())
                                                    .load(uri)
                                                    .centerCrop()
                                                    .into(binding.accountUserImage);
                                        });
                            }

                            binding.accountEditProfileBtn.setOnClickListener(v -> {

                            });
                        }
                        spinner.dismiss();
                    }

                });
        view.findViewById(R.id.accountAddressBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.navContainerView, new AddressFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
        view.findViewById(R.id.accountOrdersBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), OrderHistoryActivity.class);
                startActivity(intent);
            }
        });
        view.findViewById(R.id.accountSettingsBtn).setOnClickListener(new View.OnClickListener() {
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
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}