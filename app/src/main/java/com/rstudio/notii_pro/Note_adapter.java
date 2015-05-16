package com.rstudio.notii_pro;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Note_adapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<Note_item> noteItem;
    Calendar now;
    long remind;

	public Note_adapter(Context mContext, ArrayList<Note_item> noteItem){
		super();
		this.mContext = mContext;
		this.noteItem = noteItem;
	}
	
	@Override
	public int getCount(){
		int rtValue = 0;
		if(noteItem != null){
			rtValue = noteItem.size();
		}
		return rtValue;
	}
	
	@Override
	public Object getItem(int arg0){
		return noteItem.get(arg0);
	}
	
	@Override
	public long getItemId(int arg0){
		return arg0;
	}
	
	@Override
	public View getView(int pos, View convertView, ViewGroup parent){
		ViewHolder holder;
		
		if (convertView == null){
		
		// Settup layout
		LayoutInflater mInflater = LayoutInflater.from(mContext);
		convertView = mInflater.inflate(R.layout.item_layout, parent, false);
		
		// Settup holder
		holder = new ViewHolder();
		holder.title_item = (TextView) convertView.findViewById(R.id.title_item);
		holder.text_item = (TextView) convertView.findViewById(R.id.text_item);
		holder.color_item = (LinearLayout) convertView.findViewById(R.id.color_item);
		holder.flag = (ImageView) convertView.findViewById(R.id.flag);
        holder.remind_status = (TextView) convertView.findViewById(R.id.remind_item);

		// Settup tag
		convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}

//        Typeface bold = Typeface.createFromAsset(mContext.getAssets(), "fonts/slab_bold.ttf");
//        Typeface regular = Typeface.createFromAsset(mContext.getAssets(), "fonts/slab_regular.ttf");

		// Lay date insert vao View
		final Note_item notes = noteItem.get(pos);		
		if (notes != null) {
			holder.text_item.setText(notes.getText());
            if (notes.getTitle().toString().equals("Quick note") || notes.getTitle().toString().equals("")) {
                holder.title_item.setVisibility(View.GONE);
            }
            else {
                holder.title_item.setText(notes.getTitle());
                holder.title_item.setVisibility(View.VISIBLE);
//                holder.title_item.setTypeface(bold);
            }
            holder.color_item.setBackgroundColor(notes.getColor());
//            holder.text_item.setTypeface(regular);

            if (!notes.getBold()) {
                holder.flag.setVisibility(View.INVISIBLE);
            }
            else {
                holder.flag.setVisibility(View.VISIBLE);
            }
            now = Calendar.getInstance();
            remind = notes.getRemind();
            if (remind > now.getTimeInMillis()) {
                holder.remind_status.setText("Will remind you");
                holder.remind_status.setVisibility(View.VISIBLE);
            }
            else if (remind > 0){
                holder.remind_status.setText("Reminded");
                holder.remind_status.setVisibility(View.VISIBLE);
            }
            else {
                holder.remind_status.setVisibility(View.GONE);
            }
		}
		return convertView;
	}

	public class ViewHolder{
		TextView title_item, date_item, text_item, remind_status;
		LinearLayout color_item;
        ImageView flag;
		boolean bold;
	}
	
}