package com.smart.apsrtcbus.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.smart.apsrtcbus.R;
import com.smart.apsrtcbus.vo.SpecialServiceVO;

import java.util.ArrayList;

public class SpecialServiceAdapter extends ArrayAdapter<SpecialServiceVO> {

    private Context context = null;
    private ArrayList<SpecialServiceVO> list = null;

    public SpecialServiceAdapter(Context context, ArrayList<SpecialServiceVO> list) {
        super(context, R.layout.listview_row, list);
        this.context = context;
        this.list = list;
    }

    @Override
    public View getView(int position, View rowView, ViewGroup parent) {
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.special_service_list_row, parent, false);
        }

        TextView serviceNoView = (TextView) rowView.findViewById(R.id.serviceNoView);
        TextView serviceTypeView = (TextView) rowView.findViewById(R.id.serviceTypeView);
        TextView fromView = (TextView) rowView.findViewById(R.id.fromView);
        TextView toView = (TextView) rowView.findViewById(R.id.toView);
        TextView dateView = (TextView) rowView.findViewById(R.id.dateView);
        SpecialServiceVO serviceVO = list.get(position);
        serviceNoView.setText(serviceVO.getServiceNo());
        serviceTypeView.setText(serviceVO.getType());
        String fromTxt = serviceVO.getFrom();
        int indx = fromTxt.lastIndexOf("-");
        if (indx > 0)
            fromTxt = fromTxt.substring(0, indx);
        fromView.setText(fromTxt);
        String toTxt = serviceVO.getTo();
        indx = toTxt.lastIndexOf("-");
        if (indx > 0)
            toTxt = toTxt.substring(0, indx);
        toView.setText(toTxt);
        dateView.setText(serviceVO.getJourneyDate() + " " + serviceVO.getDeparture());
        return rowView;
    }
}
