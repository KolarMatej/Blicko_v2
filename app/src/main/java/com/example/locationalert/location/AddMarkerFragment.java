package com.example.locationalert.location;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.locationalert.R;
import com.example.locationalert.marker.AddMarkerCallback;
import com.google.android.gms.maps.model.LatLng;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddMarkerFragment extends DialogFragment {

    @BindView(R.id.police_marker_button)
    Button policeMarkerButton;

    @BindView(R.id.accident_marker_button)
    Button accidentMarkerButton;

    @BindView(R.id.road_work_marker_button)
    Button roadWorkMarkerButton;

    private AddMarkerCallback callback;
    private LatLng location;

    public void setCallback(AddMarkerCallback callback) {
        this.callback = callback;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_marker_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        policeMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick(1);
            }
        });
        accidentMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick(2);
            }
        });
        roadWorkMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick(3);
            }
        });
    }

    private void onButtonClick(int type) {
        if (callback != null && location != null) {
            callback.onAddClick(location, type);

            dismissAllowingStateLoss();
        }
    }
}
