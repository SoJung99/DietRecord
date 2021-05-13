package org.techtown.dietrecord;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class TabFragment2 extends Fragment {
    String food_time;  // 선택된 식사 시간대
    String food_kind;  // 선택된 음식 종류
    String food_amount;// 선택된 음식 양

    String voice_food_time = "(식사)";      // 음성인식 식사 시간대
    String voice_food_kind = "(음식 종류)"; // 음성인식 음식 종류
    String voice_food_amount = "(양)";     // 음성인식 음식 양

    String cal = "0kcal"; //////////

    GridView gridView;  // 먹은 아침식단 그리드뷰
    GridView gridView2; // 먹은 점심식단 그리드뷰
    GridView gridView3; // 먹은 저녁식단 그리드뷰
    List<String> list = new ArrayList<>(); // 점심 리스트
    List<String> list2 = new ArrayList<>(); // 점심 리스트
    List<String> list3 = new ArrayList<>(); // 저녁 리스트
    ArrayAdapter<String> adapter;
    ArrayAdapter<String> adapter2;
    ArrayAdapter<String> adapter3;

    Spinner spinner; // 식사 시간대 선택 박스
    Spinner spinner2;// 음식 종류 선택 박스
    Spinner spinner3;// 음식 양 선택 박스

    String food_voiceExample; //////////
    String[] food_voiceExampleResult; //////////

    TextView tv_voice_result;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment2, container, false);

        tv_voice_result = (TextView)v.findViewById(R.id.textView_voice);

        gridView = (GridView)v.findViewById(R.id.breakfastList);
        gridView2 = (GridView)v.findViewById(R.id.lunchList);
        gridView3 = (GridView)v.findViewById(R.id.dinnerList);
        // 어댑터: 리스트랑 그리드뷰를 연결해줌
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, list);
        gridView.setAdapter(adapter); // list에 어댑터를 셋팅
        adapter2 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, list2);
        gridView2.setAdapter(adapter2);
        adapter3 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, list3);
        gridView3.setAdapter(adapter3);



        // 음성인식 버튼 (btn_voice 눌렸을 때)
        Button btnVoice = (Button)v.findViewById(R.id.btn_voice);
        btnVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                food_voiceExample = "아침 떡볶이 1인분"; //////////
                food_voiceExampleResult = food_voiceExample.split(" ", 3);

                voice_food_time = food_voiceExampleResult[0];
                voice_food_kind = food_voiceExampleResult[1];
                voice_food_amount = food_voiceExampleResult[2];

                //음성인식 버튼 옆에 있는 text
                //tv_voice_result = (TextView)v.findViewById(R.id.textView_voice); 이거 선언을 저기 위에다 하니까 앱이 안꺼지넹!
                tv_voice_result.setText(voice_food_time + "/" + voice_food_kind +"/" + voice_food_amount);
            }
        });

        // 추가(음성인식) 버튼 (btn_voiceAdd 눌렸을 때)
        Button btnVoiceAdd = (Button)v.findViewById(R.id.btn_voiceAdd);
        btnVoiceAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(voice_food_time.equals("(식사)") || voice_food_kind.equals("(음식 종류)") || voice_food_amount.equals("(양)")){
                    Toast.makeText(getActivity(), "음성인식을 (다시) 시도해 주세요!", Toast.LENGTH_SHORT).show();/////////
                }
                // + else if    voice_food_kind 가 DB에 없으면 "목록상에 없는 음식입니다!" toast
                else{
                    // + DB에 추가
                    if(voice_food_time.equals("아침")){ // 아침 리스트에 추가
                        list.add(voice_food_kind + " " + voice_food_amount + "\n" + cal); // + "단위"
                        adapter.notifyDataSetChanged();
                    }
                    else if(voice_food_time.equals("점심")){ // 점심 리스트에 추가
                        list2.add(voice_food_kind + " " + voice_food_amount + "\n" + cal); // + "단위"
                        adapter2.notifyDataSetChanged();
                    }
                    else { //voice_food_time.equals("저녁") // 저녁 리스트에 추가
                        list3.add(voice_food_kind + " " + voice_food_amount + "\n" + cal); // + "단위"
                        adapter3.notifyDataSetChanged();
                    }

                    voice_food_time = "(식사)";
                    voice_food_kind = "(음식 종류)";
                    voice_food_amount = "(양)";
                    //tv_result_voice = (TextView)findViewById(R.id.textView_voice);
                    tv_voice_result.setText(voice_food_time + "/" + voice_food_kind +"/" + voice_food_amount); // + "단위"
                }
            }
        });


        // 추가(직접입력)버튼 (btn_add 눌렸을 때)
        Button btnAdd = (Button)v.findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    // + DB에 추가하기
                    if(food_time.equals("아침")){
                        list.add(food_kind + " " + food_amount + "\n" + cal); // + "단위"
                        adapter.notifyDataSetChanged(); // 이 상태를 저장
                        //Toast.makeText(getActivity(), "아침 추가완료", Toast.LENGTH_SHORT).show();//지우기//////////
                    }
                    else if(food_time.equals("점심")){
                        list2.add(food_kind + " " + food_amount + "\n" + cal); // + "단위"
                        adapter2.notifyDataSetChanged();
                        //Toast.makeText(getActivity(), "점심 추가완료", Toast.LENGTH_SHORT).show();//지우기//////////
                    }
                    else { //food_time.equals("저녁")
                        list3.add(food_kind + " " + food_amount + "\n" + cal); // + "단위"
                        adapter3.notifyDataSetChanged();
                        //Toast.makeText(getActivity(), "저녁 추가완료", Toast.LENGTH_SHORT).show();//지우기//////////
                    }
                }

            }
        });


        // 리스트 누르면 삭제
        // + DB에 삭제하기
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("기록 삭제")
                        .setMessage("해당 음식 기록을 삭제하시겠습니까?")
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {  // 아침 리스트 삭제
                                list.remove(position);
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .setNeutralButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        });
        gridView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("기록 삭제")
                        .setMessage("해당 음식 기록을 삭제하시겠습니까?")
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { // 점심 리스트 삭제
                                list2.remove(position);
                                adapter2.notifyDataSetChanged();
                            }
                        })
                        .setNeutralButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        });
        gridView3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("기록 삭제")
                        .setMessage("해당 음식 기록을 삭제하시겠습니까?")
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { // 저녁 리스트 삭제
                                list3.remove(position);
                                adapter3.notifyDataSetChanged();
                            }
                        })
                        .setNeutralButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        });



        // 음식별 정보 보기 버튼 (btn_info 버튼 눌렸을 때)
        Button btnInfo = (Button)v.findViewById(R.id.btn_info);
        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("음식별 정보")
                        .setMessage("내용 넣기")
                        .setNeutralButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
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
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
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
                        .setMessage("※ '식사_음식 종류_양(인분/개/조각)' 순으로 말해주세요.\n※ 음식별 단위는 '음식별 정보 보기' 버튼을 눌러 참고하세요.\n예) 저녁 떡볶이 1인분\n예) 아침 사과 1조각")
                        .setNeutralButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        });


        // 식사 시간대 선택 박스
        spinner = (Spinner)v.findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { //아이템 클릭했을 때
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                food_time = spinner.getSelectedItem().toString();
                //Toast.makeText(getActivity(), food_time + "==" + parent.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();//지우기///////
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        // 음식 종류 선택 박스
        spinner2 = (Spinner)v.findViewById(R.id.spinner2);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                food_kind = spinner2.getSelectedItem().toString();
                //Toast.makeText(getActivity(), "종류: " + food_kind, Toast.LENGTH_SHORT).show();//지우기///////
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        // 음식 양 선택 박스
        spinner3 = (Spinner)v.findViewById(R.id.spinner3);
        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                food_amount = spinner3.getSelectedItem().toString();
                //Toast.makeText(getActivity(), "양: " + food_amount, Toast.LENGTH_SHORT).show();//지우기///////
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return v;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

}