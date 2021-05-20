package org.techtown.dietrecord;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.util.ArrayList;

public class recyclerAdapter extends RecyclerView.Adapter<recyclerAdapter.Holder> {
    ArrayList<UserFood> list;
    recyclerAdapter(ArrayList<UserFood> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_recycler, parent, false);
        return new Holder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        //holder.tv.setText(list.get(position));
        holder.onBind(list.get(position));
    }
    @Override
    public int getItemCount() {
        return list.size();
    }

    void addItem(UserFood userFood) {
        // 외부에서 item을 추가시킬 함수입니다.
        list.add(userFood);
    }

    public interface OnItemClickListener{
        void OnItemClick(View v, int pos);
    }
    private OnItemClickListener mListener = null;
    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener ;
    }

    public class Holder extends RecyclerView.ViewHolder{
        TextView tv1;
        TextView tv2;
        TextView tv3;
        TextView tv4;

        Holder(View itemView){
            super(itemView);
            tv1 = itemView.findViewById(R.id.retext_kind);
            tv2 = itemView.findViewById(R.id.retext_amount);
            tv3 = itemView.findViewById(R.id.retext_unit);
            tv4 = itemView.findViewById(R.id.retext_cal);

            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION){
                        //리스터 객체의 매서드 호출
                        if(mListener != null){
                            mListener.OnItemClick(v, pos);
                        }
                    }
                }
            });
        }
        void onBind(UserFood d){
            tv1.setText(d.getFoodKind());
            tv2.setText(new BigDecimal(Float.toString(d.getFoodAmount())).stripTrailingZeros().toPlainString());
            tv3.setText(d.getUNIT());
            tv4.setText(new BigDecimal(Float.toString(d.getFoodCal())).stripTrailingZeros().toPlainString());
        }
    }

    public Float SumCalories(ArrayList<UserFood> a){
        float sum = 0;
        for(int i=0; i<a.size();i++){
            sum = sum + a.get(i).getFoodCal();
        }
        return sum;
    }
}
