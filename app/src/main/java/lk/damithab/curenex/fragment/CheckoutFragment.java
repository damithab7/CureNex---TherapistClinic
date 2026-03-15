package lk.damithab.curenex.fragment;

import static androidx.core.content.ContextCompat.getSystemService;

import static lk.damithab.curenex.util.RegexUtil.isCharacterValid;
import static lk.damithab.curenex.util.RegexUtil.isEmailValid;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lk.damithab.curenex.R;
import lk.damithab.curenex.activity.AddressActivity;
import lk.damithab.curenex.activity.BookingHistoryActivity;
import lk.damithab.curenex.activity.OrderHistoryActivity;
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

    private FirebaseStorage storage;
    private double total;
    private boolean paymentActive = false;

    private Address selectedAddress;

    private static final String CHANNEL_ID = "orders_channel";

    private String productId;

    private int qty;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        if (getArguments() != null) {
            this.productId = getArguments().getString("productId");
            this.qty = getArguments().getInt("qty");
        }
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

        createNotificationChannel();

        double shippingCost = 400;

//        getParentFragmentManager().setFragmentResultListener("addressRequest", this, (requestKey, bundle) -> {
//            // Cast the serializable back to your Address class
//            Address address = (Address) bundle.getSerializable("selectedAddress");
//
//            if (address != null) {
//                updateAddressUI(address);
//            }
//        });

        binding.selectAddressBtn.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddressActivity.class);
            addressResultLauncher.launch(intent);
        });


        if (productId != null) {
            getSingleProduct(product -> {
                binding.singleProductLayout.setVisibility(View.VISIBLE);
                storage.getReference(product.getImages().get(0))
                        .getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            Glide.with(requireContext())
                                    .load(uri)
                                    .centerCrop()
                                    .into(binding.singleProductImage);
                        });
                binding.singleProductPrice.setText(String.format(Locale.US, "LKR %,.2f", product.getPrice() * qty));
                binding.singleProductQty.setText("Quantity "+String.valueOf(qty));
                binding.singleProductTitle.setText(product.getTitle());
                binding.singleProductAttributes.setText("");
                paymentActive = true;

                total = product.getPrice() * qty;
                binding.checkoutSubtotal.setText(String.format(Locale.US, "LKR %,.2f", total));
                binding.checkoutShipping.setText(String.format(Locale.US, "LKR %,.2f", shippingCost));
                binding.checkoutTotal.setText(String.format(Locale.US, "LKR %,.2f", total + shippingCost));
            });
        } else {
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
        }

        binding.checkoutBtnProceed.setOnClickListener(v -> {

            if (selectedAddress == null) {
                Toast.makeText(getContext(), "Please select a address", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!binding.shippingDetailsCheckBilling.isChecked()) {
                String billing_name = binding.billingDetailsName.getText().toString();
                String billing_email = binding.billingDetailsEmail.getText().toString();
                String billing_contact = binding.billingDetailsContact.getText().toString();
                String billing_address1 = binding.billingDetailsAddress1.getText().toString();
                String billing_address2 = binding.billingDetailsAddress2.getText().toString();
                String billing_city = binding.billingDetailsCity.getText().toString();
                String billing_postCode = binding.billingDetailsPostcode.getText().toString();

                loadListeners();

                if (billing_name.isEmpty()) {
                    binding.billingDetailsNameLayout.setErrorEnabled(true);
                    binding.billingDetailsNameLayout.setError("Firstname is required!");
                    binding.billingDetailsName.requestFocus();
                    return;
                }

                if (!isCharacterValid(billing_name)) {
                    binding.billingDetailsNameLayout.setErrorEnabled(true);
                    binding.billingDetailsNameLayout.setError("Invalid firstname!");
                    binding.billingDetailsName.requestFocus();
                    return;
                }


                if (billing_email.isEmpty()) {
                    binding.billingDetailsEmailLayout.setErrorEnabled(true);
                    binding.billingDetailsEmailLayout.setError("Email address is required!");
                    binding.billingDetailsEmail.requestFocus();
                    return;
                }
                if (!isEmailValid(billing_email)) {
                    binding.billingDetailsEmailLayout.setErrorEnabled(true);
                    binding.billingDetailsEmailLayout.setError("Invalid email address!");
                    binding.billingDetailsEmail.requestFocus();
                    return;
                }

                if (billing_contact.isEmpty()) {
                    binding.billingDetailsContactLayout.setErrorEnabled(true);
                    binding.billingDetailsContactLayout.setError("Contact number is required!");
                    binding.billingDetailsContact.requestFocus();
                    return;
                }

                if (billing_address1.isEmpty()) {
                    binding.billingDetailsAddress1Layout.setErrorEnabled(true);
                    binding.billingDetailsAddress1Layout.setError("address is required!");
                    binding.billingDetailsAddress1.requestFocus();
                    return;
                }

                if (billing_address2.isEmpty()) {
                    binding.billingDetailsAddress2Layout.setErrorEnabled(true);
                    binding.billingDetailsAddress2Layout.setError("address is required!");
                    binding.billingDetailsAddress2.requestFocus();
                    return;
                }

                if (billing_city.isEmpty()) {
                    binding.billingDetailsCityLayout.setErrorEnabled(true);
                    binding.billingDetailsCityLayout.setError("city is required!");
                    binding.billingDetailsCity.requestFocus();
                    return;
                }

                if (billing_postCode.isEmpty()) {
                    binding.billingDetailsPostcodeLayout.setErrorEnabled(true);
                    binding.billingDetailsPostcodeLayout.setError("Birthdate is required!");
                    binding.billingDetailsPostcode.requestFocus();
                    return;
                }


                binding.billingDetailsName.clearFocus();
                binding.billingDetailsEmail.clearFocus();
                binding.billingDetailsContact.clearFocus();
                binding.billingDetailsAddress1.clearFocus();
                binding.billingDetailsAddress2.clearFocus();
                binding.billingDetailsCity.clearFocus();
                binding.billingDetailsPostcode.clearFocus();
            }

            paymentActive = true;

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

    private final ActivityResultLauncher<Intent> addressResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    // Get the Address object back
                    Address address = (Address) result.getData().getSerializableExtra("selectedAddress");
                    if (address != null) {
                        updateAddressUI(address);
                    }
                }
            }
    );

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

    private void getSingleProduct(FirestoreCallback<Product> callback) {
        db.collection("products").document(productId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot ds) {
                if (ds.exists()) {
                    Product product = ds.toObject(Product.class);
                    callback.onCallback(product);
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
        String shipping_email = selectedAddress.getEmail();
        String shipping_contact = selectedAddress.getContact();
        String shipping_address1 = selectedAddress.getAddress1();
        String shipping_address2 = selectedAddress.getAddress2();
        String shipping_city = selectedAddress.getCity();
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
        } else {
            /// If billing check is not checked we put same shippingDetails to billing
            order.setBillingAddress(shippingAddress);
        }

        if (productId != null) {
            getSingleProduct(product->{
                List<Order.OrderItem> orderItems = new ArrayList<>();

                /// Attributes
//                List<Order.OrderItem.Attribute> attributes = new ArrayList<>();
//
//                for (Product.Attribute at : product.getAttribute()) {
//                    Order.OrderItem.Attribute attribute = Order.OrderItem.Attribute.builder().name(at.getName()).value(at.getValues()).build();
//
//                    attributes.add(attribute);
//                }

                Order.OrderItem orderItem = Order.OrderItem.builder().productId(product.getProductId()).unitPrice(product.getPrice()).quantity(qty).build();
                orderItems.add(orderItem);

                ///  Add order items to Oder object
                order.setOrderItems(orderItems);

                DocumentReference newOrderRef = db.collection("orders").document();
                String generatedOrderId = newOrderRef.getId();
                order.setDocId(generatedOrderId);

                newOrderRef.set(order).addOnSuccessListener(aVoid -> {
                    sendNotification(order.getOrderId(), generatedOrderId);

                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.navContainerView, new HomeFragment())
                            .commit();

                });
            });
        } else {
            getCartItems(cartItems -> {
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

                            int currentQty = product.getStockCount() - cartItem.getQuantity();
                            db.collection("products").document(product.getProductId())
                                    .update("stockCount", currentQty)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {

                                        }
                                    });

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
                        sendNotification(order.getOrderId(), generatedOrderId);
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
    }

    private void loadListeners(){
        binding.billingDetailsName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.billingDetailsNameLayout.setErrorEnabled(false);
            }
        });
        binding.billingDetailsEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.billingDetailsEmailLayout.setErrorEnabled(false);
            }
        });
        binding.billingDetailsContact.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.billingDetailsContactLayout.setErrorEnabled(false);
            }
        });
        binding.billingDetailsAddress1.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.billingDetailsAddress1Layout.setErrorEnabled(false);
            }
        });
        binding.billingDetailsAddress2.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.billingDetailsAddress2Layout.setErrorEnabled(false);
            }
        });
        binding.billingDetailsCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.billingDetailsCityLayout.setErrorEnabled(false);
            }
        });
        binding.billingDetailsPostcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.billingDetailsPostcodeLayout.setErrorEnabled(false);
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Appointment Notifications";
            String description = "Channel for general appointments";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(getContext(), NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification(String orderId, String docId) {

        Intent intent = new Intent(getActivity(), OrderHistoryActivity.class);
        intent.putExtra("orderId", docId);

        int requestCode = 0;
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(),
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Order Placed Successfully! \uD83D\uDCE6")
                .setContentText("Your order #" + orderId + " is being processed. We'll let you know when it ships!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(1, builder.build());
        } else {
            checkAndRequestPermission();
        }
    }

    private void checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { /// Out android version => android version 13
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {

                } else {

                }
            });


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