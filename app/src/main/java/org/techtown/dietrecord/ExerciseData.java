package org.techtown.dietrecord;

public class ExerciseData {
    String exercise;
    String power;
    String time;
    String calories;

    public ExerciseData(String ex, String p, String t){
        exercise=ex;
        power=p;
        time=t;

        // 이건 계산해서 넣기
        calories="100";
    }

    public String getExercise(){
        return exercise;
    }

    public String getPower(){
        return power;
    }

    public String getTime(){
        return time;
    }

    public String getCalories(){
        return calories;
    }

    public void setExercise(String a){
        exercise=a;
    }

    public void setPower(String b){
        power=b;
    }

    public void setTime(String c){
        time = c;
    }

    public void setCalories(String d){
        calories=d;
    }

}
