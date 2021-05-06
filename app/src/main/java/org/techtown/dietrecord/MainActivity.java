package org.techtown.dietrecord;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.KakaoAdapter;
import com.kakao.auth.KakaoSDK;

import com.kakao.sdk.newtoneapi.SpeechRecognizeListener;
import com.kakao.sdk.newtoneapi.SpeechRecognizerClient;
import com.kakao.sdk.newtoneapi.SpeechRecognizerManager;
import com.kakao.sdk.newtoneapi.impl.util.PermissionUtils;


import java.security.MessageDigest;
import java.util.ArrayList;
public class MainActivity extends AppCompatActivity implements View.OnClickListener, SpeechRecognizeListener {


    private SpeechRecognizerClient client;

    public class MyApplication extends Application {
        @Override
        public void onCreate() {
            super.onCreate();

            // SDK 초기화

            KakaoSDK.init(new KakaoAdapter() {

                @Override
                public IApplicationConfig getApplicationConfig() {
                    return new IApplicationConfig() {
                        @Override
                        public Context getApplicationContext() {
                            return MyApplication.this;
                        }
                    };
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new MyApplication();
        getAppKeyHash();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO) && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                // 사용자가 거부하면서 다시 묻지 않기를 클릭.. 권한이 없다고 사용자에게 직접 알림.
            }
        } else {
            //startUsingSpeechSDK();
        }

        checkPermissions();

        new SpeechRecognizerManager().getInstance().initializeLibrary(this);

        findViewById(R.id.button).setOnClickListener(this);
    }
    private void getAppKeyHash(){
        try{
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for(Signature signature : info.signatures){
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                Log.e("Hash key", something);
            }
        } catch(Exception e){
            Log.e("name not found", e.toString());
        }
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
            permissionCheck = ContextCompat.checkSelfPermission(this,permissions[i]);
            if(permissionCheck == PackageManager.PERMISSION_DENIED){
                System.out.println("권한 없음 : "+permissions[i]);
                 ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},1);
                 ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                //i--;
            }
            else
                System.out.println("권한 있음 : "+permissions[i]);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        String serviceType = SpeechRecognizerClient.SERVICE_TYPE_WORD;
        Log.i("MainActivity", "ServiceType : "+serviceType);

