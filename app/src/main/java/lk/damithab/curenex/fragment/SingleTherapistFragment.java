package lk.damithab.curenex.fragment;

import android.content.Intent;
import android.net.Uri;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import lk.damithab.curenex.R;
import lk.damithab.curenex.activity.MainActivity;
import lk.damithab.curenex.activity.SignInActivity;
import lk.damithab.curenex.adapter.DateAdapter;
import lk.damithab.curenex.adapter.TimeSlotAdapter;
import lk.damithab.curenex.databinding.FragmentSingleTherapistBinding;
import lk.damithab.curenex.dialog.MessageDialog;
import lk.damithab.curenex.dialog.ToastDialog;
import lk.damithab.curenex.listener.FirestoreCallback;
import lk.damithab.curenex.model.Booking;
import lk.damithab.curenex.model.DateModel;
import lk.damithab.curenex.model.PTO;
import lk.damithab.curenex.model.Product;
import lk.damithab.curenex.model.Therapist;
import lk.damithab.curenex.model.TherapistSchedule;
import lk.damithab.curenex.util.AnimationUtil;

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

    private int completedTasks = 0;
    private final int TOTAL_TASKS = 3;

    private List<PTO> ptoList;

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

        AnimationUtil.bottomSlideDown(getActivity().findViewById(R.id.bottomNavigationView));

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

        startDataLoading(true);

        binding.singleTBooknowbtn.setOnClickListener(v -> {
            if (selectedDate == null) {
                Toast.makeText(getContext(), "Please select a date", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedSlot == null) {
                Toast.makeText(getContext(), "Please select a time slot", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            if (firebaseAuth.getCurrentUser() == null) {
                Intent intent = new Intent(getActivity(), SignInActivity.class);
                startActivity(intent);
            } else {

                String scheduleId = selectedSlot.getScheduleId();
                String bookingDate = selectedDate.getFullDate();
                String therapistDocId = docId;

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
            }
//            proceedToConfirmation(therapistDocId, scheduleId, bookingDate);
        });
    }

    private void checkAllTasksFinished() {
        completedTasks++;
        Log.d("HomeFragment", "checkAllTasksFinished: " + completedTasks);
        if (completedTasks >= TOTAL_TASKS) {
            onDataLoad(false);
            completedTasks = 0;
        }
    }

    private void startDataLoading(boolean isShimmer) {
        onDataLoad(isShimmer);
        loadData();
    }

    private synchronized void onDataLoad(boolean isShimmer) {
        if (isShimmer) {
            binding.shimmerSingleTherapistViewContainer.startShimmer();
            binding.shimmerSingleTherapistViewContainer.setVisibility(View.VISIBLE);
            binding.singleTMain.setVisibility(View.GONE);
        } else {
            binding.shimmerSingleTherapistViewContainer.stopShimmer();
            binding.shimmerSingleTherapistViewContainer.setVisibility(View.GONE);
            binding.singleTMain.setVisibility(View.VISIBLE);
        }
    }

    private void loadData() {
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

                            String therapistName = therapist.getTitle() + " " + therapist.getName();

                            binding.sTName.setText(therapistName);
                            binding.sTRating.setText(String.valueOf(therapist.getRating()));
                            binding.singleTWorkemail.setOnClickListener(v -> {

                                Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.click_anim);
                                anim.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {
                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                                        intent.setData(Uri.parse("mailto:" + therapist.getWorkEmail()));
                                        intent.putExtra(Intent.EXTRA_SUBJECT, "Inquiry from Therapist " + therapistName);

                                        startActivity(Intent.createChooser(intent, "Send Email"));

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {
                                    }
                                });
                                v.startAnimation(anim);

                            });

                            binding.singleTWorkmobile.setOnClickListener(v -> {
                                Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.click_anim);
                                anim.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {
                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        Intent intent = new Intent(Intent.ACTION_DIAL);
                                        intent.setData(Uri.parse("tel:" + therapist.getWorkMobileNo()));
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {
                                    }
                                });
                                v.startAnimation(anim);

                            });
                            binding.sTAbout.setText(therapist.getBio());
                            binding.sTRate.setText(String.format(Locale.US, "LKR %,.2f", therapist.getRate()) + "/h");
                            storage.getReference(therapist.getTherapistImage())
                                    .getDownloadUrl()
                                    .addOnSuccessListener(uri -> {
                                        Glide.with(binding.getRoot())
                                                .load(uri)
                                                .centerCrop()
                                                .into(binding.singleTImageView);
                                        checkAllTasksFinished();
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

                                            generateDatesFor(workingDays, uniqueDates->{
                                                timeSlotAdapter = new TimeSlotAdapter(new ArrayList<>(), schedule -> {
                                                    selectedTime = schedule.getStartTime();
                                                    SingleTherapistFragment.this.selectedSlot = schedule;
                                                });

                                                binding.sTTimerecycle.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                                                binding.sTTimerecycle.setAdapter(timeSlotAdapter);

                                                dateAdapter = new DateAdapter(uniqueDates, selectedDate -> {

//                                                displayTimeSlots(selectedDate, currentSchedule);

                                                    SingleTherapistFragment.this.selectedDate = selectedDate;
                                                    SingleTherapistFragment.this.selectedSlot = null;

                                                    /// Filter Time Schedules
                                                    List<TherapistSchedule> dailySlots = new ArrayList<>();
                                                    for (TherapistSchedule s : currentSchedule) {
                                                        if (s.getDayOfWeek() == selectedDate.getDayOfWeek()) {
                                                            dailySlots.add(s);
                                                        }
                                                    }

                                                    Log.d("bookigns", "Date is selected" + "therapistId " + docId + " | bookingDate " + selectedDate.getFullDate());


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
                                                    dateAdapter.getListener().onDateItemClick(uniqueDates.get(0));
                                                }
                                            });

                                        }

                                        checkAllTasksFinished();

                                    });

                        }
                    }
                }).addOnFailureListener(aVoid -> {
                    checkAllTasksFinished();
                });
    }


    private void generateDatesFor(Set<Integer> workingDays, OnDatesGeneratedListener listener) {
        List<DateModel> dateList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance(); ///Today date

        SimpleDateFormat dayNumFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat dateNumFormat = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        ptoList = new ArrayList<>();

        /// get data from PTO (Paid time Off)
        getPTO(ptoList -> {
            checkAllTasksFinished();
            /// Check dates for next 14 days
            for (int i = 0; i < 14; i++) {
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); ///mon to sunday

                String currentDayStr = dbFormat.format(calendar.getTime());
                Date currentDayDate = null;
                try {
                    currentDayDate = dbFormat.parse(currentDayStr);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                boolean isOnPTO = false;

                if(!ptoList.isEmpty()) {
                    for (PTO pto : ptoList) {
                        try {
                            if (pto.getDate().getType().equals("range")) {
                                Date start = dbFormat.parse(pto.getDate().getStartDate());
                                Date end = dbFormat.parse(pto.getDate().getEndDate());
                                if (currentDayDate != null && !currentDayDate.before(start) && !currentDayDate.after(end)) {
                                    isOnPTO = true;
                                    break;
                                }
                            } else {
                                if (currentDayStr.equals(pto.getDate().getStartDate())) {
                                    isOnPTO = true;
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                /// Adding necessary dates where therapist works
                if (workingDays.contains(dayOfWeek)) {
                    dateList.add(new DateModel(
                            dayNumFormat.format(calendar.getTime()),
                            dateNumFormat.format(calendar.getTime()),
                            dayOfWeek,
                            dbFormat.format(calendar.getTime()),
                            isOnPTO
                    ));
                }

                /// move calendar date by 1 day
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            listener.onGenerated(dateList);

        });
    }

    interface OnDatesGeneratedListener {
        void onGenerated(List<DateModel> dates);
    }

    private void getPTO(FirestoreCallback<List<PTO>> callback) {
        db.collection("therapist").document(therapistId).collection("pto").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        ptoList = qds.toObjects(PTO.class);
                        callback.onCallback(ptoList);
                    }
                });
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