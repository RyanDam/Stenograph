package com.rstudio.notii_pro.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

import com.rstudio.notii_pro.R;
import com.rstudio.notii_pro.item.CheckItem;

import java.util.ArrayList;

/**
 * Created by Ryan on 8/16/15.
 */
public class SimpleCheckItemAdapter extends BaseAdapter {

    private ArrayList<CheckItem> mCheckListItem;
    private Context mContext;
    private Typeface bold, regular;

    public SimpleCheckItemAdapter(Context context, ArrayList<CheckItem> checkListItem) {
        mCheckListItem = checkListItem;
        mContext = context;
        bold = Typeface.createFromAsset(mContext.getAssets(), "fonts/slab_bold.ttf");
        regular = Typeface.createFromAsset(mContext.getAssets(), "fonts/slab_regular.ttf");
    }

    @Override
    public int getCount() {
        return mCheckListItem.size();
    }

    @Override
    public Object getItem(int position) {
        return mCheckListItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inf = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inf.inflate(R.layout.check_item_layout_simple, null);
        }

        if (mCheckListItem.get(position) != null) {
            CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.simple_checkbox);
            checkBox.setChecked(mCheckListItem.get(position).isCheck());
            checkBox.setText(mCheckListItem.get(position).getText());
            checkBox.setTypeface(regular);
        }

        return convertView;
    }
}
