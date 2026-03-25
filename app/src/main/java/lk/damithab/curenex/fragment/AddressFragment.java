package lk.damithab.curenex.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

import lk.damithab.curenex.R;
import lk.damithab.curenex.adapter.AddressAdapter;
import lk.damithab.curenex.databinding.FragmentAddressBinding;
import lk.damithab.curenex.dialog.CustomAlertDialog;
import lk.damithab.curenex.dialog.ToastDialog;
import lk.damithab.curenex.model.Address;
import lk.damithab.curenex.model.City;

public class AddressFragment extends Fragment {

    private FragmentAddressBinding binding;

    FirebaseAuth auth;

    FirebaseFirestore db;

    private FirebaseStorage storage;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddressBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        getActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
//            @Override
//            public void handleOnBackPressed() {
//                requireActivity().getSupportFragmentManager().popBackStack();
//            }
//        });

//        List<City> cities = List.of(
//                // Western Province
//                City.builder().cityId("c1").cityName("Colombo").build(),
//                City.builder().cityId("c2").cityName("Gampaha").build(),
//                City.builder().cityId("c3").cityName("Kalutara").build(),
//                City.builder().cityId("c4").cityName("Negombo").build(),
//                City.builder().cityId("c5").cityName("Mount Lavinia").build(),
//
//                // Central Province
//                City.builder().cityId("c6").cityName("Kandy").build(),
//                City.builder().cityId("c7").cityName("Nuwara Eliya").build(),
//                City.builder().cityId("c8").cityName("Matale").build(),
//                City.builder().cityId("c9").cityName("Gampola").build(),
//
//                // Southern Province
//                City.builder().cityId("c10").cityName("Galle").build(),
//                City.builder().cityId("c11").cityName("Matara").build(),
//                City.builder().cityId("c12").cityName("Hambantota").build(),
//                City.builder().cityId("c13").cityName("Hikkaduwa").build(),
//
//                // Northern Province
//                City.builder().cityId("c14").cityName("Jaffna").build(),
//                City.builder().cityId("c15").cityName("Vavuniya").build(),
//                City.builder().cityId("c16").cityName("Mannar").build(),
//
//                // Eastern Province
//                City.builder().cityId("c17").cityName("Trincomalee").build(),
//                City.builder().cityId("c18").cityName("Batticaloa").build(),
//                City.builder().cityId("c19").cityName("Ampara").build(),
//
//                // North Western Province
//                City.builder().cityId("c20").cityName("Kurunegala").build(),
//                City.builder().cityId("c21").cityName("Puttalam").build(),
//                City.builder().cityId("c22").cityName("Chilaw").build(),
//
//                // North Central Province
//                City.builder().cityId("c23").cityName("Anuradhapura").build(),
//                City.builder().cityId("c24").cityName("Polonnaruwa").build(),
//
//                // Uva Province
//                City.builder().cityId("c25").cityName("Badulla").build(),
//                City.builder().cityId("c26").cityName("Moneragala").build(),
//                City.builder().cityId("c27").cityName("Bandarawela").build(),
//                City.builder().cityId("c28").cityName("Ella").build(),
//
//                // Sabaragamuwa Province
//                City.builder().cityId("c29").cityName("Ratnapura").build(),
//                City.builder().cityId("c30").cityName("Kegalle").build()
//        );
//
//        WriteBatch batch = db.batch();
//
//        for (City city : cities) {
//            // We use city.getCityId() as the document path so it shows up as c1, c2, etc.
//            DocumentReference ref = db.collection("cities").document(city.getCityId());
//            batch.set(ref, city);
//        }
//
//        batch.commit()
//                .addOnSuccessListener(aVoid -> Log.d("CureNex", "Success: 30 cities added with numbered IDs."))
//                .addOnFailureListener(e -> Log.e("CureNex", "Error: " + e.getMessage()));

        binding.addNewAddressBtn.setOnClickListener(v->{
            AddAddressFragment addAddressFragment = new AddAddressFragment();
            getParentFragmentManager().beginTransaction().replace(R.id.address_fragment_container, addAddressFragment)
                    .addToBackStack(null)
                    .commit();
        });

        binding.userAddressRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        db.collection("address").whereEqualTo("uid", auth.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot ds) {
                        if(!ds.isEmpty()){
                            List<Address> addressList = ds.toObjects(Address.class);

                            binding.userAddressRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                            AddressAdapter adapter = new AddressAdapter(addressList, address->{
//                                Bundle result = new Bundle();
//                                result.putSerializable("selectedAddress", address); // Ensure Address class implements Serializable
//
//                                // Set the result with a unique key
//                                getParentFragmentManager().setFragmentResult("addressRequest", result);
//                                getParentFragmentManager().popBackStack();
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("selectedAddress", address);
                                requireActivity().setResult(Activity.RESULT_OK, resultIntent);
                                requireActivity().finish();
                            });

                            adapter.setOnRemoveListener(position -> {
                                String docId = addressList.get(position).getAddressId();
                                new CustomAlertDialog(getContext())
                                        .setTitle("Confirmation Message")
                                        .setMessage("Are you sure you want to delete this address?")
                                        .setPositiveButton("Remove", v -> {
                                            db.collection("address").document(docId).delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            new ToastDialog(getActivity().getSupportFragmentManager(), "Address removed successfully!");
                                                            addressList.remove(position);
                                                            adapter.notifyItemRemoved(position);
                                                            adapter.notifyItemRangeChanged(position, addressList.size());
                                                        }
                                                    });
                                        })
                                        .setNegativeButton()
                                        .show();
//                                new AlertDialog.Builder(getActivity())
//                                        .setTitle("")
//                                        .setMessage("")
//                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick(DialogInterface dialogInterface, int i) {
//
//                                            }
//                                        })
//                                        .setNegativeButton("No", null)
//                                        .show();
                            });

                            adapter.setOnEditListener(address -> {
                                AddAddressFragment addAddressFragment = new AddAddressFragment();
                                Bundle bundle = new Bundle();
                                bundle.putString("addressId", address.getAddressId());
                                addAddressFragment.setArguments(bundle);

                                getParentFragmentManager().beginTransaction()
                                        .replace(R.id.address_fragment_container,addAddressFragment)
                                        .addToBackStack(null)
                                        .commit();
                            });

                            binding.userAddressRecycler.setAdapter(adapter);
                        }
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