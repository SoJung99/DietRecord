package org.techtown.dietrecord;

public class ExerciseData {
    String exercise;
    String power;
    String time;

    public ExerciseData(String ex, String p, String t){
        exercise=ex;
        power=p;
        time=t+"분";
    }
}
