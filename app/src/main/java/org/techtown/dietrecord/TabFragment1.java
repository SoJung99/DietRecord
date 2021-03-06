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

    private SignInButton signInButton; // ?????? ????????? ??????
    private GoogleSignInClient mGoogleSignInClient;
    private String TAG="mainTag";
    private FirebaseAuth mAuth;
    private int RC_SIGN_IN=123;
    Value wr; // 7?????? ?????? 8?????? ?????????
    int flag = 0;

    long laststartTime, lastendTime; // ???????????? ????????? ????????? ??????

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

    final String[] gender = new String[]{"??????", "??????"};
    int cal_recom_take , Car_entire , Pro_entire , Fat_entire , water_recom = 2000; // ??????
    float cal_present_take , cal_present_burning , Car_present , Pro_present , Fat_present , water_present ; // ??????

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

        // ?????? ????????? ??????
        signInButton = v.findViewById(R.id.SignIn_Button);

        // tv1
        Cursor cur_date = database.rawQuery("select ?????? from ??????????????? ", null);
        cur_date.moveToNext();
        int a_date = cur_date.getInt(0);
        String before = Integer.toString(a_date);

        try {
            Date d1 = new SimpleDateFormat("yyyyMMdd").parse(before);
            Date d2 = new Date();
            long diffDay = (d2.getTime() - d1.getTime()) / (24*60*60*1000);
            diffDay++;
            tv1.setText("???????????? " + diffDay +"??????!");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat format = new SimpleDateFormat("YYYY MM dd HH:mm:ss", Locale.UK);
        Calendar calendar = Calendar.getInstance();
        String date = format.format(calendar.getTime());
        String time = date.substring(0, 10).replaceAll(" ", "");
        int today = Integer.parseInt(time);

        Cursor cur = database.rawQuery("SELECT ?????? FROM ??????????????? WHERE ?????? = " + today, null);
        cur.moveToNext();
        String n = cur.getString(0);
        tv3.setText("  ?????? : " + n);

        cur = database.rawQuery("SELECT ?????? FROM ??????????????? WHERE ?????? = " + today, null);
        cur.moveToNext();
        int m = cur.getInt(0);
        tv4.setText("  ?????? : " + m);

        cur = database.rawQuery("SELECT ??? FROM ??????????????? WHERE ?????? = " + today, null);
        cur.moveToNext();
        float h = cur.getFloat(0);
        tv5.setText("  ??? : " + h);

        cur = database.rawQuery("SELECT ????????? FROM ??????????????? WHERE ?????? = " + today, null);
        cur.moveToNext();
        float w = cur.getFloat(0);
        tv6.setText("  ????????? : " + w);


        Cursor cur_t = database.rawQuery("SELECT sum(?????????) FROM ??????????????? WHERE ?????? = " + today , null);
        cur_t.moveToNext();
        float sum = cur_t.getFloat(0);
        String sql = "UPDATE ??????????????? SET ??????????????? = " + sum + " WHERE ?????? = " + today;
        database.execSQL(sql);

        Cursor cur_b = database.rawQuery("SELECT sum(?????????) FROM ??????????????? WHERE ?????? = " + today, null);
        cur_b.moveToNext();
        sum = cur_b.getFloat(0);
        sql = "UPDATE ??????????????? SET ??????????????? = " + sum + " WHERE ?????? = " + today;
        database.execSQL(sql);

        Handler handler1 = new Handler();
        Handler handler2 = new Handler();

        Thread th1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    // ????????? ???????????? ?????? ?????? ????????? ??????
                    if(n.equals("??????")){
                        cal_recom_take = (int) ((66.47 + (13.75 * w) + (5 * h) - (6.76 * m)) * 1.375 - 500);
                    }else{
                        cal_recom_take = (int) ((655.1 + (9.56 * w) + (1.85 * h) - (4.68 * m)) * 1.375 - 500);
                    }

                    Cursor cur1 = database.rawQuery("SELECT sum(?????????) FROM ??????????????? WHERE ?????? = " + today, null);
                    cur1.moveToNext();
                    cal_present_take = cur1.getFloat(0);

                    int int_cal_present_take = (int)cal_present_take;

                    Cursor cur_c = database.rawQuery("select sum(????????????) from ??????????????? where ?????? = " + today, null);
                    cur_c.moveToNext();
                    Car_present = cur_c.getFloat(0);
                    int int_car_present = (int)Car_present;

                    Cursor cur_p = database.rawQuery("select sum(?????????) from ??????????????? where ?????? = " + today, null);
                    cur_p.moveToNext();
                    Pro_present = cur_p.getFloat(0);
                    int int_pro_present = (int)Pro_present;

                    Cursor cur_f = database.rawQuery("select sum(??????) from ??????????????? where ?????? = " + today, null);
                    cur_f.moveToNext();
                    Fat_present = cur_f.getFloat(0);
                    int int_fat_present = (int)Fat_present;

                    Cursor cur_w = database.rawQuery("SELECT sum(???) FROM ??????????????? WHERE ???????????? = '???' AND ?????? = " + today, null);
                    cur_w.moveToNext();
                    water_present = 200 * cur_w.getFloat(0);

                    tv12.setText("   ?????? ?????? ?????? ?????? : " + water_recom);
                    tv13.setText("   ?????? ?????? ?????? : "  + water_present);

                    int int_water_present = (int)water_present;

                    Cursor cur2 = database.rawQuery("SELECT sum(?????????) FROM ??????????????? WHERE ?????? = " + today, null);
                    cur2.moveToNext();
                    cal_present_burning = cur2.getFloat(0);

                    handler1.post(new Runnable() {
                        @Override
                        public void run() {
                            pb1.setMax(cal_recom_take);
                            pb1.setProgress(int_cal_present_take);

                            tv8.setText("   ????????? ?????? ?????? ????????? : " + cal_recom_take);
                            tv9.setText("   ?????? ?????? ????????? : " + cal_present_take);

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

                            tv11.setText("   ?????? ?????? ????????? : " + cal_present_burning);
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
        if(n.equals("??????")){
            BMR = (float) (66.47 + (13.75 * w) + (5 * h) - (6.76 * m));
        }else{
            BMR = (float) (655.1 + (9.56 * w) + (1.85 * h) - (4.68 * m));
        }
        tv10.setText("   ??? ??????????????? : " + BMR);

        // ????????? ??????????????? ??????
        calendar.add(calendar.DATE, 1);
        date = format.format(calendar.getTime());
        time = date.substring(0, 10).replaceAll(" ", "");
        int tomorrow = Integer.parseInt(time);

        cur = database.rawQuery("SELECT * FROM ??????????????? WHERE ?????? = " + tomorrow, null);
        int c = cur.getCount();
        if (c > 0){
        }else{
            cur = database.rawQuery("SELECT ?????? FROM ??????????????? WHERE ?????? = " + today, null);
            cur.moveToNext();
            String s = cur.getString(0);

            cur = database.rawQuery("SELECT ?????? FROM ??????????????? WHERE ?????? = " + today, null);
            cur.moveToNext();
            int age = cur.getInt(0);

            cur = database.rawQuery("SELECT ??? FROM ??????????????? WHERE ?????? = " + today, null);
            cur.moveToNext();
            float h1 = cur.getFloat(0);

            cur = database.rawQuery("SELECT ????????? FROM ??????????????? WHERE ?????? = " + today, null);
            cur.moveToNext();
            float w1 = cur.getFloat(0);

            cur = database.rawQuery("SELECT ??????????????? FROM ??????????????? WHERE ?????? = " + today, null);
            cur.moveToNext();
            float b = cur.getFloat(0);

            cur = database.rawQuery("SELECT ??????????????? FROM ??????????????? WHERE ?????? = " + today, null);
            cur.moveToNext();
            float t = cur.getFloat(0);

            sql = "INSERT INTO ??????????????? (??????, ??????, ??????, ???, ?????????, ???????????????, ???????????????) VALUES (" + tomorrow + ", '" + s + "', " + age + ", " + h1 + ", " + w1 + ", " + b + ", " + t + ")";
            database.execSQL(sql);
        }

        btn1.setOnClickListener(new View.OnClickListener() { // ?????? ?????? ??????

            int a;
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle("??????"); // ????????????
                a = 0;

                // ?????? ??????
                builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getActivity(), gender[a] + "??? ?????????????????????.", Toast.LENGTH_SHORT).show();
                        tv3.setText("  ?????? : " + gender[a]);

                        // ADD DB??? gender[a] ???????????? ??????
                        if(a == 0) {
                            String sql = "UPDATE ??????????????? SET ?????? ='??????' WHERE ?????? = " + today;
                            database.execSQL(sql);

                            sql = "UPDATE ??????????????? SET ?????? ='??????' WHERE ?????? = " + tomorrow;
                            database.execSQL(sql);

                            refresh();

                        } else{
                            String sql = "UPDATE ??????????????? SET ?????? ='??????' WHERE ?????? = " + today;
                            database.execSQL(sql);

                            sql = "UPDATE ??????????????? SET ?????? ='??????' WHERE ?????? = " + tomorrow;
                            database.execSQL(sql);

                            refresh();
                        }
                    }
                });

                // ??????????????? ?????? ??????
                builder.setSingleChoiceItems(gender, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        a = i; // ????????? ??????????????? ??????
                    }
                });

                builder.show(); // dialog ?????? ??????
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() { // ?????? ?????? ??????
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
                        tv4.setText("  ?????? : "+ np.getValue());
                        Toast.makeText(getActivity(), np.getValue() + "??? ??? ?????????????????????.", Toast.LENGTH_SHORT).show();

                        // ADD DB??? np.getValue() ???????????? ??????
                        int a = np.getValue();

                        String sql = "UPDATE ??????????????? SET ?????? = "+ a + " WHERE ?????? = " + today;
                        database.execSQL(sql);

                        sql = "UPDATE ??????????????? SET ?????? = "+ a + " WHERE ?????? = " + tomorrow;
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

        btn3.setOnClickListener(new View.OnClickListener() { // ??? ?????? ??????
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
                        tv5.setText("  ??? : "+ np1.getValue() + "." + np2.getValue() + "cm");
                        Toast.makeText(getActivity(), np1.getValue() + "." + np2.getValue() + "cm ??? ?????????????????????.", Toast.LENGTH_SHORT).show();

                        // ADD DB??? np.getValue() ???????????? ??????
                        float a = (float) (np1.getValue() + 0.1*np2.getValue());

                        SimpleDateFormat format = new SimpleDateFormat("YYYY MM dd HH:mm:ss", Locale.UK);
                        Calendar calendar = Calendar.getInstance();
                        String date = format.format(calendar.getTime());
                        String time = date.substring(0, 10).replaceAll(" ", "");
                        int today = Integer.parseInt(time);

                        String sql = "UPDATE ??????????????? SET ??? = " + a + " WHERE ?????? = " + today;
                        database.execSQL(sql);

                        sql = "UPDATE ??????????????? SET ??? = " + a + " WHERE ?????? = " + tomorrow;
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

        btn4.setOnClickListener(new View.OnClickListener() { // ????????? ?????? ??????
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
                        tv6.setText("  ????????? : "+ np1.getValue() + "." + np2.getValue() + "kg");
                        Toast.makeText(getActivity(), np1.getValue() + "." + np2.getValue() + "kg ??? ?????????????????????.", Toast.LENGTH_SHORT).show();

                        // ADD DB??? np.getValue() ???????????? ??????
                        float a = (float) (np1.getValue() + 0.1*np2.getValue());

                        String sql = "UPDATE ??????????????? SET ????????? = " + a + " WHERE ?????? = " + today;
                        database.execSQL(sql);

                        sql = "UPDATE ??????????????? SET ????????? = " + a + " WHERE ?????? = " + tomorrow;
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

        // ???????????? ID, ?????? ?????? ??? ?????? ???????????? ??????????????? ????????? ??????
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(container.getContext(), gso);

        // ????????? ????????? onCreate ??????????????? FirebaseAuth ????????? ?????? ??????????????? ?????????
        mAuth = FirebaseAuth.getInstance();

        // ????????? ?????? ?????????
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        // ????????? ????????? ???????????? ?????? ??????
        FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_ACTIVITY_SEGMENT)//????????????
                        .addDataType(DataType.TYPE_CALORIES_EXPENDED)
                        .build();

        // ?????? ????????? ????????? ????????? ??????
        if(!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(container.getContext()), fitnessOptions)) {
            GoogleSignIn.requestPermissions(// ????????? ?????? ??????
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
                                                                    // readData ????????????
                                                                    Thread th2 = new Thread(new Runnable() {
                                                                        @Override
                                                                        public void run() { // Thread ??? ????????? ????????? ??????
                                                                            while(true) {
                                                                                readData();

                                                                                handler2.post(new Runnable() {
                                                                                    @Override
                                                                                    public void run() { // ????????? ???????????? ????????? ??????
                                                                                    }
                                                                                });

                                                                                try {
                                                                                    Thread.sleep(10000); // ????????????
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



    // ????????? ??? ??????
    // ????????? ??? ???????????? ??????, ?????? ?????? ????????? ??????
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 1);
    }

    // onActivityResult??? ??????????????? main????????????(????????? ?????? ?????? ??????)?????? sub????????????(?????? ?????????, ?????? ?????? ?????? ???)??? ???????????? ??????????????? ?????? main??????????????? ???????????? ???????????? ?????????
    // ????????? ?????? ????????? ????????? ?????? ??????
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

        if (resultCode == Activity.RESULT_OK) {// ????????? ?????? ???
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {// ?????? ????????? ???
                // readData ????????????
                Handler handler3 = new Handler();
                Thread th3 = new Thread(new Runnable() {
                    @Override
                    public void run() { // Thread ??? ????????? ????????? ??????
                        while(true) {
                            readData();

                            handler3.post(new Runnable() {
                                @Override
                                public void run() { // ????????? ???????????? ????????? ??????
                                }
                            });

                            try {
                                Thread.sleep(10000); // ????????????
                            } catch (InterruptedException e) {    }
                        }
                    }
                });
                th3.start();
            }
        }
    }

    // ????????? ???????????? ??? ???????????? ?????? ??????????????? ????????? ??????
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    // ???????????? ??????????????? ??????????????? ID ????????? ???????????? firebase ????????? ?????? ????????? ???????????? ?????? ????????? ????????? firebase??? ??????
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {// ????????? ????????? ????????? ????????? UI ????????????
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(getActivity() , "Complete", Toast.LENGTH_LONG).show();

                        } else {// ????????? ????????? ????????? ???
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getActivity() , "Authentication Failed", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // ????????? ????????? ????????????
    // ????????? ????????? ???????????? ?????? ????????? ??????????????? ????????? ??????

    // ?????????????????? ????????????????????? ?????????
    public static String milli2string(long time) {
        SimpleDateFormat format = new SimpleDateFormat("YYYY MM dd HH:mm:ss", Locale.UK);
        return format.format(time);
    }

    // ????????? ????????? ??????
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

                            // log??? ?????? ???????????? ????????? ??????
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

                            Cursor cur = database.rawQuery("SELECT * FROM ??????????????? WHERE ?????? = " + Integer.parseInt(time) + " AND ???????????? = " + Integer.parseInt(milli2string(laststartTime).substring(10).replaceAll(":", "").replaceAll(" ", "")), null);
                            int n = cur.getCount();
                            if (n == 1) {

                            } else {
                                if (String.valueOf(wr).equals("7")) {
                                    cur = database.rawQuery("SELECT * FROM ???????????????", null);
                                    n = cur.getCount() + 1;
                                    String level;
                                    if ( sum / (lastendTime - laststartTime) / 60000 < 3){
                                        level = "???";
                                    } else if ( sum / (lastendTime - laststartTime) / 60000 > 5.5){
                                        level = "???";
                                    } else {
                                        level = "???";
                                    }
                                    String sql = "INSERT INTO ??????????????? (num, ????????????, ??????, ??????, ?????????, ??????, ????????????) VALUES (" + n + ", '" + "??????" + "', '" + level + "', " + (lastendTime - laststartTime) / 60000 + ", " + sum + ", '" + Integer.parseInt(time) + "', '" + Integer.parseInt(milli2string(laststartTime).substring(10).replaceAll(":", "").replaceAll(" ", "")) + "')";
                                    database.execSQL(sql);

                                } else if (String.valueOf(wr).equals("8")) {
                                    cur = database.rawQuery("SELECT * FROM ???????????????", null);
                                    n = cur.getCount() + 1;
                                    String level;
                                    if ( sum / (lastendTime - laststartTime) / 60000 < 6.5){
                                        level = "???";
                                    } else if ( sum / (lastendTime - laststartTime) / 60000 > 8){
                                        level = "???";
                                    } else {
                                        level = "???";
                                    }
                                    String sql = "INSERT INTO ??????????????? (num, ????????????, ??????, ??????, ?????????, ??????, ????????????) VALUES (" + n + ", '" + "??????" + "', '" + level + "', " + (lastendTime - laststartTime) / 60000 + ", " + sum + ", '" + Integer.parseInt(time) + "', '" + Integer.parseInt(milli2string(laststartTime).substring(10).replaceAll(":", "").replaceAll(" ", "")) + "')";
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