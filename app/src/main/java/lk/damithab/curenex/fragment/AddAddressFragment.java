package lk.damithab.curenex.fragment;

import static lk.damithab.curenex.util.RegexUtil.isCharacterValid;
import static lk.damithab.curenex.util.RegexUtil.isEmailValid;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;

import lk.damithab.curenex.R;
import lk.damithab.curenex.databinding.FragmentAddAddressBinding;
import lk.damithab.curenex.databinding.FragmentAddressBinding;
import lk.damithab.curenex.model.Address;
import lk.damithab.curenex.model.User;

public class AddAddressFragment extends Fragment {
    private FragmentAddAddressBinding binding;

    FirebaseAuth auth;

    FirebaseFirestore db;

    private String addressId;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseFirestore db;
        if (getArguments() != null) {
            this.addressId = getArguments().getString("addressId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddAddressBinding.inflate(inflater, container, false);
        binding.shippingLayoutBtn.setOnClickListener(v -> {

            if (binding.shippingLayoutBody.getVisibility() == View.GONE) {
                binding.shippingLayoutBody.setVisibility(View.VISIBLE);
                binding.shippingLayoutBtn.setRotation(180f);
            } else {
                binding.shippingLayoutBody.setVisibility(View.GONE);
                binding.shippingLayoutBtn.setRotation(0f);
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

        String userId = auth.getCurrentUser().getUid();

        loadListeners();

        AutoCompleteTextView shippingCities = binding.shippingDetailsCity;
        String[] cities = {"Ampara","Anuradhapura", "Badulla","Colombo","Gampaha","Galle","Kurunegala","Kegalla","Bandarawela","Bandaragama"
                ,"Haputhale","Horana","Mathale","Hambanthota"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_dropdown_item_1line, cities);
        shippingCities.setAdapter(arrayAdapter);

        if(addressId != null){
            binding.saveAddressBtn.setText("Update");
            db.collection("address").document(addressId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                Address address = documentSnapshot.toObject(Address.class);
                                assert address != null;
                                String[] name = address.getName().split(" ");
                                binding.shippingDetailsName.setText(name[0]);
                                binding.shippingDetailsLastname.setText(name[1]);
                                binding.shippingDetailsEmail.setText(address.getEmail());
                                binding.shippingDetailsContact.setText(address.getContact());
                                binding.shippingDetailsAddress1.setText(address.getAddress1());
                                binding.shippingDetailsAddress2.setText(address.getAddress2());
                                binding.shippingDetailsCity.setText(address.getCity());
                                binding.shippingDetailsPostcode.setText(address.getPostcode());
                            }
                        }
                    });
        }

        binding.saveAddressBtn.setOnClickListener(v -> {

            String firstName = binding.shippingDetailsName.getText().toString().trim();
            String lastName = binding.shippingDetailsLastname.getText().toString().trim();
            String shipping_name = firstName + " " + lastName;
            String shipping_email = binding.shippingDetailsEmail.getText().toString().trim();
            String shipping_contact = binding.shippingDetailsContact.getText().toString().trim();
            String shipping_address1 = binding.shippingDetailsAddress1.getText().toString();
            String shipping_address2 = binding.shippingDetailsAddress2.getText().toString();
            String shipping_city = binding.shippingDetailsCity.getText().toString().trim();
            String shipping_postCode = binding.shippingDetailsPostcode.getText().toString().trim();

            if (firstName.isEmpty()) {
                binding.shippingDetailsNameLayout.setErrorEnabled(true);
                binding.shippingDetailsNameLayout.setError("Firstname is required!");
                binding.shippingDetailsName.requestFocus();
                return;
            }

            if (!isCharacterValid(firstName)) {
                binding.shippingDetailsNameLayout.setErrorEnabled(true);
                binding.shippingDetailsNameLayout.setError("Invalid firstname!");
                binding.shippingDetailsName.requestFocus();
                return;
            }

            if (lastName.isEmpty()) {
                binding.shippingDetailsLastnameLayout.setErrorEnabled(true);
                binding.shippingDetailsLastnameLayout.setError("Lastname is required!");
                binding.shippingDetailsLastname.requestFocus();
                return;
            }

            if (!isCharacterValid(lastName)) {
                binding.shippingDetailsLastnameLayout.setErrorEnabled(true);
                binding.shippingDetailsLastnameLayout.setError("Invalid lastname!");
                binding.shippingDetailsLastname.requestFocus();
                return;
            }

            if (shipping_email.isEmpty()) {
                binding.shippingDetailsEmailLayout.setErrorEnabled(true);
                binding.shippingDetailsEmailLayout.setError("Email address is required!");
                binding.shippingDetailsEmail.requestFocus();
                return;
            }
            if (!isEmailValid(shipping_email)) {
                binding.shippingDetailsEmailLayout.setErrorEnabled(true);
                binding.shippingDetailsEmailLayout.setError("Invalid email address!");
                binding.shippingDetailsEmail.requestFocus();
                return;
            }

            if (shipping_contact.isEmpty()) {
                binding.shippingDetailsContactLayout.setErrorEnabled(true);
                binding.shippingDetailsContactLayout.setError("Contact number is required!");
                binding.shippingDetailsContact.requestFocus();
                return;
            }

            if (shipping_address1.isEmpty()) {
                binding.shippingDetailsAddress1Layout.setErrorEnabled(true);
                binding.shippingDetailsAddress1Layout.setError("address1 is required!");
                binding.shippingDetailsAddress1.requestFocus();
                return;
            }
            if (shipping_address2.isEmpty()) {
                binding.shippingDetailsAddress2Layout.setErrorEnabled(true);
                binding.shippingDetailsAddress2Layout.setError("address2 is required!");
                binding.shippingDetailsAddress2.requestFocus();
                return;
            }

            if (shipping_city.isEmpty()) {
                binding.shippingDetailsCityLayout.setErrorEnabled(true);
                binding.shippingDetailsCityLayout.setError("City is required!");
                binding.shippingDetailsCity.requestFocus();
                return;
            }
            if (shipping_postCode.isEmpty()) {
                binding.shippingDetailsPostcodeLayout.setErrorEnabled(true);
                binding.shippingDetailsPostcodeLayout.setError("Postcode is required!");
                binding.shippingDetailsPostcode.requestFocus();
                return;
            }


            if (addressId != null) {

                Address address = Address.builder().addressId(addressId).name(shipping_name).email(shipping_email).contact(shipping_contact).address1(shipping_address1).address2(shipping_address2).city(shipping_city).uid(userId).postcode(shipping_postCode).build();

                db.collection("address")
                        .document(addressId)
                        .set(address, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            getParentFragmentManager().popBackStack();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firestore", "Error updating document", e);
                        });


                /// Edit address

            } else {
                /// Save address

                Address address = Address.builder().name(shipping_name).email(shipping_email).contact(shipping_contact).address1(shipping_address1).address2(shipping_address2).city(shipping_city).uid(userId).postcode(shipping_postCode).build();


                db.collection("address")
                        .add(address)
                        .addOnSuccessListener(documentReference -> {
                            String docId = documentReference.getId();

                            documentReference.update("addressId", docId)
                                    .addOnSuccessListener(aVoid -> {
                                        getParentFragmentManager().popBackStack();
                                        Log.d("Firestore", "Address saved with ID: " + docId);
                                        Toast.makeText(getContext(), "Address Saved", Toast.LENGTH_SHORT).show();
                                    });
                        });


            }
        });
    }

