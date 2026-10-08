package lk.damithab.curenex.dialog;

import static lk.damithab.curenex.util.RegexUtil.isCharacterValid;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

import lk.damithab.curenex.databinding.DialogProfileDialogBinding;
import lk.damithab.curenex.model.User;
import lk.damithab.curenex.module.GlideApp;

public class ProfileDialog extends DialogFragment {

    String firstName, lastName;
    Uri imageUri;
    Uri cameraImageUri;
    private DialogProfileDialogBinding binding;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage storage;
    ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode() == Activity.RESULT_OK
                                && result.getData() != null
                                && result.getData().getData() != null) {

                            Uri uri = result.getData().getData();

                            uploadProfileImage(uri);
                        }
                    }
            );
    ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.TakePicture(),
                    success -> {

                        if (success && cameraImageUri != null) {

                            uploadProfileImage(cameraImageUri);
                        }
                    }
            );
    ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {

                        if (isGranted) {

                            openCamera();

                        } else {

                            Toast.makeText(
                                    requireContext(),
                                    "Camera permission is required",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );
    private OnProfileUpdateListener listener;

    public ProfileDialog() {
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public void setOnProfileUpdateListener(OnProfileUpdateListener listener) {
        this.listener = listener;
    }

    // ---------------------------------------------------------
    // GALLERY
    // ---------------------------------------------------------

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        binding = DialogProfileDialogBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {

            firebaseFirestore.collection("users")
                    .document(firebaseAuth.getUid())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {

                        @Override
                        public void onSuccess(DocumentSnapshot ds) {

                            if (ds.exists()) {

                                User user = ds.toObject(User.class);

                                if (user != null) {

                                    if (user.getProfileUrl() != null) {

                                        if (user.getProfileUrl().startsWith("https")) {

                                            GlideApp.with(binding.getRoot())
                                                    .load(user.getProfileUrl())
                                                    .centerCrop()
                                                    .into(binding.mainImageView);

                                            imageUri = Uri.parse(user.getProfileUrl());

                                        } else {

                                            storage.getReference(user.getProfileUrl())
                                                    .getDownloadUrl()
                                                    .addOnSuccessListener(uri -> {

                                                        GlideApp.with(binding.getRoot())
                                                                .load(uri)
                                                                .centerCrop()
                                                                .into(binding.mainImageView);

                                                        imageUri = uri;
                                                    });
                                        }
                                    }

                                    binding.firstNameProfileInput.setText(
                                            user.getFirstName()
                                    );

                                    binding.lastNameProfileInput.setText(
                                            user.getLastName()
                                    );
                                }
                            }
                        }
                    });

            // Save profile
            binding.saveProfileBtn.setOnClickListener(v -> {

                firstName = binding.firstNameProfileInput
                        .getText()
                        .toString()
                        .trim();

                lastName = binding.lastNameProfileInput
                        .getText()
                        .toString()
                        .trim();

                if (firstName.isEmpty()) {
                    binding.firstNameProfileInput.setError(
                            "First name is required"
                    );
                    return;
                }

                if (lastName.isEmpty()) {
                    binding.lastNameProfileInput.setError(
                            "Last name is required"
                    );
                    return;
                }

                if (!isCharacterValid(firstName)) {
                    binding.firstNameProfileInput.setError(
                            "Invalid first name"
                    );
                    return;
                }

                if (!isCharacterValid(lastName)) {
                    binding.lastNameProfileInput.setError(
                            "Invalid last name"
                    );
                    return;
                }

                firebaseFirestore.collection("users")
                        .document(firebaseAuth.getUid())
                        .update(
                                "firstName",
                                firstName,
                                "lastName",
                                lastName
                        )
                        .addOnSuccessListener(new OnSuccessListener<Void>() {

                            @Override
                            public void onSuccess(Void unused) {

                                new ToastDialog(
                                        getActivity().getSupportFragmentManager(),
                                        "Profile updated successfully!"
                                );

                                if (listener != null) {

                                    listener.onProfileUpdated(
                                            imageUri,
                                            firstName,
                                            lastName
                                    );
                                }

                                dismiss();
                            }
                        });
            });

            // Edit profile image
            binding.profileEditImage.setOnClickListener(v -> {

                String[] options = {
                        "Gallery",
                        "Camera"
                };

                new AlertDialog.Builder(requireContext())
                        .setTitle("Choose Profile Picture")
                        .setItems(options, (dialog, which) -> {

                            if (which == 0) {

                                // Gallery
                                openGallery();

                            } else {

                                // Camera
                                openCameraWithPermission();
                            }
                        })
                        .show();
            });
        }
    }

    // ---------------------------------------------------------
    // CAMERA PERMISSION
    // ---------------------------------------------------------

    private void openGallery() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        intent.setType("image/*");

        activityResultLauncher.launch(intent);
    }

    private void openCameraWithPermission() {

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) {

            openCamera();

        } else {

            cameraPermissionLauncher.launch(
                    Manifest.permission.CAMERA
            );
        }
    }

    // ---------------------------------------------------------
    // CAMERA
    // ---------------------------------------------------------

    private void openCamera() {

        File imageFile = new File(
                requireContext().getCacheDir(),
                "profile_" + System.currentTimeMillis() + ".jpg"
        );

        cameraImageUri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".fileprovider",
                imageFile
        );

        cameraLauncher.launch(cameraImageUri);
    }

    private void uploadProfileImage(Uri uri) {

        // Show selected image immediately
        Glide.with(requireActivity())
                .load(uri)
                .circleCrop()
                .into(binding.mainImageView);

        String imageId = firebaseAuth.getUid();

        if (imageId == null) {
            return;
        }

        StorageReference imageReference =
                storage.getReference("profile-images")
                        .child(imageId);

        imageReference.putFile(uri)
                .addOnSuccessListener(takeSnapshot -> {

                    firebaseFirestore.collection("users")
                            .document(firebaseAuth.getUid())
                            .update(
                                    "profileUrl",
                                    "profile-images/" + imageId
                            )
                            .addOnSuccessListener(aVoid -> {

                                imageUri = uri;

                                Toast.makeText(
                                        requireContext(),
                                        "Profile picture updated",
                                        Toast.LENGTH_SHORT
                                ).show();
                            });

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            requireContext(),
                            "Failed to upload image",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    // ---------------------------------------------------------
    // UPLOAD PROFILE IMAGE
    // ---------------------------------------------------------

    @Override
    public void onStart() {

        super.onStart();

        Dialog dialog = getDialog();

        if (dialog != null) {

            Window window = dialog.getWindow();

            if (window != null) {

                window.setGravity(Gravity.BOTTOM);

                window.addFlags(
                        WindowManager.LayoutParams.FLAG_DIM_BEHIND
                );

                window.setDimAmount(0.7f);

                window.setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

                window.setBackgroundDrawableResource(
                        android.R.color.transparent
                );
            }

            dialog.setCanceledOnTouchOutside(true);
        }
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();

        binding = null;
    }

    public interface OnProfileUpdateListener {
        void onProfileUpdated(Uri newImageUri, String firstName, String lastName);
    }
}