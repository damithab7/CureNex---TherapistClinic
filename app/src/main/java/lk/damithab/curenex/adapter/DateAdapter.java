package lk.damithab.curenex.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import lk.damithab.curenex.R;
import lk.damithab.curenex.model.DateModel;
import lk.damithab.curenex.model.Product;
import lk.damithab.curenex.model.TherapistSchedule;
import lombok.Getter;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.ViewHolder> {
    private List<DateModel> dateList;

    @Getter
    private OnDateClickListener listener;

    private int selectedPosition = 0;

    public DateAdapter(List<DateModel> dateList, OnDateClickListener listener){
        this.dateList = dateList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_slot, parent, false);
        return new ViewHolder(view);
    }

    int getThemeColor(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DateModel date = dateList.get(position);

        Context context = holder.itemView.getContext();

        /// selection part
        if(selectedPosition == position){
            holder.itemView.setBackgroundResource(R.drawable.bg_selected_item);
            holder.dateSlotName.setTextColor(Color.WHITE);
            holder.dateSlotDay.setTextColor(Color.WHITE);
        }else{
            holder.itemView.setBackgroundResource(R.drawable.bg_unselected_item);
            holder.dateSlotName.setTextColor(getThemeColor(context, com.google.android.material.R.attr.colorOnSurface));
            holder.dateSlotDay.setTextColor(getThemeColor(context, com.google.android.material.R.attr.colorOnSurface));
        }

        if(date.isPTO()){
            holder.itemView.setEnabled(false);
            holder.itemView.setBackgroundResource(R.drawable.bg_unavailable_item);
            holder.datePTOStatus.setVisibility(View.VISIBLE);
            holder.dateSlotName.setText(date.getDayName());
            holder.dateSlotDay.setText(date.getDateNum());
        }else{
            holder.itemView.setEnabled(true);
            holder.datePTOStatus.setVisibility(View.GONE);
            holder.dateSlotName.setText(date.getDayName());
            holder.dateSlotDay.setText(date.getDateNum());
        }

        holder.itemView.setOnClickListener(v->{
            int oldPos = selectedPosition;
            selectedPosition = holder.getBindingAdapterPosition();

            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);

            if(listener != null){
                listener.onDateItemClick(dateList.get(selectedPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView dateSlotName, dateSlotDay, datePTOStatus;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.dateSlotName = itemView.findViewById(R.id.item_dateslot_name);
            this.dateSlotDay = itemView.findViewById(R.id.item_dateslot_day);
            this.datePTOStatus = itemView.findViewById(R.id.item_date_pto_status);
        }
    }

    public interface OnDateClickListener{

        void onDateItemClick(DateModel schedule);
    }

}