    private void loadListeners() {
        binding.shippingDetailsName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.shippingDetailsNameLayout.setErrorEnabled(false);
            }
        });
        binding.shippingDetailsLastname.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.shippingDetailsLastnameLayout.setErrorEnabled(false);
            }
        });
        binding.shippingDetailsEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.shippingDetailsEmailLayout.setErrorEnabled(false);
            }
        });
        binding.shippingDetailsContact.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.shippingDetailsContactLayout.setErrorEnabled(false);
            }
        });
        binding.shippingDetailsAddress1.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.shippingDetailsAddress1Layout.setErrorEnabled(false);
            }
        });
        binding.shippingDetailsAddress2.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.shippingDetailsAddress2Layout.setErrorEnabled(false);
            }
        });
        binding.shippingDetailsCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.shippingDetailsCityLayout.setErrorEnabled(false);
            }
        });
        binding.shippingDetailsPostcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.shippingDetailsPostcodeLayout.setErrorEnabled(false);
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
//        getActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.GONE);
//        getActivity().findViewById(R.id.main_toolbar).setVisibility(View.GONE);
    }

    @Override
    public void onStop() {
        super.onStop();
//        getActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.VISIBLE);
//        getActivity().findViewById(R.id.main_toolbar).setVisibility(View.VISIBLE);
    }
}