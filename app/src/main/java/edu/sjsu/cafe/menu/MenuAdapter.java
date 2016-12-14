package edu.sjsu.cafe.menu;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import edu.sjsu.cafe.AppUtility;
import edu.sjsu.cafe.R;

/**
 * Created by Akshay on 12/7/2016.
 */

public class MenuAdapter extends ArrayAdapter<MenuSection> {

    Context context;
    List<MenuSection> menuSections;
    AssetManager assetManager;
    String imageUrl;

    public MenuAdapter(Context context, int resource, List<MenuSection> objects) {
        super(context, resource, objects);
        this.context = context;
        this.menuSections = objects;
        this.assetManager = context.getAssets();
    }

    @Override
    public int getCount() {
        return menuSections.size();
    }

    @Override
    public MenuSection getItem(int i) {
        return menuSections.get(i);
    }

    @Override
    public long getItemId(int i) {
        return menuSections.indexOf(getItem(i));
    }

    /* private view holder class */
    private class ViewHolder {
        ImageView menuSectionImage;
        TextView menuSectionItemText;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;


        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (view == null) {
            view = mInflater.inflate(R.layout.menu_section, null);
            holder = new ViewHolder();
            holder.menuSectionItemText = (TextView) view.findViewById(R.id.section_title);
            holder.menuSectionImage = (ImageView) view.findViewById(R.id.section_bg_image);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        MenuSection menuSection = menuSections.get(i);

        holder.menuSectionItemText.setText(menuSection.getSectionName());

        try {
            imageUrl = menuSection.getImagePath();
            if (!AppUtility.isNullOrEmpty(imageUrl)) {
                Picasso.with(holder.menuSectionImage.getContext())
                        .load(Uri.parse(imageUrl))
                        .into(holder.menuSectionImage);
            }
        } catch (Exception ex) {
            ex.printStackTrace();

        }

        return view;
    }
}
