package com.example.lostandfound;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnCreate  = findViewById(R.id.btnCreateAdvert);
        Button btnList    = findViewById(R.id.btnShowItems);
        Button btnMap     = findViewById(R.id.btnShowOnMap);

        btnCreate.setOnClickListener(v ->
                startActivity(new Intent(this, CreateAdvertActivity.class)));

        btnList.setOnClickListener(v ->
                startActivity(new Intent(this, ItemListActivity.class)));

        btnMap.setOnClickListener(v ->
                startActivity(new Intent(this, MapActivity.class)));
    }
}
