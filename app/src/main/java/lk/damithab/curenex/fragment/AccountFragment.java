package lk.damithab.curenex.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
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
import lk.damithab.curenex.activity.AddressActivity;
import lk.damithab.curenex.activity.MainActivity;
import lk.damithab.curenex.activity.OrderHistoryActivity;
import lk.damithab.curenex.activity.SettingsActivity;
import lk.damithab.curenex.activity.SignInActivity;
import lk.damithab.curenex.databinding.FragmentAccountBinding;
import lk.damithab.curenex.dialog.ProfileDialog;
import lk.damithab.curenex.dialog.SpinnerDialog;
import lk.damithab.curenex.model.User;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;

    FirebaseAuth auth;

    FirebaseFirestore db;

    private FirebaseStorage storage;

    private FirebaseAuth firebaseAuth;

    private int completedTasks = 0;
    private final int TOTAL_TASKS = 2;


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

        startDataLoading(true);

//        binding.accountEditProfileBtn.bringToFront();
        binding.accountEditProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("FragmentAccount", "onViewCreated: Edit Button");
                ProfileDialog dialog = new ProfileDialog();
                dialog.setOnProfileUpdateListener((newImageUri, firstName, lastName) -> {
                    binding.accountUserName.setText(firstName + " " + lastName);
                    if (newImageUri != null) {
                        Glide.with(binding.getRoot())
                                .load(newImageUri)
                                .circleCrop()
                                .into(binding.accountUserImage);
                    }
                });
                dialog.show(getParentFragmentManager(), "ProfileDialog");
            }
        });

        if (firebaseAuth.getCurrentUser() != null) {
            binding.accountBtnSignOut.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), SignInActivity.class);
                startActivity(intent);

                firebaseAuth.signOut();
                ((MainActivity) getActivity()).loadFragment(new HomeFragment());
                ((MainActivity) getActivity()).clearNavigationHeader();

            });
        }

        binding.accountAddressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddressActivity.class);
                startActivity(intent);
//                getParentFragmentManager().beginTransaction()
//                        .replace(R.id.navContainerView, new AddressFragment())
//                        .addToBackStack(null)
//                        .commit();
            }
        });
        binding.accountOrdersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(view.getContext(), OrderHistoryActivity.class);
                startActivity(intent);

            }
        });
        binding.accountSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

    }

    private void checkAllTasksFinished() {
        completedTasks++;
        Log.d("HomeFragment", "checkAllTasksFinished: " + completedTasks);
        if (completedTasks >= TOTAL_TASKS) {
            onDataLoad(false);
            completedTasks = 0; // Reset for swipe-to-refresh
        }
    }

    private void startDataLoading(boolean isShimmer) {
        onDataLoad(isShimmer);
        loadData();
    }

    private synchronized void onDataLoad(boolean isShimmer) {
        if (isShimmer) {
            binding.shimmerListingViewContainer.startShimmer();
            binding.shimmerListingViewContainer.setVisibility(View.VISIBLE);
            binding.accountMain.setVisibility(View.GONE);
        } else {
            binding.shimmerListingViewContainer.stopShimmer();
            binding.shimmerListingViewContainer.setVisibility(View.GONE);
            binding.accountMain.setVisibility(View.VISIBLE);
        }
    }

    private void loadData() {
        db.collection("users").whereEqualTo("uid", auth.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        checkAllTasksFinished();
                        if (!isAdded()) return;
                        if (!qds.isEmpty()) {
                            User user = qds.toObjects(User.class).get(0);
                            binding.accountUserName.setText(user.getFirstName() + " " + user.getLastName());

                            if (user.getProfileUrl().startsWith("https")) {
                                checkAllTasksFinished();
                                Glide.with(binding.getRoot())
                                        .load(user.getProfileUrl())
                                        .centerCrop()
                                        .into(binding.accountUserImage);
                            } else {
                                storage.getReference(user.getProfileUrl())
                                        .getDownloadUrl()
                                        .addOnSuccessListener(uri -> {
                                            checkAllTasksFinished();
                                            Glide.with(binding.getRoot())
                                                    .load(uri)
                                                    .centerCrop()
                                                    .into(binding.accountUserImage);
                                        }).addOnFailureListener(error -> {
                                            checkAllTasksFinished();
                                        });
                            }

                        }


                    }

                }).addOnFailureListener(aVoid -> {
                    checkAllTasksFinished();
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