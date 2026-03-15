package lk.damithab.curenex.adapter;

import android.content.Context;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Locale;

import lk.damithab.curenex.R;
import lk.damithab.curenex.model.Service;
import lk.damithab.curenex.model.Therapist;
import lk.damithab.curenex.module.GlideApp;

public class HomeTherapistAdapter extends RecyclerView.Adapter<HomeTherapistAdapter.ViewHolder> {
    private List<Therapist> therapistList;

    private Context context;

    private HomeTherapistAdapter.OnHomeTherapistItemClickListener listener;

    private FirebaseFirestore db;

    private FirebaseStorage storage;

    public HomeTherapistAdapter(List<Therapist> therapistList, HomeTherapistAdapter.OnHomeTherapistItemClickListener listener) {
        this.therapistList = therapistList;
        this.listener = listener;
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public HomeTherapistAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_therapist, parent, false);
        context = parent.getContext();
        return new HomeTherapistAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeTherapistAdapter.ViewHolder holder, int position) {

        Therapist therapist = therapistList.get(position);
        holder.therapistName.setText(therapist.getTitle() + " " + therapist.getName());

        db.collection("services").whereEqualTo("serviceId", therapist.getServiceId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot qds) {
                if (!qds.isEmpty()) {
                    Service service = qds.getDocuments().get(0).toObject(Service.class);
                    holder.therapistService.setText(service.getName());
                }
            }
        });

        StringBuilder rateText = new StringBuilder();
        rateText.append(String.format(Locale.US, "LKR %,.2f",therapist.getRate()))
                .append(" /h");

        holder.therapistRating.setText(rateText);

        holder.therapistStars.setRating(therapist.getRating());

        StringBuilder starRateText = new StringBuilder();
        starRateText.append(" (")
                .append(therapist.getRating())
                .append(")");
        holder.therapistRateText.setText(starRateText);

        StorageReference ref = storage.getReference(therapist.getTherapistImage());

        GlideApp.with(holder.itemView.getContext())
                .load(ref)
                .centerCrop()
                .placeholder(R.drawable.imageplaceholder2)
                .into(holder.therapistImage);

        holder.itemView.setOnClickListener(v -> {
            Animation animation = AnimationUtils.loadAnimation(v.getContext(), R.anim.button_click);
            v.startAnimation(animation);
            if (listener != null) {
                listener.onHomeTherapistItemClick(therapist);
            }
        });
    }

    @Override
    public int getItemCount() {
        return therapistList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView therapistImage;
        TextView therapistName, therapistService, therapistRating, therapistRateText;
        RatingBar therapistStars;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.therapistImage = itemView.findViewById(R.id.home_item_therapist_image);
            this.therapistName = itemView.findViewById(R.id.home_item_therapist_name);
            this.therapistService = itemView.findViewById(R.id.home_item_therapist_service);
            this.therapistRating = itemView.findViewById(R.id.home_item_therapist_rate);
            this.therapistStars = itemView.findViewById(R.id.home_item_therapist_stars);
            this.therapistRateText = itemView.findViewById(R.id.home_item_therapist_rate_text);
        }

    }

    public interface OnHomeTherapistItemClickListener {
        void onHomeTherapistItemClick(Therapist therapist);
    }
}
