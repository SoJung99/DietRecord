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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.KakaoAdapter;
import com.kakao.auth.KakaoSDK;
import com.kakao.sdk.newtoneapi.SpeechRecognizerManager;
import com.kakao.sdk.newtoneapi.SpeechRecognizeListener;
import com.kakao.sdk.newtoneapi.SpeechRecognizerClient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class TabFragment2 extends Fragment implements View.OnClickListener, SpeechRecognizeListener{
    String food_time;  // 선택된 식사 시간대
    String food_kind;  // 선택된 음식 종류
    String food_amount;// 선택된 음식 양

    Button btnVoice;
    TextView tv_voice_result;

    String voice_food_time = "(식사)";      // 음성인식 식사 시간대
    String voice_food_kind = "(음식 종류)"; // 음성인식 음식 종류
    String voice_food_amount = "(양)";     // 음성인식 음식 양
    String voice_food_unit = null;          // 단위
    Cursor voice_cursor = null;

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

        btnVoice = (Button)v.findViewById(R.id.btn_voice);
        btnVoice.setOnClickListener(this);
        tv_voice_result = (TextView)v.findViewById(R.id.textView_voice);
        TextView sumC = (TextView)v.findViewById(R.id.sumCal);
        TextView sumC1 = (TextView)v.findViewById(R.id.sumCal_breakfast);
        TextView sumC2 = (TextView)v.findViewById(R.id.sumCal_lunch);
        TextView sumC3 = (TextView)v.findViewById(R.id.sumCal_dinner);

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
                if(voice_cursor == null){
                    Toast.makeText(getActivity(), "음성인식을 (다시) 시도해 주세요!", Toast.LENGTH_SHORT).show();
                }
                else{ //DB, 리스트에 넣기
                    voice_cursor.moveToFirst();
                    Float cal = Float.parseFloat(voice_food_amount) * voice_cursor.getFloat(1);
                    Float A = voice_cursor.getFloat(2);
                    Float B = voice_cursor.getFloat(3);
                    Float C = voice_cursor.getFloat(4);
                    String sql1 = "INSERT INTO 사용자식단 (식사, 음식구분, 양, 단위, 칼로리, 탄수화물, 단백질, 지방, 날짜) ";
                    String sql2 = "VALUES ('"+voice_food_time+"', '"+voice_food_kind+"', "+Float.parseFloat(voice_food_amount)+", '"+voice_food_unit+"', "+cal+", "+A+", "+B+", "+C+", "+getDate()+")";
                    database.execSQL(sql1+sql2);

                    Cursor CURSOR = database.rawQuery("SELECT * FROM 사용자식단 ORDER BY ROWID DESC LIMIT 1", null);
                    CURSOR.moveToFirst();
                    int id = CURSOR.getInt(0);
                    UserFood userFood = new UserFood(id, voice_food_kind, Float.parseFloat(voice_food_amount), voice_food_unit, cal);
                    if(voice_food_time.equals("아침")){ // 아침 리스트에 추가
                        recyclerADAPTER.addItem(userFood);
                        recyclerADAPTER.notifyDataSetChanged();
                        sumC1.setText(recyclerADAPTER.SumCalories(LIST)+"kcal");
                    }
                    else if(voice_food_time.equals("점심")){ // 점심 리스트에 추가
                        recyclerADAPTER2.addItem(userFood);
                        recyclerADAPTER2.notifyDataSetChanged();
                        sumC2.setText(recyclerADAPTER2.SumCalories(LIST2)+"kcal");
                    }
                    else if(voice_food_time.equals("저녁")){ // 저녁 리스트에 추가
                        recyclerADAPTER3.addItem(userFood);
                        recyclerADAPTER3.notifyDataSetChanged();
                        sumC3.setText(recyclerADAPTER3.SumCalories(LIST3)+"kcal");
                    }
                    sumC.setText((recyclerADAPTER.SumCalories(LIST)+recyclerADAPTER2.SumCalories(LIST2)+recyclerADAPTER3.SumCalories(LIST3))+"kcal");

                    voiceReset();
                    tv_voice_result.setText(voice_food_time + "/" + voice_food_kind +"/" + voice_food_amount);
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
                if(food_time.equals("(식사)")){
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
                        .setMessage("※ '식사_음식 종류_양(컵/개/인분/조각)' 순으로 말해주세요.\n※ 음식별 단위는 '음식별 정보 보기' 버튼을 눌러 참고하세요.\n\n예) 저녁 떡볶이 1인분\n예) 아침 사과 1개\n예) 점심 고등어구이 1.5")
                        .setNeutralButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { }})
                        .show();
            }
        });
        // 식사 시간대 선택 박스
        Spinner spinner = (Spinner)v.findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { //아이템 클릭했을 때
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                food_time = spinner.getSelectedItem().toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
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
            SpeechRecognizerClient.Builder builder = new SpeechRecognizerClient.Builder().
                    setServiceType(serviceType).
                    setUserDictionary("아침 물\n아침 갈비탕\n아침 갈치구이\n아침 감자샐러드\n아침 고등어구이\n아침 곤약\n아침 군고구마\n아침 군만두\n아침 귤\n아침 김치볶음\n아침 김치전\n아침 김치찌개\n아침 깍두기\n"+
                            "아침 다시마\n아침 달걀프라이\n아침 닭가슴살\n아침 당근\n아침 도넛\n아침 돼지불고기\n아침 된장찌개\n아침 두부\n아침 딸기\n아침 떡국\n아침 떡볶이\n아침 라면\n아침 멸치볶음\n아침 물냉면\n아침 바게트\n아침 바나나\n아침 밥\n아침 배\n아침 배추김치\n아침 백김치\n아침 백설기\n아침 베이글\n아침 북어국\n아침 브로콜리\n아침 비빔냉면\n"+
                            "아침 사과\n아침 삶은감자\n아침 삶은달걀\n아침 삼계탕\n아침 샐러리\n아침 생크림케이크\n아침 설렁탕\n아침 소갈비찜\n아침 소고기등심\n아침 수박\n아침 스파게티\n아침 시금치나물\n아침 시리얼\n아침 식빵\n아침 아몬드\n아침 아보카도\n아침 야채죽\n아침 양배추찜\n아침 양상추\n아침 연어\n아침 오이\n아침 요거트\n아침 우동\n"+
                            "아침 잡채\n아침 장조림\n아침 짜장면\n아침 짬뽕\n아침 찐단호박\n아침 초콜릿케이크\n아침 치즈버거\n아침 치킨\n아침 치킨샐러드\n아침 칼국수\n아침 콘샐러드\n아침 콩나물국\n아침 탕수육\n아침 토마토\n아침 파전\n아침 파프리카\n아침 햄버거\n아침 호밀빵\n"+
                            "점심 물\n점심 갈비탕\n점심 갈치구이\n점심 감자샐러드\n점심 고등어구이\n점심 곤약\n점심 군고구마\n점심 군만두\n점심 귤\n점심 김치볶음\n점심 김치전\n점심 김치찌개\n점심 깍두기\n"+
                            "점심 다시마\n점심 달걀프라이\n점심 닭가슴살\n점심 당근\n점심 도넛\n점심 돼지불고기\n점심 된장찌개\n점심 두부\n점심 딸기\n점심 떡국\n점심 떡볶이\n점심 라면\n점심 멸치볶음\n점심 물냉면\n점심 바게트\n점심 바나나\n점심 밥\n점심 배\n점심 배추김치\n점심 백김치\n점심 백설기\n점심 베이글\n점심 북어국\n점심 브로콜리\n점심 비빔냉면\n"+
                            "점심 사과\n점심 삶은감자\n점심 삶은달걀\n점심 삼계탕\n점심 샐러리\n점심 생크림케이크\n점심 설렁탕\n점심 소갈비찜\n점심 소고기등심\n점심 수박\n점심 스파게티\n점심 시금치나물\n점심 시리얼\n점심 식빵\n점심 아몬드\n점심 아보카도\n점심 야채죽\n점심 양배추찜\n점심 양상추\n점심 연어\n점심 오이\n점심 요거트\n점심 우동\n"+
                            "점심 잡채\n점심 장조림\n점심 짜장면\n점심 짬뽕\n점심 찐단호박\n점심 초콜릿케이크\n점심 치즈버거\n점심 치킨\n점심 치킨샐러드\n점심 칼국수\n점심 콘샐러드\n점심 콩나물국\n점심 탕수육\n점심 토마토\n점심 파전\n점심 파프리카\n점심 햄버거\n점심 호밀빵\n"+
                            "저녁 물\n저녁 갈비탕\n저녁 갈치구이\n저녁 감자샐러드\n저녁 고등어구이\n저녁 곤약\n저녁 군고구마\n저녁 군만두\n저녁 귤\n저녁 김치볶음\n저녁 김치전\n저녁 김치찌개\n저녁 깍두기\n"+
                            "저녁 다시마\n저녁 달걀프라이\n저녁 닭가슴살\n저녁 당근\n저녁 도넛\n저녁 돼지불고기\n저녁 된장찌개\n저녁 두부\n저녁 딸기\n저녁 떡국\n저녁 떡볶이\n저녁 라면\n저녁 멸치볶음\n저녁 물냉면\n저녁 바게트\n저녁 바나나\n저녁 밥\n저녁 배\n저녁 배추김치\n저녁 백김치\n저녁 백설기\n저녁 베이글\n저녁 북어국\n저녁 브로콜리\n저녁 비빔냉면\n"+
                            "저녁 사과\n저녁 삶은감자\n저녁 삶은달걀\n저녁 삼계탕\n저녁 샐러리\n저녁 생크림케이크\n저녁 설렁탕\n저녁 소갈비찜\n저녁 소고기등심\n저녁 수박\n저녁 스파게티\n저녁 시금치나물\n저녁 시리얼\n저녁 식빵\n저녁 아몬드\n저녁 아보카도\n저녁 야채죽\n저녁 양배추찜\n저녁 양상추\n저녁 연어\n저녁 오이\n저녁 요거트\n저녁 우동\n"+
                            "저녁 잡채\n저녁 장조림\n저녁 짜장면\n저녁 짬뽕\n저녁 찐단호박\n저녁 초콜릿케이크\n저녁 치즈버거\n저녁 치킨\n저녁 치킨샐러드\n저녁 칼국수\n저녁 콘샐러드\n저녁 콩나물국\n저녁 탕수육\n저녁 토마토\n저녁 파전\n저녁 파프리카\n저녁 햄버거\n저녁 호밀빵\n"+
                            "컵\n개\n조각\n인분\n"
                            //"0.5컵\n 1컵\n 1.5컵\n 2컵\n 2.5컵\n 3컵\n 3.5컵\n 4컵\n 4.5컵\n 5컵\n 5.5컵\n 6컵\n 6.5컵\n 7컵\n 7.5컵\n 8컵\n 8.5컵\n 9컵\n 9.5컵\n 10컵\n"+
                            //"0.5개\n 1개\n 1.5개\n 2개\n 2.5개\n 3개\n 3.5개\n 4개\n 4.5개\n 5개\n 5.5개\n 6개\n 6.5개\n 7개\n 7.5개\n 8개\n 8.5개\n 9개\n 9.5개\n 10개\n"+
                            //"0.5조각\n 1조각\n 1.5조각\n 2조각\n 2.5조각\n 3조각\n 3.5조각\n 4조각\n 4.5조각\n 5조각\n 5.5조각\n 6조각\n 6.5조각\n 7조각\n 7.5조각\n 8조각\n 8.5조각\n 9조각\n 9.5조각\n 10조각\n"+
                            //"0.5인분\n 1인분\n 1.5\n 2\n 2.5\n 3\n 3.5\n 4\n 4.5\n 5\n 5.5\n 6인분\n 6.5인분\n 7인분\n 7.5인분\n 8인분\n 8.5인분\n 9인분\n 9.5인분\n 10인분"
                            );
            client = builder.build();
            client.setSpeechRecognizeListener(this);
            client.startRecording(true);
            Toast.makeText(getActivity(), "음성인식을 시작합니다.", Toast.LENGTH_SHORT).show();
            btnVoice.setEnabled(false);
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
        StringTokenizer strToken = new StringTokenizer(str," ");
        String string = str.replace(" ","/");////////////////////////////////////////////////////////////////
        try{
            voice_food_time = strToken.nextToken();
            voice_food_kind = strToken.nextToken();
            voice_food_amount = strToken.nextToken();
            Toast.makeText(getActivity(), "/"+string+"/", Toast.LENGTH_SHORT).show();/////////////
            //Toast.makeText(getActivity(), "/"+voice_food_time + "/" +voice_food_kind +"/" + voice_food_amount+"/", Toast.LENGTH_SHORT).show();/////////////
            voice_food_amount = voice_food_amount.replace("반","0.5");
            voice_food_amount = voice_food_amount.replace("한","1");
            voice_food_amount = voice_food_amount.replace("두","2");
            voice_food_amount = voice_food_amount.replace("세","3");
            voice_food_amount = voice_food_amount.replaceAll("[^0123456789.]","");
            //Toast.makeText(getActivity(), "/"+voice_food_amount+"/", Toast.LENGTH_SHORT).show();///////////////////////////////////
        }catch(Exception a){
            Toast.makeText(getActivity(), "Exception)\n음성인식이 잘못되었습니다.\n다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
            voiceReset();
            btnVoice.setEnabled(true);
            return;
        }
        if(!voice_food_time.equals("아침") && !voice_food_time.equals("점심") && !voice_food_time.equals("저녁")){
            Toast.makeText(getActivity(), "식사는 아침, 점심, 저녁 중에서 선택하여 말해주세요.", Toast.LENGTH_SHORT).show();//////////////////////////////
            voiceReset();
            btnVoice.setEnabled(true);
            return;
        }
        else if(!isNumber(voice_food_amount)){
            Toast.makeText(getActivity(), "음식의 양을 다시 말해주세요.", Toast.LENGTH_SHORT).show();///////////////////////////////////
            voiceReset();
            btnVoice.setEnabled(true);
            return;
        }
        else {
            voice_cursor = database.rawQuery("SELECT * FROM 음식정보 WHERE 음식구분='"+voice_food_kind+"'", null);
            if(voice_cursor==null){
                Toast.makeText(getActivity(), "목록상에 없는 음식입니다!", Toast.LENGTH_SHORT).show();
                voiceReset();
                btnVoice.setEnabled(true);
                return;
            }
            else {
                voice_cursor.moveToFirst();
                voice_food_unit = voice_cursor.getString(5);
                tv_voice_result.setText(voice_food_time + "/" + voice_food_kind + "/" + voice_food_amount + voice_food_unit);
                btnVoice.setEnabled(true);
            }
        }

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
                if(c==46) continue;// 점(.)은 46
                // 숫자가 아니라면
                if (c < 48 || c > 57) {
                    result = false;
                }
            }
        }
        return result;
    }

    public void voiceReset(){
        voice_food_time = "(식사)";
        voice_food_kind = "(음식 종류)";
        voice_food_amount = "(양)";
        voice_food_unit = null;
        voice_cursor = null;
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