package com.hufi.sensor;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Timer;

public class SpeechToTextHistoryActivity extends AppCompatActivity {
    Button btnXoa;
    ListView listSpeechToTextHistory;
    ArrayList<SpeechToTextHistoryClass> arrayList;
    Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speechtotext);

        db = new Database(SpeechToTextHistoryActivity.this);

        btnXoa = findViewById(R.id.btnXoa);
        listSpeechToTextHistory = findViewById(R.id.listSpeechToTextHistory);

        arrayList = new ArrayList<>();
        SpeechToTextHistoryAdapter adapter = new SpeechToTextHistoryAdapter(this, R.layout.list_speechtotext, arrayList);
        listSpeechToTextHistory.setAdapter(adapter);

        adapter.clear();
        arrayList.addAll(db.getSpeechToTextHistoryAll());
        adapter.notifyDataSetChanged();

        btnXoa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.deleteSpeechToTextHistoryAll();

                adapter.clear();
                arrayList.addAll(db.getSpeechToTextHistoryAll());
                adapter.notifyDataSetChanged();
            }
        });
    }
}