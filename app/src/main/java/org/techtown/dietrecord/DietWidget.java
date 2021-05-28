package org.techtown.dietrecord;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Implementation of App Widget functionality.
 */
public class DietWidget extends AppWidgetProvider {

    // DB 관련
    DataAdapter mDbHelper;
    DataBaseHelper dbHelper;
    SQLiteDatabase database;
    // 날짜 관련
    Calendar calendar;

    private static final String BTN1_CLICKED = "button1Click";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {


        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.diet_widget);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

        views.setTextViewText(R.id.dateN, "N일차");
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        initLoadDB(context);        // 데이터베이스 연결

        ComponentName widget = new ComponentName(context, DietWidget.class);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.diet_widget);

        Cursor cur = database.rawQuery("SELECT 날짜 FROM 사용자정보",null);
        cur.moveToFirst();

        // 오늘 날짜를 Int형으로 20210528 형태로 받아옴.
        calendar = Calendar.getInstance();
        int date =calendar.get(Calendar.YEAR)*10000+(calendar.get(Calendar.MONTH)+1)*100+calendar.get(Calendar.DATE);

        // 지금 몇일차인지 변경
        int d = date - cur.getInt(0) + 1;
        views.setTextViewText(R.id.dateN, Integer.toString(d));

        int cal = 0;
        cur = database.rawQuery("SELECT SUM(칼로리) FROM 사용자운동 WHERE 날짜 = "+ date, null);
        cur.moveToFirst();
        views.setTextViewText(R.id.food, cur.getDouble(0)+" kcal");
        appWidgetManager.updateAppWidget(appWidgetIds, views);

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
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        initLoadDB(context);        // 데이터베이스 연결

        ComponentName widget = new ComponentName(context, DietWidget.class);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.diet_widget);

    }
    @Override
    public void onReceive(Context context, Intent intent){

        initLoadDB(context);        // 데이터베이스 연결
        super.onReceive(context, intent);
        ComponentName widget = new ComponentName(context, DietWidget.class);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.diet_widget);

        String action = intent.getAction();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        if(action.equals(BTN1_CLICKED)){        // 버튼 1 클릭되면 (식단 입력)

            views.setTextViewText(R.id.dateN, "일차");
            Cursor cur = database.rawQuery("SELECT 날짜 FROM 사용자정보",null);
            cur.moveToFirst();

            // 오늘 날짜를 Int형으로 20210528 형태로 받아옴.
            calendar = Calendar.getInstance();
            int date =calendar.get(Calendar.YEAR)*10000+(calendar.get(Calendar.MONTH)+1)*100+calendar.get(Calendar.DATE);

            // 지금 몇일차인지 변경
            int d = date - cur.getInt(0) + 1;
            views.setTextViewText(R.id.dateN, d+"일차");

            int cal = 0;
            cur = database.rawQuery("SELECT SUM(칼로리) FROM 사용자식단 WHERE 날짜 = "+ date, null);
            cur.moveToFirst();
            views.setTextViewText(R.id.food, cur.getDouble(0)+" kcal");

            cal = 0;
            cur = database.rawQuery("SELECT SUM(칼로리) FROM 사용자운동 WHERE 날짜 = "+ date, null);
            cur.moveToFirst();
            views.setTextViewText(R.id.exercise, cur.getDouble(0)+" kcal");


        }

        views.setOnClickPendingIntent(R.id.rr, getPendingSelfIntent(context, BTN1_CLICKED));


        // 화면 초기화
        ComponentName cpName = new ComponentName(context, DietWidget.class);
        appWidgetManager.updateAppWidget(cpName, views);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    public void initLoadDB(Context ct){     // database 불러오기
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