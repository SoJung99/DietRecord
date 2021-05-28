package org.techtown.dietrecord;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.tabs.TabLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class TabFragment4 extends Fragment{

    private LineChart chart;

    Button button1, button2, button3, button4, button5;

    ArrayList<ExerciseData> list;
    DataAdapter mDbHelper;
    DataBaseHelper dbHelper;
    SQLiteDatabase database ;

    float day = 1;
    int attribute_switch = 1;
    public void intitLoadDB(){
        mDbHelper = new DataAdapter(getActivity().getApplicationContext());
        mDbHelper.createDatabase();
        mDbHelper.open();

        dbHelper = new DataBaseHelper(getActivity().getApplicationContext());
        dbHelper.openDataBase();
        dbHelper.close();
        database = dbHelper.getWritableDatabase();

        list = mDbHelper.getTableData();

        //mDbHelper.close();
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment4, container, false);

        intitLoadDB();

        chart = view.findViewById(R.id.linechart);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(7,true);
        xAxis.setTextColor(Color.BLUE);
        xAxis.setTextSize(15);


        YAxis yAxisLeft = chart.getAxisLeft();
        yAxisLeft.setTextColor(Color.RED);
        yAxisLeft.setTextSize(15);

        YAxis yAxisRight = chart.getAxisRight();
        yAxisRight.setDrawLabels(false);
        yAxisRight.setDrawAxisLine(false);
        yAxisRight.setDrawGridLines(false);

        button1 = (Button) view.findViewById(R.id.button);
        button2 = (Button) view.findViewById(R.id.button2);
        button3 = (Button) view.findViewById(R.id.button3);
        button4 = (Button) view.findViewById(R.id.button4);
        button5 = (Button) view.findViewById(R.id.button5);

        ArrayList<Entry> values = new ArrayList<>();


        for(int i = 0; i <= 6; i++) {
            SimpleDateFormat format = new SimpleDateFormat("YYYY MM dd HH:mm:ss", Locale.UK);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, -(6-i));  // 오늘 날짜에서 하루를 뺌.
            String date = format.format(calendar.getTime());
            String time = date.substring(0, 10).replaceAll(" ", "");
            Cursor cur = database.rawQuery("SELECT sum(칼로리) FROM 사용자운동 where 날짜 = " + Integer.parseInt(time) + "", null);
            cur.moveToNext();
            values.add(new Entry(i-6, cur.getFloat(0)));
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


        Legend legend = chart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(Color.BLACK);
        legend.setTextSize(15);
        legend.setForm(Legend.LegendForm.CIRCLE);
        int[] colorClassArray = new int[] {Color.RED, Color.BLUE};
        String[] legendName = {"kcal" , "Day"};
        LegendEntry[] legendEntries = new LegendEntry[2];

        for(int i=0;i<legendEntries.length;i++) {
            LegendEntry entry = new LegendEntry();
            entry.formColor = colorClassArray[i];
            entry.label = String.valueOf(legendName[i]);
            legendEntries[i] = entry;
        }
        legend.setCustom(legendEntries);


        // set data
        chart.setData(data);
        chart.getData().setValueTextSize(15);
        chart.getDescription().setText("일간 운동칼로리");
        chart.getDescription().setTextSize(20);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                day = 1;
                ArrayList<Entry> values = new ArrayList<>();
                if(attribute_switch == 1) {
                    for(int i = 0; i <= 6; i++) {
                        SimpleDateFormat format = new SimpleDateFormat("YYYY MM dd HH:mm:ss", Locale.UK);
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DATE, -(6-i));  // 오늘 날짜에서 하루를 뺌.
                        String date = format.format(calendar.getTime());
                        String time = date.substring(0, 10).replaceAll(" ", "");
                        Cursor cur = database.rawQuery("SELECT sum(칼로리) FROM 사용자운동 where 날짜 = " + Integer.parseInt(time) + "", null);
                        cur.moveToNext();
                        values.add(new Entry(i-6, cur.getFloat(0)));
                    }
                }
                else if(attribute_switch == 2){
                    for(int i = 0; i <= 6; i++) {
                        SimpleDateFormat format = new SimpleDateFormat("YYYY MM dd HH:mm:ss", Locale.UK);
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DATE, -(6-i));  // 오늘 날짜에서 하루를 뺌.
                        String date = format.format(calendar.getTime());
                        String time = date.substring(0, 10).replaceAll(" ", "");
                        Cursor cur = database.rawQuery("SELECT sum(칼로리) FROM 사용자식단 where 날짜 = " + Integer.parseInt(time) + "", null);
                        cur.moveToNext();
                        values.add(new Entry(i-6, cur.getFloat(0)));
                    }
                }
                else {
                    for(int i = 0; i <= 6; i++) {
                        SimpleDateFormat format = new SimpleDateFormat("YYYY MM dd HH:mm:ss", Locale.UK);
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DATE, -(6-i));  // 오늘 날짜에서 하루를 뺌.
                        String date = format.format(calendar.getTime());
                        String time = date.substring(0, 10).replaceAll(" ", "");
                        Cursor cur = database.rawQuery("SELECT sum(몸무게) FROM 사용자정보 where 날짜 = " + Integer.parseInt(time) + "", null);
                        cur.moveToNext();
                        values.add(new Entry(i-6, cur.getFloat(0)));
                    }
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


                Legend legend = chart.getLegend();
                legend.setEnabled(true);
                legend.setTextColor(Color.BLACK);
                legend.setTextSize(15);
                legend.setForm(Legend.LegendForm.CIRCLE);
                int[] colorClassArray = new int[] {Color.RED, Color.BLUE};
                String[] legendName = {"kcal" , "Day"};
                LegendEntry[] legendEntries = new LegendEntry[2];

                for(int i=0;i<legendEntries.length;i++) {
                    LegendEntry entry = new LegendEntry();
                    entry.formColor = colorClassArray[i];
                    entry.label = String.valueOf(legendName[i]);
                    legendEntries[i] = entry;
                }
                legend.setCustom(legendEntries);


                // set data
                chart.setData(data);
                chart.getData().setValueTextSize(15);
                if(attribute_switch == 1) {
                    chart.getDescription().setText("일간 운동칼로리");
                }
                else if(attribute_switch == 2){
                    chart.getDescription().setText("일간 음식칼로리");
                }
                else {
                    chart.getDescription().setText("일간 몸무게");
                }
                chart.invalidate();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                day = 7;
                ArrayList<Entry> values = new ArrayList<>();
                if(attribute_switch == 1) {
                    for(int i = 0; i <= 6; i++) {
                        SimpleDateFormat format = new SimpleDateFormat("YYYY MM dd HH:mm:ss", Locale.UK);
                        Calendar calendar1 = Calendar.getInstance();
                        Calendar calendar2 = Calendar.getInstance();
                        calendar1.add(Calendar.DATE, - (6-i) * 7);
                        calendar2.add(Calendar.DATE, - (7-i) * 7);
                        String date1 = format.format(calendar1.getTime());
                        String date2 = format.format(calendar2.getTime());
                        String time1 = date1.substring(0, 10).replaceAll(" ", "");
                        String time2 = date2.substring(0, 10).replaceAll(" ", "");
                        Cursor cur = database.rawQuery("SELECT sum(칼로리) FROM 사용자운동 where 날짜 <= "+Integer.parseInt(time1)+" AND 날짜 > "+Integer.parseInt(time2)+"", null);
                        cur.moveToNext();
                        values.add(new Entry(i-6, cur.getFloat(0)));
                    }
                }
                else if (attribute_switch == 2){
                    for(int i = 0; i <= 6; i++) {
                        SimpleDateFormat format = new SimpleDateFormat("YYYY MM dd HH:mm:ss", Locale.UK);
                        Calendar calendar1 = Calendar.getInstance();
                        Calendar calendar2 = Calendar.getInstance();
                        calendar1.add(Calendar.DATE, - (6-i) * 7);
                        calendar2.add(Calendar.DATE, - (7-i) * 7);
                        String date1 = format.format(calendar1.getTime());
                        String date2 = format.format(calendar2.getTime());
                        String time1 = date1.substring(0, 10).replaceAll(" ", "");
                        String time2 = date2.substring(0, 10).replaceAll(" ", "");
                        Cursor cur = database.rawQuery("SELECT sum(칼로리) FROM 사용자식단 where 날짜 <= "+Integer.parseInt(time1)+" AND 날짜 > "+Integer.parseInt(time2)+"", null);
                        cur.moveToNext();
                        values.add(new Entry(i-6, cur.getFloat(0)));
                    }
                }
                else {
                    for(int i = 0; i <= 6; i++) {
                        SimpleDateFormat format = new SimpleDateFormat("YYYY MM dd HH:mm:ss", Locale.UK);
                        Calendar calendar1 = Calendar.getInstance();
                        Calendar calendar2 = Calendar.getInstance();
                        calendar1.add(Calendar.DATE, - (6-i) * 7);
                        calendar2.add(Calendar.DATE, - (7-i) * 7);
                        String date1 = format.format(calendar1.getTime());
                        String date2 = format.format(calendar2.getTime());
                        String time1 = date1.substring(0, 10).replaceAll(" ", "");
                        String time2 = date2.substring(0, 10).replaceAll(" ", "");
                        Cursor cur = database.rawQuery("SELECT avg(몸무게) FROM 사용자정보 where 날짜 <= "+Integer.parseInt(time1)+" AND 날짜 > "+Integer.parseInt(time2)+"", null);
                        cur.moveToNext();
                        values.add(new Entry(i-6, cur.getFloat(0)));
                    }
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

                Legend legend = chart.getLegend();
                legend.setEnabled(true);
                legend.setTextColor(Color.BLACK);
                legend.setTextSize(15);
                legend.setForm(Legend.LegendForm.CIRCLE);
                int[] colorClassArray = new int[] {Color.RED, Color.BLUE};
                String[] legendName = {"kcal" , "Week"};
                LegendEntry[] legendEntries = new LegendEntry[2];

                for(int i=0;i<legendEntries.length;i++) {
                    LegendEntry entry = new LegendEntry();
                    entry.formColor = colorClassArray[i];
                    entry.label = String.valueOf(legendName[i]);
                    legendEntries[i] = entry;
                }
                legend.setCustom(legendEntries);

                // set data
                chart.setData(data);
                chart.getData().setValueTextSize(15);
                if(attribute_switch == 1) {
                    chart.getDescription().setText("주간 운동칼로리");
                }
                else if(attribute_switch == 2){
                    chart.getDescription().setText("주간 음식칼로리");
                }
                else {
                    chart.getDescription().setText("주간 몸무게평균");
                }
                chart.invalidate();
            }
        });


        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attribute_switch = 1;
                ArrayList<Entry> values = new ArrayList<>();
                if(day == 1) {
                    for(int i = 0; i <= 6; i++) {
                        SimpleDateFormat format = new SimpleDateFormat("YYYY MM dd HH:mm:ss", Locale.UK);
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DATE, -(6-i));  // 오늘 날짜에서 하루를 뺌.
                        String date = format.format(calendar.getTime());
                        String time = date.substring(0, 10).replaceAll(" ", "");
                        Cursor cur = database.rawQuery("SELECT sum(칼로리) FROM 사용자운동 where 날짜 = " + Integer.parseInt(time) + "", null);

                        cur.moveToNext();
                        values.add(new Entry(i-6, cur.getFloat(0)));
                    }
                }
                else {
                    for(int i = 0; i <= 6; i++) {
                        SimpleDateFormat format = new SimpleDateFormat("YYYY MM dd HH:mm:ss", Locale.UK);
                        Calendar calendar1 = Calendar.getInstance();
                        Calendar calendar2 = Calendar.getInstance();
                        calendar1.add(Calendar.DATE, - (6-i) * 7);
                        calendar2.add(Calendar.DATE, - (7-i) * 7);
                        String date1 = format.format(calendar1.getTime());
                        String date2 = format.format(calendar2.getTime());
                        String time1 = date1.substring(0, 10).replaceAll(" ", "");
                        String time2 = date2.substring(0, 10).replaceAll(" ", "");
                        Cursor cur = database.rawQuery("SELECT sum(칼로리) FROM 사용자운동 where 날짜 <= "+Integer.parseInt(time1)+" AND 날짜 > "+Integer.parseInt(time2)+"", null);
                        cur.moveToNext();
                        values.add(new Entry(i-6, cur.getFloat(0)));
                    }
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

                Legend legend = chart.getLegend();
                legend.setEnabled(true);
                legend.setTextColor(Color.BLACK);
                legend.setTextSize(15);
                legend.setForm(Legend.LegendForm.CIRCLE);
                int[] colorClassArray = new int[] {Color.RED, Color.BLUE};
                String[] legendName = {"kcal" , "Day"};
                if(day == 7) {
                    legendName[1] = "Week";
                }
                LegendEntry[] legendEntries = new LegendEntry[2];

                for(int i=0;i<legendEntries.length;i++) {
                    LegendEntry entry = new LegendEntry();
                    entry.formColor = colorClassArray[i];
                    entry.label = String.valueOf(legendName[i]);
                    legendEntries[i] = entry;
                }
                legend.setCustom(legendEntries);

                // set data
                chart.setData(data);
                chart.getData().setValueTextSize(15);
                if(day == 1) {
                    chart.getDescription().setText("일간 운동칼로리");
                }
                else {
                    chart.getDescription().setText("주간 운동칼로리");
                }
                chart.invalidate();
            }
        });

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attribute_switch = 2;
                ArrayList<Entry> values = new ArrayList<>();
                if(day == 1) {
                    for(int i = 0; i <= 6; i++) {
                        SimpleDateFormat format = new SimpleDateFormat("YYYY MM dd HH:mm:ss", Locale.UK);
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DATE, -(6-i));  // 오늘 날짜에서 하루를 뺌.
                        String date = format.format(calendar.getTime());
                        String time = date.substring(0, 10).replaceAll(" ", "");
                        Cursor cur = database.rawQuery("SELECT sum(칼로리) FROM 사용자식단 where 날짜 = " + Integer.parseInt(time) + "", null);
                        cur.moveToNext();
                        values.add(new Entry(i-6, cur.getFloat(0)));
                    }
                }
                else {
                    for(int i = 0; i <= 6; i++) {
                        SimpleDateFormat format = new SimpleDateFormat("YYYY MM dd HH:mm:ss", Locale.UK);
                        Calendar calendar1 = Calendar.getInstance();
                        Calendar calendar2 = Calendar.getInstance();
                        calendar1.add(Calendar.DATE, - (6-i) * 7);
                        calendar2.add(Calendar.DATE, - (7-i) * 7);
                        String date1 = format.format(calendar1.getTime());
                        String date2 = format.format(calendar2.getTime());
                        String time1 = date1.substring(0, 10).replaceAll(" ", "");
                        String time2 = date2.substring(0, 10).replaceAll(" ", "");
                        Cursor cur = database.rawQuery("SELECT sum(칼로리) FROM 사용자식단 where 날짜 <= "+Integer.parseInt(time1)+" AND 날짜 > "+Integer.parseInt(time2)+"", null);
                        cur.moveToNext();
                        values.add(new Entry(i-6, cur.getFloat(0)));
                    }
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

                Legend legend = chart.getLegend();
                legend.setEnabled(true);
                legend.setTextColor(Color.BLACK);
                legend.setTextSize(15);
                legend.setForm(Legend.LegendForm.CIRCLE);
                int[] colorClassArray = new int[] {Color.RED, Color.BLUE};
                String[] legendName = {"kcal" , "Day"};
                if(day == 7) {
                    legendName[1] = "Week";
                }
                LegendEntry[] legendEntries = new LegendEntry[2];

                for(int i=0;i<legendEntries.length;i++) {
                    LegendEntry entry = new LegendEntry();
                    entry.formColor = colorClassArray[i];
                    entry.label = String.valueOf(legendName[i]);
                    legendEntries[i] = entry;
                }
                legend.setCustom(legendEntries);

                // set data
                chart.setData(data);
                chart.getData().setValueTextSize(15);
                if(day == 1) {
                    chart.getDescription().setText("일간 음식칼로리");
                }
                else {
                    chart.getDescription().setText("주간 음식칼로리");
                }
                chart.invalidate();
            }
        });

        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attribute_switch = 3;
                ArrayList<Entry> values = new ArrayList<>();
                if(day == 1) {
                    for(int i = 0; i <= 6; i++) {
                        SimpleDateFormat format = new SimpleDateFormat("YYYY MM dd HH:mm:ss", Locale.UK);
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DATE, -(6-i));  // 오늘 날짜에서 하루를 뺌.
                        String date = format.format(calendar.getTime());
                        String time = date.substring(0, 10).replaceAll(" ", "");
                        Cursor cur = database.rawQuery("SELECT sum(몸무게) FROM 사용자정보 where 날짜 = " + Integer.parseInt(time) + "", null);
                        cur.moveToNext();
                        values.add(new Entry(i-6, cur.getFloat(0)));
                    }
                }
                else {
                    for(int i = 0; i <= 6; i++) {
                        SimpleDateFormat format = new SimpleDateFormat("YYYY MM dd HH:mm:ss", Locale.UK);
                        Calendar calendar1 = Calendar.getInstance();
                        Calendar calendar2 = Calendar.getInstance();
                        calendar1.add(Calendar.DATE, - (6-i) * 7);
                        calendar2.add(Calendar.DATE, - (7-i) * 7);
                        String date1 = format.format(calendar1.getTime());
                        String date2 = format.format(calendar2.getTime());
                        String time1 = date1.substring(0, 10).replaceAll(" ", "");
                        String time2 = date2.substring(0, 10).replaceAll(" ", "");
                        Cursor cur = database.rawQuery("SELECT avg(몸무게) FROM 사용자정보 where 날짜 <= "+Integer.parseInt(time1)+" AND 날짜 > "+Integer.parseInt(time2)+"", null);
                        cur.moveToNext();
                        values.add(new Entry(i-6, cur.getFloat(0)));
                    }
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

                Legend legend = chart.getLegend();
                legend.setEnabled(true);
                legend.setTextColor(Color.BLACK);
                legend.setTextSize(15);
                legend.setForm(Legend.LegendForm.CIRCLE);
                int[] colorClassArray = new int[] {Color.RED, Color.BLUE};
                String[] legendName = {"kcal" , "Day"};
                if(day == 7) {
                    legendName[1] = "Week";
                }
                LegendEntry[] legendEntries = new LegendEntry[2];

                for(int i=0;i<legendEntries.length;i++) {
                    LegendEntry entry = new LegendEntry();
                    entry.formColor = colorClassArray[i];
                    entry.label = String.valueOf(legendName[i]);
                    legendEntries[i] = entry;
                }
                legend.setCustom(legendEntries);

                // set data
                chart.setData(data);
                chart.getData().setValueTextSize(15);
                if(day == 1) {
                    chart.getDescription().setText("일간 몸무게");
                }
                else {
                    chart.getDescription().setText("주간 몸무게평균");
                }
                chart.invalidate();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}