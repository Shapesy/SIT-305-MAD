package com.example.lostandfound;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ItemListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        List<Item> items = dbHelper.getAllItems();

        ListView listView = findViewById(R.id.listView);
        TextView tvEmpty  = findViewById(R.id.tvEmpty);

        if (items.isEmpty()) {
            tvEmpty.setVisibility(TextView.VISIBLE);
            listView.setVisibility(ListView.GONE);
            return;
        }

        List<String> display = new ArrayList<>();
        for (Item item : items) {
            display.add("[" + item.getPostType() + "]  " + item.getName()
                    + "\n" + item.getDescription()
                    + "\nLocation: " + item.getLocation()
                    + "  |  Date: " + item.getDate());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, display);
        listView.setAdapter(adapter);
    }
}
