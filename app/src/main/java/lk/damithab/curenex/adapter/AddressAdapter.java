package lk.damithab.curenex.adapter;

import android.graphics.Color;
import android.util.Log;
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
import com.google.android.material.button.MaterialButton;

import java.util.List;

import lk.damithab.curenex.R;
import lk.damithab.curenex.model.Address;
import lk.damithab.curenex.model.Product;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {
    private List<Address> addressList;

    private OnAddressClickListener listener;

    private OnAddressEditListener onAddressEditListener;
    private OnAddressRemoveListener onAddressRemoveListener;

    public AddressAdapter(List<Address> addressList, OnAddressClickListener listener) {
        this.addressList = addressList;
        this.listener = listener;
    }

    public void setOnEditListener(OnAddressEditListener onEditListener){
        this.onAddressEditListener = onEditListener;
    }
   public void setOnRemoveListener(OnAddressRemoveListener onRemoveListener){
        this.onAddressRemoveListener = onRemoveListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        int currentPosition = holder.getAbsoluteAdapterPosition();
        if(currentPosition == RecyclerView.NO_POSITION){
            return;
        }

        Address address = addressList.get(position);
        holder.addressName.setText(address.getName());
        holder.addressAddress1.setText(address.getAddress1());
        holder.addressCity.setText(address.getCity());
        holder.addressPostcode.setText(address.getPostcode());
        holder.addressMobile.setText(address.getContact());

        holder.itemView.setOnClickListener(v -> {

            Animation animation = AnimationUtils.loadAnimation(v.getContext(), R.anim.button_click);
            v.startAnimation(animation);
            if (listener != null) {
                listener.onAddressItemClick(address);
            }
        });

        holder.btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = holder.getAbsoluteAdapterPosition();
                Log.i("Position", String.valueOf(pos));
                if (pos != RecyclerView.NO_POSITION && onAddressRemoveListener != null) {
                    onAddressRemoveListener.onAddressItemRemove(currentPosition);
                }
            }
        });

        holder.btnEdit.setOnClickListener(v->{
            if(onAddressEditListener != null){
                onAddressEditListener.onAddressItemEdit(address);
            }
        });


    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView addressName, addressAddress1, addressCity, addressPostcode, addressMobile;

        MaterialButton btnRemove, btnEdit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.addressName = itemView.findViewById(R.id.item_address_name);
            this.addressAddress1 = itemView.findViewById(R.id.item_address_address);
            this.addressCity = itemView.findViewById(R.id.item_address_city);
            this.addressPostcode = itemView.findViewById(R.id.item_address_postcode);
            this.addressMobile = itemView.findViewById(R.id.item_address_mobile);
            this.btnRemove = itemView.findViewById(R.id.item_address_remove);
            this.btnEdit = itemView.findViewById(R.id.item_address_edit);
        }
    }

    public interface OnAddressClickListener {
        void onAddressItemClick(Address address);
    }

    public interface OnAddressRemoveListener {
        void onAddressItemRemove(int position);
    }

    public interface OnAddressEditListener {
        void onAddressItemEdit(Address address);
    }
}
