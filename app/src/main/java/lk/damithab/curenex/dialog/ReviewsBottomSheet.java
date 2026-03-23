package lk.damithab.curenex.dialog;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.RangeSlider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lk.damithab.curenex.R;
import lk.damithab.curenex.databinding.FilterTherapistBottomSheetBinding;
import lk.damithab.curenex.databinding.ReviewsBottomSheetBinding;
import lk.damithab.curenex.model.Gender;
import lk.damithab.curenex.model.Reviews;
import lk.damithab.curenex.model.Service;
import lk.damithab.curenex.model.TherapistFilter;

public class ReviewsBottomSheet extends BottomSheetDialogFragment {

    private OnApplyListener listener;
    private ReviewsBottomSheetBinding binding;

    private FirebaseAuth auth;

    private FirebaseFirestore db;

    private String therapistId;

    private Object obj;

    public interface OnApplyListener {
        void onAdded();
    }

    public interface OnResetListener {
        void onReset();
    }

    public ReviewsBottomSheet(Object obj, OnApplyListener listener) {
        this.listener = listener;
        this.obj = obj;
        this.therapistId = therapistId;
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ReviewsBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MaterialButton apply = binding.reviewsApplyBtn;

        if (obj instanceof Reviews) {
            // already submitted a review
            Reviews review = (Reviews) obj;

            binding.reviewBottomSheetReview.setText(review.getReviewText());
            binding.reviewRating.setRating(review.getReviewRate());

            apply.setEnabled(false);
            binding.reviewBottomSheetReview.setEnabled(false);
        } else {
            apply.setEnabled(true);
            binding.reviewBottomSheetReview.setEnabled(true);
        }

        apply.setOnClickListener(v -> {

            String rateText = binding.reviewBottomSheetReview.getText().toString().trim();
            float starRate = binding.reviewRating.getRating();

            if (starRate == 0) {
                new MessageDialog(getParentFragmentManager(), "Please select a rating");
                return;
            }

            if (rateText.isEmpty()) {
                new MessageDialog(getParentFragmentManager(), "Please leave a rate before apply");
                return;
            }

            Reviews reviews = Reviews.builder().reviewRate(starRate).reviewText(rateText).type("therapist").uid(auth.getUid()).typeId(therapistId).build();

            db.collection("reviews").document().set(reviews)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

            listener.onAdded();
            dismiss();
        });

    }
}
