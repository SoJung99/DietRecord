package org.techtown.dietrecord;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class TabFragment2 extends Fragment {
    String food_time;  // 선택된 식사 시간대
    String food_kind;  // 선택된 음식 종류
    String food_amount;// 선택된 음식 양

    String voice_food_time = "(식사)";      // 음성인식 식사 시간대
    String voice_food_kind = "(음식 종류)"; // 음성인식 음식 종류
    String voice_food_amount = "(양)";     // 음성인식 음식 양
    //voice_food_unit

    Spinner spinner; // 식사 시간대 선택 박스
    Spinner spinner2;// 음식 종류 선택 박스
    Spinner spinner3;// 음식 양 선택 박스

    ArrayList<UserFood> LIST = new ArrayList<>();
    ArrayList<UserFood> LIST2 = new ArrayList<>();
    ArrayList<UserFood> LIST3 = new ArrayList<>();

    TextView tv_voice_result;
    TextView sumC;
    TextView sumC1;
    TextView sumC2;
    TextView sumC3;

    recyclerAdapter recyclerADAPTER = new recyclerAdapter(LIST);
    recyclerAdapter recyclerADAPTER2 = new recyclerAdapter(LIST2);
    recyclerAdapter recyclerADAPTER3 = new recyclerAdapter(LIST3);

    RecyclerView recyclerView;
    RecyclerView recyclerView2;
    RecyclerView recyclerView3;

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

        // 오늘 것만 가져오기
        LIST = mDbHelper.getBreakfast();
        LIST2 = mDbHelper.getLunch();
        LIST3 = mDbHelper.getDinner();

        // db 닫기
        //mDbHelper.close();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment2, container, false);


        tv_voice_result = (TextView)v.findViewById(R.id.textView_voice);
        sumC = (TextView)v.findViewById(R.id.sumCal);
        sumC1 = (TextView)v.findViewById(R.id.sumCal_breakfast);
        sumC2 = (TextView)v.findViewById(R.id.sumCal_lunch);
        sumC3 = (TextView)v.findViewById(R.id.sumCal_dinner);

        initLoadDB();

        recyclerADAPTER = new recyclerAdapter(LIST);
        recyclerView = v.findViewById(R.id.my_recycler_view);
        int numberOfColumns = 2;
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), numberOfColumns));
        recyclerView.setAdapter(recyclerADAPTER);
        recyclerADAPTER.notifyDataSetChanged();

        recyclerADAPTER2 = new recyclerAdapter(LIST2);
        recyclerView2 = v.findViewById(R.id.my_recycler_view2);
        recyclerView2.setLayoutManager(new GridLayoutManager(getActivity(), numberOfColumns));
        recyclerView2.setAdapter(recyclerADAPTER2);
        recyclerADAPTER2.notifyDataSetChanged();

        recyclerADAPTER3 = new recyclerAdapter(LIST3);
        recyclerView3 = v.findViewById(R.id.my_recycler_view3);
        recyclerView3.setLayoutManager(new GridLayoutManager(getActivity(), numberOfColumns));
        recyclerView3.setAdapter(recyclerADAPTER3);
        recyclerADAPTER3.notifyDataSetChanged();

        sumC.setText((recyclerADAPTER.SumCalories(LIST)+recyclerADAPTER2.SumCalories(LIST2)+recyclerADAPTER3.SumCalories(LIST3))+"kcal");
        sumC1.setText(recyclerADAPTER.SumCalories(LIST)+"kcal");
        sumC2.setText(recyclerADAPTER2.SumCalories(LIST2)+"kcal");
        sumC3.setText(recyclerADAPTER3.SumCalories(LIST3)+"kcal");

        // 음성인식 버튼 (btn_voice 눌렸을 때)
        Button btnVoice = (Button)v.findViewById(R.id.btn_voice);
        btnVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String food_voiceExample = "아침 사과 1"; //////////
                String food_voiceExample = "점심 수박 1.5"; //////////
                //String food_voiceExample = "저녁 된장찌개 0.5"; //////////
                String[] food_voiceExampleResult = food_voiceExample.split(" ", 3);
                voice_food_time = food_voiceExampleResult[0];
                voice_food_kind = food_voiceExampleResult[1];
                voice_food_amount = food_voiceExampleResult[2];

                String str = null;
                tv_voice_result.setText(voice_food_time + "/" +voice_food_kind +"/" + voice_food_amount);
                // + ...voice_food_amount + voice_food_unit);  ////////
                // + 하나라도 null이면 다시 시도해주세요.
                // + 아침, 점심, 저녁이 아니거나, amount가 real이 아니거나. 잘못된 입력값. 다시 시도해주세요.
            }
        });
        // 추가(음성인식) 버튼 (btn_voiceAdd 눌렸을 때)
        Button btnVoiceAdd = (Button)v.findViewById(R.id.btn_voiceAdd);
        btnVoiceAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(voice_food_time.equals("(식사)") || voice_food_kind.equals("(음식 종류)") || voice_food_amount.equals("(양)")){
                    Toast.makeText(getActivity(), "음성인식을 (다시) 시도해 주세요!", Toast.LENGTH_SHORT).show();
                }
                // + if (voic_food_unit, 단위랑 not equals) voice_food_kind의 단위는 뭐뭐입니다. void_food_amount+단위 로 기록하겠습니까? 네, 아니오
                else{
                    //Cursor cursor = database.rawQuery("SELECT * FROM 사용자식단", null);
                    //int id = cursor.getCount() + 1;
                    Cursor cur = database.rawQuery("SELECT * FROM 음식정보 WHERE 음식구분='"+voice_food_kind+"'", null);
                    if(cur==null){
                        Toast.makeText(getActivity(), "목록상에 없는 음식입니다!", Toast.LENGTH_SHORT).show();
                    }

                    else{ //DB, 리스트에 넣기
                        cur.moveToFirst();
                        Float cal = Float.parseFloat(voice_food_amount) * cur.getFloat(1);
                        Float A = cur.getFloat(2);
                        Float B = cur.getFloat(3);
                        Float C = cur.getFloat(4);
                        String unit = cur.getString(5);

                        String sql1 = "INSERT INTO 사용자식단 (식사, 음식구분, 양, 단위, 칼로리, 탄수화물, 단백질, 지방, 날짜) ";
                        String sql2 = "VALUES ('"+voice_food_time+"', '"+voice_food_kind+"', "+Float.parseFloat(voice_food_amount)+", '"+unit+"', "+cal+", "+A+", "+B+", "+C+", "+getDate()+")";
                        database.execSQL(sql1+sql2);

                        Cursor CURSOR = database.rawQuery("SELECT * FROM 사용자식단 ORDER BY ROWID DESC LIMIT 1", null);
                        //Cursor CURSOR = database.rawQuery("SELECT * FROM 사용자식단 WHERE rowid", null);
                        CURSOR.moveToFirst(); //이걸 해줘야 하네..안그럼 꺼지넹......
                        //Toast.makeText(MainActivity.this, ""+CURSOR.getInt(0), Toast.LENGTH_SHORT).show();
                        int id = CURSOR.getInt(0);
                        UserFood userFood = new UserFood(id, voice_food_kind, Float.parseFloat(voice_food_amount), unit, cal);
                        //UserFood userFood = new UserFood(0, voice_food_kind, Float.parseFloat(voice_food_amount), unit, cal);
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
                    }

                    voice_food_time = "(식사)";
                    voice_food_kind = "(음식 종류)";
                    voice_food_amount = "(양)";
                    //tv_result_voice = (TextView)findViewById(R.id.textView_voice);
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
                    //Cursor cursor = database.rawQuery("SELECT * FROM 사용자식단", null);
                    //int id = cursor.getCount() + 1;
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
                    //Toast.makeText(MainActivity.this, ""+CURSOR.getInt(0), Toast.LENGTH_SHORT).show();
                    int id = CURSOR.getInt(0);

                    //Cursor CURSOR = database.rawQuery("SELECT * FROM 사용자식단 ORDER BY num DESC LIMIT 1", null);
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
                        //.setMessage("내용 넣기")
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
                        .setMessage("※ '식사_음식 종류_양(컵/인분/개/조각)' 순으로 말해주세요.\n※ 음식별 단위는 '음식별 정보 보기' 버튼을 눌러 참고하세요.\n예) 저녁 떡볶이 1인분\n예) 아침 사과 1조각")
                        .setNeutralButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { }})
                        .show();
            }
        });
        // 식사 시간대 선택 박스
        spinner = (Spinner)v.findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { //아이템 클릭했을 때
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                food_time = spinner.getSelectedItem().toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        // 음식 종류 선택 박스
        spinner2 = (Spinner)v.findViewById(R.id.spinner2);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                food_kind = spinner2.getSelectedItem().toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        // 음식 양 선택 박스
        spinner3 = (Spinner)v.findViewById(R.id.spinner3);
        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                food_amount = spinner3.getSelectedItem().toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });


        return v;
    }


    public int getDate(){
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int date = year*10000 + month*100 + day;
        //Toast.makeText(MainActivity.this, "날짜: " + date, Toast.LENGTH_SHORT).show();
        return date;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

}