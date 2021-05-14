package org.techtown.dietrecord;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ViewHolder>{

    private ArrayList<ExerciseData> exerciseData;

    public ExerciseAdapter(ArrayList<ExerciseData> exerciseData){
        this.exerciseData = exerciseData;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView exer;
        TextView power;
        TextView time;
        TextView cal;

        ViewHolder(View exercise_data) {
            super(exercise_data);
            exer = itemView.findViewById(R.id.exer);
            power = itemView.findViewById(R.id.power);
            time = itemView.findViewById(R.id.time);
            cal = itemView.findViewById(R.id.calories);
        }
    }

    public interface ExerciseViewClickListener{
        void onItemClicked(int position);
        void onItemLongClicked(int position);
    }

    private ExerciseViewClickListener mListener;

    public void setOnClickListener(ExerciseViewClickListener listener){
        this.mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.exercise_data,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExerciseData data = exerciseData.get(position);
        holder.exer.setText(data.getExercise());
        holder.power.setText(data.getPower());
        holder.time.setText(data.getTime()+"분");
        holder.cal.setText(data.getCalories()+"kcals");

        if(mListener != null){
            final int pos = position;
            holder.itemView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    mListener.onItemClicked(pos);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener(){

                @Override
                public boolean onLongClick(View view) {
                    mListener.onItemLongClicked(pos);
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return exerciseData.size();
    }

    public void remove(int position){
        try{
            exerciseData.remove(position);
            notifyDataSetChanged();
        }catch(IndexOutOfBoundsException e){
            e.printStackTrace();
        }
    }

    public String getAllCalories(){
        int a=0;
        for(int i=0; i<getItemCount(); i++){
            a+=Integer.parseInt(exerciseData.get(i).calories);
        }

        return Integer.toString(a);
    }
}
