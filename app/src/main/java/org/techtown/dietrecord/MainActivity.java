package org.techtown.dietrecord;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ArrayList<ExerciseData> list;
    DataAdapter mDbHelper;
    DataBaseHelper dbHelper;
    SQLiteDatabase database;

    public void intitLoadDB(){
        mDbHelper = new DataAdapter(this.getApplicationContext());
        mDbHelper.createDatabase();
        mDbHelper.open();

        dbHelper = new DataBaseHelper(this.getApplicationContext());
        dbHelper.openDataBase();
        dbHelper.close();
        database = dbHelper.getWritableDatabase();

        list = mDbHelper.getTableData();

        // mDbHelper.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ViewPager pager = findViewById(R.id.main_viewPager);
        TabLayout tabLayout = findViewById(R.id.main_tablayout);

        // 앱 최초 실행시 작업
        SharedPreferences pref = getSharedPreferences("isFirst", Activity.MODE_PRIVATE);
        boolean first = pref.getBoolean("isFirst", false);
        if(first==false){
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("isFirst",true);
            editor.commit();

            intitLoadDB();

            SimpleDateFormat format = new SimpleDateFormat("YYYY MM dd HH:mm:ss", Locale.UK);
            Calendar calendar = Calendar.getInstance();
            String date = format.format(calendar.getTime());
            String time = date.substring(0, 10).replaceAll(" ", "");
            int today = Integer.parseInt(time);

            String sql = "INSERT INTO 사용자정보 (날짜, 성별, 나이, 키, 몸무게, 소모칼로리, 섭취칼로리) VALUES ("+today+", '"+'-'+"', "+0+", "+0+", "+0+", "+0+", "+0+")";
            database.execSQL(sql);
        }


        tabLayout.getChildAt(0).setBackgroundColor(Color.parseColor("#ff8080")); // 배경색
        pager.setOffscreenPageLimit(2); //현재 페이지의 양쪽에 보유해야하는 페이지 수를 설정 (상황에 맞게 사용하시면 됩니다.)
        tabLayout.setupWithViewPager(pager); //텝레이아웃과 뷰페이저를 연결
        pager.setAdapter(new PageAdapter(getSupportFragmentManager(),this)); //뷰페이저 어뎁터 설정 연결

    }


    static class PageAdapter extends FragmentStatePagerAdapter { //뷰 페이저 어뎁터


        PageAdapter(FragmentManager fm, Context context) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        }


        @NonNull
        @Override
        public Fragment getItem(int position) {
            if (position == 0) { //프래그먼트 사용 포지션 설정 0 이 첫탭
                return new TabFragment1();
            } else if (position == 1){
                return new TabFragment2();
            } else if (position == 2){
                return new TabFragment3();
            } else if (position == 3){
                return new TabFragment4();
            } else{
                return null;
            }

        }


        @Override
        public int getCount() {
            return 4;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) { //텝 레이아웃의 타이틀 설정
                return "상태";
            } else if(position == 1){
                return "식단";
            } else if(position == 2){
                return "운동";
            } else if(position == 3){
                return "통계";
            } else {
                return null;
            }
        }
    }

}