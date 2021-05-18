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
import android.widget.Toast;

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

public class TabFragment3 extends Fragment implements View.OnClickListener, ExerciseAdapter.ExerciseViewClickListener{


    Context context;
    ArrayList<ExerciseData> list = new ArrayList<>();

    final ExerciseAdapter adapter = new ExerciseAdapter(list);
    static int i=0;

    Spinner ex_spinner, power_spinner;
    EditText time;
    String[] ex_items = {"걷기","윗몸일으키기","달리기","스쿼트"};
    String[] power_items = {"상","중","하"};

    TextView allcal;
    TextView voice_result;

    Button voice;
    Button info;
    Button submit;
    Button voice_submit;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.fragment3, container, false);


        voice = (Button)v.findViewById(R.id.voice_bt);
        info = (Button)v.findViewById(R.id.exercise_info);
        submit = (Button)v.findViewById(R.id.submit_bt);
        time = (EditText)v.findViewById(R.id.time_input);
        allcal = (TextView)v.findViewById(R.id.cal);
        voice_submit = (Button)v.findViewById(R.id.voice_submit);
        voice_result = (TextView)v.findViewById(R.id.voice_result);

        voice.setOnClickListener(this);
        info.setOnClickListener(this);
        submit.setOnClickListener(this);
        voice_submit.setOnClickListener(this);


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



        RecyclerView recyclerView = v.findViewById(R.id.recycle);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        for(int i=0; i<5; i++) {
            list.add(new ExerciseData("걷기", "중", "30"));
        }

        recyclerView.setAdapter(adapter);
        adapter.setOnClickListener(this);

        allcal.setText(adapter.getAllCalories()+"kcals");



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
            // voice_result 에 음성인식 결과 저장.
        }
        else if(view==voice_submit){
            // 데이터베이스에 해당 운동 기록 저장
            // voice_result 를 다시 초기화해줌
            // adapter의 list에도 기록 추가후 notifyDataSetChanged()를 통해 화면에 해당 기록 추가
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
                /*new AlertDialog.Builder(getActivity()).setTitle("시간 미입력")
                        .setMessage("운동 시간도 입력해주세요.")
                        .setNeutralButton("확인",new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dlg, int sumthin){}
                        }).show();
                */
                Toast.makeText(getActivity(), "운동 시간을 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
            else{
                ExerciseData dataset = new ExerciseData((String)ex_spinner.getSelectedItem(),(String)power_spinner.getSelectedItem(),time.getText().toString());
                //record.append("\n"+dataset.exercise+"  "+dataset.power+"  "+dataset.time);
                list.add(0,dataset);
                adapter.notifyDataSetChanged();
                time.setText(null);
                allcal.setText(adapter.getAllCalories()+"kcals");
                // 데이터베이스에도 추가!

            }
        }

    }

    @Override
    public void onItemClicked(int position) {

    }

    @Override
    public void onItemLongClicked(int position) {
        final int p = position;
        new AlertDialog.Builder(getActivity()).setTitle("기록 삭제")
                .setMessage("해당 운동 기록을 삭제하시겠습니까?")
                .setPositiveButton("네",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dlg, int sumthin){
                        adapter.remove(p);
                        allcal.setText(adapter.getAllCalories()+"kcals");
                    }
                }).setNegativeButton("아니오",new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dlg, int sumthin) { }
        }).show();
    }
}
