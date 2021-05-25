package org.techtown.dietrecord;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.KakaoAdapter;
import com.kakao.auth.KakaoSDK;
import com.kakao.sdk.newtoneapi.SpeechRecognizerManager;
import com.kakao.sdk.newtoneapi.SpeechRecognizeListener;
import com.kakao.sdk.newtoneapi.SpeechRecognizerClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class TabFragment2 extends Fragment implements View.OnClickListener, SpeechRecognizeListener{
    String food_time, food_kind, food_amount;  // 선택된 식사 시간대 // 선택된 음식 종류 // 선택된 음식 양

    Button btnVoice;
    TextView tv_voice_result;

    String voice_food_kind = "(음식 종류)", voice_food_amount = "(양)", food2;
    Cursor voice_cursor = null;
    String[] item_kind;

    RadioGroup rg;
    RadioButton rb1, rb2, rb3;

    ArrayList<UserFood> LIST = new ArrayList<>();
    ArrayList<UserFood> LIST2 = new ArrayList<>();
    ArrayList<UserFood> LIST3 = new ArrayList<>();

    private SpeechRecognizerClient client;

    DataAdapterUserFood mDbHelper;
    DataBaseHelper dbHelper;
    SQLiteDatabase database ;

    private void initLoadDB() {
        mDbHelper = new DataAdapterUserFood(getActivity().getApplicationContext());
        mDbHelper.createDatabase();
        mDbHelper.open();

        dbHelper = new DataBaseHelper(getActivity().getApplicationContext());
        dbHelper.openDataBase();
        dbHelper.close();
        database = dbHelper.getWritableDatabase();
        // 오늘 것만 리스트에 가져오기
        LIST = mDbHelper.getBreakfast();
        LIST2 = mDbHelper.getLunch();
        LIST3 = mDbHelper.getDinner();
        // db 닫기
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment2, container, false);
        new MyApplication();

        item_kind = getResources().getStringArray(R.array.itemKind);

        btnVoice = (Button)v.findViewById(R.id.btn_voice);
        btnVoice.setOnClickListener(this);
        tv_voice_result = (TextView)v.findViewById(R.id.textView_voice);
        TextView sumC = (TextView)v.findViewById(R.id.sumCal);
        TextView sumC1 = (TextView)v.findViewById(R.id.sumCal_breakfast);
        TextView sumC2 = (TextView)v.findViewById(R.id.sumCal_lunch);
        TextView sumC3 = (TextView)v.findViewById(R.id.sumCal_dinner);
        rg = v.findViewById(R.id.rg);
        rb1 = v.findViewById(R.id.rb1);
        rb2 = v.findViewById(R.id.rb2);
        rb3 = v.findViewById(R.id.rb3);

        initLoadDB();

        recyclerAdapter recyclerADAPTER = new recyclerAdapter(LIST);
        RecyclerView recyclerView = v.findViewById(R.id.my_recycler_view);
        int numberOfColumns = 2;
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), numberOfColumns));
        recyclerView.setAdapter(recyclerADAPTER);
        recyclerADAPTER.notifyDataSetChanged();

        recyclerAdapter recyclerADAPTER2 = new recyclerAdapter(LIST2);
        RecyclerView recyclerView2 = v.findViewById(R.id.my_recycler_view2);
        recyclerView2.setLayoutManager(new GridLayoutManager(getActivity(), numberOfColumns));
        recyclerView2.setAdapter(recyclerADAPTER2);
        recyclerADAPTER2.notifyDataSetChanged();

        recyclerAdapter recyclerADAPTER3 = new recyclerAdapter(LIST3);
        RecyclerView recyclerView3 = v.findViewById(R.id.my_recycler_view3);
        recyclerView3.setLayoutManager(new GridLayoutManager(getActivity(), numberOfColumns));
        recyclerView3.setAdapter(recyclerADAPTER3);
        recyclerADAPTER3.notifyDataSetChanged();

        sumC.setText((recyclerADAPTER.SumCalories(LIST)+recyclerADAPTER2.SumCalories(LIST2)+recyclerADAPTER3.SumCalories(LIST3))+"kcal");
        sumC1.setText(recyclerADAPTER.SumCalories(LIST)+"kcal");
        sumC2.setText(recyclerADAPTER2.SumCalories(LIST2)+"kcal");
        sumC3.setText(recyclerADAPTER3.SumCalories(LIST3)+"kcal");

        // 추가(음성인식) 버튼 (btn_voiceAdd 눌렸을 때)
        Button btnVoiceAdd = (Button)v.findViewById(R.id.btn_voiceAdd);
        btnVoiceAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(food_time == null) {
                    Toast.makeText(getActivity(), "식사를 선택해 주세요!", Toast.LENGTH_SHORT).show();
                }
                else {
                    voice_cursor = database.rawQuery("SELECT * FROM 음식정보 WHERE 음식구분='" + voice_food_kind + "'", null);
                    if (voice_cursor == null) {
                        Toast.makeText(getActivity(), "음성인식을 (다시) 시도해 주세요!", Toast.LENGTH_SHORT).show();
                    } else { //DB, 리스트에 넣기
                        voice_cursor.moveToFirst();
                        Float cal = Float.parseFloat(voice_food_amount) * voice_cursor.getFloat(1);
                        Float A = voice_cursor.getFloat(2);
                        Float B = voice_cursor.getFloat(3);
                        Float C = voice_cursor.getFloat(4);
                        String unit = voice_cursor.getString(5);
                        String sql1 = "INSERT INTO 사용자식단 (식사, 음식구분, 양, 단위, 칼로리, 탄수화물, 단백질, 지방, 날짜) ";
                        String sql2 = "VALUES ('" + food_time + "', '" + voice_food_kind + "', " + Float.parseFloat(voice_food_amount) + ", '" + unit + "', " + cal + ", " + A + ", " + B + ", " + C + ", " + getDate() + ")";
                        database.execSQL(sql1 + sql2);

                        Cursor CURSOR = database.rawQuery("SELECT * FROM 사용자식단 ORDER BY ROWID DESC LIMIT 1", null);
                        CURSOR.moveToFirst();
                        int id = CURSOR.getInt(0);
                        UserFood userFood = new UserFood(id, voice_food_kind, Float.parseFloat(voice_food_amount), unit, cal);
                        if (food_time.equals("아침")) { // 아침 리스트에 추가
                            recyclerADAPTER.addItem(userFood);
                            recyclerADAPTER.notifyDataSetChanged();
                            sumC1.setText(recyclerADAPTER.SumCalories(LIST) + "kcal");
                        } else if (food_time.equals("점심")) { // 점심 리스트에 추가
                            recyclerADAPTER2.addItem(userFood);
                            recyclerADAPTER2.notifyDataSetChanged();
                            sumC2.setText(recyclerADAPTER2.SumCalories(LIST2) + "kcal");
                        } else if (food_time.equals("저녁")) { // 저녁 리스트에 추가
                            recyclerADAPTER3.addItem(userFood);
                            recyclerADAPTER3.notifyDataSetChanged();
                            sumC3.setText(recyclerADAPTER3.SumCalories(LIST3) + "kcal");
                        }
                        sumC.setText((recyclerADAPTER.SumCalories(LIST) + recyclerADAPTER2.SumCalories(LIST2) + recyclerADAPTER3.SumCalories(LIST3)) + "kcal");
                        voiceReset();
                    }
                }
            }
        });

        // 추가(직접입력)버튼 (btn_add 눌렸을 때)
        Button btnAdd = (Button)v.findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] strings = food_kind.split(" ", 2);
                food_kind = strings[0];
                if(food_time == null){
                    Toast.makeText(getActivity(), "식사를 선택하세요!", Toast.LENGTH_SHORT).show();
                }
                else if(food_kind.equals("(음식 종류)")){
                    Toast.makeText(getActivity(), "음식 종류를 선택하세요!", Toast.LENGTH_SHORT).show();
                }
                else if(food_amount.equals("(양)")){
                    Toast.makeText(getActivity(), "양을 선택하세요!", Toast.LENGTH_SHORT).show();
                }
                else{ // 선택이 모두 완료되면
                    Cursor cur = database.rawQuery("SELECT * FROM 음식정보 WHERE 음식구분='"+food_kind+"'", null);
                    cur.moveToFirst();
                    Float cal = Float.parseFloat(food_amount) * cur.getFloat(1);
                    Float A = cur.getFloat(2);
                    Float B = cur.getFloat(3);
                    Float C = cur.getFloat(4);
                    String unit = cur.getString(5);
                    String sql1 = "INSERT INTO 사용자식단 (식사, 음식구분, 양, 단위, 칼로리, 탄수화물, 단백질, 지방, 날짜) ";
                    String sql2 = "VALUES ('"+food_time+"', '"+food_kind+"', "+Float.parseFloat(food_amount)+", '"+unit+"', "+cal+", "+A+", "+B+", "+C+", "+getDate()+")";
                    database.execSQL(sql1+sql2);

                    Cursor CURSOR = database.rawQuery("SELECT * FROM 사용자식단 ORDER BY ROWID DESC LIMIT 1", null);
                    CURSOR.moveToFirst();
                    int id = CURSOR.getInt(0);
                    UserFood userFood = new UserFood(id, food_kind, Float.parseFloat(food_amount), unit, cal);
                    if(food_time.equals("아침")){
                        recyclerADAPTER.addItem(userFood);
                        recyclerADAPTER.notifyDataSetChanged();
                        sumC1.setText(recyclerADAPTER.SumCalories(LIST)+"kcal");
                    }
                    else if(food_time.equals("점심")){
                        recyclerADAPTER2.addItem(userFood);
                        recyclerADAPTER2.notifyDataSetChanged();
                        sumC2.setText(recyclerADAPTER2.SumCalories(LIST2)+"kcal");

                    }
                    else if(food_time.equals("저녁")){
                        recyclerADAPTER3.addItem(userFood);
                        recyclerADAPTER3.notifyDataSetChanged();
                        sumC3.setText(recyclerADAPTER3.SumCalories(LIST3)+"kcal");
                    }
                    sumC.setText((recyclerADAPTER.SumCalories(LIST)+recyclerADAPTER2.SumCalories(LIST2)+recyclerADAPTER3.SumCalories(LIST3))+"kcal");
                }

            }
        });

        //삭제하기
        recyclerADAPTER.setOnItemClickListener(new recyclerAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View v, int pos) {
                new AlertDialog.Builder(getActivity()).setTitle("기록 삭제")
                        .setMessage("해당 음식 기록을 삭제하시겠습니까?")
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {  // 아침 리스트 삭제
                                String sql = "DELETE FROM 사용자식단 WHERE num="+LIST.get(pos).getId()+"";
                                database.execSQL(sql);
                                LIST.remove(pos);
                                recyclerADAPTER.notifyDataSetChanged();
                                sumC1.setText(recyclerADAPTER.SumCalories(LIST)+"kcal");
                                sumC.setText((recyclerADAPTER.SumCalories(LIST)+recyclerADAPTER2.SumCalories(LIST2)+recyclerADAPTER3.SumCalories(LIST3))+"kcal");
                            }
                        })
                        .setNeutralButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { }})
                        .show();
            }
        });
        recyclerADAPTER2.setOnItemClickListener(new recyclerAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View v, int pos) {
                new AlertDialog.Builder(getActivity()).setTitle("기록 삭제")
                        .setMessage("해당 음식 기록을 삭제하시겠습니까?")
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {  // 점심 리스트 삭제
                                String sql = "DELETE FROM 사용자식단 WHERE num="+LIST2.get(pos).getId()+"";
                                database.execSQL(sql);
                                LIST2.remove(pos);
                                recyclerADAPTER2.notifyDataSetChanged();
                                sumC2.setText(recyclerADAPTER2.SumCalories(LIST2)+"kcal");
                                sumC.setText((recyclerADAPTER.SumCalories(LIST)+recyclerADAPTER2.SumCalories(LIST2)+recyclerADAPTER3.SumCalories(LIST3))+"kcal");
                            }})
                        .setNeutralButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { }})
                        .show();
            }
        });
        recyclerADAPTER3.setOnItemClickListener(new recyclerAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View v, int pos) {
                new AlertDialog.Builder(getActivity()).setTitle("기록 삭제")
                        .setMessage("해당 음식 기록을 삭제하시겠습니까?")
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {  // 저녁 리스트 삭제
                                String sql = "DELETE FROM 사용자식단 WHERE num="+LIST3.get(pos).getId()+"";
                                database.execSQL(sql);
                                LIST3.remove(pos);
                                recyclerADAPTER3.notifyDataSetChanged();
                                sumC3.setText(recyclerADAPTER3.SumCalories(LIST3)+"kcal");
                                sumC.setText((recyclerADAPTER.SumCalories(LIST)+recyclerADAPTER2.SumCalories(LIST2)+recyclerADAPTER3.SumCalories(LIST3))+"kcal");
                            }
                        })
                        .setNeutralButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { }})
                        .show();
            }
        });

        // 음식별 정보 보기 버튼 (btn_info 버튼 눌렸을 때)
        Button btnInfo = (Button)v.findViewById(R.id.btn_info);
        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context mContext = getActivity().getApplicationContext();
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                new AlertDialog.Builder(getActivity())
                        .setTitle("음식별 정보")
                        .setView(inflater.inflate(R.layout.food_info, (ViewGroup)v.findViewById(R.id.foodInfo)))
                        .setNeutralButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { }})
                        .show();
            }
        });
        // 추천 식단 보기 버튼 (btn_recom 버튼 눌렸을 때)
        Button btnRecom = (Button)v.findViewById(R.id.btn_recom);
        btnRecom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("추천 식단")
                        .setMessage("내용 넣기")
                        .setNeutralButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { }})
                        .show();
            }
        });
        // 음성인식 예시 보기 버튼 (btn_ex 버튼 눌렸을 때)
        Button btnEx = (Button)v.findViewById(R.id.btn_ex);
        btnEx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("음성인식 예시")
                        .setMessage("※ '식사_음식 종류_양(컵/개/인분/조각)' 순으로 말해주세요.\n※ 음식별 단위는 '음식별 정보 보기' 버튼을 눌러 참고하세요.\n\n예) 저녁 떡볶이 1인분" +
                                "\n예) 아침 아몬드 20\n예) 아침 사과 1개\n예) 점심 고등어구이 1.5")
                        .setNeutralButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { }})
                        .show();
            }
        });
        // 식사 시간대 선택. 라디오 버튼
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.rb1){ //아침
                    food_time = rb1.getText().toString();
                }
                else if(checkedId == R.id.rb2){ //점심
                    food_time = rb2.getText().toString();
                }
                else if(checkedId == R.id.rb3){ //저녁
                    food_time = rb3.getText().toString();
                }
            }
        });
        // 음식 종류 선택 박스
        Spinner spinner2 = (Spinner)v.findViewById(R.id.spinner2);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                food_kind = spinner2.getSelectedItem().toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        // 음식 양 선택 박스
        Spinner spinner3 = (Spinner)v.findViewById(R.id.spinner3);
        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                food_amount = spinner3.getSelectedItem().toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // 여기부터 카카오 API
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.RECORD_AUDIO) && ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(getActivity(), new String[] { Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {// 사용자가 거부하면서 다시 묻지 않기를 클릭.. 권한이 없다고 사용자에게 직접 알림.
            }
        } else {//startUsingSpeechSDK();
        }
        checkPermissions();
        new SpeechRecognizerManager().getInstance().initializeLibrary(getActivity());


        return v;
    }

    @Override
    public void onClick(View view) {
        String serviceType = SpeechRecognizerClient.SERVICE_TYPE_WORD;
        if (view == btnVoice) {
            if(food_time == null) {
                Toast.makeText(getActivity(), "식사를 선택해 주세요!", Toast.LENGTH_SHORT).show();
            }
            else {
                SpeechRecognizerClient.Builder builder = new SpeechRecognizerClient.Builder().setServiceType(serviceType).
                        setUserDictionary(
                                //"아침\n점심\n저녁\n"+
                                "컵\n개\n조각\n인분\n"
                                        + "물\n갈비탕\n갈치구이\n감자샐러드\n고등어구이\n곤약\n군고구마\n군만두\n귤\n김치볶음\n김치전\n김치찌개\n깍두기\n"
                                        + "다시마\n달걀프라이\n닭가슴살\n당근\n도넛\n돼지불고기\n된장찌개\n두부\n딸기\n떡국\n떡볶이\n라면\n멸치볶음\n물냉면\n바게트\n바나나\n밥\n배\n배추김치\n백김치\n백설기\n베이글\n북어국\n브로콜리\n비빔냉면\n"
                                        + "사과\n삶은달걀\n삶은감자\n삼계탕\n샐러리\n생크림케이크\n설렁탕\n소갈비찜\n소고기등심\n수박\n스파게티\n시금치나물\n시리얼\n식빵\n아몬드\n아보카도\n야채죽\n양배추찜\n양상추\n연어\n오이\n요거트\n우동\n"
                                        + "잡채\n장조림\n짜장면\n짬뽕\n찐단호박\n초콜릿케이크\n치즈버거\n치킨\n치킨샐러드\n칼국수\n콘샐러드\n콩나물국\n탕수육\n토마토\n파전\n파프리카\n햄버거\n호밀빵\n"
                                        + "0.5컵\n 1컵\n 1.5컵\n 2컵\n 2.5컵\n 3컵\n 3.5컵\n 4컵\n 4.5컵\n 5컵\n 5.5컵\n 6컵\n 6.5컵\n 7컵\n 7.5컵\n 8컵\n 8.5컵\n 9컵\n 9.5컵\n 10컵\n"
                                        + "0.5개\n 1개\n 1.5개\n 2개\n 2.5개\n 3개\n 3.5개\n 4개\n 4.5개\n 5개\n 5.5개\n 6개\n 6.5개\n 7개\n 7.5개\n 8개\n 8.5개\n 9개\n 9.5개\n 10개\n"
                                        + "0.5조각\n 1조각\n 1.5조각\n 2조각\n 2.5조각\n 3조각\n 3.5조각\n 4조각\n 4.5조각\n 5조각\n 5.5조각\n 6조각\n 6.5조각\n 7조각\n 7.5조각\n 8조각\n 8.5조각\n 9조각\n 9.5조각\n 10조각\n"
                                        + "0.5인분\n 1인분\n 1.5\n 2\n 2.5\n 3\n 3.5\n 4\n 4.5\n 5\n 5.5\n 6인분\n 6.5인분\n 7인분\n 7.5인분\n 8인분\n 8.5인분\n 9인분\n 9.5인분\n 10인분\n"
                        );
                client = builder.build();
                client.setSpeechRecognizeListener(this);
                client.startRecording(true);
                Toast.makeText(getActivity(), "음성인식을 시작합니다.", Toast.LENGTH_SHORT).show();
                btnVoice.setEnabled(false);
            }
        }
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
                System.out.println("에러코드 메시지: "+errorMsg);
                Toast.makeText(getActivity(), "음성인식이 잘못되었습니다.\n다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                voiceReset();
                btnVoice.setEnabled(true);
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
        System.out.println("/"+str+"/");
        StringTokenizer strToken = new StringTokenizer(str," ");
        try{
            voice_food_kind = strToken.nextToken();
            voice_food_amount = strToken.nextToken();
            food2 = voice_food_amount;
            for(int i=0; strToken.hasMoreElements(); i++){
                voice_food_amount = voice_food_amount + strToken.nextToken();
            }
            voice_food_amount = rePlace(voice_food_amount);
        }catch(Exception a){
            Toast.makeText(getActivity(), "Exception) "+ str + /////
                    "\n음성인식이 잘못되었습니다.\n다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
            voiceReset();
            btnVoice.setEnabled(true);
            return;
        }
        int x = binaryStringSearch(item_kind, voice_food_kind);
        if(voice_food_amount.length() == 0 || voice_food_amount.equals("0")){
            Toast.makeText(getActivity(), "음식의 양을 다시 말해주세요!", Toast.LENGTH_SHORT).show();//
            voiceReset();
            btnVoice.setEnabled(true);
            return;
        }
        if(x == -1){
            int y = getFood(voice_food_kind+food2);
            if(y == -1) {
                Toast.makeText(getActivity(), "목록상에 없는 음식입니다!", Toast.LENGTH_SHORT).show();
                voiceReset();
                btnVoice.setEnabled(true);
                return;
            }
            x = y;
        }
        StringTokenizer stringTokenizerToken = new StringTokenizer(item_kind[x]," ");
        voice_food_kind = stringTokenizerToken.nextToken();
        String unit = stringTokenizerToken.nextToken().replace("(", "");
        unit = unit.replace(")", "");
        tv_voice_result.setText(voice_food_kind + "/" + voice_food_amount+unit);
        btnVoice.setEnabled(true);
    }

    public static int binaryStringSearch(String[] strArr, String str) {

        int low = 2;
        int high = strArr.length -1;
        int result = -1;
        str.replace("북엇", "북어");
        if(str.equals("물")) return 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            if (new StringTokenizer(strArr[mid]," ").nextToken().equals(str)) {
                result = mid;
                return result;
            }else if (new StringTokenizer(strArr[mid]," ").nextToken().compareTo(str) < 0) {
                low = mid + 1;
            }else {
                high = mid - 1;
            }
        }
        return result;
    }

    public String rePlace(String str){
        float num = 0;
        str = str.replace("반","/0.5/");
        str = str.replace("방","/0.5/");
        str = str.replace("한","/1/");
        str = str.replace("일","/1/");
        str = str.replace("두","/2/");
        str = str.replace("세","/3/");
        str = str.replace("네","/4/");
        str = str.replace("내","/4/");
        str = str.replace("다섯","/5/");
        str = str.replace("열","/10/");
        str = str.replaceAll("[^0123456789./]","");
        StringTokenizer strToken = new StringTokenizer(str,"/");
        for(int i=0; strToken.hasMoreElements(); i++){
            num = num + Float.parseFloat(strToken.nextToken());
        }
        return new BigDecimal(Float.toString(num)).stripTrailingZeros().toPlainString();
    }
    public void voiceReset(){
        voice_food_kind = "(음식 종류)";
        voice_food_amount = "(양)";
        tv_voice_result.setText(voice_food_kind +"/" + voice_food_amount);
    }
    public int getFood(String food){
        int result = -1;
        for(int i=1; i<item_kind.length; i++){
            if( food.charAt(0) != item_kind[i].charAt(0)) {
                continue;
            }
            if(food.length() == 1){
                result = i;
                break;
            }
            if(food.charAt(1) == item_kind[i].charAt(1)){
                String str = new StringTokenizer(item_kind[i]," ").nextToken();
                if(str.equals("김치볶음")||str.equals("김치전")||str.equals("김치찌개")||str.equals("삶은감자")||str.equals("삶은달걀")||str.equals("치킨샐러드")){
                    if(food.charAt(2)==item_kind[i].charAt(2)){
                        result = i;
                        break;
                    }
                    else continue;
                }
                else {
                    result = i;
                    break;
                }
            }
        }
        return result;
    }

    public int getDate(){
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int date = year*10000 + month*100 + day;
        return date;
    }

    @Override
    public void onAudioLevel(float audioLevel) {
    }

    @Override
    public void onFinished() {
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

}