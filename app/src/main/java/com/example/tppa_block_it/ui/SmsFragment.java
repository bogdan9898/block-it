package com.example.tppa_block_it.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.tppa_block_it.R;

public class SmsFragment extends Fragment {
    private static final String TAG = "SmsFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_sms, container, false);
        final TextView textView = root.findViewById(R.id.sms_text_view);
        textView.setText("SmsFragment: WIP");
        return root;
    }
}
