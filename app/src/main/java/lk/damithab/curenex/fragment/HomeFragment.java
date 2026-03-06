package lk.damithab.curenex.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import lk.damithab.curenex.R;
import lk.damithab.curenex.adapter.HomeServiceAdapter;
import lk.damithab.curenex.adapter.HomeTherapistAdapter;
import lk.damithab.curenex.adapter.PromotionSliderAdapter;
import lk.damithab.curenex.api.ZenAPI;
import lk.damithab.curenex.client.RetrofitClient;
import lk.damithab.curenex.databinding.FragmentHomeBinding;
import lk.damithab.curenex.dto.QuoteDTO;
import lk.damithab.curenex.model.Promotion;
import lk.damithab.curenex.model.Service;
import lk.damithab.curenex.model.Therapist;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private HomeServiceAdapter homeServiceAdapter;
    private HomeTherapistAdapter homeTherapistAdapter;

    private RecyclerView serviceRecyclerView, homeTherapistRecycler;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        serviceRecyclerView = binding.homeServicesRecycle;
        serviceRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        homeTherapistRecycler = binding.bestTherapistHomeRecycle;
        homeTherapistRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        /// Zen API Calling
        Retrofit instance = RetrofitClient.getInstance(getContext());
        ZenAPI zenAPI = instance.create(ZenAPI.class);

        Call<List<QuoteDTO>> quoteDTOCall = zenAPI.getQuote();
        quoteDTOCall.enqueue(new Callback<List<QuoteDTO>>() {
            @Override
            public void onResponse(Call<List<QuoteDTO>> call, Response<List<QuoteDTO>> response) {
                if (response.isSuccessful()) {
                    QuoteDTO quoteDTO = response.body().get(0);
                    Log.i("HomeFra", quoteDTO.getQ());
                    if (quoteDTO != null) {
                        binding.homeQuoteTxt.setText(quoteDTO.getQ());
                        binding.homeQuoteAuthor.setText(quoteDTO.getA());
                    }
                } else {
                    Log.i("ZenAPIError", response.message());
                }
            }

            @Override
            public void onFailure(Call<List<QuoteDTO>> call, Throwable t) {
                t.printStackTrace();
            }

        });

        /// Load Promotional Images
        db.collection("promotions")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {

                        if (!qds.isEmpty()) {
                            List<Promotion> promotions = qds.toObjects(Promotion.class);

                            PromotionSliderAdapter adapter = new PromotionSliderAdapter(promotions);
                            binding.homePromotionSlider.setAdapter(adapter);

                            binding.homeDotsIndicator.attachTo(binding.homePromotionSlider);
                        }
                    }
                });


//        Service s1 =new Service("ser1", "Physiotherapy", "https://loremflickr.com/400/400/physiotherapy?lock=15");
//        Service s2 =new Service("ser2", "Psychotherapy", "https://loremflickr.com/400/400/psychotherapy?lock=1");
//        Service s3 =new Service("ser3", "Ergonomics", "https://loremflickr.com/400/400/office,posture?lock=23");
//        Service s4 =new Service("ser4", "Nutrition", "https://loremflickr.com/400/400/healthy,food?lock=4");
//        Service s5 =new Service("ser5", "Psychodynamic", "https://loremflickr.com/400/400/psychology?lock=21");
//        Service s6 =new Service("ser6", "Interpersonal", "https://loremflickr.com/400/400/nurse?lock=17");
//
//        List<Service> serv =List.of(s1, s2, s3 , s4, s5, s6);
//
//        WriteBatch batch =db.batch();
//
//        for(Service service: serv){
//            DocumentReference ref = db.collection("services").document();
//            batch.set(ref, service);
//        }
//
//        batch.commit();

        binding.homeServicesSeeAll.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.navContainerView, new ServiceFragment())
                    .commit();
        });
        binding.homeTherapistsSeeAll.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.navContainerView, new TherapistFragment())
                    .commit();
        });

        db.collection("services").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        QuerySnapshot result = task.getResult();
                        //List<Category> categories = result.toObjects(Category.class);

                        List<Service> services = task.getResult().toObjects(Service.class);
                        homeServiceAdapter = new HomeServiceAdapter(services, service -> {

                            Bundle bundle = new Bundle();
                            bundle.putString("serviceId", service.getServiceId());

                            TherapistFragment fragment = new TherapistFragment();
                            fragment.setArguments(bundle);

                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.navContainerView, fragment)
                                    .addToBackStack(null)
                                    .commit();

                        });

                        serviceRecyclerView.setAdapter(homeServiceAdapter);

                    }
                });

        db.collection("therapist").whereGreaterThan("rating", 4).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        QuerySnapshot result = task.getResult();
                        //List<Category> categories = result.toObjects(Category.class);

                        if (!result.isEmpty()) {
                            List<Therapist> therapists = result.toObjects(Therapist.class);
                            homeTherapistAdapter = new HomeTherapistAdapter(therapists, therapist -> {

                                Bundle bundle = new Bundle();
                                bundle.putString("therapistId", therapist.getTherapistId());

                                SingleTherapistFragment fragment = new SingleTherapistFragment();
                                fragment.setArguments(bundle);

                                getParentFragmentManager().beginTransaction()
                                        .replace(R.id.navContainerView, fragment)
                                        .addToBackStack(null)
                                        .commit();

                            });

                            homeTherapistRecycler.setAdapter(homeTherapistAdapter);
                        }

                    }
                });
    }
}