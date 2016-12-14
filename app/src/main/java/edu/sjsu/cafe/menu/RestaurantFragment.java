package edu.sjsu.cafe.menu;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import edu.sjsu.cafe.AppUtility;
import edu.sjsu.cafe.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class RestaurantFragment extends Fragment {
    MenuAdapter adapter;
    List<MenuSection> menuSections;
    View view;

    public RestaurantFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(view == null) {
            view = inflater.inflate(R.layout.fragment_restaurant, container, false);
        }

        ListView listView = (ListView) view.findViewById(R.id.menu);
        menuSections = AppUtility.getMenuSections();
        adapter = new MenuAdapter(RestaurantFragment.this.getContext(), R.layout.menu_section, menuSections);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(RestaurantFragment.this.getContext(), RestaurantMenuDetailsActivity.class);
                intent.putExtra("menu_section", adapter.getItem(i).getSectionName());
                startActivity(intent);
            }
        });
        return view;
    }
}
