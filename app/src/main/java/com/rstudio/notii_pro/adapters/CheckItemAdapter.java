package com.rstudio.notii_pro.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.QuoteSpan;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;

import com.rstudio.notii_pro.Editnote;
import com.rstudio.notii_pro.R;
import com.rstudio.notii_pro.item.CheckItem;

import java.util.ArrayList;

/**
 * Created by Ryan on 8/11/15.
 */
public class CheckItemAdapter extends BaseAdapter {

    private static final StrikethroughSpan STRIKE_THROUGH_SPAN = new StrikethroughSpan();
    private static final QuoteSpan QUOTE_SPAN = new QuoteSpan();

    private ArrayList<CheckItem> mData;
    private Context mContext;
    private int idNote;
    private Editnote mCallback;
    private Typeface bold, regular;
    private ListView listCallback;

    public CheckItemAdapter(Context mContext, ArrayList<CheckItem> mData, int id, Editnote callback, ListView listBack) {
        this.mData = mData;
        this.mContext = mContext;
        this.idNote = id;
        this.mCallback = callback;
        bold = Typeface.createFromAsset(mContext.getAssets(), "fonts/slab_bold.ttf");
        regular = Typeface.createFromAsset(mContext.getAssets(), "fonts/slab_regular.ttf");
        listCallback = listBack;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        final Holder hold = new Holder();

        if (view == null) {
            LayoutInflater iflt = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = iflt.inflate(R.layout.check_item_layout, null);
        }

        hold.text = (EditText) view.findViewById(R.id.check_item_tb);
        hold.check = (CheckBox) view.findViewById(R.id.check_item_cb);
        hold.add = (ImageButton) view.findViewById(R.id.check_item_add);
        hold.del = (ImageButton) view.findViewById(R.id.check_item_del);
        hold.root = (FrameLayout) view.findViewById(R.id.requestFocus);

        hold.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mData.get(position).setText(hold.text.getText().toString());
//                hold.root.requestFocus();
//                hideKeyboard(hold.text);
                notifyDataSetChanged();
            }
        });

        hold.del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mData.get(position) != null)
                    mData.remove(position);
                notifyDataSetChanged();
            }
        });

        hold.check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mData.get(position) != null) {
                    mData.get(position).setIsCheck(isChecked);
                    if (isChecked) {
                        hold.text.setTextColor(mContext.getResources().getColor(R.color.check_text_empty));
                    }
                    else {
                        hold.text.setTextColor(mContext.getResources().getColor(R.color.check_text_fielded));
                    }
                    mCallback.setEdited(true);
                }
            }
        });

        TextWatcher watcher = (TextWatcher) view.getTag();
        if (watcher != null) {
            hold.text.removeTextChangedListener(watcher);
        }
        watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mData.get(position) != null) {
                    if (mData.get(position).getText().compareTo(s.toString()) != 0) {
                        mData.get(position).setText(s.toString());
                        mCallback.setEdited(true);
                    }
                }
            }
        };
        view.setTag(watcher);
        hold.text.addTextChangedListener(watcher);

        hold.text.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (position == mData.size() - 1) {
                        hold.add.setVisibility(View.VISIBLE);
                        Log.d("Focus", "Focused");
                        View currentFocus = mCallback.getCurrentFocus();
                        if (currentFocus != null) {
                            Log.d("Focus", "Focus at " + mContext.getResources().getResourceEntryName(currentFocus.getId()));
                        }
                    }
                }
                else {
                    Log.d("Focus", "Unfocused");
                    View currentFocus = mCallback.getCurrentFocus();
                    if (currentFocus != null) {
                        Log.d("Focus", "Focus at " + mContext.getResources().getResourceEntryName(currentFocus.getId()));
                    }
                }
            }
        });

        hold.add.setVisibility(View.GONE);
        hold.del.setVisibility(View.GONE);

        if (mData.get(position) != null) {
            hold.text.setText(mData.get(position).getText());
            hold.text.setTypeface(regular);
            hold.check.setChecked(mData.get(position).isCheck());
            if (mData.get(position).getText() != null) {
                if (mData.get(position).getText().compareTo("") != 0) {
                    hold.text.setTextColor(mContext.getResources().getColor(R.color.check_text_fielded));
                }
                else {
                }
            }
            else {
                hold.text.setTextColor(mContext.getResources().getColor(R.color.check_text_empty));
                hold.del.setVisibility(View.GONE);
            }
            if (mData.get(position).isCheck()) {
                hold.text.setTextColor(mContext.getResources().getColor(R.color.check_text_empty));
            }
            else {
                hold.text.setTextColor(mContext.getResources().getColor(R.color.check_text_fielded));
            }
        }

        return view;
    }

    @Override
    public void notifyDataSetChanged() {
        if (mData.size() > 0) {
            if (mData.get(mData.size() - 1).getText() != null) {
                if (mData.get(mData.size() - 1).getText().compareTo("") != 0) {
                    CheckItem item = new CheckItem(false, "", idNote, mData.size());
                    item.setID(mData.size());
                    mData.add(mData.size(), item);
                }
            }
        }
        else {
            CheckItem item = new CheckItem(false, "", idNote, mData.size());
            item.setID(mData.size());
            mData.add(mData.size(), item);
        }

        super.notifyDataSetChanged();

//        if (listCallback != null) {
//            listCallback.post(new Runnable() {
//                @Override
//                public void run() {
//                    listCallback.setSelection(listCallback.getCount() - 1);
//                }
//            });
//        }

    }

    public void hideKeyboard(View view) {
        InputMethodManager input = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (view != null) {
            input.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    class Holder {
        EditText text;
        CheckBox check;
        ImageButton add;
        ImageButton del;
        FrameLayout root;

        public Holder() {
        }
    }
}
