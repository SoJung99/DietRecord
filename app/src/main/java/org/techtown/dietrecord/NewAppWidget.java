package org.techtown.dietrecord;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class NewAppWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);
        views.setTextViewText(R.id.appwidget_text2, widgetText);//
        views.setTextViewText(R.id.day, widgetText);//

        /*//!! 클릭시 앱으로 이동
        //앱을 띄우고 MainActivity로 이동한다
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(new ComponentName(context, MainActivity.class));
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.day, pi);
        //!!*/

        //Intent intent = new Intent(context, MainActivity.class);
        //PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        //views.setOnClickPendingIntent(R.layout.new_app_widget, pendingIntent);
        //!!

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        //!!
        super.onUpdate(context, appWidgetManager, appWidgetIds);//!!
        for (int appWidgetId : appWidgetIds) {
            //updateAppWidget(context, appWidgetManager, appWidgetId);


            //!!
            //updateAppWidget(context, appWidgetManager, appWidgetId);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);

            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            remoteViews.setOnClickPendingIntent(R.id.day, pendingIntent);

            //새로고침 작업을 별도의 메서드로 빼기
            refresh(context, remoteViews);
            //새로고침 작업이 와료 후 위젯에게 업데이트 할것을 통지
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
            //!!

        }
    }
    private void refresh(Context context, RemoteViews remoteViews){
        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE);
        remoteViews.setTextViewText(R.id.appwidget_text, sharedPreferences.getString("textBox", "0kcal"));
        remoteViews.setTextViewText(R.id.appwidget_text2, sharedPreferences.getString("textBox2", "0kcal"));
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}