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
import lk.damithab.curenex.databinding.FragmentListingBinding;
import lk.damithab.curenex.model.Product;

public class ListingFragment extends Fragment {

    private FragmentListingBinding binding;

    private ListingAdapter adapter;

    private String categoryId;

    private FirebaseFirestore db;

    private int completedTasks = 0;
    private final int TOTAL_TASKS = 1;

    private boolean setLoading = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentListingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.recyclerviewListing.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
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
            binding.shimmerListingViewContainer.startShimmer();
            binding.shimmerListingViewContainer.setVisibility(View.VISIBLE);
            binding.recyclerviewListing.setVisibility(View.GONE);
        } else {
            binding.shimmerListingViewContainer.stopShimmer();
            binding.shimmerListingViewContainer.setVisibility(View.GONE);
            binding.recyclerviewListing.setVisibility(View.VISIBLE);
        }
    }

    private void loadData() {
        db.collection("products")
                .whereEqualTo("categoryId", categoryId)
                .orderBy("title", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(ds -> {
                    checkAllTasksFinished();
                    if (!ds.isEmpty()) {
                        List<Product> products = ds.toObjects(Product.class);

                        adapter = new ListingAdapter(products, product -> {
                            Bundle bundle = new Bundle();
                            bundle.putString("productId", product.getProductId());

                            ProductDetailsFragment productDetailsFragment = new ProductDetailsFragment();
                            productDetailsFragment.setArguments(bundle);

                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.navContainerView, productDetailsFragment)
                                    .addToBackStack(null)
                                    .commit();
                        });

                        binding.recyclerviewListing.setAdapter(adapter);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        checkAllTasksFinished();
                        Log.e("Firestore", "Error: " + e.getMessage());
                    }
                });

    }


}