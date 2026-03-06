package lk.damithab.curenex.adapter;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.damithab.curenex.R;
import lk.damithab.curenex.model.DateModel;
import lk.damithab.curenex.model.TherapistSchedule;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.ViewHolder> {
    private List<TherapistSchedule> timeSlots;

    private OnTimeSlotClickListener listener;

    private int selectedPosition = 0;

    private Map<String, Integer> bookingCounts = new HashMap<>();

    public TimeSlotAdapter(List<TherapistSchedule> timeSlots, OnTimeSlotClickListener listener){
        this.timeSlots = timeSlots;
        this.listener = listener;
    }

    public void setList(List<TherapistSchedule> newList, Map<String, Integer> counts){
        this.timeSlots = newList;
        this.bookingCounts = counts;
        this.selectedPosition = -1;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time_slot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TherapistSchedule slot = timeSlots.get(position);
        Log.d("TimeSlotAdapter", slot.getScheduleId());
        int max = slot.getMaxAppointments();
        int currentBooked = bookingCounts.getOrDefault(slot.getScheduleId(), 0);
        int availableSlots = max - currentBooked;

        holder.timeSlotText.setText(slot.getStartTime());

        if(availableSlots <= 0){
            holder.itemView.setEnabled(false);
            holder.itemView.setAlpha(0.5f);
            holder.availableSlotsText.setText("Full");
        }else{
            holder.itemView.setEnabled(true);
            holder.itemView.setAlpha(1.0f);
            holder.availableSlotsText.setText("Available: "+availableSlots);
        }

        /// selection part
        if(selectedPosition == position){
            holder.itemView.setBackgroundResource(R.drawable.bg_selected_time);
        }else{
            holder.itemCard.setBackgroundResource(R.drawable.bg_unselected_time);
        }

        holder.itemView.setOnClickListener(v->{
            int oldPos = selectedPosition;
            selectedPosition = holder.getBindingAdapterPosition();

            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);

            if(listener != null){
                listener.onTimeItemClick(slot);
            }
        });
    }

    @Override
    public int getItemCount() {
        return timeSlots.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView timeSlotText;
        TextView availableSlotsText;

        MaterialCardView itemCard;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.timeSlotText = itemView.findViewById(R.id.item_time_slot);
            this.availableSlotsText = itemView.findViewById(R.id.availableSlotsText);
            this.itemCard = itemView.findViewById(R.id.item_time_slot_cart);
        }
    }

    public interface OnTimeSlotClickListener{

        void onTimeItemClick(TherapistSchedule schedule);
    }
}