        if(id == R.id.button){

            SpeechRecognizerClient.Builder builder = new SpeechRecognizerClient.Builder().
                    setServiceType(serviceType).
                    setUserDictionary("걷기 상\n걷기 중\n걷기 하\n달리기 상\n달리기 중\n달리기 하\n줄넘기 상\n줄넘기 중\n줄넘기 하\n수영 상\n수영 중\n수영 하\n" +
                            "사이클 상\n사이클 중\n사이클 하\n요가 상\n요가 중\n요가 하\n런지 상\n런지 중\n런지 하\n스쿼트 상\n스쿼트 중\n스쿼트 하\n윗몸일으키기 상\n윗몸일으키기 중\n윗몸일으키기 하\n" +
                            "푸쉬업 상\n푸쉬업 중\n푸쉬업 하\n등산 상\n등산 중\n등산 하\n댄스 상\n댄스 중\n댄스 하\n훌라후프 상\n훌라후프 중\n훌라후프 하\n버피 상\n버피 중\n버피 하\n" +
                            "플랭크 상\n플랭크 중\n플랭크 하\n팔벌려뛰기 상\n팔벌려뛰기 중\n팔벌려뛰기 하\n풀업 상\n풀업 중\n풀업 하\n계단오르기 상\n계단오르기 중\n계단오르기 하\n" +
                            "에어로빅 상\n에어로빅 중\n에어로빅 하\n파워워킹 상\n파워워킹 중\n파워워킹 하\n딥스 상\n딥스 중\n딥스 하\n벤치프레스 상\n벤치프레스 중\n벤치프레스 하\n" +
                            "로잉머신 상\n로잉머신 중\n로잉머신 하\n짐볼운동 상\n짐볼운동 중\n짐볼운동 하\n복싱 상\n복싱 중\n복싱 하\n케틀벨 상\n케틀벨 중\n케틀벨 하\n" +
                            "농구 상\n농구 중\n농구 하\n테니스 상\n테니스 중\n테니스 하\n축구 상\n축구 중\n축구 하\n탁구 상\n탁구 중\n탁구 하");


            /*
             //음식 음성인식
            SpeechRecognizerClient.Builder builder = new SpeechRecognizerClient.Builder().
                    setServiceType(serviceType).
                    setUserDictionary("물\n사과\n귤\n딸기\n바나나\n배\n토마토\n수박\n아몬드\n밥\n된장찌개\n김치찌개\n콩나물국\n북어국\n" +
                    "소갈비찜\n돼지불고기\n김치볶음\n장조림\n멸치볶음\n잡채\n김치전\n파전\n시금치나물\n배추김치\n깍두기\n갈치구이\n고등어구이\n" +
                    "갈비탕\n설렁탕\n삼계탕\n떡국\n칼국수\n물냉면\n비빔냉면\n햄버거\n치즈버거\n생크림케이크\n초콜릿케이크\n도넛\n베이글\n"+
                    "식빵\n바게트\n달걀프라이\n짜장면\n짬뽕\n탕수육\n우동\n군만두\n라면\n스파게티\n떡볶이\n치킨\n콘샐러드\n치킨샐러드\n감자샐러드\n"+
                    "백설기\n삶은달걀\n아보카도\n당근\n양상추\n연어\n다시마\n브로콜리\n소고기등심\n군고구마\n닭가슴살\n두부\n단호박\n시리얼\n오이\n"+
                    "요거트\n샐러리\n호밀빵\n양배추\n파프리카\n곤약\n백김치\n삶은감자\n야채죽\n한개\n두개\n세개\n네개\n다섯개\n여섯개\n일곱개\n여덟개\n아홉개\n열개\n반개\n한컵\n두컵\n세컵\n네컵\n다섯컵\n반컵\n여섯컵\n일곱컵\n여덟컵\n아홉컵\n" +
                            "열컵\n1인분\n2인분\n3인분\n4인분\n5인분\n6인분\n7인분\n8인분\n9인분\n10인분\n반인분\n하나\n둘\n셋\n넷\n다섯\n여섯\n일곱\n여덟\n아홉\n열\n반\n한조각\n두조각\n세조각\n네조각\n다섯조각\n여섯조각\n일곱조각\n" +
                            "여덟조각\n아홉조각\n열조각\n반조각\n북어국 1인분\n북어국 2인분\n북어국 반인분\n김치볶음 1인분\n김치볶음 2인분\n김치볶음 반인분\n소갈비찜 1인분\n소갈비찜 2인분\n소갈비찜 반인분\n" +
                            "돼지불고기 1인분\n돼지불고기 2인분\n돼지불고기 반인분\n멸치볶음 1인분\n멸치볶음 2인분\n멸치볶음 반인분");
            */

            client = builder.build();
            client.setSpeechRecognizeListener(this);

            client.startRecording(true);

            Toast.makeText(this, "음성인식을 시작합니다.", Toast.LENGTH_SHORT).show();
            
            findViewById(R.id.button).setEnabled(false);
        }
    }
    public void onDestroy() {
        super.onDestroy();

        SpeechRecognizerManager.getInstance().finalizeLibrary();
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

    }

    @Override
    public void onPartialResult(String partialResult) {

    }

    @Override
    public void onResults(Bundle results) {
        final StringBuilder builder = new StringBuilder();
        ArrayList<String> texts = results.getStringArrayList(SpeechRecognizerClient.KEY_RECOGNITION_RESULTS);

        builder.append(texts.get(0));

        TextView result = findViewById(R.id.result);
        result.setText(builder.toString());

        findViewById(R.id.button).setEnabled(true);
    }

    @Override
    public void onAudioLevel(float audioLevel) {

    }

    @Override
    public void onFinished() {

    }
}

