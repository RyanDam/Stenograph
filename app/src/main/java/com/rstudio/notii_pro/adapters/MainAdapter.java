package com.rstudio.notii_pro.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
//import com.origamilabs.library.views.StaggeredGridView;
import com.rstudio.notii_pro.Editnote;
import com.rstudio.notii_pro.MainActivity;
import com.rstudio.notii_pro.R;
import com.rstudio.notii_pro.database.DatabaseMng;
import com.rstudio.notii_pro.item.CheckItem;
import com.rstudio.notii_pro.item.NoteItem;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Ryan on 11/27/15.
 */
public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<NoteItem> noteItem;
    private Calendar now;
    private long remind;
    private Typeface bold, regular;
    private DatabaseMng database;
    private MainActivity mListViewCallBack;
    private LayoutInflater mInflater;
    public Intent mMainToEdit;
    private Animation anim;

    public MainAdapter(Context ctx, ArrayList<NoteItem> data, MainActivity callback) {
        mContext = ctx;
        noteItem = data;
        database = new DatabaseMng(mContext);
        bold = Typeface.createFromAsset(mContext.getAssets(), "fonts/slab_bold.ttf");
        regular = Typeface.createFromAsset(mContext.getAssets(), "fonts/slab_regular.ttf");
        mListViewCallBack = callback;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMainToEdit = new Intent(ctx, Editnote.class);
        anim = AnimationUtils.loadAnimation(mContext, R.anim.card_show_up);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_layout, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.id = position;
        final NoteItem notes = noteItem.get(position);
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
                if(checkItems == null) return;
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
    }

    @Override
    public int getItemCount() {
        return noteItem.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        public View mView;
        TextView title_item, date_item, text_item, remind_status;
        LinearLayout color_item;
        ImageView flag;
        LinearLayout list;
        int id;
        boolean bold;
        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            title_item = (TextView) mView.findViewById(R.id.title_item);
            text_item = (TextView) mView.findViewById(R.id.text_item);
            color_item = (LinearLayout) mView.findViewById(R.id.color_item);
            flag = (ImageView) mView.findViewById(R.id.flag);
            remind_status = (TextView) mView.findViewById(R.id.remind_item);
            list = (LinearLayout) mView.findViewById(R.id.list_item);
            mView.setOnClickListener(this);
            mView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (text_item.getText().toString().compareTo("") == 0) {
                // if it doesn't have any text
                mMainToEdit.putExtra("CheckMode", true);
            } else {
                mMainToEdit.putExtra("CheckMode", false);
            }
            mMainToEdit.putExtra("Note", noteItem.get(id).getId());
            ((AppCompatActivity)mContext).startActivityForResult(mMainToEdit, MainActivity.EDIT_NOTE);
        }

        @Override
        public boolean onLongClick(View v) {
            Bundle bundle = new Bundle();
            bundle.putInt("Position", id);
            if (longClickNotes(bundle) != null) {
                longClickNotes(bundle).show();
            }
            return false;
        }
    }

    public Dialog longClickNotes(Bundle bundle) {
        final int position = bundle.getInt("Position", -1);
        if (position == -1) return null;
        MaterialDialog.Builder dialog = new MaterialDialog.Builder(mContext);
        if (noteItem.get(position).getBold()) {
            dialog.items(R.array.long_click_note_mark)
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                            if (i == 0) {
                                NoteItem item = database.getNote(noteItem.get(position).getId());
                                item.setBold(false);
                                database.updateNote(item);
                            } else if (i == 2) {
                                database.removeCheckItemWithNoteId(noteItem.get(position).getId());
                                database.removeNote(noteItem.get(position).getId());
                            }
                            else if (i == 1) {
                                if (noteItem.get(position).getText().compareTo("") == 0) {
                                    ArrayList<CheckItem> tempCheckItems = database
                                            .getAllCheckItemWithNoteID(noteItem.get(position).getId());

                                    String text = "- ";
                                    for (int j = 0; j < tempCheckItems.size() - 1; j++) {
                                        if (j == tempCheckItems.size() - 2) {
                                            text = text + tempCheckItems.get(j).getText();
                                        }
                                        else {
                                            text = text + tempCheckItems.get(j).getText() + "\n- ";
                                        }
                                    }

                                    Intent share = new Intent();
                                    share.setAction(Intent.ACTION_SEND);
                                    share.putExtra(Intent.EXTRA_TITLE, noteItem.get(position).getTitle());
                                    share.putExtra(Intent.EXTRA_TEXT, text);
                                    share.setType("text/plain");
                                    mContext.startActivity(share);
                                }
                                else {
                                    Intent share = new Intent();
                                    share.setAction(Intent.ACTION_SEND);
                                    share.putExtra(Intent.EXTRA_TITLE, noteItem.get(position).getTitle());
                                    share.putExtra(Intent.EXTRA_TEXT, noteItem.get(position).getText());
                                    share.setType("text/plain");
                                    mContext.startActivity(share);
                                }
                            }
                            mListViewCallBack.loadDatabase();
                        }
                    });
        }
        else {
            dialog.items(R.array.long_click_note_no_mark)
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                            if (i == 0) {
                                NoteItem item = database.getNote(noteItem.get(position).getId());
                                item.setBold(true);
                                database.updateNote(item);
                            }
                            else if (i == 2) {
                                database.removeCheckItemWithNoteId(noteItem.get(position).getId());
                                database.removeNote(noteItem.get(position).getId());
                            }
                            else if (i == 1) {
                                if (noteItem.get(position).getText().compareTo("") == 0) {
                                    ArrayList<CheckItem> tempCheckItems = database
                                            .getAllCheckItemWithNoteID(noteItem.get(position).getId());

                                    String text = "- ";
                                    for (int j = 0; j < tempCheckItems.size() - 1; j++) {
                                        if (j == tempCheckItems.size() - 2) {
                                            text = text + tempCheckItems.get(j).getText();
                                        }
                                        else {
                                            text = text + tempCheckItems.get(j).getText() + "\n- ";
                                        }
                                    }

                                    Intent share = new Intent();
                                    share.setAction(Intent.ACTION_SEND);
                                    share.putExtra(Intent.EXTRA_TITLE, noteItem.get(position).getTitle());
                                    share.putExtra(Intent.EXTRA_TEXT, text);
                                    share.setType("text/plain");
                                    mContext.startActivity(share);
                                }
                                else {
                                    Intent share = new Intent();
                                    share.setAction(Intent.ACTION_SEND);
                                    share.putExtra(Intent.EXTRA_TITLE, noteItem.get(position).getTitle());
                                    share.putExtra(Intent.EXTRA_TEXT, noteItem.get(position).getText());
                                    share.setType("text/plain");
                                    mContext.startActivity(share);
                                }
                            }
                            mListViewCallBack.loadDatabase();
                        }
                    });
        }
        return dialog.build();
    }

    public void removeItem(int pos) {
        noteItem.remove(pos);
        notifyItemRemoved(pos);
    }

    public void addItem(NoteItem item, int pos) {
        noteItem.add(pos, item);
        notifyItemInserted(pos);
    }

    public void updateItem(NoteItem item, int pos) {
        noteItem.set(pos, item);
        notifyItemChanged(pos);
        notifyDataSetChanged();
    }

    public void notifyDataChanged() {
        notifyDataSetChanged();
//        for (int i = 0; i < noteItem.size(); i++) {
//            notifyItemInserted(i);
//        }
    }

}
