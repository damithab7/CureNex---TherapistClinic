package lk.damithab.curenex.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.Arrays;
import java.util.List;

import lk.damithab.curenex.R;
import lk.damithab.curenex.adapter.CategoryAdapter;
import lk.damithab.curenex.databinding.FragmentShopBinding;
import lk.damithab.curenex.dialog.SpinnerDialog;
import lk.damithab.curenex.model.Category;
import lk.damithab.curenex.model.Product;
import lk.damithab.curenex.model.TherapistSchedule;

public class ShopFragment extends Fragment {

    private FragmentShopBinding binding;

    private CategoryAdapter adapter;

    private RecyclerView categoryRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentShopBinding.inflate(inflater, container, false);
        return  binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

//        Category c1 = new Category("cat1", "Support", "https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?q=80&w=500");
//        Category c2 = new Category("cat2", "Gear", "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?q=80&w=500");
//        Category c3 = new Category("cat3", "Supplements", "https://images.unsplash.com/photo-1584017911766-d451b3d0e843?q=80&w=500");
//        Category c4 = new Category("cat4", "Ergonomics Supplies", "https://images.unsplash.com/photo-1593642702749-b7d2a804fbcf?q=80&w=500");
//        Category c5 = new Category("cat5", "Therapy Tools", "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?q=80&w=500");
//        Category c6 = new Category("cat6", "Fitness & Recovery", "https://images.unsplash.com/photo-1605296867304-46d5465a13f1?q=80&w=500");
//        Category c7 = new Category("cat7", "Mental Wellness", "https://images.unsplash.com/photo-1506126613408-eca07ce68773?q=80&w=500");
//        Category c8 = new Category("cat8", "Diagnostic Kits", "https://images.unsplash.com/photo-1576091160550-2173dba999ef?q=80&w=500");
//
//        List<Category> cats = List.of(c1, c2, c3, c4, c5, c6, c7, c8);
//
//        WriteBatch batch = db.batch();
//
//        for(Category c: cats){
//            DocumentReference ref = db.collection("categories").document();
//            batch.set(ref, c);
//        }
//
//        batch.commit();


//        Product p1 = new Product(
//                "pid1",
//                "Orthopedic Lumbar Support",
//                "High-density memory foam backrest for spinal alignment.",
//                5800.0,
//                "cat1",
//                Arrays.asList("https://images.unsplash.com/photo-1588058364549-71f353df190a?w=500", "https://images.unsplash.com/photo-1590069230005-db393739a7ec?w=500"),
//                25,
//                true
//        );
//
//        Product p2 = new Product(
//                "pid2",
//                "Adjustable Elbow Brace",
//                "Compression sleeve for tendonitis and tennis elbow relief.",
//                1950.0,
//                "cat1",
//                Arrays.asList("https://images.unsplash.com/photo-1598440499033-547119f8c4c9?w=500", "https://images.unsplash.com/photo-1581093450021-4a7360e9a6ad?w=500"),
//                40,
//                true
//        );
//
//        Product p3 = new Product(
//                "pid3",
//                "Cervical Neck Collar",
//                "Soft foam support for neck pain and post-injury recovery.",
//                3200.0,
//                "cat1",
//                Arrays.asList("https://images.unsplash.com/photo-1576091160550-2173dba999ef?w=500", "https://images.unsplash.com/photo-1516549655169-df83a0774514?w=500"),
//                15,
//                true
//        );
//        List<Product> products = List.of(p1,p2,p3);
//
//        WriteBatch batch = db.batch();
//
//        for(Product p: products){
//            DocumentReference ref = db.collection("products").document();
//            batch.set(ref, p);
//        }
//
//        batch.commit();

        // Tuesday (Day 3) Slots
//        TherapistSchedule t1 = new TherapistSchedule(null, 3, "09:00", 3, "tid1", true);
//        TherapistSchedule t2 = new TherapistSchedule(null, 3, "10:30", 4, "tid1", true);
//        TherapistSchedule t3 = new TherapistSchedule(null, 3, "12:00", 5, "tid1", true);

// Friday (Day 6) Slots
//        TherapistSchedule f1 = new TherapistSchedule(null, 6, "14:00", 3, "tid1", true);
//        TherapistSchedule f2 = new TherapistSchedule(null, 6, "15:30", 4, "tid1", true);
//        TherapistSchedule f3 = new TherapistSchedule(null, 6, "17:00", 2, "tid1", true);

//        List<TherapistSchedule> schedules = List.of(t1, t2, t3, f1, f2, f3);
//
//        WriteBatch batch = db.batch();
//
//        for (TherapistSchedule s : schedules) {
//            // Generate a new document reference with an auto-ID
//            DocumentReference ref = db.collection("therapistSchedule").document();
//
//            // Assign that unique Firestore ID to the object's scheduleId field
//            s.setScheduleId(ref.getId());
//
//            batch.set(ref, s);
//        }
//
//        batch.commit().addOnSuccessListener(aVoid -> {
//            Log.d("Firestore", "Success: 6 therapistSchedule slots created for tid1");
//        });

        categoryRecyclerView = binding.recyclerviewShop;

        categoryRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false));

        SpinnerDialog spinner = SpinnerDialog.show(getParentFragmentManager());

        db.collection("categories").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        QuerySnapshot result = task.getResult();
                        //List<Category> categories = result.toObjects(Category.class);

                        List<Category> categories = task.getResult().toObjects(Category.class);
                        adapter = new CategoryAdapter(categories, category -> {

                            Bundle bundle = new Bundle();
                            bundle.putString("categoryId", category.getCategoryId());

                            ListingFragment fragment = new ListingFragment();
                            fragment.setArguments(bundle);

                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.navContainerView, fragment)
                                    .addToBackStack(null)
                                    .commit();

                        });

                        spinner.dismiss();

                        categoryRecyclerView.setAdapter(adapter);
                    }
                });
    }
}