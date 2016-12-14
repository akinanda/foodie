package edu.sjsu.cafe.menu;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.sjsu.cafe.AppUtility;
import edu.sjsu.cafe.R;

public class RestaurantMenuDetailsActivity extends AppCompatActivity {
    private RecyclerView recyclerview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_menu_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        String menuSection = i.getExtras().getString("menu_section");
        getSupportActionBar().setTitle("Menu > "+menuSection);
        ArrayList<HashMap<String, ArrayList<String>>> menuItems = AppUtility.getMenuItems(menuSection);

        recyclerview = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        List<ExpandableListAdapter.Item> data = new ArrayList<>();

        if (menuItems != null && !menuItems.isEmpty()) {
            for (HashMap<String, ArrayList<String>> menuItem : menuItems) {
                for (String section : menuItem.keySet()) {
                    data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, section));
                    for (String item : menuItem.get(section)) {
                        data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, item));
                    }
                }
            }
        }
        recyclerview.setAdapter(new ExpandableListAdapter(data));
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
