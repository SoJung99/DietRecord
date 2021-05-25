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
public class NewAppWidget extends AppWidgetProvider implements SpeechRecognizeListener{

    String[] ex_items = {"걷기","뛰기","줄넘기","수영","사이클","파워워킹","런지",
            "스쿼트","윗몸일으키기","푸쉬업","등산","댄스","훌라후프","버피테스트","플랭크","팔벌려뛰기","풀업",
            "계단오르기","에어로빅","요가","딥스","벤치프레스","로잉머신","짐볼운동","복싱","케틀벨","농구","테니스","축구",
            "탁구"};
    String[] power_items = {"상","중","하"};

    DataAdapter mDbHelper;
    DataBaseHelper dbHelper;
    SQLiteDatabase database;

    Calendar calendar;

    ProgressBar food;
    ProgressBar exer;

    ExerciseData voice_exer = null;
    Context con;
    // 음성인식
    private SpeechRecognizerClient client;


    private static final String BTN1_CLICKED = "button1Click";
    private static final String BTN2_CLICKED = "button2Click";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        new MyApplication();
        /*
        // 여기부터 카카오 API
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.RECORD_AUDIO) && ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions((Activity) context, new String[] { Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                // 사용자가 거부하면서 다시 묻지 않기를 클릭.. 권한이 없다고 사용자에게 직접 알림.
            }
        } else {
            //startUsingSpeechSDK();
        }


        checkPermissions();*/

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

        Cursor cur = database.rawQuery("SELECT * from 사용자정보 WHERE 날짜 = " + date, null);
        cur.moveToFirst();
        views.setTextViewText(R.id.date,Integer.toString(date-cur.getInt(0)+1)+"일차");

        /*
        cur = database.rawQuery("SELECT 칼로리 from 사용자정보", null);
        cur.moveToFirst();
        views.setTextViewText(R.id.exercise, cur.getDouble(0)+" / 권장 운동 칼로리");
        */

        if(action.equals(BTN1_CLICKED)){        // 버튼 1 클릭되면 (식단 입력)

            Toast.makeText(context,"버튼1 클릭", Toast.LENGTH_SHORT).show();




        }
        else if(action.equals(BTN2_CLICKED)){   // 버튼 2 클릭되면 (운동 입력)
            String serviceType = SpeechRecognizerClient.SERVICE_TYPE_WORD;
            SpeechRecognizerClient.Builder builder = new SpeechRecognizerClient.Builder().
                    setServiceType(serviceType).
                    setUserDictionary("걷기 상\n걷기 중\n걷기 하\n뛰기 상\n뛰기 중\n뛰기 하\n줄넘기 상\n줄넘기 중\n줄넘기 하\n수영 상\n수영 중\n수영 하\n" +
                            "사이클 상\n사이클 중\n사이클 하\n요가 상\n요가 중\n요가 하\n런지 상\n런지 중\n런지 하\n스쿼트 상\n스쿼트 중\n스쿼트 하\n윗몸일으키기 상\n윗몸일으키기 중\n윗몸일으키기 하\n" +
                            "푸쉬업 상\n푸쉬업 중\n푸쉬업 하\n등산 상\n등산 중\n등산 하\n댄스 상\n댄스 중\n댄스 하\n훌라후프 상\n훌라후프 중\n훌라후프 하\n버피 상\n버피 중\n버피 하\n" +
                            "플랭크 상\n플랭크 중\n플랭크 하\n팔벌려뛰기 상\n팔벌려뛰기 중\n팔벌려뛰기 하\n풀업 상\n풀업 중\n풀업 하\n계단오르기 상\n계단오르기 중\n계단오르기 하\n" +
                            "에어로빅 상\n에어로빅 중\n에어로빅 하\n파워워킹 상\n파워워킹 중\n파워워킹 하\n딥스 상\n딥스 중\n딥스 하\n벤치프레스 상\n벤치프레스 중\n벤치프레스 하\n" +
                            "로잉머신 상\n로잉머신 중\n로잉머신 하\n짐볼운동 상\n짐볼운동 중\n짐볼운동 하\n복싱 상\n복싱 중\n복싱 하\n케틀벨 상\n케틀벨 중\n케틀벨 하\n" +
                            "농구 상\n농구 중\n농구 하\n테니스 상\n테니스 중\n테니스 하\n축구 상\n축구 중\n축구 하\n탁구 상\n탁구 중\n탁구 하");

            client = builder.build();
            client.setSpeechRecognizeListener((SpeechRecognizeListener) this);

            client.startRecording(true);

            Toast.makeText(context,"운동 입력 음성인식을 시작합니다.", Toast.LENGTH_SHORT).show();

        }

