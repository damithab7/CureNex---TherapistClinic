package lk.damithab.curenex.fragment;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

import lk.damithab.curenex.R;
import lk.damithab.curenex.adapter.AllTherapistAdapter;
import lk.damithab.curenex.databinding.FragmentAllTherapistBinding;
import lk.damithab.curenex.dialog.FilterTherapistBottomSheet;
import lk.damithab.curenex.listener.FirestoreCallback;
import lk.damithab.curenex.model.Therapist;

public class AllTherapistFragment extends Fragment {

    private FragmentAllTherapistBinding binding;

    private FirebaseFirestore db;

    private FirebaseStorage storage;
    private int completedTasks = 0;
    private final int TOTAL_TASKS = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAllTherapistBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.allTherapistRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        startDataLoading(true);
        loadAllTherapists();

        getActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        binding.allTherapistFilterBtn.setOnClickListener(v -> {


            FilterTherapistBottomSheet sheet = new FilterTherapistBottomSheet((therapistFilter) -> {
                Query query = db.collection("therapist");

                if (therapistFilter.getGenderId() != null) {
                    query = query.whereEqualTo("genderId", therapistFilter.getGenderId());
                }
                if (therapistFilter.getServiceId() != null) {
                    query = query.whereEqualTo("serviceId", therapistFilter.getServiceId());
                }

                query = query.whereGreaterThanOrEqualTo("rate", therapistFilter.getStartPrice())
                        .whereLessThanOrEqualTo("rate", therapistFilter.getEndPrice())
                        .whereEqualTo("status", Boolean.TRUE);

                startDataLoading(true);

                query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        startDataLoading(false);
                        List<Therapist> therapistList = new ArrayList<>();
                        AllTherapistAdapter adapter = new AllTherapistAdapter(therapistList, therapist -> {
                        });

                        if (!qds.isEmpty()) {
                            therapistList = qds.toObjects(Therapist.class);
                            adapter = new AllTherapistAdapter(therapistList, therapist -> {
                                Bundle bundle = new Bundle();
                                bundle.putString("therapistId", therapist.getTherapistId());

                                SingleTherapistFragment fragment = new SingleTherapistFragment();
                                fragment.setArguments(bundle);

                                getParentFragmentManager().beginTransaction()
                                        .replace(R.id.navContainerView, fragment)
                                        .addToBackStack(null)
                                        .commit();
                            });
                            binding.allTherapistResultCount.setText(String.valueOf(qds.size()));

                        } else {
                            binding.allTherapistResultCount.setText("No Results");
                        }

                        binding.allTherapistRecycler.setAdapter(adapter);
                    }
                }).addOnFailureListener(error -> {
                    startDataLoading(false);
                });
            }, ()->{
                loadAllTherapists();
                startDataLoading(false);
            });

            sheet.show(getChildFragmentManager(), "FilterAllTherapist");
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
    }

    private synchronized void onDataLoad(boolean isShimmer) {
        if (isShimmer) {
            binding.shimmerAllTherapistContainer.startShimmer();
            binding.shimmerAllTherapistContainer.setVisibility(View.VISIBLE);
            binding.allTherapistMain.setVisibility(View.GONE);
        } else {
            binding.shimmerAllTherapistContainer.stopShimmer();
            binding.shimmerAllTherapistContainer.setVisibility(View.GONE);
            binding.allTherapistMain.setVisibility(View.VISIBLE);
        }
    }


    private void loadAllTherapists() {
        getAllTherapists(therapists -> {
            AllTherapistAdapter adapter = new AllTherapistAdapter(therapists, therapist -> {
                Bundle bundle = new Bundle();
                bundle.putString("therapistId", therapist.getTherapistId());

                SingleTherapistFragment fragment = new SingleTherapistFragment();
                fragment.setArguments(bundle);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.navContainerView, fragment)
                        .addToBackStack(null)
                        .commit();
            });
            binding.allTherapistResultCount.setText(String.valueOf(therapists.size()));
            binding.allTherapistRecycler.setAdapter(adapter);
        });
    }

    private void getAllTherapists(FirestoreCallback<List<Therapist>> callback) {
        db.collection("therapist")
                .whereEqualTo("status", Boolean.TRUE)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        checkAllTasksFinished();
                        if (!qds.isEmpty()) {
                            List<Therapist> therapists = qds.toObjects(Therapist.class);
                            callback.onCallback(therapists);
                        }
                    }
                }).addOnFailureListener(error -> {
                    checkAllTasksFinished();
                });
    }
}