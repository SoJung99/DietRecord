package org.techtown.dietrecord;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
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
import androidx.fragment.app.Fragment;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


@RequiresApi(api = Build.VERSION_CODES.O)
public class TabFragment1 extends Fragment {

    private SignInButton signInButton;
    private GoogleSignInClient mGoogleSignInClient;
    private String TAG="mainTag";
    private FirebaseAuth mAuth;
    private int RC_SIGN_IN=123;

    long laststartTime, lastendTime, calstartTime, calendTime;

    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;


    final String[] gender = new String[]{"남자", "여자"};

    int howlong = 1; // n일째 다이어트
    int cal_recom_take = 2100, cal_present_take = 1300; // 섭취칼로리
    int cal_recom_burning = 1500, cal_present_burning = 800; // 소모칼로리
    int water_recom = 2000, water_present = 1500; // 물섭취
    int Car_entire = 100, Car_present = 25; // 탄수화물
    int Pro_entire = 100, Pro_present = 50; // 단백질
    int Fat_entire = 100, Fat_present = 75; // 지방

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment1, container, false);

        Button btn1 = (Button) v.findViewById(R.id.button1);
        Button btn2 = (Button) v.findViewById(R.id.button2);
        Button btn3 = (Button) v.findViewById(R.id.button3);
        Button btn4 = (Button) v.findViewById(R.id.button4);

        ProgressBar pb1 = (ProgressBar) v.findViewById(R.id.progressBar1);
        ProgressBar pb2 = (ProgressBar) v.findViewById(R.id.progressBar2);
        ProgressBar pb3 = (ProgressBar) v.findViewById(R.id.progressBar3);
        ProgressBar pb4 = (ProgressBar) v.findViewById(R.id.progressBar4);
        ProgressBar pb5 = (ProgressBar) v.findViewById(R.id.progressBar5);
        ProgressBar pb6 = (ProgressBar) v.findViewById(R.id.progressBar6);

        TextView tv1 = (TextView) v.findViewById(R.id.textView1);
        TextView tv3 = (TextView) v.findViewById(R.id.textView3);
        TextView tv4 = (TextView) v.findViewById(R.id.textView4);
        TextView tv5 = (TextView) v.findViewById(R.id.textView5);
        TextView tv6 = (TextView) v.findViewById(R.id.textView6);
        TextView tv8 = (TextView) v.findViewById(R.id.textView8);
        TextView tv9 = (TextView) v.findViewById(R.id.textView9);
        TextView tv10 = (TextView) v.findViewById(R.id.textView10);
        TextView tv11 = (TextView) v.findViewById(R.id.textView11);
        TextView tv12 = (TextView) v.findViewById(R.id.textView12);
        TextView tv13 = (TextView) v.findViewById(R.id.textView13);

        // 로그인 버튼
        signInButton = v.findViewById(R.id.SignIn_Button);

        tv1.setText("다이어트 " + howlong +"일차!");
        // ADD howlong++

        tv8.setText("   오늘의 권장 섭취 칼로리 : " + cal_recom_take);
        tv9.setText("   현재 섭취 칼로리 : " + cal_present_take);

        pb1.setMax(cal_recom_take);
        pb1.setProgress(cal_present_take);

        tv10.setText("   오늘의 권장 운동 칼로리 : " + cal_recom_burning);
        tv11.setText("   현재 운동 칼로리 : " + cal_present_burning);

        pb2.setMax(cal_recom_burning);
        pb2.setProgress(cal_present_burning);

        tv12.setText("   하루 권장 섭취 수분 : " + water_recom);
        tv13.setText("   현재 섭취 수분 : "  + water_present);

        pb3.setMax(water_recom);
        pb3.setProgress(water_present);

        pb4.setMax(Car_entire);
        pb4.setProgress(Car_present);

        pb5.setMax(Pro_entire);
        pb5.setProgress(Pro_present);

        pb6.setMax(Fat_entire);
        pb6.setProgress(Fat_present);

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

                Button okBtn = (Button) ageDialog.findViewById(R.id.age_btn_ok);
                Button cancelBtn = (Button) ageDialog.findViewById(R.id.age_btn_cancel);

                final NumberPicker np = (NumberPicker) ageDialog.findViewById(R.id.agePicker);
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
                        tv4.setText("  나이 : "+ String.valueOf(np.getValue()));
                        Toast.makeText(getActivity(), np.getValue() + "세 로 변경되었습니다.", Toast.LENGTH_SHORT).show();
                        // ADD DB에 np.getValue() 저장하는 코드
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

                Button okBtn = (Button) heightDialog.findViewById(R.id.height_btn_ok);
                Button cancelBtn = (Button) heightDialog.findViewById(R.id.height_btn_cancel);

                final NumberPicker np1 = (NumberPicker) heightDialog.findViewById(R.id.heightPicker_wh);
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

                final NumberPicker np2 = (NumberPicker) heightDialog.findViewById(R.id.heightPicker_dec);
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
                        tv5.setText("  키 : "+ String.valueOf(np1.getValue()) + "." + String.valueOf(np2.getValue()) + "cm");
                        Toast.makeText(getActivity(), String.valueOf(np1.getValue()) + "." + String.valueOf(np2.getValue()) + "cm 로 변경되었습니다.", Toast.LENGTH_SHORT).show();
                        // ADD DB에 np.getValue() 저장하는 코드
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

                Button okBtn = (Button) weightDialog.findViewById(R.id.weight_btn_ok);
                Button cancelBtn = (Button) weightDialog.findViewById(R.id.weight_btn_cancel);

                final NumberPicker np1 = (NumberPicker) weightDialog.findViewById(R.id.weightPicker_wh);
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

                final NumberPicker np2 = (NumberPicker) weightDialog.findViewById(R.id.weightPicker_dec);
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
                        tv6.setText("  몸무게 : "+ String.valueOf(np1.getValue()) + "." + String.valueOf(np2.getValue()) + "kg");
                        Toast.makeText(getActivity(), String.valueOf(np1.getValue()) + "." + String.valueOf(np2.getValue()) + "kg 로 변경되었습니다.", Toast.LENGTH_SHORT).show();
                        // ADD DB에 np.getValue() 저장하는 코드
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
                                    if (task.isSuccessful()) {
                                        Fitness.getRecordingClient(getContext(), GoogleSignIn.getLastSignedInAccount(getContext()))
                                                .subscribe(DataType.TYPE_CALORIES_EXPENDED)
                                                .addOnCompleteListener(
                                                        new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.i(TAG, "Successfully subscribed!");
                                                                    readData();
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

        return v;
    }

    // 로그인 인 텐트
    // 로그인 할 구글계정 선택, 권한 부여 메시지 표시
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
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
                readData();
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
    // 밀리세컨드 단위로 바꿔줌
    public static String milli2string(long time) {
        SimpleDateFormat format = new SimpleDateFormat("E MMM dd HH:mm:ss", Locale.UK);
        return format.format(time);
    }

    // 걸음수 조회
    private void readData() {
        final Calendar cal = Calendar.getInstance();
        Date now = Calendar.getInstance().getTime();
        cal.setTime(now);

        cal.add(Calendar.DATE, -1);

        //12시부터 18시 사이의 걸음수 조회
        // 가져올 데이터 시작 시간(12시)
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        long startTime = cal.getTimeInMillis();

        // 가져올 데이터 종료 시간(18시)
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH), 24, 0, 0);
        long endTime = cal.getTimeInMillis();


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
                            if(dp.getStartTime(TimeUnit.MILLISECONDS) >= laststartTime && dp.getEndTime(TimeUnit.MILLISECONDS) <= lastendTime) {
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
                        Log.i(TAG," : " + Float.toString(sum));


                    }
                });


        // 구글핏에서는 걸음수 데이터가 걸을때마다 생성되고 걸음이 없으면 데이터 생성X
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}