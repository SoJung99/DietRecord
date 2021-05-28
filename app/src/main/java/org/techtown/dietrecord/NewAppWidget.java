package org.techtown.dietrecord;


import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.KakaoAdapter;
import com.kakao.auth.KakaoSDK;
import com.kakao.sdk.newtoneapi.SpeechRecognizeListener;
import com.kakao.sdk.newtoneapi.SpeechRecognizerClient;
import com.kakao.sdk.newtoneapi.SpeechRecognizerManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.StringTokenizer;

/**
 * Implementation of App Widget functionality.
 */
public class NewAppWidget extends AppWidgetProvider{


    DataAdapter mDbHelper;
    DataBaseHelper dbHelper;
    SQLiteDatabase database;

    Calendar calendar;

    Context con;

    private static final String BTN1_CLICKED = "button1Click";
    private static final String BTN2_CLICKED = "button2Click";
    private static final String P1 = "P1update";
    private static final String P2 = "P2update";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        ComponentName widget = new ComponentName(context, NewAppWidget.class);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);

        appWidgetManager.updateAppWidget(widget, views);

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action){
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context,0,intent,0);
    }

    @Override
    public void onReceive(Context context, Intent intent){


        new SpeechRecognizerManager().getInstance().initializeLibrary(context);

        initLoadDB(context);        // 데이터베이스 연결
        super.onReceive(context, intent);
        ComponentName widget = new ComponentName(context, NewAppWidget.class);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);

        String action = intent.getAction();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        calendar = Calendar.getInstance();
        Integer date =calendar.get(Calendar.YEAR)*10000+(calendar.get(Calendar.MONTH)+1)*100+calendar.get(Calendar.DATE);


        Cursor cur = database.rawQuery("SELECT * from 사용자정보", null);
        cur.moveToFirst();
        views.setTextViewText(R.id.date,Integer.toString(date-cur.getInt(0)+1)+"일차");

        /*
        cur = database.rawQuery("SELECT 칼로리 from 사용자정  WHERE 날짜 = " + date, null);
        cur.moveToFirst();
        views.setTextViewText(R.id.exercise, cur.getDouble(0)+" / 권장 운동 칼로리");
        */

        if(action.equals(BTN1_CLICKED)){        // 버튼 1 클릭되면 (식단 입력)




        }
        else if(action.equals(BTN2_CLICKED)){   // 버튼 2 클릭되면 (운동 입력)

        }

        views.setOnClickPendingIntent(R.id.button, getPendingSelfIntent(context, BTN1_CLICKED));
        views.setOnClickPendingIntent(R.id.button2, getPendingSelfIntent(context, BTN2_CLICKED));


        //views.setProgressBar(R.id.food,2000,1200,false);
        //views.setProgressBar(R.id.exer,1000,800,false);

        // 화면 초기화
        ComponentName cpName = new ComponentName(context, NewAppWidget.class);
        appWidgetManager.updateAppWidget(cpName, views);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }



    public void initLoadDB(Context ct){
        mDbHelper = new DataAdapter(ct);
        mDbHelper.createDatabase();
        mDbHelper.open();

        dbHelper = new DataBaseHelper(ct);
        dbHelper.openDataBase();
        dbHelper.close();
        database = dbHelper.getWritableDatabase();

        //mDbHelper.close();
    }

}