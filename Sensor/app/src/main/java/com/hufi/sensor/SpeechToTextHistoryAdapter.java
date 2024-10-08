package com.hufi.sensor;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class SpeechToTextHistoryAdapter extends ArrayAdapter<SpeechToTextHistoryClass> {
    Context context;
    int layoutResource;
    ArrayList<SpeechToTextHistoryClass> data;

    public SpeechToTextHistoryAdapter(@NonNull Context context, int resource, @NonNull ArrayList<SpeechToTextHistoryClass> objects) {
        super(context, resource, objects);
        this.data=objects;
        this.layoutResource=resource;
        this.context=context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        convertView = layoutInflater.inflate(layoutResource, parent, false);

        SpeechToTextHistoryClass b = data.get(position);

        String url = b.getDate();
        String text = b.getText();

        TextView lbUrl = convertView.findViewById(R.id.lbDate);
        lbUrl.setText(url);

        TextView lbTitle = convertView.findViewById(R.id.lbText);
        lbTitle.setText(text);

        if (position % 2 == 1) {
            convertView.setBackgroundColor(Color.parseColor("#505050"));
        } else {
            convertView.setBackgroundColor(Color.parseColor("#6c6c6c"));
        }

        return convertView;
    }
}
