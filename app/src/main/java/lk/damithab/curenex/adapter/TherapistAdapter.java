package lk.damithab.curenex.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;
import java.util.Locale;

import lk.damithab.curenex.R;
import lk.damithab.curenex.model.Product;
import lk.damithab.curenex.model.Therapist;

public class TherapistAdapter extends RecyclerView.Adapter<TherapistAdapter.ViewHolder> {
    private List<Therapist> therapistList;

    private OnTherapistClickListener listener;

    private FirebaseStorage storage;

    public TherapistAdapter(List<Therapist> therapists, OnTherapistClickListener listener){
        this.therapistList = therapists;
        this.listener = listener;
        storage = FirebaseStorage.getInstance();
    }


    @NonNull
    @Override
    public TherapistAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_therapist, parent, false);
        return new TherapistAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TherapistAdapter.ViewHolder holder, int position) {
        Therapist therapist = therapistList.get(position);
        holder.therapistName.setText(therapist.getName());
        holder.therapistRating.setText(String.format(Locale.US, "LKR %,.2f",therapist.getRate()) +"/h");
        holder.therapistStarRating.setRating(therapist.getRating());
        StringBuilder starRateText = new StringBuilder();

        starRateText.append(" (")
                .append(therapist.getRating())
                .append(")");
        holder.therapistStarRateText.setText(starRateText);

        storage.getReference(therapist.getTherapistImage())
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Glide.with(holder.itemView.getContext())
                            .load(uri)
                            .centerCrop()
                            .into(holder.therapistImage);
                });

        holder.itemView.setOnClickListener(v -> {

            Animation animation = AnimationUtils.loadAnimation(v.getContext(), R.anim.button_click);
            v.startAnimation(animation);
            if(listener != null){
                listener.onTherapistItemClick(therapist);
            }
        });
    }

    @Override
    public int getItemCount() {
        return therapistList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView therapistImage;
        TextView therapistName, therapistRating, therapistStarRateText;
        RatingBar therapistStarRating;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.therapistImage = itemView.findViewById(R.id.item_therapist_image);
            this.therapistName = itemView.findViewById(R.id.item_therapist_name_text);
            this.therapistRating = itemView.findViewById(R.id.item_therapist_rating_text);
            this.therapistStarRating = itemView.findViewById(R.id.item_therapist_rating);
            this.therapistStarRateText = itemView.findViewById(R.id.item_therapist_star_rate_text);
        }
    }

    public interface OnTherapistClickListener{
        void onTherapistItemClick(Therapist therapist);
    }
}
