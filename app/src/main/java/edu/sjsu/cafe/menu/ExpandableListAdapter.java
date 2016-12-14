package edu.sjsu.cafe.menu;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import edu.sjsu.cafe.AppUtility;
import edu.sjsu.cafe.R;



public class ExpandableListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int HEADER = 0;
    public static final int CHILD = 1;
    Context context;
    TextView itemTextView, itemPriceTextView, itemName, itemPrice;
    String[] menuItemElems;

    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
    LinearLayout.LayoutParams priceParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    private List<Item> data;

    public ExpandableListAdapter(List<Item> data) {
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View view = null;
        context = parent.getContext();
        float dp = context.getResources().getDisplayMetrics().density;
        int subItemPaddingLeft = (int) (20 * dp);
        int subItemPaddingTopAndBottom = (int) (10 * dp);
        switch (type) {
            case HEADER:
                LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.menu_list_header, parent, false);
                ListHeaderViewHolder header = new ListHeaderViewHolder(view);
                return header;
            case CHILD:
                LinearLayout linearLayout = new LinearLayout(context);
                linearLayout.setLayoutParams(layoutParams);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                linearLayout.setPadding(subItemPaddingLeft, subItemPaddingTopAndBottom, 0, subItemPaddingTopAndBottom);

                itemTextView = new TextView(context);
                itemTextView.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
                itemTextView.setTextSize(16);
                itemTextView.setLayoutParams(layoutParams);
                linearLayout.addView(itemTextView);

                priceParams.setMargins(10, 0, 20, 0);
                itemPriceTextView = new TextView(context);
                itemPriceTextView.setTextColor(context.getResources().getColor(R.color.colorAAA));
                itemPriceTextView.setPadding(15, 0, 15, 0);
                itemPriceTextView.setLayoutParams(priceParams);
                linearLayout.addView(itemPriceTextView);

                return new RecyclerView.ViewHolder(linearLayout) {
                };
        }
        return null;
    }

    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Item item = data.get(position);
        switch (item.type) {
            case HEADER:
                final ListHeaderViewHolder itemController = (ListHeaderViewHolder) holder;
                itemController.refferalItem = item;
                itemController.header_title.setText(item.text);
                if (item.invisibleChildren == null) {
                    itemController.btn_expand_toggle.setText(context.getString(R.string.minus_sign));
                } else {
                    itemController.btn_expand_toggle.setText(context.getString(R.string.add_sign));
                }
                itemController.btn_expand_toggle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (item.invisibleChildren == null) {
                            item.invisibleChildren = new ArrayList<Item>();
                            int count = 0;
                            int pos = data.indexOf(itemController.refferalItem);
                            while (data.size() > pos + 1 && data.get(pos + 1).type == CHILD) {
                                item.invisibleChildren.add(data.remove(pos + 1));
                                count++;
                            }
                            notifyItemRangeRemoved(pos + 1, count);
                            itemController.btn_expand_toggle.setText(context.getString(R.string.add_sign));
                        } else {
                            int pos = data.indexOf(itemController.refferalItem);
                            int index = pos + 1;
                            for (Item i : item.invisibleChildren) {
                                data.add(index, i);
                                index++;
                            }
                            notifyItemRangeInserted(pos + 1, index - pos - 1);
                            itemController.btn_expand_toggle.setText(context.getString(R.string.minus_sign));
                            item.invisibleChildren = null;
                        }
                    }
                });
                break;
            case CHILD:
                try {
                    LinearLayout menuItem = (LinearLayout) holder.itemView;
                    menuItemElems = data.get(position).text.split(",");
                    itemName = (TextView) menuItem.getChildAt(0);
                    itemName.setText(AppUtility.trimText(menuItemElems[0], 28));
                    itemPrice = (TextView) menuItem.getChildAt(1);
                    itemPrice.setText(menuItemElems[1]);
                } catch (Exception ex) {

                }
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position).type;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private static class ListHeaderViewHolder extends RecyclerView.ViewHolder {
        public TextView header_title;
        public TextView btn_expand_toggle;
        public Item refferalItem;

        public ListHeaderViewHolder(View itemView) {
            super(itemView);
            header_title = (TextView) itemView.findViewById(R.id.header_title);
            btn_expand_toggle = (TextView) itemView.findViewById(R.id.btn_expand_toggle);
        }
    }

    public static class Item {
        public int type;
        public String text;
        public List<Item> invisibleChildren;

        public Item() {
        }

        public Item(int type, String text) {
            this.type = type;
            this.text = text;
        }
    }
}
