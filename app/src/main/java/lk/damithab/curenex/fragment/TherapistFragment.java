package lk.damithab.curenex.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import lk.damithab.curenex.R;
import lk.damithab.curenex.databinding.FragmentTherapistBinding;

public class TherapistFragment extends Fragment {

    private FragmentTherapistBinding binding;

    private String serviceId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            serviceId =getArguments().getString("serviceId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_therapist, container, false);
    }
}