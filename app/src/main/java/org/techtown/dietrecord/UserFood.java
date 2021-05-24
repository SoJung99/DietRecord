package org.techtown.dietrecord;

public class UserFood {
    //id, foodKind, foodAmount, UNIT, foodCal.
    public int id;
    public String meal; // 아침 점심 저녁
    public String foodKind;
    public float foodAmount;
    public String UNIT;      // 조각,개,컵,인분
    public float foodCal;
    public float carb; //탄
    public float prot; //단
    public float fat; //지
    public int date;

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

    public String getMeal() {
        return meal;
    }

    public void setMeal(String meal) {
        this.meal = meal;
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

    public float getCarb() {
        return carb;
    }

    public void setCarb(float carb) {
        this.carb = carb;
    }

    public float getProt() {
        return prot;
    }

    public void setProt(float prot) {
        this.prot = prot;
    }

    public float getFat() {
        return fat;
    }

    public void setFat(float fat) {
        this.fat = fat;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }
}
