package lk.damithab.curenex.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import lk.damithab.curenex.R;
import lk.damithab.curenex.model.Booking;
import lk.damithab.curenex.model.Reviews;
import lk.damithab.curenex.model.Therapist;
import lk.damithab.curenex.module.GlideApp;

public class PastBookingsAdapter extends RecyclerView.Adapter<PastBookingsAdapter.ViewHolder> {

    private List<Booking> bookingList;

    private FirebaseFirestore db;

    private FirebaseStorage storage;

    private OnReviewBtnClick listener;

    public void setReviewBtnListener (OnReviewBtnClick listener){
        this.listener = listener;
    }

    public PastBookingsAdapter(List<Booking> bookingList) {
        this.bookingList = bookingList;
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bookings, parent, false);
        return new PastBookingsAdapter.ViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        db.collection("therapist").whereEqualTo("therapistId", booking.getTherapistId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        if(!qds.isEmpty()){
                            Therapist therapist = qds.toObjects(Therapist.class).get(0);
                            holder.bookingTherapistName.setText(therapist.getTitle()+" "+therapist.getName());
                            StorageReference ref = storage.getReference(therapist.getTherapistImage());

                            GlideApp.with(holder.itemView.getContext())
                                    .load(ref)
                                    .centerCrop()
                                    .placeholder(R.drawable.imageplaceholder2)
                                    .into(holder.therapistImage);
                        }
                    }
                });
        holder.bookingId.setText("#" + booking.getBookingId().toUpperCase());
        holder.bookingDate.setText(booking.getBookingDate());
        holder.bookingTimeSlot.setText(booking.getBookingTime());
        holder.bookingStatus.setText(booking.getStatus());
        holder.statusCard.setCardBackgroundColor(R.color.md_theme_surfaceVariant);
        holder.bookingTotal.setText(String.format(Locale.US, "LKR %,.2f",booking.getTotal()));

        if(Objects.equals(booking.getStatus(), "Cancelled")){
            holder.statusCard.setCardBackgroundColor(Color.RED);
            holder.bookingStatus.setTextColor(Color.WHITE);
            holder.cancelButton.setVisibility(View.GONE);
        }

        db.collection("reviews").whereEqualTo("uid", booking.getUid()).whereEqualTo("type","therapist")
                .whereEqualTo("typeId", booking.getTherapistId()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        if(!qds.isEmpty()){
                            Reviews review = qds.toObjects(Reviews.class).get(0);
                            holder.cancelButton.setEnabled(true);
                            holder.cancelButton.setText("Set Review");
                            holder.cancelButton.setOnClickListener(v->{
                                if(listener != null){
                                    listener.onClick(review);
                                }
                            });
                        }else{
                            holder.cancelButton.setEnabled(true);
                            holder.cancelButton.setText("View Review");
                            holder.cancelButton.setOnClickListener(v->{
                                if(listener != null){
                                    listener.onClick(booking);
                                }
                            });
                        }
                    }
                });

    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView bookingId, bookingTherapistName, bookingDate, bookingTimeSlot, bookingStatus, bookingTotal;

        ImageView therapistImage;

        MaterialButton cancelButton;

        CardView statusCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.bookingId = itemView.findViewById(R.id.item_booking_id);
            this.bookingTherapistName = itemView.findViewById(R.id.item_booking_therapist_name);
            this.therapistImage = itemView.findViewById(R.id.item_booking_therapist_img);
            this.bookingId = itemView.findViewById(R.id.item_booking_id);
            this.bookingDate = itemView.findViewById(R.id.item_booking_date);
            this.bookingTimeSlot = itemView.findViewById(R.id.item_booking_time_slot);
            this.bookingStatus = itemView.findViewById(R.id.item_booking_status);
            this.bookingTotal = itemView.findViewById(R.id.item_booking_total);
            this.cancelButton = itemView.findViewById(R.id.item_booking_cancel_btn);
            this.statusCard = itemView.findViewById(R.id.item_booking_status_card);
        }
    }

    public interface OnReviewBtnClick {
        void onClick(Object object);
    }

}

