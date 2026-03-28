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
import androidx.core.app.RemoteInput;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import org.w3c.dom.Document;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lk.damithab.curenex.R;
import lk.damithab.curenex.activity.BookingHistoryActivity;
import lk.damithab.curenex.activity.MainActivity;
import lk.damithab.curenex.databinding.FragmentBookingOrderBinding;
import lk.damithab.curenex.dialog.SpinnerDialog;
import lk.damithab.curenex.listener.FirestoreCallback;
import lk.damithab.curenex.model.Booking;
import lk.damithab.curenex.model.City;
import lk.damithab.curenex.model.Notification;
import lk.damithab.curenex.model.Therapist;
import lk.damithab.curenex.util.RegexUtil;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;

public class BookingOrderFragment extends Fragment {

    private FragmentBookingOrderBinding binding;

    private String scheduleId;
    private String bookingDate;
    private String bookingTime;
    private String therapistId;

    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private double total;

    private boolean paymentActive = false;

    private FirebaseStorage storage;

    private static final String CHANNEL_ID = "bookings_channel";

    private static final String DATE_PICKER_TAG = "DATE_PICKER";

    private int completedTasks = 0;
    private final int TOTAL_TASKS = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.scheduleId = getArguments().getString("schedule_id");
            this.bookingDate = getArguments().getString("booking_date");
            this.therapistId = getArguments().getString("therapist_id");
            this.bookingTime = getArguments().getString("booking_time");

