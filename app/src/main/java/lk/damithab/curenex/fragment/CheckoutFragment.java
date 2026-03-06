package lk.damithab.curenex.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lk.damithab.curenex.R;
import lk.damithab.curenex.adapter.CartAdapter;
import lk.damithab.curenex.adapter.CheckoutItemsAdapter;
import lk.damithab.curenex.databinding.FragmentCheckoutBinding;
import lk.damithab.curenex.listener.FirestoreCallback;
import lk.damithab.curenex.model.Address;
import lk.damithab.curenex.model.CartItem;
import lk.damithab.curenex.model.Order;
import lk.damithab.curenex.model.Product;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;

public class CheckoutFragment extends Fragment {
    private FragmentCheckoutBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private double total;
    private boolean paymentActive = false;

    private Address selectedAddress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCheckoutBinding.inflate(inflater, container, false);

        binding.billingLayoutBtn.setOnClickListener(v -> {
            if (binding.billingLayoutBody.getVisibility() == View.GONE) {
                binding.billingLayoutBody.setVisibility(View.VISIBLE);
                binding.billingLayoutBtn.setRotation(180f);
            } else {
                binding.billingLayoutBody.setVisibility(View.GONE);
                binding.billingLayoutBtn.setRotation(0f);
            }
        });


        binding.shippingDetailsCheckBilling.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.billingLayout.setVisibility(View.GONE);
            } else {
                binding.billingLayout.setVisibility(View.VISIBLE);
                binding.billingLayoutBody.setVisibility(View.VISIBLE);
            }
        });

