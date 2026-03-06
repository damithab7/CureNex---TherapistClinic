package lk.damithab.curenex.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lk.damithab.curenex.R;
import lk.damithab.curenex.adapter.CartAdapter;
import lk.damithab.curenex.databinding.FragmentCartBinding;
import lk.damithab.curenex.dialog.SpinnerDialog;
import lk.damithab.curenex.model.CartItem;
import lk.damithab.curenex.model.Product;

public class CartFragment extends Fragment {

    private FragmentCartBinding binding;

    private List<CartItem> cartItems;

    private SpinnerDialog spinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.cartProcessBtn.setEnabled(false);

        BottomNavigationView navigationView = getActivity().findViewById(R.id.bottomNavigationView);

        spinner = SpinnerDialog.show(getParentFragmentManager());

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            String uid = firebaseAuth.getCurrentUser().getUid();

            db.collection("users").document(uid).collection("cart").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot qds) {
                            if (!qds.isEmpty()) {
                                binding.cartProcessBtn.setEnabled(true);

                                cartItems = new ArrayList<>();

                                for (DocumentSnapshot ds : qds.getDocuments()) {
                                    CartItem cartItem = ds.toObject(CartItem.class);
                                    if (cartItem != null) {
                                        String documentId = ds.getId();
                                        cartItem.setDocumentId(documentId);

                                        cartItems.add(cartItem);
                                    }
                                }

                                LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

                                binding.cartCartItems.setLayoutManager(layoutManager);

                                CartAdapter adapter = new CartAdapter(cartItems);


                                adapter.setOnQuantityChangeListener(cartItem -> {
                                    SpinnerDialog dialog = SpinnerDialog.show(getParentFragmentManager());
                                    String documentId = cartItem.getDocumentId();
                                    db.collection("users").document(uid)
                                            .collection("cart")
                                            .document(documentId)
                                            .update("quantity", cartItem.getQuantity())
                                            .addOnSuccessListener(aVoid -> {
                                                updateTotal();
                                                dialog.dismiss();
                                            });
                                });


                                adapter.setOnRemoveListener(position -> {
                                    SpinnerDialog dialog = SpinnerDialog.show(getParentFragmentManager());
                                    String documentId = cartItems.get(position).getDocumentId();
                                    db.collection("users").document(uid)
                                            .collection("cart").document(documentId)
                                            .delete().addOnSuccessListener(aVoid -> {
                                                cartItems.remove(position);
                                                adapter.notifyItemRemoved(position);
                                                adapter.notifyItemRangeChanged(position, cartItems.size());
                                                if(!cartItems.isEmpty()) {
                                                    navigationView.getOrCreateBadge(R.id.nav_cart)
                                                            .setNumber(cartItems.size());
                                                }else{
                                                    navigationView.removeBadge(R.id.nav_cart);
                                                }
                                                updateTotal();
                                                dialog.dismiss();
                                            });

                                });
                                binding.cartCartItems.setAdapter(adapter);
                                updateTotal();
                            } else {
                                navigationView.removeBadge(R.id.nav_cart);
                            }

                            spinner.dismiss();
                        }
                    });
        }

        binding.cartProcessBtn.setOnClickListener(v -> {
            CheckoutFragment checkoutFragment = new CheckoutFragment();
            getParentFragmentManager().beginTransaction().replace(R.id.navContainerView, checkoutFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }


    private void updateTotal() {
        if (cartItems == null || cartItems.isEmpty()) {
            binding.cartProcessBtn.setEnabled(false);
            binding.cartShippingPrice.setText((String.format(Locale.US, "LKR %,.2f", 0.00)));
            binding.cartSubtotalPrice.setText((String.format(Locale.US, "LKR %,.2f", 0.00)));
            binding.cartItemsTotal.setText(String.format(Locale.US, "LKR %,.2f", 0.00));
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<String> productIds = new ArrayList<>();
        cartItems.forEach(cartItem -> {
            productIds.add(cartItem.getProductId());
        });

        db.collection("products").whereIn("productId", productIds).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot qds) {
                Map<String, Product> productMap = new HashMap<>();
                qds.getDocuments().forEach(ds -> {
                    Product product = ds.toObject(Product.class);
                    if (product != null) {
                        productMap.put(product.getProductId(), product);
                    }
                });

//                final double[] total = {0};
//                cartItems.forEach(cartItem -> {
//                    Product product = productMap.get(cartItem.getProductId());
//                    if(product != null){
//                        total[0] += product.getPrice() * cartItem.getQuantity();
//                    }
//                });

                double shippingPrice = 400;
                double total = 0;
                for (CartItem cartItem : cartItems) {
                    Product product = productMap.get(cartItem.getProductId());
                    if (product != null) {
                        total += product.getPrice() * cartItem.getQuantity();
                    }
                }

                binding.cartShippingPrice.setText((String.format(Locale.US, "LKR %,.2f", shippingPrice)));
                binding.cartSubtotalPrice.setText((String.format(Locale.US, "LKR %,.2f", total)));
                binding.cartItemsTotal.setText((String.format(Locale.US, "LKR %,.2f", total + shippingPrice)));

            }
        });

    }
}