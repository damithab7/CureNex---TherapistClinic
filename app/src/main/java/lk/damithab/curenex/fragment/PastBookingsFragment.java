package lk.damithab.curenex.fragment;

import android.os.Bundle;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import lk.damithab.curenex.R;
import lk.damithab.curenex.adapter.PastBookingsAdapter;
import lk.damithab.curenex.adapter.UpcomingBookingsAdapter;
import lk.damithab.curenex.databinding.FragmentPastBookingsBinding;
import lk.damithab.curenex.databinding.FragmentUpcomingBookingsBinding;
import lk.damithab.curenex.dialog.ReviewsBottomSheet;
import lk.damithab.curenex.dialog.SpinnerDialog;
import lk.damithab.curenex.model.Booking;
import lk.damithab.curenex.model.Reviews;


public class PastBookingsFragment extends Fragment {

    private FragmentPastBookingsBinding binding;

    private FirebaseFirestore db;

    private FirebaseAuth auth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPastBookingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Calendar calendar = Calendar.getInstance(); ///Today date
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); //
        String todayDate = dbFormat.format(calendar.getTime());

        SpinnerDialog spinner = SpinnerDialog.show(getParentFragmentManager());

        db.collection("bookings")
                .whereEqualTo("uid", auth.getUid())
                .whereLessThan("bookingDate", todayDate)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        if (!qds.isEmpty()) {
                            List<Booking> bookingList = qds.toObjects(Booking.class);
                            PastBookingsAdapter adapter = new PastBookingsAdapter(bookingList);
                            adapter.setReviewBtnListener(obj -> {

                                Booking booking = (Booking) obj;
                                ReviewsBottomSheet sheet = new ReviewsBottomSheet(obj, () -> {
                                    adapter.notifyDataSetChanged();
                                });

                                sheet.show(getChildFragmentManager(), "ScheduleBottomSheet");

                            });

                            binding.pastBookingsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                            binding.pastBookingsRecyclerView.setAdapter(adapter);
                        }
                        spinner.dismiss();
                    }
                });

    }
}