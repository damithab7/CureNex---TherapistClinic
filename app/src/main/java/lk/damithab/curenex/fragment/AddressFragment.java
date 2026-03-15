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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

import lk.damithab.curenex.R;
import lk.damithab.curenex.adapter.AddressAdapter;
import lk.damithab.curenex.databinding.FragmentAddressBinding;
import lk.damithab.curenex.dialog.CustomAlertDialog;
import lk.damithab.curenex.dialog.ToastDialog;
import lk.damithab.curenex.model.Address;

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