package lk.damithab.curenex.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

import lk.damithab.curenex.R;
import lk.damithab.curenex.adapter.HomeServiceAdapter;
import lk.damithab.curenex.adapter.ServiceAdapter;
import lk.damithab.curenex.databinding.FragmentHomeBinding;
import lk.damithab.curenex.databinding.FragmentServiceBinding;
import lk.damithab.curenex.dialog.SpinnerDialog;
import lk.damithab.curenex.model.Service;
import lk.damithab.curenex.model.Therapist;

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
        serviceRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));

        FirebaseFirestore db = FirebaseFirestore.getInstance();

//        Therapist t1 = new Therapist(
//                "tid1",
//                "EqldpazzG5erZSgYNCVk8Fyx0xw2",
//                "ser2",
//                "gender1",
//                "Dr.",
//                "A senior clinical psychologist specializing in Cognitive Behavioral Therapy (CBT). She has extensive experience in treating clinical depression, chronic anxiety, and PTSD. Her approach focuses on empowering patients with practical tools to manage their emotional well-being and achieve long-term mental resilience.",
//                7500.0
//        );
//
//        Therapist t2 = new Therapist(
//                "tid2",
//                "HzCCHBHjNmY4XUPQiy8AjMNkBJG2",
//                "ser2",
//                "gender2",
//                "Dr.",
//                "Specializes in adolescent mental health and family dynamics. He uses an integrative therapeutic approach to help young adults navigate stress, identity challenges, and academic pressure. He is highly regarded for his research into mindfulness-based stress reduction and its application in modern fast-paced environments.",
//                6500.0
//        );
//
//        Therapist t3 = new Therapist(
//                "tid3",
//                "bNGKVYISjCND4QFN8QHU12iy51X2",
//                "ser2",
//                "gender1",
//                "Ms.",
//                "A compassionate counselor with a focus on relationship therapy and emotional intelligence. She provides a safe, non-judgmental space for individuals and couples to work through communication barriers, grief, and self-esteem issues. Her sessions are tailored to help clients reconnect with their inner strengths and find balance.",
//                5000.0
//        );
//
//                List<Therapist> therapists = List.of(t1,t2,t3);
//
//        WriteBatch batch = db.batch();
//
//        for(Therapist p: therapists){
//            DocumentReference ref = db.collection("therapist").document();
//            batch.set(ref, p);
//        }
//
//        batch.commit();

        SpinnerDialog spinner = SpinnerDialog.show(getParentFragmentManager());

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
                                    .replace(R.id.navContainerView, fragment)
                                    .addToBackStack(null)
                                    .commit();

                        });

                        spinner.dismiss();

                        serviceRecyclerView.setAdapter(serviceAdapter);

                    }
                });
    }
}