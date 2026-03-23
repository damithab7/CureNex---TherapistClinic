package lk.damithab.curenex.dialog;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.RangeSlider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lk.damithab.curenex.R;
import lk.damithab.curenex.databinding.FilterListingBottomSheetBinding;
import lk.damithab.curenex.databinding.FilterTherapistBottomSheetBinding;
import lk.damithab.curenex.model.Gender;
import lk.damithab.curenex.model.Service;
import lk.damithab.curenex.model.TherapistFilter;
import lk.damithab.curenex.model.TherapistSchedule;

public class FilterTherapistBottomSheet extends BottomSheetDialogFragment {

    private OnApplyListener listener;

    private OnResetListener resetListener;

    private FilterTherapistBottomSheetBinding binding;

    private String selectedTime;

    private FirebaseFirestore db;

    private Map<String, Integer> dayMap = new HashMap<>();

    private Spinner serviceSpinner, genderSpinner;

    private List<Gender> genderList;
    private List<Service> serviceList;

    private String genderId, serviceId;

    private float valueFrom, valueTo;

    public interface OnApplyListener {
        void onAdded(TherapistFilter filter);
    }
    public interface OnResetListener {
        void onReset();
    }

    public FilterTherapistBottomSheet(OnApplyListener listener, OnResetListener resetListener) {
        this.listener = listener;
        this.resetListener = resetListener;
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FilterTherapistBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MaterialButton apply = binding.filterTherapistApplyBtn;
        MaterialButton reset = binding.filterTherapistResetBtn;

        serviceSpinner = binding.filterTherapistServiceSpinner;
        genderSpinner = binding.filterTherapistGenderSpinner;

        db.collection("gender").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot qds) {
                if (!qds.isEmpty()) {
                    genderList = qds.toObjects(Gender.class);

                    List<String> genders = new ArrayList<>();

                    for (Gender gender : genderList) {
                        genders.add(gender.getName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireActivity(), R.layout.spinner_item, genders);
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    genderSpinner.setAdapter(adapter);
                    genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            genderId = genderList.get(i).getGenderId();
                            Log.d("AddTherapistFragment", "GenderId: " + genderId);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                }
            }
        });

        db.collection("services").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot qds) {
                if (!qds.isEmpty()) {
                    serviceList = qds.toObjects(Service.class);

                    List<String> services = new ArrayList<>();

                    for (Service service : serviceList) {
                        services.add(service.getName());
                    }


                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireActivity(), R.layout.spinner_item, services);
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    serviceSpinner.setAdapter(adapter);
                    serviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            serviceId = serviceList.get(i).getServiceId();
                            Log.d("AddTherapistFragment", "ServiceId: " + serviceId);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                }
            }
        });

        List<String> weekDays = generateDayOfWeek();

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<String>(requireActivity(), R.layout.spinner_item, weekDays);
        statusAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        serviceSpinner.setAdapter(statusAdapter);

        RangeSlider rangeSlider = binding.filterTherapistPriceBar;

        rangeSlider.setValueFrom(0.0f);
        rangeSlider.setValueTo(1000000.0f);
        rangeSlider.setStepSize(1.0f);
        rangeSlider.setValues(0.0f, 500000.0f);

        rangeSlider.setLabelFormatter(value -> {
            float actualPrice = value / 100.0f;
            return String.format(Locale.US, "LKR %.2f", actualPrice);
        });

        List<Float> initialValues = rangeSlider.getValues();
        valueFrom = (initialValues.get(0) / 100.0f);
        valueTo = (initialValues.get(1) / 100.0f);

        rangeSlider.addOnChangeListener(new RangeSlider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                List<Float> values = slider.getValues();
                valueFrom = values.get(0) / 100.0f;
                valueTo = values.get(1) / 100.0f;
            }
        });


        apply.setOnClickListener(v -> {

            TherapistFilter therapistFilter = new TherapistFilter();
            therapistFilter.setServiceId(serviceId);
            therapistFilter.setGenderId(genderId);
            therapistFilter.setStartPrice(formatValue(valueFrom));
            therapistFilter.setEndPrice(formatValue(valueTo));

            Log.d("Filter bottom sheet", "onViewCreated: "+serviceId +" gender" + genderId + " "+String.valueOf(valueTo) + String.valueOf(valueFrom));

            listener.onAdded(therapistFilter);
            dismiss();
        });

        reset.setOnClickListener(v->{
            rangeSlider.setValues(0.0f, 5000.0f);
            serviceSpinner.setSelection(0);
            genderSpinner.setSelection(0);

            // Reset local variables
            valueFrom = 0.0f;
            valueTo = 5000.0f;
            serviceId = null;
            genderId = null;

            dismiss();
        });
    }

    private List<String>generateDayOfWeek(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dayNumFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        List<String> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            String dayName = dayNumFormat.format(calendar.getTime());
            int dayInt = calendar.get(Calendar.DAY_OF_WEEK);

            dayMap.put(dayName, dayInt);

            days.add(dayName);

            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        return days;
    }

    private double formatValue(float value) {
        return new BigDecimal(Float.toString(value))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
