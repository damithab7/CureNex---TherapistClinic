package lk.damithab.curenex.fragment;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lk.damithab.curenex.R;
import lk.damithab.curenex.activity.SignInActivity;
import lk.damithab.curenex.adapter.ProductSliderAdapter;
import lk.damithab.curenex.adapter.SectionAdapter;
import lk.damithab.curenex.databinding.FragmentProductDetailsBinding;
import lk.damithab.curenex.dialog.SpinnerDialog;
import lk.damithab.curenex.dialog.ToastDialog;
import lk.damithab.curenex.model.CartItem;
import lk.damithab.curenex.model.Product;
import lk.damithab.curenex.util.AnimationUtil;

public class ProductDetailsFragment extends Fragment {

    private FragmentProductDetailsBinding binding;

    private String productId;

    private int quantity = 1;
    private int avbQuantity;
    private Map<String, ChipGroup> attributeGroups = new HashMap<>();

    private boolean isAttribute = true;
    private int attrActualSize = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getString("productId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProductDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AnimationUtil.bottomSlideDown(getActivity().findViewById(R.id.bottomNavigationView));

        SpinnerDialog dialog = SpinnerDialog.show(getParentFragmentManager());

        getActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // Load Product Details

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Log.d("TEssst", "onViewCreated: Testttttt");

        db.collection("products")
                .whereEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        if (!qds.isEmpty()) {
                            Product product = qds.getDocuments().get(0).toObject(Product.class);

                            ProductSliderAdapter adapter = new ProductSliderAdapter(product.getImages());
                            binding.productImageSlider.setAdapter(adapter);

                            binding.dotsIndicator.attachTo(binding.productImageSlider);

                            binding.productDetailsRating.setTag(product.getRating());

                            binding.productDetailsTitle.setText(product.getTitle());
                            binding.productDetailsPrice.setText(String.format(Locale.US, "LKR %,.2f", product.getPrice()));
                            binding.productDetailsAvbQty.setText(String.valueOf(product.getStockCount()));

                            avbQuantity = product.getStockCount();

                            if (product.getAttribute() != null) {
                                isAttribute = false;
                                attrActualSize = product.getAttribute().size();
                                product.getAttribute().forEach(attribute -> {
                                    renderAttribute(attribute, binding.productDetailsAttributeContainer);
                                });
                            }

                            dialog.dismiss();
                        }
                    }
                });

        binding.productDetailsBtnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                binding.productDetailsQuantity.setText(String.valueOf(quantity));
            }
        });
        binding.productDetailsBtnPlus.setOnClickListener(v -> {
            if (quantity < avbQuantity) {
                quantity++;
                binding.productDetailsQuantity.setText(String.valueOf(quantity));
            }
        });

        loadTopSellProduct();

        binding.productDetailsBtnAddCart.setOnClickListener(v -> {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            if (firebaseAuth.getCurrentUser() == null) {
                Intent intent = new Intent(getActivity(), SignInActivity.class);
                startActivity(intent);
            } else {
                List<CartItem.Attribute> attributes = getFinalSelections();
                if (!isAttribute) {
                    if (attributes.size() != attrActualSize) {
                        Snackbar.make(binding.getRoot(), "Select attributes", Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                }
                CartItem cartItem = new CartItem(productId, quantity, attributes);
                String uid = firebaseAuth.getCurrentUser().getUid();
                db.collection("users").document(uid).collection("cart")
                        .document(productId)
                        .set(cartItem)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                final int[] cartCount = {0};
                                db.collection("users").document(uid).collection("cart").get()
                                        .addOnSuccessListener(qds -> {
                                            int count = qds.size();

                                            BottomNavigationView navigationView = getActivity().findViewById(R.id.bottomNavigationView);
                                            if (count > 0) {
                                                navigationView.getOrCreateBadge(R.id.nav_cart).setNumber(count);
                                            } else {
                                                navigationView.removeBadge(R.id.nav_cart);
                                            }
                                        });
                                new ToastDialog(getActivity().getSupportFragmentManager(), "Added to cart successfully!");
//                                Snackbar.make(view, "Item added to cart", Snackbar.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void loadTopSellProduct() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products")
                .whereNotEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        if (!qds.isEmpty()) {
                            List<Product> products = qds.toObjects(Product.class);

                            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
                            binding.productDetailsTopSellSection.itemSectionContainer.setLayoutManager(layoutManager);
                            SectionAdapter adapter = new SectionAdapter(products, product -> {
                                Bundle bundle = new Bundle();
                                bundle.putString("productId", product.getProductId());

                                ProductDetailsFragment productDetailsFragment = new ProductDetailsFragment();
                                productDetailsFragment.setArguments(bundle);

                                getParentFragmentManager().beginTransaction()
                                        .replace(R.id.navContainerView, productDetailsFragment)
                                        .addToBackStack(null)
                                        .commit();
                            });

                            binding.productDetailsTopSellSection.itemSectionTitle.setText("Top Selling Products");
                            binding.productDetailsTopSellSection.itemSectionContainer.setAdapter(adapter);
                        }
                    }
                });
    }

    private void renderAttribute(Product.Attribute attribute, ViewGroup container) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);

        TextView label = new TextView(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                100,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        label.setLayoutParams(layoutParams);

        label.setText(attribute.getName());

        row.addView(label);

        /// Create options
        ChipGroup group = new ChipGroup(getContext());

        group.setSelectionRequired(true);
        group.setSingleSelection(true);

        attribute.getValues().forEach(value -> {
            Log.d("ChipError", "Drawables: " + "Chip is the problem");
            Chip chip = new Chip(getContext());
            Log.d("ChipError", "Drawables: " + java.util.Arrays.toString(chip.getCompoundDrawables()));
            chip.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            chip.setCheckable(true);
            chip.setChipStrokeWidth(3f);

            chip.setTag(value);

            if ("color".equals(attribute.getType())) {
                chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor(value)));
                float radiusInDp = 20f;
                float radiusInPx = radiusInDp * getResources().getDisplayMetrics().density;

                chip.setShapeAppearanceModel(
                        chip.getShapeAppearanceModel()
                                .toBuilder()
                                .setAllCorners(com.google.android.material.shape.CornerFamily.ROUNDED, radiusInPx)
                                .build());
            } else {
                chip.setText(value);
            }

            group.addView(chip);
        });

        row.addView(group);
        container.addView(row);
        attributeGroups.put(attribute.getName(), group);
    }

    private List<CartItem.Attribute> getFinalSelections() {

        List<CartItem.Attribute> attributes = new ArrayList<>();

        for (Map.Entry<String, ChipGroup> entry : attributeGroups.entrySet()) {
            String attributeName = entry.getKey();
            ChipGroup chipGroup = entry.getValue();

            int checkedChipId = chipGroup.getCheckedChipId();
            if (checkedChipId != -1) {
                Chip chip = getView().findViewById(checkedChipId);
                String value = chip.getTag().toString();

                attributes.add(new CartItem.Attribute(attributeName, value));
            }
        }


        return attributes;

    }

    @Override
    public void onResume() {
        super.onResume();
        AnimationUtil.bottomSlideDown(getActivity().findViewById(R.id.bottomNavigationView));
    }

    @Override
    public void onStop() {
        super.onStop();
        AnimationUtil.bottomSlideUp(getActivity().findViewById(R.id.bottomNavigationView));
    }
}