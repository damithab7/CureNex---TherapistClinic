package lk.damithab.curenex.fragment;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

import lk.damithab.curenex.R;
import lk.damithab.curenex.adapter.ListingAdapter;
import lk.damithab.curenex.adapter.TherapistAdapter;
import lk.damithab.curenex.databinding.FragmentTherapistBinding;
import lk.damithab.curenex.model.Product;
import lk.damithab.curenex.model.Therapist;

public class TherapistFragment extends Fragment {

    private FragmentTherapistBinding binding;

    private TherapistAdapter adapter;

    private String serviceId;

    private FirebaseFirestore db;

    private int completedTasks = 0;
    private final int TOTAL_TASKS = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            serviceId = getArguments().getString("serviceId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTherapistBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.recyclerviewTherapist.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        db = FirebaseFirestore.getInstance();

        startDataLoading(true);

        getActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().getSupportFragmentManager().popBackStack();
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
            binding.shimmerTherapistViewContainer.startShimmer();
            binding.shimmerTherapistViewContainer.setVisibility(View.VISIBLE);
            binding.recyclerviewTherapist.setVisibility(View.GONE);
        } else {
            binding.shimmerTherapistViewContainer.stopShimmer();
            binding.shimmerTherapistViewContainer.setVisibility(View.GONE);
            binding.recyclerviewTherapist.setVisibility(View.VISIBLE);
        }
    }

    private void loadData(){
        db.collection("therapist")
                .whereEqualTo("serviceId", serviceId)
                .whereEqualTo("status", Boolean.TRUE)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(ds->{
                    /// need to find a way to get User details and Service details. UPDATE -> I put workEmail & workMobileNo in therapist document
                    /// I added name in Therapist class so I don't need to get user details.
                    checkAllTasksFinished();
                    if (!ds.isEmpty()){
                        List<Therapist> therapists = ds.toObjects(Therapist.class);

                        adapter = new TherapistAdapter(therapists, therapist -> {
                            Bundle bundle = new Bundle();
                            bundle.putString("therapistId", therapist.getTherapistId());

                            SingleTherapistFragment fragment = new SingleTherapistFragment();
                            fragment.setArguments(bundle);

                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.navContainerView, fragment)
                                    .addToBackStack(null)
                                    .commit();
                        });

                        binding.recyclerviewTherapist.setAdapter(adapter);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        checkAllTasksFinished();
                        Log.e("Firestore", "Error: "+e.getMessage());
                    }
                });
    }
}