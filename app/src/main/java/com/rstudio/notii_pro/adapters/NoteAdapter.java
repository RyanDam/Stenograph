package com.rstudio.notii_pro.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

//import com.origamilabs.library.views.StaggeredGridView;
import com.rstudio.notii_pro.database.DatabaseMng;
import com.rstudio.notii_pro.item.CheckItem;
import com.rstudio.notii_pro.item.NoteItem;
import com.rstudio.notii_pro.R;

import java.util.ArrayList;
import java.util.Calendar;

public class NoteAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<NoteItem> noteItem;
    private Calendar now;
    private long remind;
	private Typeface bold, regular;
	private DatabaseMng database;
//	private StaggeredGridView mListViewCallBack;
	private LayoutInflater mInflater;

	public NoteAdapter(Context mContext, ArrayList<NoteItem> noteItem /*StaggeredGridView callback*/){
		super();
		this.mContext = mContext;
		database = new DatabaseMng(mContext);
		this.noteItem = noteItem;
        bold = Typeface.createFromAsset(mContext.getAssets(), "fonts/slab_bold.ttf");
        regular = Typeface.createFromAsset(mContext.getAssets(), "fonts/slab_regular.ttf");
//		mListViewCallBack = callback;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
	public View getView(final int pos, View convertView, ViewGroup parent){
		ViewHolder holder;
		
		if (convertView == null){
		
		// Settup layout

		convertView = mInflater.inflate(R.layout.item_layout, parent, false);
		
		// Settup holder
		holder = new ViewHolder();
		holder.title_item = (TextView) convertView.findViewById(R.id.title_item);
		holder.text_item = (TextView) convertView.findViewById(R.id.text_item);
		holder.color_item = (LinearLayout) convertView.findViewById(R.id.color_item);
		holder.flag = (ImageView) convertView.findViewById(R.id.flag);
        holder.remind_status = (TextView) convertView.findViewById(R.id.remind_item);
		holder.list = (LinearLayout) convertView.findViewById(R.id.list_item);

		// Settup tag
		convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}

		// Lay date insert vao View
		final NoteItem notes = noteItem.get(pos);
		if (notes != null) {
			holder.text_item.setText(notes.getText());
            if (notes.getTitle().toString().equals("Quick note") || notes.getTitle().toString().equals("")) {
                holder.title_item.setVisibility(View.GONE);
            }
            else {
                holder.title_item.setText(notes.getTitle());
                holder.title_item.setVisibility(View.VISIBLE);
                holder.title_item.setTypeface(bold);
            }
            holder.color_item.setBackgroundColor(notes.getColor());
            holder.text_item.setTypeface(regular);

            if (!notes.getBold()) {
                holder.flag.setVisibility(View.INVISIBLE);
            }
            else {
                holder.flag.setVisibility(View.VISIBLE);
            }
            now = Calendar.getInstance();
            remind = notes.getRemind();
            if (remind > now.getTimeInMillis()) {
                holder.remind_status.setText(mContext.getResources().getString(R.string.will_remind_text_note));
                holder.remind_status.setVisibility(View.VISIBLE);
            }
            else if (remind > 0){
                holder.remind_status.setText(mContext.getResources().getString(R.string.will_reminded_text_note));
                holder.remind_status.setVisibility(View.VISIBLE);
            }
            else {
                holder.remind_status.setVisibility(View.GONE);
            }

			holder.text_item.setVisibility(View.VISIBLE);
			holder.list.setVisibility(View.VISIBLE);
			holder.list.removeAllViews();

			if (holder.text_item.getText().toString().compareTo("") == 0) {
				holder.text_item.setVisibility(View.GONE);
				ArrayList<CheckItem> checkItems = database.getAllCheckItemWithNoteID(notes.getId());
				checkItems.remove(checkItems.size() - 1);
				if (checkItems.size() >= 7) {
					for (int i = 0; i < 7; i++) {
						View view = mInflater.inflate(R.layout.check_item_layout_simple, null);
						CheckBox checkItem = (CheckBox) view.findViewById(R.id.simple_checkbox);
						checkItem.setChecked(checkItems.get(i).isCheck());
						checkItem.setText(checkItems.get(i).getText());
						checkItem.setTypeface(regular);
						if (checkItem.isChecked()) {
							checkItem.setTextColor(mContext.getResources().getColor(R.color.text_note_color_semi));
						}
						else {
							checkItem.setTextColor(mContext.getResources().getColor(R.color.text_note_color));
						}
						holder.list.addView(view);
					}
				}
				else {
					for (int i = 0; i < checkItems.size(); i++) {
						View view = mInflater.inflate(R.layout.check_item_layout_simple, null);
						CheckBox checkItem = (CheckBox) view.findViewById(R.id.simple_checkbox);
						checkItem.setChecked(checkItems.get(i).isCheck());
						checkItem.setText(checkItems.get(i).getText());
						checkItem.setTypeface(regular);
						if (checkItem.isChecked()) {
							checkItem.setTextColor(mContext.getResources().getColor(R.color.text_note_color_semi));
						}
						else {
							checkItem.setTextColor(mContext.getResources().getColor(R.color.text_note_color));
						}
						holder.list.addView(view);
					}
				}
			}
			else {
				holder.list.setVisibility(View.GONE);
			}

		}
		return convertView;
	}

	public class ViewHolder{
		TextView title_item, date_item, text_item, remind_status;
		LinearLayout color_item;
        ImageView flag;
		LinearLayout list;
		boolean bold;
	}
	
}