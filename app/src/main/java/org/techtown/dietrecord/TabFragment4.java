package org.techtown.dietrecord;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class TabFragment4 extends Fragment implements View.OnClickListener{

    private LineChart chart;

    Button button1, button2, button3, button4, button5;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view;

        view = inflater.inflate(R.layout.fragment4, container, false);

        chart = view.findViewById(R.id.linechart);

        button1 = (Button) view.findViewById(R.id.button);
        button2 = (Button) view.findViewById(R.id.button2);
        button3 = (Button) view.findViewById(R.id.button3);
        button4 = (Button) view.findViewById(R.id.button4);
        button5 = (Button) view.findViewById(R.id.button5);

        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
        button5.setOnClickListener(this);

        ArrayList<Entry> values = new ArrayList<>();

        for (int i = 0; i < 10; i++) {

            float val = (float) (Math.random() * 100);
            values.add(new Entry(i, val));
        }
        LineDataSet set1;
        set1 = new LineDataSet(values, null);
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1); // add the data sets

        // create a data object with the data sets
        LineData data = new LineData(dataSets);

        // black lines and points
        set1.setColor(Color.BLACK);
        set1.setCircleColor(Color.RED);

        // set data
        chart.setData(data);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onClick(View view) {
        //일간 데이터로 변경
        if(view == button1) {
            ArrayList<Entry> values = new ArrayList<>();

            for (int i = 0; i < 10; i++) {

                float val = (float) (Math.random() * 100);
                values.add(new Entry(i, val));
            }
            LineDataSet set1;
            set1 = new LineDataSet(values, null);
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1); // add the data sets

            // create a data object with the data sets
            LineData data = new LineData(dataSets);

            // black lines and points
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.RED);

            // set data
            chart.setData(data);

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.detach(this).attach(this).commit();
        }

        //주간 데이터로 변경
        if(view == button2) {
            ArrayList<Entry> values = new ArrayList<>();

            for (int i = 0; i < 10; i++) {

                float val = (float) (Math.random() * 100);
                values.add(new Entry(i, val));
            }
            LineDataSet set1;
            set1 = new LineDataSet(values, null);
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1); // add the data sets

            // create a data object with the data sets
            LineData data = new LineData(dataSets);

            // black lines and points
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.RED);

            // set data
            chart.setData(data);

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.detach(this).attach(this).commit();
        }

        //몸무게 데이터로 변경
        if(view == button3) {
            ArrayList<Entry> values = new ArrayList<>();

            for (int i = 0; i < 10; i++) {

                float val = (float) (Math.random() * 100);
                values.add(new Entry(i, val));
            }
            LineDataSet set1;
            set1 = new LineDataSet(values, null);
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1); // add the data sets

            // create a data object with the data sets
            LineData data = new LineData(dataSets);

            // black lines and points
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.RED);

            // set data
            chart.setData(data);

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.detach(this).attach(this).commit();
        }

        //운동칼로리 데이터로 변경

        if(view == button4) {
            ArrayList<Entry> values = new ArrayList<>();

            for (int i = 0; i < 10; i++) {

                float val = (float) (Math.random() * 100);
                values.add(new Entry(i, val));
            }
            LineDataSet set1;
            set1 = new LineDataSet(values, null);
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1); // add the data sets

            // create a data object with the data sets
            LineData data = new LineData(dataSets);

            // black lines and points
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.RED);

            // set data
            chart.setData(data);

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.detach(this).attach(this).commit();
        }

        //음식칼로리 데이터로 변경
        if(view == button5) {
            ArrayList<Entry> values = new ArrayList<>();

            for (int i = 0; i < 10; i++) {

                float val = (float) (Math.random() * 100);
                values.add(new Entry(i, val));
            }
            LineDataSet set1;
            set1 = new LineDataSet(values, null);
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1); // add the data sets

            // create a data object with the data sets
            LineData data = new LineData(dataSets);

            // black lines and points
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.RED);

            // set data
            chart.setData(data);

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.detach(this).attach(this).commit();
        }
    }
}