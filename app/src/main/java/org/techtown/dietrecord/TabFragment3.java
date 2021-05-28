package org.techtown.dietrecord;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.KakaoAdapter;
import com.kakao.auth.KakaoSDK;
import com.kakao.sdk.newtoneapi.SpeechRecognizeListener;
import com.kakao.sdk.newtoneapi.SpeechRecognizerClient;
import com.kakao.sdk.newtoneapi.SpeechRecognizerManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class TabFragment3 extends Fragment implements View.OnClickListener, ExerciseAdapter.ExerciseViewClickListener, SpeechRecognizeListener {

    //운동창
    Context context;
    ArrayList<ExerciseData> list;

    ExerciseAdapter adapter = new ExerciseAdapter(list);
    static int i=0;
    Calendar calendar;  // 날짜 가져오기 위한 캘린더 객체

    Spinner ex_spinner, power_spinner;
    EditText time;
    String[] ex_items = {"걷기","뛰기","줄넘기","수영","사이클","파워워킹","런지",
            "스쿼트","윗몸일으키기","푸쉬업","등산","댄스","훌라후프","버피테스트","플랭크","팔벌려뛰기","풀업",
            "계단오르기","에어로빅","요가","딥스","벤치프레스","로잉머신","짐볼운동","복싱","케틀벨","농구","테니스","축구",
            "탁구"};
    String[] power_items = {"상","중","하"};

    TextView allcal;
    TextView voice_result;
    RecyclerView recyclerView;
    Button voice;
    Button info;
    Button submit;
    Button voice_submit;
    Button voice_info;

    ExerciseData voice_exer = null;

    private SpeechRecognizerClient client;

    DataAdapter mDbHelper;
    DataBaseHelper dbHelper;
    SQLiteDatabase database ;

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
                            return MyApplication.this;
                        }
                    };
                }
            });
        }
    }
    final Handler handler = new Handler(Looper.getMainLooper())
    {
        public void handleMessage(Message msg)
        {
            listupdate();
        }
    };
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.fragment3, container, false);
        new MyApplication();

        voice = (Button)v.findViewById(R.id.voice_bt);
        info = (Button)v.findViewById(R.id.exercise_info);
        submit = (Button)v.findViewById(R.id.submit_bt);
        time = (EditText)v.findViewById(R.id.time_input);
        allcal = (TextView)v.findViewById(R.id.cal);
        voice_submit = (Button)v.findViewById(R.id.voice_submit);
        voice_result = (TextView)v.findViewById(R.id.voice_result);
        voice_info = (Button)v.findViewById(R.id.voice_info);

        voice.setOnClickListener(this);
        info.setOnClickListener(this);
        submit.setOnClickListener(this);
        voice_submit.setOnClickListener(this);
        voice_info.setOnClickListener(this);


        ex_spinner = v.findViewById(R.id.exer_spinner);
        power_spinner = v.findViewById(R.id.power_spinner);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, ex_items);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, power_items);

        // 드롭다운 클릭시 선택창
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // 스피너 어댑터 설정
        ex_spinner.setAdapter(adapter1);
        power_spinner.setAdapter(adapter2);
        // 스피너에서 선택 했을 경우 이벤트 처리
        ex_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        power_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        intitLoadDB();

        adapter.notifyDataSetChanged();

        adapter = new ExerciseAdapter(list);


        recyclerView = v.findViewById(R.id.recycle);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);



        recyclerView.setAdapter(adapter);
        adapter.setOnClickListener(this);

        allcal.setText(adapter.getAllCalories()+"kcals");


        // 여기부터 카카오 API
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.RECORD_AUDIO) && ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(getActivity(), new String[] { Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                // 사용자가 거부하면서 다시 묻지 않기를 클릭.. 권한이 없다고 사용자에게 직접 알림.
            }
        } else {
            //startUsingSpeechSDK();
        }

        checkPermissions();

        new SpeechRecognizerManager().getInstance().initializeLibrary(getActivity());




        new Thread()
        {
            public void run()
            {
                while (true) {
                    Message msg = handler.obtainMessage();
                    handler.sendMessage(msg);
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e)
                    { e.printStackTrace(); }
                }
            }

        }.start();
        /*new Thread(new Runnable() {
            @Override public void run() {
                while (true) {
                    listupdate();
                    try {
                        Thread.sleep(1000*10);
                    } catch (InterruptedException e)
                    { e.printStackTrace(); }
                }
            }
        }).start();*/




        return v;
    }

    public void listupdate(){
        list = mDbHelper.getTableData();

        adapter = new ExerciseAdapter(list);
        recyclerView.setAdapter(adapter);
        adapter.setOnClickListener(this);
        adapter.notifyDataSetChanged();
        allcal.setText(adapter.getAllCalories()+"kcals");
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }


    @Override
    public void onClick(View view) {
        String serviceType = SpeechRecognizerClient.SERVICE_TYPE_WORD;

        if(view==voice){
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
            client.setSpeechRecognizeListener(this);

            client.startRecording(true);

            Toast.makeText(getActivity(), "음성인식을 시작합니다.", Toast.LENGTH_SHORT).show();

            voice.setEnabled(false);
        }
        else if(view==voice_info){
            new AlertDialog.Builder(getActivity()).setTitle("음성 인식 예시")
                    .setMessage("※ '운동종류_강도_시간(분)' 순으로 말해주세요.\n※ 운동별 칼로리는 '운동 정보 보기' 버튼을 눌러 참고하세요.\n예) 걷기 상 10분\n예) 윗몸일으키기 중 25분")
                    .setNeutralButton("확인",new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dlg, int sumthin){}
                    }).show();
        }
        else if(view==voice_submit){
            // 데이터베이스에 해당 운동 기록 저장
            // voice_result 를 다시 초기화해줌
            voice_result.setText("운동구분 / 운동강도 / 운동시간(분)");
            // adapter의 list에도 기록 추가후 notifyDataSetChanged()를 통해 화면에 해당 기록 추가

            if(voice_exer==null){
                Toast.makeText(getActivity(), "음성인식을 먼저해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            Cursor cur = database.rawQuery("SELECT * FROM 사용자운동", null);
            Integer n = cur.getCount() + 1;
            Cursor c = database.rawQuery("SELECT * FROM 운동정보 WHERE 운동구분='"+voice_exer.exercise+"' AND 운동강도='"+voice_exer.power+"'", null);

            Double a;
            if(c!=null) {
                c.moveToFirst();
                System.out.println(c.getCount());
                a = Integer.parseInt(voice_exer.time) * c.getDouble(2);
            }
            else{
                Toast.makeText(getActivity(), "음성인식 입력 데이터에 문제가 있습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            calendar = Calendar.getInstance();
            Integer date =calendar.get(Calendar.YEAR)*10000+(calendar.get(Calendar.MONTH)+1)*100+calendar.get(Calendar.DATE);

            String sql = "INSERT INTO 사용자운동 (num, 운동구분, 강도, 시간, 칼로리, 날짜) VALUES ("+n+", '"+voice_exer.exercise+"', '"+voice_exer.power+"', "+Integer.parseInt(voice_exer.time)+", "+ a+", "+ date+ ")";
            database.execSQL(sql);

            list = mDbHelper.getTableData();

            adapter = new ExerciseAdapter(list);
            recyclerView.setAdapter(adapter);
            adapter.setOnClickListener(this);
            adapter.notifyDataSetChanged();
            allcal.setText(adapter.getAllCalories()+"kcals");
        }
        else if(view==info){
            LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            new AlertDialog.Builder(getActivity()).setTitle("운동 입력 정보")
                    .setView(inflater.inflate(R.layout.exercise_info, (ViewGroup)view.findViewById(R.id.exerciseInfo)))
                    .setNeutralButton("확인",new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dlg, int sumthin){}
                    }).show();

        }
        else if(view==submit){
            if(time.getText().toString().length()==0){
                Toast.makeText(getActivity(), "운동 시간을 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
            else{
                ExerciseData dataset = new ExerciseData((String)ex_spinner.getSelectedItem(),(String)power_spinner.getSelectedItem(),time.getText().toString());

                //list.add(0,dataset);

                time.setText(null);
                //allcal.setText(adapter.getAllCalories()+"kcals");
                Cursor cur = database.rawQuery("SELECT * FROM 사용자운동", null);
                Integer n = cur.getCount() + 1;
                Cursor c = database.rawQuery("SELECT * FROM 운동정보 WHERE 운동구분='"+dataset.exercise+"' AND 운동강도='"+dataset.power+"'", null);


                c.moveToFirst();
                System.out.println(c.getCount());
                Double a = Integer.parseInt(dataset.time)* c.getDouble(2);


                calendar = Calendar.getInstance();
                Integer date =calendar.get(Calendar.YEAR)*10000+(calendar.get(Calendar.MONTH)+1)*100+calendar.get(Calendar.DATE);

                String sql = "INSERT INTO 사용자운동 (num, 운동구분, 강도, 시간, 칼로리, 날짜) VALUES ("+n+", '"+dataset.exercise+"', '"+dataset.power+"', "+Integer.parseInt(dataset.time)+", "+ a+", "+date+")";
                database.execSQL(sql);
                list = mDbHelper.getTableData();

                adapter = new ExerciseAdapter(list);
                 recyclerView.setAdapter(adapter);
                adapter.setOnClickListener(this);
                adapter.notifyDataSetChanged();
                allcal.setText(adapter.getAllCalories()+"kcals");


            }
        }

    }

    @Override
    public void onItemClicked(int position) {
        final int p = position;
        new AlertDialog.Builder(getActivity()).setTitle("기록 삭제")
                .setMessage("해당 운동 기록을 삭제하시겠습니까?")
                .setPositiveButton("네",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dlg, int sumthin){
                        removeP(p);
                    }
                }).setNegativeButton("아니오",new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dlg, int sumthin) { }
        }).show();
    }

    public void removeP(int p){
        ExerciseData d = list.get(p);
        calendar = Calendar.getInstance();
        Integer date =calendar.get(Calendar.YEAR)*10000+(calendar.get(Calendar.MONTH)+1)*100+calendar.get(Calendar.DATE);
        String sql = "DELETE FROM 사용자운동 WHERE 운동구분='"+d.exercise+"' AND 강도='"+d.power+"' AND 시간="+d.time+" AND 날짜="+date;
        database.execSQL(sql);
        list = mDbHelper.getTableData();

        adapter = new ExerciseAdapter(list);
        recyclerView.setAdapter(adapter);
        adapter.setOnClickListener(this);
        adapter.notifyDataSetChanged();
        allcal.setText(adapter.getAllCalories()+"kcals");

    }
    @Override
    public void onItemLongClicked(int position) {

    }

    public void onDestroy() {
        super.onDestroy();

        SpeechRecognizerManager.getInstance().finalizeLibrary();
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
            permissionCheck = ContextCompat.checkSelfPermission(getActivity(),permissions[i]);
            if(permissionCheck == PackageManager.PERMISSION_DENIED){
                System.out.println("권한 없음 : "+permissions[i]);
                if(i==3) ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO},1);
                if(i==2) ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);

            }
            else
                System.out.println("권한 있음 : "+permissions[i]);
        }
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
                Toast.makeText(getActivity(), "음성인식이 잘못되었습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                voice.setEnabled(true);
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



        StringTokenizer strToken = new StringTokenizer(str," ");

        try{
            voice_exer = new ExerciseData(strToken.nextToken(),strToken.nextToken(),strToken.nextToken());
        }catch(Exception a){
            Toast.makeText(getActivity(), "음성인식이 잘못되었습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            voice.setEnabled(true);
            return;
        }

        voice_exer.setTime(voice_exer.time.substring(0,voice_exer.time.length()-1));

        if(binaryStringSearch(ex_items, voice_exer.exercise) == -1){
            Toast.makeText(getActivity(), voice_exer.exercise+" 해당 운동구분은 데이터베이스에 없습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            voice.setEnabled(true);
            return;
        }
        if(binaryStringSearch(power_items, voice_exer.power) == -1){
            Toast.makeText(getActivity(), "해당 운동강도는 데이터베이스에 없습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            voice.setEnabled(true);
            return;
        }
        if(!isNumber(voice_exer.time)){
            Toast.makeText(getActivity(), "시간은 숫자여야 합니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            voice.setEnabled(true);
            return;
        }

        voice_result.setText(new String(voice_exer.exercise+" / "+voice_exer.power+" / "+voice_exer.time));
        voice.setEnabled(true);
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

}
