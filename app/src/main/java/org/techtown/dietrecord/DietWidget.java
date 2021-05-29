package org.techtown.dietrecord;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Implementation of App Widget functionality.
 */
public class DietWidget extends AppWidgetProvider {

    DataAdapter mDbHelper;
    DataBaseHelper dbHelper;
    SQLiteDatabase database;

    Calendar calendar;

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
        // There may be multiple widgets active, so update all of them
        initLoadDB(context);

        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int appWidgetId : appWidgetIds) {

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.diet_widget);

            //(위젯 누르면 앱 이동)
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            //remoteViews.setOnClickPendingIntent(R.id.day, pendingIntent); //(DAY누르면 앱 이동)
            remoteViews.setOnClickPendingIntent(R.id.dietwidget, pendingIntent); //dietwidget 레이아웃 id

            //새로고침 작업을 별도의 메서드로 빼기
            refresh(context, remoteViews);

            // N일차 바꾸기
            Cursor ccc = database.rawQuery("SELECT 날짜 FROM 사용자정보", null);
            ccc.moveToFirst();
            calendar = Calendar.getInstance();
            int dd =calendar.get(Calendar.YEAR)*10000+(calendar.get(Calendar.MONTH)+1)*100+calendar.get(Calendar.DATE);

            remoteViews.setTextViewText(R.id.dateN,dd-ccc.getInt(0)+1+"일차");

            //새로고침 작업이 와료 후 위젯에게 업데이트 할것을 통지
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }
    private void refresh(Context context, RemoteViews remoteViews){
        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE);
        remoteViews.setTextViewText(R.id.dateN,sharedPreferences.getString("ndate","N일차"));
        remoteViews.setTextViewText(R.id.food, sharedPreferences.getString("textBox", "0kcal"));
        remoteViews.setTextViewText(R.id.exercise, sharedPreferences.getString("textBox2", "0kcal"));

    }
    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created

        ComponentName widget = new ComponentName(context, DietWidget.class);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.diet_widget);

    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


}