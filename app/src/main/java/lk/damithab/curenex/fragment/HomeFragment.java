package lk.damithab.curenex.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lk.damithab.curenex.R;
import lk.damithab.curenex.activity.SignInActivity;
import lk.damithab.curenex.adapter.HomeProductAdapter;
import lk.damithab.curenex.adapter.HomeServiceAdapter;
import lk.damithab.curenex.adapter.HomeTherapistAdapter;
import lk.damithab.curenex.adapter.PromotionSliderAdapter;
import lk.damithab.curenex.api.ZenAPI;
import lk.damithab.curenex.client.RetrofitClient;
import lk.damithab.curenex.databinding.FragmentHomeBinding;
import lk.damithab.curenex.dto.QuoteDTO;
import lk.damithab.curenex.model.Product;
import lk.damithab.curenex.model.Promotion;
import lk.damithab.curenex.model.Service;
import lk.damithab.curenex.model.Therapist;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private HomeServiceAdapter homeServiceAdapter;
    private HomeTherapistAdapter homeTherapistAdapter;
    private HomeProductAdapter homeProductAdapter;

    private RecyclerView serviceRecyclerView, homeTherapistRecycler, productRecycler;

    private FirebaseFirestore db;

    private int completedTasks = 0;
    private final int TOTAL_TASKS = 4;

    private boolean setLoading = true;

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

        productRecycler = binding.homeProductsRecycle;
        productRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        db = FirebaseFirestore.getInstance();

        binding.homeSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startDataLoading(true);
            }
        });

        if (setLoading) {
            startDataLoading(true);
            setLoading = false;
        } else {
            startDataLoading(false);
        }

        binding.homeServicesSeeAll.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.navContainerView, new ServiceFragment())
                    .commit();
            if (getActivity() != null) {
                com.google.android.material.bottomnavigation.BottomNavigationView navBar =
                        getActivity().findViewById(R.id.bottomNavigationView);

                if (navBar != null) {
                    navBar.getMenu().findItem(R.id.nav_service).setChecked(true);
                }
            }
        });
        binding.homeTherapistsSeeAll.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.navContainerView, new AllTherapistFragment())
                    .commit();
        });
        binding.homeProductsSeeAll.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.navContainerView, new ShopFragment())
                    .commit();
        });

        MaterialButton homeAllTherapistBtn = binding.homeTopLayout.getRoot().findViewById(R.id.home_top_find_btn);
        homeAllTherapistBtn.setOnClickListener(v->{
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.navContainerView, new AllTherapistFragment())
                    .addToBackStack(null)
                    .commit();
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
        binding.homeSwipeRefreshLayout.setRefreshing(false);
        onDataLoad(isShimmer);

        zenApiCalling();
//        loadPromotionalImages();
        loadServices();
        loadProducts();
        loadTherapists();
    }

    private synchronized void onDataLoad(boolean isShimmer) {
        if (isShimmer) {
            binding.shimmerViewContainer.startShimmer();
            binding.shimmerViewContainer.setVisibility(View.VISIBLE);
            binding.homeMain.setVisibility(View.GONE);
        } else {
            binding.shimmerViewContainer.stopShimmer();
            binding.shimmerViewContainer.setVisibility(View.GONE);
            binding.homeMain.setVisibility(View.VISIBLE);
        }
    }


    private void loadTherapists() {
        db.collection("therapist").whereGreaterThan("rating", 4).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        checkAllTasksFinished();

                        binding.homeSwipeRefreshLayout.setRefreshing(false);

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
                }).addOnFailureListener(aVoid -> {
                    checkAllTasksFinished();
                });
    }

    private void loadProducts() {
        db.collection("products").whereGreaterThan("rating", 4).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        checkAllTasksFinished();

                        binding.homeSwipeRefreshLayout.setRefreshing(false);

                        QuerySnapshot result = task.getResult();
                        //List<Category> categories = result.toObjects(Category.class);

                        if (!result.isEmpty()) {
                            List<Product> products = result.toObjects(Product.class);
                            homeProductAdapter = new HomeProductAdapter(products, product -> {

                                Bundle bundle = new Bundle();
                                bundle.putString("productId", product.getProductId());

                                ProductDetailsFragment fragment = new ProductDetailsFragment();
                                fragment.setArguments(bundle);

                                getParentFragmentManager().beginTransaction()
                                        .replace(R.id.navContainerView, fragment)
                                        .addToBackStack(null)
                                        .commit();

                            });

                            homeProductAdapter.setBuyNowListener(product -> {

                                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                                if (firebaseAuth.getCurrentUser() == null) {
                                    Intent intent = new Intent(getActivity(), SignInActivity.class);
                                    startActivity(intent);
                                } else {
                                    CheckoutFragment checkoutFragment = new CheckoutFragment();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("productId", product.getProductId());
                                    bundle.putInt("qty", 1);
                                    checkoutFragment.setArguments(bundle);
                                    getParentFragmentManager().beginTransaction().replace(R.id.navContainerView, checkoutFragment)
                                            .addToBackStack(null)
                                            .commit();
                                }

                            });

                            productRecycler.setAdapter(homeProductAdapter);
                        }

                    }
                }).addOnFailureListener(aVoid -> {
                    checkAllTasksFinished();
                });
    }

    //    private void loadPromotionalImages(){
//        db.collection("promotions")
//                .get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot qds) {
//
//                        checkAllTasksFinished();
//                        if (!qds.isEmpty()) {
//                            List<Promotion> promotions = qds.toObjects(Promotion.class);
//
//                            PromotionSliderAdapter adapter = new PromotionSliderAdapter(promotions);
//                            binding.homePromotionSlider.setAdapter(adapter);
//
//                            binding.homeDotsIndicator.attachTo(binding.homePromotionSlider);
//                        }
//                    }
//                }).addOnFailureListener(aVoid->{
//                    checkAllTasksFinished();
//                });
//
//    }
    private void zenApiCalling() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://zenquotes.io/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ZenAPI zenAPI = retrofit.create(ZenAPI.class);

        Call<List<QuoteDTO>> quoteDTOCall = zenAPI.getQuote();
        quoteDTOCall.enqueue(new Callback<List<QuoteDTO>>() {
            @Override
            public void onResponse(Call<List<QuoteDTO>> call, Response<List<QuoteDTO>> response) {
                checkAllTasksFinished();
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
                checkAllTasksFinished();
                t.printStackTrace();
            }

        });
    }

    private void loadServices() {
        db.collection("services").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        checkAllTasksFinished();
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
                }).addOnFailureListener(aVoid -> {
                    checkAllTasksFinished();
                });

    }
}