//        binding.shippingLayoutBtn.setOnClickListener(v -> {
//
//            if (binding.shippingLayoutBody.getVisibility() == View.GONE) {
//                binding.shippingLayoutBody.setVisibility(View.VISIBLE);
//                binding.shippingLayoutBtn.setRotation(180f);
//            } else {
//                binding.shippingLayoutBody.setVisibility(View.GONE);
//                binding.shippingLayoutBtn.setRotation(0f);
//            }
//        });
//
//        binding.billingLayoutBtn.setOnClickListener(v -> {
//            if (binding.billingLayoutBody.getVisibility() == View.GONE) {
//                binding.billingLayoutBody.setVisibility(View.VISIBLE);
//                binding.billingLayoutBtn.setRotation(180f);
//            } else {
//                binding.billingLayoutBody.setVisibility(View.GONE);
//                binding.billingLayoutBtn.setRotation(0f);
//            }
//        });
//
//
//        binding.shippingDetailsCheckBilling.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            if (isChecked) {
//                binding.billingLayout.setVisibility(View.GONE);
//            } else {
//                binding.billingLayout.setVisibility(View.VISIBLE);
//                binding.billingLayoutBody.setVisibility(View.VISIBLE);
//            }
//        });


        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        getActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.GONE);
        getActivity().findViewById(R.id.main_toolbar).setVisibility(View.GONE);

        double shippingCost = 400;

        getParentFragmentManager().setFragmentResultListener("addressRequest", this, (requestKey, bundle) -> {
            // Cast the serializable back to your Address class
            Address address = (Address) bundle.getSerializable("selectedAddress");

            if (address != null) {
                updateAddressUI(address);
            }
        });

        binding.selectAddressBtn.setOnClickListener(v->{
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.navContainerView, new AddressFragment())
                    .addToBackStack(null)
                    .commit();
        });


        getCartItems(cartItems -> {


            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            binding.productCheckoutItemsRecycler.setLayoutManager(layoutManager);
            CheckoutItemsAdapter adapter = new CheckoutItemsAdapter(cartItems);
            binding.productCheckoutItemsRecycler.setAdapter(adapter);

            ArrayList<String> productIds = new ArrayList<>();
            cartItems.forEach(cartItem -> {
                productIds.add(cartItem.getProductId());
            });


            getProductsByIds(productIds, data -> {

                double subTotal = 0;

                for (CartItem cartItem : cartItems) {
                    Product product = data.get(cartItem.getProductId());
                    if (product != null) {
                        subTotal += product.getPrice() * cartItem.getQuantity();
                    }
                }

                total = subTotal + shippingCost;
                binding.checkoutSubtotal.setText(String.format(Locale.US, "LKR %,.2f", subTotal));
                binding.checkoutShipping.setText(String.format(Locale.US, "LKR %,.2f", shippingCost));
                binding.checkoutTotal.setText(String.format(Locale.US, "LKR %,.2f", total));
                paymentActive = true;
            });

        });


        binding.checkoutBtnProceed.setOnClickListener(v -> {

            if(selectedAddress != null){
                paymentActive = true;
            }else{
                Toast.makeText(getContext(), "Please select a address", Toast.LENGTH_SHORT).show();
                return;
            }

            if (paymentActive) {
                InitRequest req = new InitRequest();
                req.setSandBox(true);

                req.setMerchantId("1221265");
                req.setMerchantSecret("MjQxNzgwMzA4NTc3MTc1MDUyMTMwMjYwNjQ2MTU5MDg1NjMyMzg=");
                req.setCurrency("LKR");
                req.setAmount(total);
                req.setOrderId("ES0I-001");
                req.setItemsDescription("");

                String[] name = selectedAddress.getName().split(" ");
                String firstName = name[0];
                String lastName = name[1];

                req.getCustomer().setFirstName(firstName);
                req.getCustomer().setLastName(lastName);
                req.getCustomer().setEmail(selectedAddress.getEmail());
                req.getCustomer().setPhone(selectedAddress.getContact());
                req.getCustomer().getAddress().setAddress(selectedAddress.getAddress1());
                req.getCustomer().getAddress().setCity(selectedAddress.getCity());
                req.getCustomer().getAddress().setCountry("Sri Lanka");

                req.setNotifyUrl("https://curenex.requestcatcher.com/");

                Intent intent = new Intent(getActivity(), PHMainActivity.class);
                intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);

                payhereLauncher.launch(intent);
            }
        });



    }

    private void loadDefaultAddress() {
        db.collection("address")
                .whereEqualTo("uid", firebaseAuth.getCurrentUser().getUid())
                .limit(1) // Just get the first one
                .get()
                .addOnSuccessListener(qds -> {
                    if (!qds.isEmpty()) {
                        Address address = qds.getDocuments().get(0).toObject(Address.class);
                        updateAddressUI(address);
                    } else {
                        binding.checkoutAddressLayout.setVisibility(View.GONE);
                    }
                });
    }

    private void updateAddressUI(Address address) {
        selectedAddress = address;
        binding.checkoutAddressName.setText(address.getName());
        binding.checkoutAddressAddress1.setText(address.getAddress1());
        binding.checkoutAddressCity.setText(address.getCity());
        binding.checkoutAddressPostcode.setText(address.getPostcode());
        binding.checkoutAddressMobile.setText(address.getContact());
        binding.checkoutAddressLayout.setVisibility(View.VISIBLE);
    }


    private void getCartItems(FirestoreCallback<List<CartItem>> callback) {
        String uid = firebaseAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).collection("cart").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot qds) {
                if (!qds.isEmpty()) {
                    List<CartItem> cartItems = qds.toObjects(CartItem.class);
                    callback.onCallback(cartItems);
                }
            }
        });
    }

    private void getProductsByIds(List<String> productIds, FirestoreCallback<Map<String, Product>> callback) {

        Map<String, Product> products = new HashMap<>();

        if (productIds == null || productIds.isEmpty()) {
            callback.onCallback(products);
            return;
        }

        db.collection("products").whereIn("productId", productIds).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot qds) {

                qds.getDocuments().forEach(ds -> {
                    Product product = ds.toObject(Product.class);
                    if (product != null) {
                        products.put(product.getProductId(), product);
                    }
                });

                callback.onCallback(products);
            }
        });

    }

    private final ActivityResultLauncher<Intent> payhereLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            Intent data = result.getData();
            if (data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
                PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);

                if (response != null && response.isSuccess()) {

                    StatusResponse statusResponse = response.getData();


                    // Save order to firestore
                    saveOrder(statusResponse);


                    Log.i("PAYHERE", "Payment Success!");

                } else {
                    Log.e("PAYHERE", response.getData().getMessage());
                }

            }
        } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
            Log.e("PAYHERE", "Payment Canceled!");
        }

    });

    private void saveOrder(StatusResponse statusResponse) {
        getCartItems(cartItems -> {

            String uid = firebaseAuth.getCurrentUser().getUid();

            Order order = new Order();
            order.setOrderId(String.valueOf(System.currentTimeMillis()));
            order.setUserId(uid);
            order.setTotalAmount(total);
            order.setStatus("PAID");
            order.setOrderDate(Timestamp.now());


            String user_id = selectedAddress.getUid();
            String shipping_id = selectedAddress.getAddressId();
            String shipping_name = selectedAddress.getName();
            String shipping_email =  selectedAddress.getEmail();
            String shipping_contact =  selectedAddress.getContact();
            String shipping_address1 =  selectedAddress.getAddress1();
            String shipping_address2 =  selectedAddress.getAddress2();
            String shipping_city =  selectedAddress.getCity();
            String shipping_postCode = selectedAddress.getPostcode();

            Address shippingAddress = Address.builder().addressId(shipping_id).name(shipping_name).email(shipping_email).contact(shipping_contact).address1(shipping_address1).address2(shipping_address2).city(shipping_city).postcode(shipping_postCode).uid(user_id).build();
            order.setShippingAddress(shippingAddress);

            if (!binding.shippingDetailsCheckBilling.isChecked()) {
                String billing_name = binding.billingDetailsName.getText().toString();
                String billing_email = binding.billingDetailsEmail.getText().toString();
                String billing_contact = binding.billingDetailsContact.getText().toString();
                String billing_address1 = binding.billingDetailsAddress1.getText().toString();
                String billing_address2 = binding.billingDetailsAddress2.getText().toString();
                String billing_city = binding.billingDetailsCity.getText().toString();
                String billing_postCode = binding.billingDetailsPostcode.getText().toString();

                Address billingAddress = Address.builder().name(billing_name).email(billing_email).contact(billing_contact).address1(billing_address1).address2(billing_address2).city(billing_city).postcode(billing_postCode).uid(user_id).build();
                order.setBillingAddress(billingAddress);
            }else{
                /// If billing check is not checked we put same shippingDetails to billing
                order.setBillingAddress(shippingAddress);
            }

            /// //////
            ArrayList<String> productIds = new ArrayList<>();
            cartItems.forEach(cartItem -> {
                productIds.add(cartItem.getProductId());
            });

            List<Order.OrderItem> orderItems = new ArrayList<>();

            getProductsByIds(productIds, data -> {

                for (CartItem cartItem : cartItems) {
                    Product product = data.get(cartItem.getProductId());

                    if (product != null) {

                        List<Order.OrderItem.Attribute> attributes = new ArrayList<>();

                        for (CartItem.Attribute at : cartItem.getAttributes()) {
                            Order.OrderItem.Attribute attribute = Order.OrderItem.Attribute.builder().name(at.getName()).value(at.getValue()).build();

                            attributes.add(attribute);
                        }

                        Order.OrderItem orderItem = Order.OrderItem.builder().productId(cartItem.getProductId()).unitPrice(product.getPrice()).quantity(cartItem.getQuantity()).attributes(attributes).build();
                        orderItems.add(orderItem);


                        ///  Add order items to Oder object
                        order.setOrderItems(orderItems);

                    }
                }
                DocumentReference newOrderRef = db.collection("orders").document();
                String generatedOrderId = newOrderRef.getId();
                order.setDocId(generatedOrderId);

                newOrderRef.set(order).addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Order Saved!", Toast.LENGTH_SHORT).show();
                    // Clear cart
                    db.collection("users").document(uid).collection("cart")
                            .get()
                            .addOnSuccessListener(qds -> {
                                qds.getDocuments().forEach(ds -> {
                                    ds.getReference().delete();
                                });
                            });


                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.navContainerView, new HomeFragment())
                            .commit();

                });

            });


        });
    }


    @Override
    public void onResume() {
        super.onResume();
        getActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.GONE);
        getActivity().findViewById(R.id.main_toolbar).setVisibility(View.GONE);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.main_toolbar).setVisibility(View.VISIBLE);
    }

}