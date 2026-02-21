package lk.damithab.curenex.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import lk.damithab.curenex.R;
import lk.damithab.curenex.adapter.HomeServiceAdapter;
import lk.damithab.curenex.adapter.ServiceAdapter;
import lk.damithab.curenex.databinding.FragmentHomeBinding;
import lk.damithab.curenex.databinding.FragmentServiceBinding;
import lk.damithab.curenex.model.Service;

public class ServiceFragment extends Fragment {

    private FragmentServiceBinding binding;

    private ServiceAdapter serviceAdapter;

    private RecyclerView serviceRecyclerView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentServiceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        serviceRecyclerView = binding.recyclerviewService;
        serviceRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("services").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        QuerySnapshot result = task.getResult();
                        //List<Category> categories = result.toObjects(Category.class);

                        List<Service> services = task.getResult().toObjects(Service.class);
                        serviceAdapter = new ServiceAdapter(services, service -> {

                            Bundle bundle = new Bundle();
                            bundle.putString("serviceId", service.getServiceId());

                            TherapistFragment fragment = new TherapistFragment();
                            fragment.setArguments(bundle);

                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit();

                        });

                        serviceRecyclerView.setAdapter(serviceAdapter);

                    }
                });
    }
}