package org.techtown.dietrecord;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static androidx.viewpager.widget.PagerAdapter.POSITION_NONE;

@RequiresApi(api = Build.VERSION_CODES.O)
public class TabFragment1 extends Fragment {

    private SignInButton signInButton; // 구글 로그인 버튼
    private GoogleSignInClient mGoogleSignInClient;
    private String TAG="mainTag";
    private FirebaseAuth mAuth;
    private int RC_SIGN_IN=123;
    Value wr; // 7이면 걷기 8이면 달리기
    int flag = 0;

    long laststartTime, lastendTime; // 구글핏의 마지막 데이터 시간

    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;

    // DB
    ArrayList<ExerciseData> list;
    DataAdapter mDbHelper;
    DataBaseHelper dbHelper;
    SQLiteDatabase database;

    public void intitLoadDB(){
        mDbHelper = new DataAdapter(getActivity().getApplicationContext());
        mDbHelper.createDatabase();
        mDbHelper.open();

        dbHelper = new DataBaseHelper(getActivity().getApplicationContext());
        dbHelper.openDataBase();
        dbHelper.close();
        database = dbHelper.getWritableDatabase();

        list = mDbHelper.getTableData();

        // mDbHelper.close();
    }

    final String[] gender = new String[]{"남자", "여자"};
    int cal_recom_take , Car_entire , Pro_entire , Fat_entire , water_recom = 2000; // 권장
    float cal_present_take , cal_present_burning , Car_present , Pro_present , Fat_present , water_present ; // 실제

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment1, container, false);

        intitLoadDB();

        Button btn1 = v.findViewById(R.id.button1);
        Button btn2 = v.findViewById(R.id.button2);
        Button btn3 = v.findViewById(R.id.button3);
        Button btn4 = v.findViewById(R.id.button4);

        ProgressBar pb1 = v.findViewById(R.id.progressBar1);
        ProgressBar pb3 = v.findViewById(R.id.progressBar3);
        ProgressBar pb4 = v.findViewById(R.id.progressBar4);
        ProgressBar pb5 = v.findViewById(R.id.progressBar5);
        ProgressBar pb6 = v.findViewById(R.id.progressBar6);

        TextView tv1 = v.findViewById(R.id.textView1);
        TextView tv3 = v.findViewById(R.id.textView3);
        TextView tv4 = v.findViewById(R.id.textView4);
        TextView tv5 = v.findViewById(R.id.textView5);
        TextView tv6 = v.findViewById(R.id.textView6);
        TextView tv8 = v.findViewById(R.id.textView8);
        TextView tv9 = v.findViewById(R.id.textView9);
        TextView tv10 = v.findViewById(R.id.textView10);
        TextView tv11 = v.findViewById(R.id.textView11);
        TextView tv12 = v.findViewById(R.id.textView12);
        TextView tv13 = v.findViewById(R.id.textView13);

        // 구글 로그인 버튼
        signInButton = v.findViewById(R.id.SignIn_Button);

        // tv1
        Cursor cur_date = database.rawQuery("select 날짜 from 사용자정보 ", null);
        cur_date.moveToNext();
        int a_date = cur_date.getInt(0);
        String before = Integer.toString(a_date);

        try {
            Date d1 = new SimpleDateFormat("yyyyMMdd").parse(before);
            Date d2 = new Date();
            long diffDay = (d2.getTime() - d1.getTime()) / (24*60*60*1000);
            diffDay++;
            tv1.setText("다이어트 " + diffDay +"일차!");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat format = new SimpleDateFormat("YYYY MM dd HH:mm:ss", Locale.UK);
        Calendar calendar = Calendar.getInstance();
        String date = format.format(calendar.getTime());
        String time = date.substring(0, 10).replaceAll(" ", "");
        int today = Integer.parseInt(time);

        Cursor cur = database.rawQuery("SELECT 성별 FROM 사용자정보 WHERE 날짜 = " + today, null);
        cur.moveToNext();
        String n = cur.getString(0);
        tv3.setText("  성별 : " + n);

        cur = database.rawQuery("SELECT 나이 FROM 사용자정보 WHERE 날짜 = " + today, null);
        cur.moveToNext();
        int m = cur.getInt(0);
        tv4.setText("  나이 : " + m);

        cur = database.rawQuery("SELECT 키 FROM 사용자정보 WHERE 날짜 = " + today, null);
        cur.moveToNext();
        float h = cur.getFloat(0);
        tv5.setText("  키 : " + h);

        cur = database.rawQuery("SELECT 몸무게 FROM 사용자정보 WHERE 날짜 = " + today, null);
        cur.moveToNext();
        float w = cur.getFloat(0);
        tv6.setText("  몸무게 : " + w);


        Cursor cur_t = database.rawQuery("SELECT sum(칼로리) FROM 사용자식단 WHERE 날짜 = " + today , null);
        cur_t.moveToNext();
        float sum = cur_t.getFloat(0);
        String sql = "UPDATE 사용자정보 SET 섭취칼로리 = " + sum + " WHERE 날짜 = " + today;
        database.execSQL(sql);

        Cursor cur_b = database.rawQuery("SELECT sum(칼로리) FROM 사용자운동 WHERE 날짜 = " + today, null);
        cur_b.moveToNext();
        sum = cur_b.getFloat(0);
        sql = "UPDATE 사용자정보 SET 소모칼로리 = " + sum + " WHERE 날짜 = " + today;
        database.execSQL(sql);

        Handler handler1 = new Handler();
        Handler handler2 = new Handler();

        Thread th1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    // 해리스 베네딕트 하루 권장 칼로리 계산
                    if(n.equals("남자")){
                        cal_recom_take = (int) ((66.47 + (13.75 * w) + (5 * h) - (6.76 * m)) * 1.375 - 500);
                    }else{
                        cal_recom_take = (int) ((655.1 + (9.56 * w) + (1.85 * h) - (4.68 * m)) * 1.375 - 500);
                    }

                    Cursor cur1 = database.rawQuery("SELECT sum(칼로리) FROM 사용자식단 WHERE 날짜 = " + today, null);
                    cur1.moveToNext();
                    cal_present_take = cur1.getFloat(0);

                    int int_cal_present_take = (int)cal_present_take;

                    Cursor cur_c = database.rawQuery("select sum(탄수화물) from 사용자식단 where 날짜 = " + today, null);
                    cur_c.moveToNext();
                    Car_present = cur_c.getFloat(0);
                    int int_car_present = (int)Car_present;

                    Cursor cur_p = database.rawQuery("select sum(단백질) from 사용자식단 where 날짜 = " + today, null);
                    cur_p.moveToNext();
                    Pro_present = cur_p.getFloat(0);
                    int int_pro_present = (int)Pro_present;

                    Cursor cur_f = database.rawQuery("select sum(지방) from 사용자식단 where 날짜 = " + today, null);
                    cur_f.moveToNext();
                    Fat_present = cur_f.getFloat(0);
                    int int_fat_present = (int)Fat_present;

                    Cursor cur_w = database.rawQuery("SELECT sum(양) FROM 사용자식단 WHERE 음식구분 = '물' AND 날짜 = " + today, null);
                    cur_w.moveToNext();
                    water_present = 200 * cur_w.getFloat(0);

                    tv12.setText("   하루 권장 섭취 수분 : " + water_recom);
                    tv13.setText("   현재 섭취 수분 : "  + water_present);

                    int int_water_present = (int)water_present;

                    Cursor cur2 = database.rawQuery("SELECT sum(칼로리) FROM 사용자운동 WHERE 날짜 = " + today, null);
                    cur2.moveToNext();
                    cal_present_burning = cur2.getFloat(0);

                    handler1.post(new Runnable() {
                        @Override
                        public void run() {
                            pb1.setMax(cal_recom_take);
                            pb1.setProgress(int_cal_present_take);

                            tv8.setText("   오늘의 권장 섭취 칼로리 : " + cal_recom_take);
                            tv9.setText("   현재 섭취 칼로리 : " + cal_present_take);

                            Pro_entire = (int) (2.20462 * w * 0.9);
                            pb5.setMax(Pro_entire);
                            pb5.setProgress(int_pro_present);

                            Fat_entire = (int) (2.20462 * w * 0.4);
                            pb6.setMax(Fat_entire);
                            pb6.setProgress(int_fat_present);

                            Car_entire = cal_recom_take - (Pro_entire * 4 + Fat_entire * 9) / 4;
                            pb4.setMax(Car_entire);
                            pb4.setProgress(int_car_present);

                            pb3.setMax(water_recom);
                            pb3.setProgress(int_water_present);

                            tv11.setText("   현재 운동 칼로리 : " + cal_present_burning);
                        }
                    });

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {    }
                }
            }
        });
        th1.start();

        float BMR;
        if(n.equals("남자")){
            BMR = (float) (66.47 + (13.75 * w) + (5 * h) - (6.76 * m));
        }else{
            BMR = (float) (655.1 + (9.56 * w) + (1.85 * h) - (4.68 * m));
        }
        tv10.setText("   내 기초대사량 : " + BMR);

        // 날마다 사용자정보 추가
        calendar.add(calendar.DATE, 1);
        date = format.format(calendar.getTime());
        time = date.substring(0, 10).replaceAll(" ", "");
        int tomorrow = Integer.parseInt(time);

        cur = database.rawQuery("SELECT * FROM 사용자정보 WHERE 날짜 = " + tomorrow, null);
        int c = cur.getCount();
        if (c > 0){
        }else{
            cur = database.rawQuery("SELECT 성별 FROM 사용자정보 WHERE 날짜 = " + today, null);
            cur.moveToNext();
            String s = cur.getString(0);

            cur = database.rawQuery("SELECT 나이 FROM 사용자정보 WHERE 날짜 = " + today, null);
            cur.moveToNext();
            int age = cur.getInt(0);

            cur = database.rawQuery("SELECT 키 FROM 사용자정보 WHERE 날짜 = " + today, null);
            cur.moveToNext();
            float h1 = cur.getFloat(0);

            cur = database.rawQuery("SELECT 몸무게 FROM 사용자정보 WHERE 날짜 = " + today, null);
            cur.moveToNext();
            float w1 = cur.getFloat(0);

            cur = database.rawQuery("SELECT 소모칼로리 FROM 사용자정보 WHERE 날짜 = " + today, null);
            cur.moveToNext();
            float b = cur.getFloat(0);

            cur = database.rawQuery("SELECT 섭취칼로리 FROM 사용자정보 WHERE 날짜 = " + today, null);
            cur.moveToNext();
            float t = cur.getFloat(0);

            sql = "INSERT INTO 사용자정보 (날짜, 성별, 나이, 키, 몸무게, 소모칼로리, 섭취칼로리) VALUES (" + tomorrow + ", '" + s + "', " + age + ", " + h1 + ", " + w1 + ", " + b + ", " + t + ")";
            database.execSQL(sql);
        }

        btn1.setOnClickListener(new View.OnClickListener() { // 성별 변경 버튼

            int a;
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle("성별"); // 제목설정
                a = 0;

                // 확인 버튼
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getActivity(), gender[a] + "로 변경되었습니다.", Toast.LENGTH_SHORT).show();
                        tv3.setText("  성별 : " + gender[a]);

                        // ADD DB에 gender[a] 저장하는 코드
                        if(a == 0) {
                            String sql = "UPDATE 사용자정보 SET 성별 ='남자' WHERE 날짜 = " + today;
                            database.execSQL(sql);

                            sql = "UPDATE 사용자정보 SET 성별 ='남자' WHERE 날짜 = " + tomorrow;
                            database.execSQL(sql);

                            refresh();

                        } else{
                            String sql = "UPDATE 사용자정보 SET 성별 ='여자' WHERE 날짜 = " + today;
                            database.execSQL(sql);

                            sql = "UPDATE 사용자정보 SET 성별 ='여자' WHERE 날짜 = " + tomorrow;
                            database.execSQL(sql);

                            refresh();
                        }
                    }
                });

                // 라디오버튼 목록 출력
                builder.setSingleChoiceItems(gender, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        a = i; // 어떤것 선택했는지 저장
                    }
                });

                builder.show(); // dialog 화면 출력
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() { // 나이 변경 버튼
            @Override
            public void onClick(View v){
                Dialog ageDialog = new Dialog(getActivity());
                ageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                ageDialog.setContentView(R.layout.agedialog);

                Button okBtn = ageDialog.findViewById(R.id.age_btn_ok);
                Button cancelBtn = ageDialog.findViewById(R.id.age_btn_cancel);

                final NumberPicker np = ageDialog.findViewById(R.id.agePicker);
                np.setMinValue(0);
                np.setMaxValue(100);
                np.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
                java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
                for(java.lang.reflect.Field pf : pickerFields) {
                    if(pf.getName().equals("mSelectionDivider")){
                        pf.setAccessible(true);
                        try{
                            @SuppressLint("ResourceAsColor") ColorDrawable colorDrawable = new ColorDrawable(android.R.color.white);
                            pf.set(np, colorDrawable);
                        } catch (IllegalArgumentException e){
                            e.printStackTrace();
                        } catch (Resources.NotFoundException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
                np.setWrapSelectorWheel(false);
                np.setValue(25);
                np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

                    }
                });
                okBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v){
                        tv4.setText("  나이 : "+ np.getValue());
                        Toast.makeText(getActivity(), np.getValue() + "세 로 변경되었습니다.", Toast.LENGTH_SHORT).show();

                        // ADD DB에 np.getValue() 저장하는 코드
                        int a = np.getValue();

                        String sql = "UPDATE 사용자정보 SET 나이 = "+ a + " WHERE 날짜 = " + today;
                        database.execSQL(sql);

                        sql = "UPDATE 사용자정보 SET 나이 = "+ a + " WHERE 날짜 = " + tomorrow;
                        database.execSQL(sql);

                        refresh();

                        ageDialog.dismiss();
                    }
                });
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v){
                        ageDialog.dismiss();
                    }
                });
                ageDialog.show();
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() { // 키 변경 버튼
            @Override
            public void onClick(View v){
                Dialog heightDialog = new Dialog(getActivity());
                heightDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                heightDialog.setContentView(R.layout.heightdialog);

                Button okBtn = heightDialog.findViewById(R.id.height_btn_ok);
                Button cancelBtn = heightDialog.findViewById(R.id.height_btn_cancel);

                final NumberPicker np1 = heightDialog.findViewById(R.id.heightPicker_wh);
                np1.setMinValue(130);
                np1.setMaxValue(220);
                np1.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
                np1.setWrapSelectorWheel(false);
                np1.setValue(160);
                np1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    }
                });

                final NumberPicker np2 = heightDialog.findViewById(R.id.heightPicker_dec);
                np2.setMinValue(0);
                np2.setMaxValue(9);
                np2.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
                np2.setWrapSelectorWheel(false);
                np2.setValue(0);
                np2.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    }
                });

                java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
                for(java.lang.reflect.Field pf : pickerFields) {
                    if(pf.getName().equals("mSelectionDivider")){
                        pf.setAccessible(true);
                        try{
                            @SuppressLint("ResourceAsColor") ColorDrawable colorDrawable = new ColorDrawable(android.R.color.white);
                            pf.set(np1, colorDrawable);
                        } catch (IllegalArgumentException e){
                            e.printStackTrace();
                        } catch (Resources.NotFoundException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }

                okBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v){
                        tv5.setText("  키 : "+ np1.getValue() + "." + np2.getValue() + "cm");
                        Toast.makeText(getActivity(), np1.getValue() + "." + np2.getValue() + "cm 로 변경되었습니다.", Toast.LENGTH_SHORT).show();

                        // ADD DB에 np.getValue() 저장하는 코드
                        float a = (float) (np1.getValue() + 0.1*np2.getValue());

                        SimpleDateFormat format = new SimpleDateFormat("YYYY MM dd HH:mm:ss", Locale.UK);
                        Calendar calendar = Calendar.getInstance();
                        String date = format.format(calendar.getTime());
                        String time = date.substring(0, 10).replaceAll(" ", "");
                        int today = Integer.parseInt(time);

                        String sql = "UPDATE 사용자정보 SET 키 = " + a + " WHERE 날짜 = " + today;
                        database.execSQL(sql);

                        sql = "UPDATE 사용자정보 SET 키 = " + a + " WHERE 날짜 = " + tomorrow;
                        database.execSQL(sql);

                        refresh();

                        heightDialog.dismiss();
                    }
                });
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v){
                        heightDialog.dismiss();
                    }
                });
                heightDialog.show();
            }
        });

        btn4.setOnClickListener(new View.OnClickListener() { // 몸무게 변경 버튼
            @Override
            public void onClick(View v){
                Dialog weightDialog = new Dialog(getActivity());
                weightDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                weightDialog.setContentView(R.layout.weightdialog);

                Button okBtn = weightDialog.findViewById(R.id.weight_btn_ok);
                Button cancelBtn = weightDialog.findViewById(R.id.weight_btn_cancel);

                final NumberPicker np1 = weightDialog.findViewById(R.id.weightPicker_wh);
                np1.setMinValue(40);
                np1.setMaxValue(130);
                np1.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
                np1.setWrapSelectorWheel(false);
                np1.setValue(60);
                np1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    }
                });

                final NumberPicker np2 = weightDialog.findViewById(R.id.weightPicker_dec);
                np2.setMinValue(0);
                np2.setMaxValue(9);
                np2.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
                np2.setWrapSelectorWheel(false);
                np2.setValue(0);
                np2.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    }
                });

                java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
                for(java.lang.reflect.Field pf : pickerFields) {
                    if(pf.getName().equals("mSelectionDivider")){
                        pf.setAccessible(true);
                        try{
                            @SuppressLint("ResourceAsColor") ColorDrawable colorDrawable = new ColorDrawable(android.R.color.white);
                            pf.set(np1, colorDrawable);
                        } catch (IllegalArgumentException e){
                            e.printStackTrace();
                        } catch (Resources.NotFoundException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }

                okBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v){
                        tv6.setText("  몸무게 : "+ np1.getValue() + "." + np2.getValue() + "kg");
                        Toast.makeText(getActivity(), np1.getValue() + "." + np2.getValue() + "kg 로 변경되었습니다.", Toast.LENGTH_SHORT).show();

                        // ADD DB에 np.getValue() 저장하는 코드
                        float a = (float) (np1.getValue() + 0.1*np2.getValue());

                        String sql = "UPDATE 사용자정보 SET 몸무게 = " + a + " WHERE 날짜 = " + today;
                        database.execSQL(sql);

                        sql = "UPDATE 사용자정보 SET 몸무게 = " + a + " WHERE 날짜 = " + tomorrow;
                        database.execSQL(sql);

                        refresh();

                        weightDialog.dismiss();
                    }
                });
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v){
                        weightDialog.dismiss();
                    }
                });
                weightDialog.show();
            }
        });

        // 사용자의 ID, 메일 주소 등 기본 프로필을 요청하도록 로그인 구성
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(container.getContext(), gso);

        // 로그인 작업의 onCreate 메서드에서 FirebaseAuth 객체의 공유 인스턴스를 가져옴
        mAuth = FirebaseAuth.getInstance();

        // 로그인 버튼 리스너
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        // 걸음수 조회할 피트니스 권한 객체
        FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_ACTIVITY_SEGMENT)//활동내역
                        .addDataType(DataType.TYPE_CALORIES_EXPENDED)
                        .build();

        // 구글 계정에 권한이 있는지 체크
        if(!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(container.getContext()), fitnessOptions)) {
            GoogleSignIn.requestPermissions(// 없으면 권한 요청
                    this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(container.getContext()),
                    fitnessOptions);
        } else {
            Fitness.getRecordingClient(container.getContext(), GoogleSignIn.getLastSignedInAccount(container.getContext()))
                    .subscribe(DataType.TYPE_ACTIVITY_SEGMENT)
                    .addOnCompleteListener(
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful() && mGoogleSignInClient.getApplicationContext() != null) {
                                        Fitness.getRecordingClient(getContext(), GoogleSignIn.getLastSignedInAccount(getContext()))
                                                .subscribe(DataType.TYPE_CALORIES_EXPENDED)
                                                .addOnCompleteListener(
                                                        new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.i(TAG, "Successfully subscribed!");
                                                                    // readData 반복실행
                                                                    Thread th2 = new Thread(new Runnable() {
                                                                        @Override
                                                                        public void run() { // Thread 로 작업할 내용을 구현
                                                                            while(true) {
                                                                                readData();

                                                                                handler2.post(new Runnable() {
                                                                                    @Override
                                                                                    public void run() { // 화면에 변경하는 작업을 구현
                                                                                    }
                                                                                });

                                                                                try {
                                                                                    Thread.sleep(10000); // 시간지연
                                                                                } catch (InterruptedException e) {    }
                                                                            }
                                                                        }
                                                                    });
                                                                    th2.start();

                                                                } else {
                                                                    Log.w(TAG, "There was a problem subscribing.", task.getException());
                                                                }
                                                            }
                                                        });
                                    } else {
                                        Log.w(TAG, "There was a problem subscribing.", task.getException());
                                    }
                                }
                            });
        }

        mDbHelper.close();
        return v;
    }

    public void refresh(){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if(getActivity() != null) {
            ft.detach(this).attach(this).commit();
        }
    }



    // 로그인 인 텐트
    // 로그인 할 구글계정 선택, 권한 부여 메시지 표시
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 1);
    }

    // onActivityResult란 일반적으로 main액티비티(로그인 버튼 있는 화면)에서 sub액티비티(구글 아이디, 비번 입력 화면 등)를 호출하여 넘어갔다가 다시 main액티비티로 돌아올때 사용하는 메소드
    // 로그인 버튼 누르면 나오는 화면 처리
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(getActivity() , "Google sign in Failed", Toast.LENGTH_LONG).show();
            }
        }

        if (resultCode == Activity.RESULT_OK) {// 로그인 성공 시
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {// 권한 얻었을 시
                // readData 반복실행
                Handler handler3 = new Handler();
                Thread th3 = new Thread(new Runnable() {
                    @Override
                    public void run() { // Thread 로 작업할 내용을 구현
                        while(true) {
                            readData();

                            handler3.post(new Runnable() {
                                @Override
                                public void run() { // 화면에 변경하는 작업을 구현
                                }
                            });

                            try {
                                Thread.sleep(10000); // 시간지연
                            } catch (InterruptedException e) {    }
                        }
                    }
                });
                th3.start();
            }
        }
    }

    // 활동을 초기화할 때 사용자가 현재 로그인되어 있는지 확인
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    // 사용자가 정상적으로 로그인하면 ID 토큰을 가져와서 firebase 사용자 인증 정보로 교환하고 해당 정보를 사용해 firebase에 인증
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {// 로그인 성공시 사용자 정보로 UI 업데이트
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(getActivity() , "Complete", Toast.LENGTH_LONG).show();

                        } else {// 로그인 실패시 메시지 창
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getActivity() , "Authentication Failed", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // 구글핏 데이터 가져오기
    // 원하는 시간대 조정하기 쉽게 시간은 밀리세컨드 단위로 통일

    // 밀리세컨드를 날짜시간단위로 바꿔줌
    public static String milli2string(long time) {
        SimpleDateFormat format = new SimpleDateFormat("YYYY MM dd HH:mm:ss", Locale.UK);
        return format.format(time);
    }

    // 구글핏 칼로리 조회
    private void readData() {
        final Calendar cal = Calendar.getInstance();
        Date now = Calendar.getInstance().getTime();
        cal.setTime(now);

        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        long startTime = cal.getTimeInMillis();

        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH), 24, 0, 0);
        long endTime = cal.getTimeInMillis();

        if(getActivity() != null && GoogleSignIn.getLastSignedInAccount(getActivity()) != null) {
            Fitness.getHistoryClient(getActivity(),
                    GoogleSignIn.getLastSignedInAccount(getActivity()))
                    .readData(new DataReadRequest.Builder()
                            .read(DataType.TYPE_ACTIVITY_SEGMENT)
                            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                            .build())
                    .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                        @Override
                        public void onSuccess(DataReadResponse response) {

                            DataSet dataSet = response.getDataSet(DataType.TYPE_ACTIVITY_SEGMENT);

                            Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());

                            // log를 찍어 넘어오는 데이터 확인
                            for (DataPoint dp : dataSet.getDataPoints()) {
                                Log.i(TAG, "Data point:");
                                Log.i(TAG, "\tType: " + dp.getDataType().getName());

                                Log.i(TAG, "\tStart: " + milli2string(dp.getStartTime(TimeUnit.MILLISECONDS)));
                                laststartTime = dp.getStartTime(TimeUnit.MILLISECONDS);
                                Log.i(TAG, "\tEnd: " + milli2string(dp.getEndTime(TimeUnit.MILLISECONDS)));
                                lastendTime = dp.getEndTime(TimeUnit.MILLISECONDS);
                                for (Field field : dp.getDataType().getFields()) {
                                    Log.i(TAG, "\tField: " + field.getName() + " Value: " + dp.getValue(field));
                                    wr = dp.getValue(field);
                                    Log.i(TAG, String.valueOf(wr));
                                }
                            }


                        }
                    });

            Fitness.getHistoryClient(getActivity(),
                    GoogleSignIn.getLastSignedInAccount(getActivity()))
                    .readData(new DataReadRequest.Builder()
                            .read(DataType.TYPE_CALORIES_EXPENDED)
                            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                            .build())
                    .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                        @Override
                        public void onSuccess(DataReadResponse response) {
                            float sum = 0;

                            DataSet dataSet = response.getDataSet(DataType.TYPE_CALORIES_EXPENDED);

                            Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());

                            for (DataPoint dp : dataSet.getDataPoints()) {
                                if (dp.getStartTime(TimeUnit.MILLISECONDS) >= laststartTime && dp.getEndTime(TimeUnit.MILLISECONDS) <= lastendTime) {
                                    Log.i(TAG, "Data point:");
                                    Log.i(TAG, "\tType: " + dp.getDataType().getName());

                                    Log.i(TAG, "\tStart: " + milli2string(dp.getStartTime(TimeUnit.MILLISECONDS)));
                                    Log.i(TAG, "\tEnd: " + milli2string(dp.getEndTime(TimeUnit.MILLISECONDS)));
                                    for (Field field : dp.getDataType().getFields()) {
                                        Log.i(TAG, "\tField: " + field.getName() + " Value: " + dp.getValue(field));
                                        sum += dp.getValue(field).asFloat();
                                    }
                                }
                            }
                            String time = milli2string(laststartTime).substring(0, 10).replaceAll(" ", "");

                            Cursor cur = database.rawQuery("SELECT * FROM 사용자운동 WHERE 날짜 = " + Integer.parseInt(time) + " AND 시작시간 = " + Integer.parseInt(milli2string(laststartTime).substring(10).replaceAll(":", "").replaceAll(" ", "")), null);
                            int n = cur.getCount();
                            if (n == 1) {

                            } else {
                                if (String.valueOf(wr).equals("7")) {
                                    cur = database.rawQuery("SELECT * FROM 사용자운동", null);
                                    n = cur.getCount() + 1;
                                    String level;
                                    if ( sum / (lastendTime - laststartTime) / 60000 < 3){
                                        level = "하";
                                    } else if ( sum / (lastendTime - laststartTime) / 60000 > 5.5){
                                        level = "상";
                                    } else {
                                        level = "중";
                                    }
                                    String sql = "INSERT INTO 사용자운동 (num, 운동구분, 강도, 시간, 칼로리, 날짜, 시작시간) VALUES (" + n + ", '" + "걷기" + "', '" + level + "', " + (lastendTime - laststartTime) / 60000 + ", " + sum + ", '" + Integer.parseInt(time) + "', '" + Integer.parseInt(milli2string(laststartTime).substring(10).replaceAll(":", "").replaceAll(" ", "")) + "')";
                                    database.execSQL(sql);

                                } else if (String.valueOf(wr).equals("8")) {
                                    cur = database.rawQuery("SELECT * FROM 사용자운동", null);
                                    n = cur.getCount() + 1;
                                    String level;
                                    if ( sum / (lastendTime - laststartTime) / 60000 < 6.5){
                                        level = "하";
                                    } else if ( sum / (lastendTime - laststartTime) / 60000 > 8){
                                        level = "상";
                                    } else {
                                        level = "중";
                                    }
                                    String sql = "INSERT INTO 사용자운동 (num, 운동구분, 강도, 시간, 칼로리, 날짜, 시작시간) VALUES (" + n + ", '" + "뛰기" + "', '" + level + "', " + (lastendTime - laststartTime) / 60000 + ", " + sum + ", '" + Integer.parseInt(time) + "', '" + Integer.parseInt(milli2string(laststartTime).substring(10).replaceAll(":", "").replaceAll(" ", "")) + "')";
                                    database.execSQL(sql);
                                }
                            }
                        }
                    });
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

}