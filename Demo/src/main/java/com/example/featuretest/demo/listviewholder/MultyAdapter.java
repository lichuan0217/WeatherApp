package com.example.featuretest.demo.listviewholder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.featuretest.demo.R;

import java.util.List;

/**
 * Created by chuanl on 9/11/14.
 */
public class MultyAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<String> data;

    private static final int VIEW_COUNT = 2;
    private static final int VIEW_TYPE_0 = 0;
    private static final int VIEW_TYPE_1 = 1;

    public static class ViewHolder0{
        public final TextView textView;

        public ViewHolder0(View view){
            textView = (TextView)view.findViewById(R.id.item_text);
        }
    }

    public static class ViewHolder1{
        public final Button button;

        public ViewHolder1(View view){
            button = (Button)view.findViewById(R.id.button);
        }
    }

    public MultyAdapter(Context context, List data){
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        int type = getItemViewType(position);
        View v = view;
        switch (type){
            case VIEW_TYPE_0:
                ViewHolder0 viewHolder0 = null;
                if(v == null){
                    v = inflater.inflate(R.layout.item_list_0, viewGroup, false);
                    viewHolder0 = new ViewHolder0(v);
                    v.setTag(viewHolder0);
                }
                viewHolder0 = (ViewHolder0)v.getTag();
                viewHolder0.textView.setText(data.get(position));
                break;
            case VIEW_TYPE_1:
                ViewHolder1 viewHolder1 = null;
                if(v == null) {
                    v = inflater.inflate(R.layout.item_list_1, viewGroup, false);
                    viewHolder1 = new ViewHolder1(v);
                    v.setTag(viewHolder1);
                }
                viewHolder1 = (ViewHolder1)v.getTag();
                viewHolder1.button.setText(data.get(position));
                break;
        }

        return v;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return position % 2 == 0 ? VIEW_TYPE_0 : VIEW_TYPE_1;
    }
}
