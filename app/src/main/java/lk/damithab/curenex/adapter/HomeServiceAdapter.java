package lk.damithab.curenex.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lk.damithab.curenex.R;
import lk.damithab.curenex.dto.ServiceDTO;

public class HomeServiceAdapter extends RecyclerView.Adapter<HomeServiceAdapter.ViewHolder> {
    private final List<ServiceDTO> serviceList;

    private Context context;

    public HomeServiceAdapter(List<ServiceDTO> categoryDTOList){
        this.serviceList = categoryDTOList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_service,parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ServiceDTO service = serviceList.get(position);
        holder.serviceName.setText(service.getName());

        String imageName = "category_"+service.getName().toLowerCase(); // name without extension
        String packageName = context.getPackageName();

// 1. Get the integer ID of the resource
        int resId = context.getResources().getIdentifier(imageName, "drawable", packageName);

// 2. Build the URI
        Uri uri = Uri.parse("android.resource://" + packageName + "/" + resId);
        holder.serviceImage.setImageURI(uri);
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView serviceImage;
        private TextView serviceName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.serviceImage = itemView.findViewById(R.id.home_c_image);
            this.serviceName = itemView.findViewById(R.id.home_c_text);
        }

        public ImageView getCategoryImage() {
            return serviceImage;
        }

        public TextView getCategoryName() {
            return serviceName;
        }
    }
}