        views.setOnClickPendingIntent(R.id.button, getPendingSelfIntent(context, BTN1_CLICKED));
        views.setOnClickPendingIntent(R.id.button2, getPendingSelfIntent(context, BTN2_CLICKED));

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

    @Override
    public void onReady() {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int errorCode, String errorMsg) {
        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(con, "음성인식이 잘못되었습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            }
        }, 0);
    }

    @Override
    public void onPartialResult(String partialResult) {

    }

    @Override
    public void onResults(Bundle results) {
        final StringBuilder builder = new StringBuilder();
        ArrayList<String> texts = results.getStringArrayList(SpeechRecognizerClient.KEY_RECOGNITION_RESULTS);


        builder.append(texts.get(0));


        String str = builder.toString();


        System.out.println(str);
        StringTokenizer strToken = new StringTokenizer(str," ");

        try{
            voice_exer = new ExerciseData(strToken.nextToken(),strToken.nextToken(),strToken.nextToken());
        }catch(Exception a){
            Toast.makeText(con, "음성인식이 잘못되었습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        voice_exer.setTime(voice_exer.time.substring(0,voice_exer.time.length()-1));

        if(binaryStringSearch(ex_items, voice_exer.exercise) == -1){
            Toast.makeText(con, voice_exer.exercise+" 해당 운동구분은 데이터베이스에 없습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(binaryStringSearch(power_items, voice_exer.power) == -1){
            Toast.makeText(con, "해당 운동강도는 데이터베이스에 없습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!isNumber(voice_exer.time)){
            Toast.makeText(con, "시간은 숫자여야 합니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cur = database.rawQuery("Select * FROM 사용자운동", null);
        Integer n = cur.getCount() + 1;


        Cursor c = database.rawQuery("SELECT * FROM 운동정보 WHERE 운동구분='"+voice_exer.exercise+"' AND 운동강도='"+voice_exer.power+"'", null);
        c.moveToFirst();

        Double a = Integer.parseInt(voice_exer.time)* c.getDouble(2);
        calendar = Calendar.getInstance();
        Integer date =calendar.get(Calendar.YEAR)*10000+(calendar.get(Calendar.MONTH)+1)*100+calendar.get(Calendar.DATE);

        String sql = "INSERT INTO 사용자운동 (num, 운동구분, 강도, 시간, 칼로리, 날짜) VALUES ("+n+", '"+voice_exer.exercise+"', '"+voice_exer.power+"', "+Integer.parseInt(voice_exer.time)+", "+ a+", "+date+")";
        database.execSQL(sql);

        RemoteViews views = new RemoteViews(con.getPackageName(), R.layout.new_app_widget);
        views.setTextViewText(R.id.date, n+"개");

        Toast.makeText(con, "음성인식이 완료되었습니다.", Toast.LENGTH_SHORT).show();
    }
    static boolean isNumber(String str) {
        boolean result = true;
        // null, 공백일시
        if (str == null || str.length() == 0) {
            result = false;
        }
        // null이나 공백이 아닐시
        else {
            for (int i = 0; i < str.length(); i++) {
                int c = (int) str.charAt(i);
                // 숫자가 아니라면
                if (c < 48 || c > 57) {
                    result = false;
                }
            }
        }
        return result;
    }
    public static int binaryStringSearch(String[] strArr, String str) {

        int result = -1;

        for(int i=0; i<strArr.length-1;i++){
            if(str.matches(strArr[i]))
                return i;
        }
        return result;
    }
    @Override
    public void onAudioLevel(float audioLevel) {

    }

    @Override
    public void onFinished() {

    }
    private void checkPermissions(){
        String[] permissions = {
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                android.Manifest.permission.RECORD_AUDIO
        };

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for(int i=0; i<permissions.length; i++){
            permissionCheck = ContextCompat.checkSelfPermission(con,permissions[i]);
            if(permissionCheck == PackageManager.PERMISSION_DENIED){
                System.out.println("권한 없음 : "+permissions[i]);
                if(i==3) ActivityCompat.requestPermissions((Activity) con, new String[]{Manifest.permission.RECORD_AUDIO},1);
                if(i==2) ActivityCompat.requestPermissions((Activity) con, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);

            }
            else
                System.out.println("권한 있음 : "+permissions[i]);
        }
    }
    public class MyApplication extends Application {

        @Override
        public void onCreate(){
            super.onCreate();

            //카카오 SDK 초기화
            KakaoSDK.init(new KakaoAdapter(){

                @Override
                public IApplicationConfig getApplicationConfig() {
                    return new IApplicationConfig(){
                        @Override
                        public Context getApplicationContext() {
                            return NewAppWidget.MyApplication.this;
                        }
                    };
                }
            });
        }
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