package org.dualcom.xai_support;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class list_box extends BaseAdapter {

    Context ctx;
    LayoutInflater lInflater;
    ArrayList<list_const> objects;

    list_box(Context context, ArrayList<list_const> products) {
        ctx = context;
        objects = products;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // кол-во элементов
    @Override
    public int getCount() {
        return objects.size();
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view

        list_const p = getIncorrects(position);

        View view = convertView;
        //if (view == null) {
            if(p.from.equals("admin"))
                view = lInflater.inflate(R.layout.check_incorrect, parent, false);
            else
                view = lInflater.inflate(R.layout.new_incorrect, parent, false);
        //}

        ((TextView) view.findViewById(R.id.serial)).setText(p.serial);
        ((TextView) view.findViewById(R.id.messageView)).setText(p.message);
        ((TextView) view.findViewById(R.id.dataView)).setText(p.data);

        return view;
    }

    // товар по позиции
    list_const getIncorrects(int position) {
        return ((list_const) getItem(position));
    }
}
