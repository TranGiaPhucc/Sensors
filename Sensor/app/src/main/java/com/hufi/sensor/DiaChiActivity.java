package com.hufi.sensor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class DiaChiActivity extends AppCompatActivity {
    Button btnXoa;
    ListView listDiaChi;
    ArrayList<DiaChi> arrayList;
    Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diachi);

        db = new Database(DiaChiActivity.this);

        btnXoa = findViewById(R.id.btnXoa);
        listDiaChi = findViewById(R.id.listDiaChi);

        arrayList = new ArrayList<>();
        DiaChiAdapter adapterDiaChi = new DiaChiAdapter(this, R.layout.list_diachi, arrayList);
        listDiaChi.setAdapter(adapterDiaChi);

        adapterDiaChi.clear();
        arrayList.addAll(db.getDiaChiAll());
        adapterDiaChi.notifyDataSetChanged();

        btnXoa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.deleteDiaChiAll();

                adapterDiaChi.clear();
                arrayList.addAll(db.getDiaChiAll());
                adapterDiaChi.notifyDataSetChanged();
            }
        });
    }
}