package org.techtown.dietrecord;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class TabFragment3 extends Fragment implements View.OnClickListener{

    /* 리사이클러뷰
    Context context;
    ArrayList<ExerciseData> list;
    */

    Spinner ex_spinner, power_spinner;
    EditText time;
    String[] ex_items = {"걷기","윗몸일으키기","달리기","스쿼트"};
    String[] power_items = {"상","중","하"};
    TextView record;

    Button voice;
    Button info;
    Button submit;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.fragment3, container, false);


        voice = (Button)v.findViewById(R.id.voice_bt);
        info = (Button)v.findViewById(R.id.exercise_info);
        submit = (Button)v.findViewById(R.id.submit_bt);
        time = (EditText)v.findViewById(R.id.time_input);

        voice.setOnClickListener(this);
        info.setOnClickListener(this);
        submit.setOnClickListener(this);


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

        /* 리사이클러 뷰
        list = new ArrayList<>();

        list.add(new ExerciseData("걷기","중","30"));
        list.add(new ExerciseData("걷기","중","30"));
        list.add(new ExerciseData("걷기","중","30"));
        list.add(new ExerciseData("걷기","중","30"));
        list.add(new ExerciseData("걷기","중","30"));

        RecyclerView recyclerView = v.findViewById(R.id.recycle);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        ExerAdapter exerAdapter = new ExerAdapter(list);
        recyclerView.setAdapter(exerAdapter);

        exerAdapter.addItem(new ExerciseData("달리기","상","20"));
        exerAdapter.notifyDataSetChanged();
        */

        record = v.findViewById(R.id.exer_record);
        record.append("\n자고 싶다.");
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }


    @Override
    public void onClick(View view) {
        if(view==voice){
            // 음성인식 처리
        }
        else if(view==info){
            new AlertDialog.Builder(getActivity()).setTitle("운동 입력 정보")
                    .setMessage("운동종류 강도 시간(분) 으로 말해주세요. 강도는 상 중 하. 시간은 30분과 같은 방법으로 말해주세요.")
                    .setNeutralButton("확인",new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dlg, int sumthin){}
                    }).show();
        }
        else if(view==submit){
            if(time.getText().toString().length()==0){
                new AlertDialog.Builder(getActivity()).setTitle("시간 미입력")
                        .setMessage("운동 시간도 입력해주세요.")
                        .setNeutralButton("확인",new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dlg, int sumthin){}
                        }).show();

            }
            else{
                ExerciseData dataset = new ExerciseData((String)ex_spinner.getSelectedItem(),(String)power_spinner.getSelectedItem(),time.getText().toString());
                record.append("\n"+dataset.exercise+"  "+dataset.power+"  "+dataset.time);

                // 데이터베이스에도 추가!

            }
        }

    }
}

