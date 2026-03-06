package lk.damithab.curenex.fragment;

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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import lk.damithab.curenex.R;
import lk.damithab.curenex.adapter.DateAdapter;
import lk.damithab.curenex.adapter.TimeSlotAdapter;
import lk.damithab.curenex.databinding.FragmentSingleTherapistBinding;
import lk.damithab.curenex.model.Booking;
import lk.damithab.curenex.model.DateModel;
import lk.damithab.curenex.model.Therapist;
import lk.damithab.curenex.model.TherapistSchedule;

public class SingleTherapistFragment extends Fragment {

    private FragmentSingleTherapistBinding binding;

    private String therapistId;

    private DateAdapter dateAdapter;

    private TimeSlotAdapter timeSlotAdapter;

    private FirebaseFirestore db;
    private TherapistSchedule selectedSlot = null;
    private DateModel selectedDate = null;

    private String selectedTime = null;

    private FirebaseAuth firebaseAuth;

    private String docId;

    private FirebaseStorage storage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.therapistId = getArguments().getString("therapistId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        storage = FirebaseStorage.getInstance();
        binding = FragmentSingleTherapistBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        getActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

//         Define dummy users from your users collection
//        String user1 = "EqldpazzG5erZSgYNCVk8Fyx0xw2";
//        String user2 = "HzCCHBHjNmY4XUPQiy8AjMNkBJG2";
//        String user3 = "bNGKVYISjCND4QFN8QHU12iy51X2";
//
/// Use the Firestore document ID for therapistId as we discussed
//        String therapistDocId = "HMkbqNgOJEoOeRjfhWE5";
//        String scheduleId = "sch3"; // From your schedule sub-collection
//        String testDate = "2026-02-27";
//
//        List<Booking> dummyBookings = List.of(
//                new Booking("b1", scheduleId, therapistDocId, testDate, user1, "Confirmed"),
//                new Booking("b2", scheduleId, therapistDocId, testDate, user2, "Confirmed"),
//                new Booking("b3", scheduleId, therapistDocId, testDate, user3, "Pending")
//        );
//
//        WriteBatch batch = db.batch();
//
//        for (Booking b : dummyBookings) {
//            // Creating documents with auto-generated IDs in the root 'bookings' collection
//            DocumentReference ref = db.collection("bookings").document();
//            b.setBookingId(ref.getId()); // Sync the internal ID with Firestore ID
//            batch.set(ref, b);
//        }
//
//        batch.commit();

        getActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.GONE);

        getActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        db.collection("therapist")
                .whereEqualTo("therapistId", therapistId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        if (!qds.isEmpty()) {

                            DocumentSnapshot doc = qds.getDocuments().get(0);
                            docId = doc.getId();

                            Therapist therapist = qds.getDocuments().get(0).toObject(Therapist.class);

                            binding.sTName.setText(therapist.getTitle() + " " + therapist.getName());
                            binding.sTAbout.setText(therapist.getBio());
                            binding.sTRate.setText(String.format(Locale.US, "LKR %,.2f",therapist.getRate()) +"/h");
                            storage.getReference(therapist.getTherapistImage())
                                    .getDownloadUrl()
                                    .addOnSuccessListener(uri -> {
                                        Glide.with(binding.getRoot())
                                                .load(uri)
                                                .centerCrop()
                                                .into(binding.singleTImageView);
                                    });

                            db.collection("therapist").document(docId).collection("schedule")
                                    .get()
                                    .addOnSuccessListener(ds -> {
                                        if (!ds.isEmpty()) {

                                            List<TherapistSchedule> currentSchedule = ds.toObjects(TherapistSchedule.class);

                                            Set<Integer> workingDays = new HashSet<>();
                                            for (TherapistSchedule slot : currentSchedule) {
                                                workingDays.add(slot.getDayOfWeek());
                                            }

                                            /// Initialize time slot adapter here

                                            List<DateModel> uniqueDates = generateDatesFor(workingDays);

                                            timeSlotAdapter = new TimeSlotAdapter(new ArrayList<>(), schedule -> {
                                                selectedTime = schedule.getStartTime();
                                                SingleTherapistFragment.this.selectedSlot = schedule;
                                            });

                                            binding.sTTimerecycle.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                                            binding.sTTimerecycle.setAdapter(timeSlotAdapter);

                                            dateAdapter = new DateAdapter(uniqueDates, selectedDate -> {

//                                                displayTimeSlots(selectedDate, currentSchedule);

                                                SingleTherapistFragment.this.selectedDate = selectedDate; // Store the chosen date
                                                SingleTherapistFragment.this.selectedSlot = null; // Reset time selection when day changes


                                                /// Filter Time Schedules
                                                List<TherapistSchedule> dailySlots = new ArrayList<>();
                                                for (TherapistSchedule s : currentSchedule) {
                                                    if (s.getDayOfWeek() == selectedDate.getDayOfWeek()) {
                                                        dailySlots.add(s);
                                                    }
                                                }

                                                Log.d("bookigns", "Date is selected"+"therapistId "+docId+" | bookingDate "+ selectedDate.getFullDate());

                                                db.collection("bookings")
                                                        .whereEqualTo("therapistId", docId)
                                                        .whereEqualTo("bookingDate", selectedDate.getFullDate())
                                                        .get()
                                                        .addOnSuccessListener(bookingDocs -> {

                                                            Map<String, Integer> bookingCounts = new HashMap<>();
                                                            for (DocumentSnapshot bookingDoc : bookingDocs) {
                                                                String sid = bookingDoc.getString("scheduleId");
                                                                bookingCounts.put(sid, bookingCounts.getOrDefault(sid, 0) + 1);
                                                            }

                                                            timeSlotAdapter.setList(dailySlots, bookingCounts);

                                                        });


                                            });

                                            binding.sTDaysrecycle.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                                            binding.sTDaysrecycle.setAdapter(dateAdapter);

                                            if (!uniqueDates.isEmpty()) {
                                                // This simulates a click on the first generated date
                                                dateAdapter.getListener().onDateItemClick(uniqueDates.get(0));
                                            }
                                        }

                                    });

                        }
                    }
                });


        binding.singleTBooknowbtn.setOnClickListener(v->{
            if (selectedDate == null) {
                Toast.makeText(getContext(), "Please select a date", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedSlot == null) {
                Toast.makeText(getContext(), "Please select a time slot", Toast.LENGTH_SHORT).show();
                return;
            }

            // --- SUCCESS: You have everything you need ---
            String scheduleId = selectedSlot.getScheduleId(); // The specific ID from Firestore
            String bookingDate = selectedDate.getFullDate(); // e.g., "2026-02-27"
            String therapistDocId = docId; // The therapist's auto-ID (HMkbq...)

            Log.d("SingleTherapist", scheduleId + bookingDate + therapistDocId);

            BookingOrderFragment bookingOrderFragment = new BookingOrderFragment();

            Bundle args = new Bundle();
            args.putString("schedule_id", scheduleId);
            args.putString("booking_date", bookingDate);
            args.putString("booking_time", selectedTime);
            args.putString("therapist_id", therapistDocId);

            bookingOrderFragment.setArguments(args);

            getParentFragmentManager().beginTransaction().replace(R.id.navContainerView, bookingOrderFragment)
                    .addToBackStack(null)
                    .commit();

//            proceedToConfirmation(therapistDocId, scheduleId, bookingDate);
        });
    }


    private List<DateModel> generateDatesFor(Set<Integer> workingDays) {
        List<DateModel> dateList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance(); ///Today date

        SimpleDateFormat dayNumFormat = new SimpleDateFormat("EEE", Locale.getDefault()); ///Like "Fri, Tue"
        SimpleDateFormat dateNumFormat = new SimpleDateFormat("dd", Locale.getDefault()); /// like 30, 28
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); /// to Store bookings date

        /// Check dates for next 14 days
        for (int i = 0; i < 14; i++) {
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); ///mon to sunday

            /// Adding necessary dates where therapist works
            if (workingDays.contains(dayOfWeek)) {
                dateList.add(new DateModel(
                        dayNumFormat.format(calendar.getTime()),
                        dateNumFormat.format(calendar.getTime()),
                        dayOfWeek,
                        dbFormat.format(calendar.getTime())
                ));
            }

            /// move calendar date by 1 day
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        return dateList;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.GONE);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.VISIBLE);
    }
}