            Log.i(BookingOrderFragment.class.getSimpleName(), scheduleId + bookingDate + therapistId + bookingTime);
        }
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBookingOrderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.GONE);
        getActivity().findViewById(R.id.main_toolbar).setVisibility(View.GONE);

        createNotificationChannel();

        startDataLoading(true);

        getActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // DOB date picker
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Birth Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        TextInputEditText birthDateEditText = binding.bookingDetailsBirthdate;

        birthDateEditText.setOnClickListener(v -> {
            if (getParentFragmentManager().findFragmentByTag(DATE_PICKER_TAG) == null) {
                datePicker.show(getParentFragmentManager(), DATE_PICKER_TAG);
            }
        });

        binding.bookingCancelBtn.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // 3. Catch the result and format it
        datePicker.addOnPositiveButtonClickListener(selection -> {
            // Convert the selection (milliseconds) to a readable date
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String dateString = sdf.format(new Date(selection));

            birthDateEditText.setText(dateString);
        });

        double serviceFee = 500.0;

        total = 0;

        db.collection("cities").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        checkAllTasksFinished();
                        List<String> cities = new ArrayList<>();
                        if(!qds.isEmpty()){
                            List<City> cityList = qds.toObjects(City.class);
                            for(City city: cityList){
                                cities.add(city.getCityName());
                            }

                            AutoCompleteTextView shippingCities = binding.bookingDetailsCity;
                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_dropdown_item_1line, cities);
                            shippingCities.setAdapter(arrayAdapter);
                        }

                    }
                }).addOnFailureListener(error->{
                    checkAllTasksFinished();
                });

        getTherapistDetails(therapist -> {
            storage.getReference(therapist.getTherapistImage())
                    .getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        checkAllTasksFinished();
                        Glide.with(getContext())
                                .load(uri)
                                .centerCrop()
                                .into(binding.bookingTherapistImage);
                    }).addOnFailureListener(error->{
                        checkAllTasksFinished();
                    });

            binding.bookingTherapistName.setText(therapist.getTitle() + " " + therapist.getName());
            binding.bookingTherapistWorkemail.setText(therapist.getWorkEmail());
            binding.bookingTherapistDate.setText(bookingDate);
            binding.bookingTherapistTime.setText("Slot " + bookingTime);
            binding.bookingSubtotal.setText(String.valueOf(therapist.getRate()));
            StringBuilder rateText = new StringBuilder();
            rateText.append(therapist.getRate())
                    .append(" /h");

            total = serviceFee + therapist.getRate();

            binding.bookingTherapistRating.setText(rateText.toString());
            binding.bookingTherapistRating.setText(rateText);
            binding.bookingTotal.setText(String.valueOf(total));

        });

        binding.bookingServiceFee.setText(String.valueOf(serviceFee));

        binding.bookingMakepaymentBtn.setOnClickListener(v -> {

            String firstName = binding.bookingDetailsFirstname.getText().toString();
            String lastName = binding.bookingDetailsLastname.getText().toString();
            String contactNo = binding.bookingDetailsContact.getText().toString();
            String address = binding.bookingDetailsAddress1.getText().toString();
            String city = binding.bookingDetailsCity.getText().toString();
            String dob = binding.bookingDetailsBirthdate.getText().toString();

            binding.bookingDetailsFirstname.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {

                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    binding.bookingDetailsFirstnameLayout.setErrorEnabled(false);
                }
            });
            binding.bookingDetailsLastname.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {

                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    binding.bookingDetailsLastnameLayout.setErrorEnabled(false);
                }
            });
            binding.bookingDetailsContact.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {

                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    binding.bookingDetailsContactLayout.setErrorEnabled(false);
                }
            });
            binding.bookingDetailsAddress1.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {

                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    binding.bookingDetailsAddress1Layout.setErrorEnabled(false);
                }
            });
            binding.bookingDetailsCity.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {

                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    binding.bookingDetailsCityLayout.setErrorEnabled(false);
                }
            });
            binding.bookingDetailsBirthdate.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {

                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    binding.bookingDetailsBirthdateLayout.setErrorEnabled(false);
                }
            });

            if (firstName.isEmpty()) {
                binding.bookingDetailsFirstnameLayout.setErrorEnabled(true);
                binding.bookingDetailsFirstnameLayout.setError("Firstname is required!");
                binding.bookingDetailsFirstname.requestFocus();
                return;
            }

            if (!isCharacterValid(firstName)) {
                binding.bookingDetailsFirstnameLayout.setErrorEnabled(true);
                binding.bookingDetailsFirstnameLayout.setError("Invalid firstname!");
                binding.bookingDetailsFirstname.requestFocus();
                return;
            }


            if (lastName.isEmpty()) {
                binding.bookingDetailsLastnameLayout.setErrorEnabled(true);
                binding.bookingDetailsLastnameLayout.setError("Lastname is required!");
                binding.bookingDetailsLastname.requestFocus();
                return;
            }

            if (!isCharacterValid(lastName)) {
                binding.bookingDetailsLastnameLayout.setErrorEnabled(true);
                binding.bookingDetailsLastnameLayout.setError("Invalid lastname!");
                binding.bookingDetailsLastname.requestFocus();
                return;
            }


            if (contactNo.isEmpty()) {
                binding.bookingDetailsContactLayout.setErrorEnabled(true);
                binding.bookingDetailsContactLayout.setError("Contact number is required!");
                binding.bookingDetailsContact.requestFocus();
                return;
            }

            if (address.isEmpty()) {
                binding.bookingDetailsAddress1Layout.setErrorEnabled(true);
                binding.bookingDetailsAddress1Layout.setError("address is required!");
                binding.bookingDetailsAddress1.requestFocus();
                return;
            }

            if (city.isEmpty()) {
                binding.bookingDetailsCityLayout.setErrorEnabled(true);
                binding.bookingDetailsCityLayout.setError("city is required!");
                binding.bookingDetailsCity.requestFocus();
                return;
            }

            if (dob.isEmpty()) {
                binding.bookingDetailsBirthdateLayout.setErrorEnabled(true);
                binding.bookingDetailsBirthdateLayout.setError("Birthdate is required!");
                binding.bookingDetailsBirthdate.requestFocus();
                return;
            }


            binding.bookingDetailsFirstname.clearFocus();
            binding.bookingDetailsLastname.clearFocus();
            binding.bookingDetailsContact.clearFocus();
            binding.bookingDetailsAddress1.clearFocus();
            binding.bookingDetailsCity.clearFocus();
            binding.bookingDetailsBirthdate.clearFocus();

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

                Log.d("BookingFragment", binding.bookingDetailsFirstname.getText().toString());

                req.getCustomer().setFirstName(binding.bookingDetailsFirstname.getText().toString());
                req.getCustomer().setLastName(binding.bookingDetailsLastname.getText().toString());
                req.getCustomer().setPhone(binding.bookingDetailsContact.getText().toString());
                req.getCustomer().setEmail(firebaseAuth.getCurrentUser().getEmail());
                req.getCustomer().getAddress().setAddress(binding.bookingDetailsAddress1.getText().toString());
                req.getCustomer().getAddress().setCity(binding.bookingDetailsCity.getText().toString());
                req.getCustomer().getAddress().setCountry("Sri Lanka");

                req.setNotifyUrl("https://curenex.requestcatcher.com/");

                Intent intent = new Intent(getActivity(), PHMainActivity.class);
                intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);

                payhereLauncher.launch(intent);
            }
        });
    }

    private void checkAllTasksFinished() {
        completedTasks++;
        if (completedTasks >= TOTAL_TASKS) {
            onDataLoad(false);
        }
    }

    private void startDataLoading(boolean isShimmer) {
        onDataLoad(isShimmer);
    }

    private synchronized void onDataLoad(boolean isShimmer) {
        if (isShimmer) {
            binding.shimmerViewBookingContainer.startShimmer();
            binding.shimmerViewBookingContainer.setVisibility(View.VISIBLE);
            binding.bookingBottomLayout.setVisibility(View.GONE);
            binding.bookingOrderMain.setVisibility(View.GONE);
        } else {
            binding.shimmerViewBookingContainer.stopShimmer();
            binding.shimmerViewBookingContainer.setVisibility(View.GONE);
            binding.bookingBottomLayout.setVisibility(View.VISIBLE);
            binding.bookingOrderMain.setVisibility(View.VISIBLE);
        }
    }


    private void getTherapistDetails(FirestoreCallback<Therapist> callback) {
        db.collection("therapist").document(therapistId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Therapist therapist = documentSnapshot.toObject(Therapist.class);
                    Log.i("BookingOrder", therapist.getName());
                    callback.onCallback(therapist);
                }
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
                    saveBooking(statusResponse);


                    Log.i("PAYHERE", "Payment Success!");

                } else {
                    Log.e("PAYHERE", response.getData().getMessage());
                }

            }
        } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
            Log.e("PAYHERE", "Payment Canceled!");
        }

    });

    private void saveBooking(StatusResponse response) {

        SpinnerDialog spinner = SpinnerDialog.show(getParentFragmentManager());

        String uid = firebaseAuth.getCurrentUser().getUid();

        String firstName = binding.bookingDetailsFirstname.getText().toString().trim();
        String lastName = binding.bookingDetailsLastname.getText().toString().trim();
        String contactNo = binding.bookingDetailsContact.getText().toString().trim();
        String address = binding.bookingDetailsAddress1.getText().toString().trim();
        String city = binding.bookingDetailsCity.getText().toString().trim();
        String dob = binding.bookingDetailsBirthdate.getText().toString().trim();

        String name = firstName + " " + lastName;

        Booking booking = new Booking();
        booking.setBookingId(String.valueOf(System.currentTimeMillis()));
        booking.setScheduleId(scheduleId);
        booking.setTherapistId(therapistId);
        booking.setBookingDate(bookingDate);
        booking.setBookingTime(bookingTime);
        booking.setPatientName(name);
        booking.setPatientAddress(address);
        booking.setPatientMobile(contactNo);
        booking.setPatientCity(city);
        booking.setPatientDateOfBirth(dob);
        booking.setTotal(total);
        booking.setUid(uid);
        booking.setStatus("Confirmed");


        DocumentReference newBookingRef = db.collection("bookings").document();
        String generatedOrderId = newBookingRef.getId();
        booking.setDocId(generatedOrderId);
        newBookingRef.set(booking).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                spinner.dismiss();
                sendNotification(booking.getBookingId());
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.navContainerView, new BookingConfirmedFragment())
                        .addToBackStack(null)
                        .commit();
            }
        }).addOnFailureListener(error->{
            spinner.dismiss();
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

    private void sendNotification(String bookingId) {
        Intent intent = new Intent(getActivity(), BookingHistoryActivity.class);
        int requestCode = 0;

        String title = "Booking Confirmed!";
        String message = "Booking Confirmed: #" + bookingId;
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(),
                requestCode,
                intent,
                PendingIntent.FLAG_MUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());

        /// Save notification to db
        Notification notification = new Notification();

        Calendar calendar = Calendar.getInstance(); ///Today date
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()); /// to Store bookings date

        notification.setDate(dbFormat.format(calendar.getTime()));
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setUid(firebaseAuth.getUid());

        DocumentReference notifiRef = db.collection("notifications").document();
        String generatedId = notifiRef.getId();
        notification.setNotificationId(generatedId);
        notifiRef.set(notification).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        });
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(1, builder.build());
        }else{
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