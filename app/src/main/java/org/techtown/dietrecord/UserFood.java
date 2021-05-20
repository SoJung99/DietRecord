package org.techtown.dietrecord;

public class UserFood {
    //id, foodKind, foodAmount, UNIT, foodCal. 5개
    public int id;
    //public String meal; // 아침 점심 저녁
    public String foodKind;
    public float foodAmount;
    public String UNIT;      // 조각,개,컵,인분
    public float foodCal;
    //public float carb; //탄
    //public float prot; //단
    //public float fat; //지
    //public String date;

    public UserFood(){
    }

    public UserFood(int id, String foodKind, float foodAmount, String UNIT, float foodCal) {
        this.id = id;
        this.foodKind = foodKind;
        this.foodAmount = foodAmount;
        this.UNIT = UNIT;
        this.foodCal = foodCal;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFoodKind() {
        return foodKind;
    }

    public void setFoodKind(String foodKind) {
        this.foodKind = foodKind;
    }

    public float getFoodAmount() {
        return foodAmount;
    }

    public void setFoodAmount(float foodAmount) {
        this.foodAmount = foodAmount;
    }

    public String getUNIT() {
        return UNIT;
    }

    public void setUNIT(String UNIT) {
        this.UNIT = UNIT;
    }

    public float getFoodCal() {
        return foodCal;
    }

    public void setFoodCal(float foodCal) {
        this.foodCal = foodCal;
    }
    /*
        @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("foodData{");
        sb.append("id=").append(id);
        sb.append(", meal='").append(meal).append('\'');
        sb.append(", food='").append(kind).append('\'');
        sb.append(", amount='").append(amount).append('\'');
        sb.append(", unit='").append(unit).append('\'');
        sb.append(", calories='").append(calories).append('\'');
        sb.append('}');
        return sb.toString();
    }
     */
}
