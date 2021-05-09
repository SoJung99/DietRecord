package org.techtown.dietrecord;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ExerAdapter extends RecyclerView.Adapter<Holder>{
    ArrayList<ExerciseData> list;

    ExerAdapter(ArrayList<ExerciseData> list){
        this.list = list;
    }

    ExerAdapter(){

    }

    void addItem(ExerciseData data){
        list.add(data);
    }


    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.exercise_data,parent,false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.exer.setText(list.get(position).exercise);
        holder.power.setText(list.get(position).power);
        holder.time.setText(list.get(position).time);

    }


    @Override
    public int getItemCount() {
        return 0;
    }
}

class Holder extends RecyclerView.ViewHolder{
    TextView exer;
    TextView power;
    TextView time;

    public Holder(@NonNull View exercise_Data){
        super(exercise_Data);
        exer = itemView.findViewById(R.id.exercise);
        power = itemView.findViewById(R.id.power);
        time = itemView.findViewById(R.id.time);
    }

}