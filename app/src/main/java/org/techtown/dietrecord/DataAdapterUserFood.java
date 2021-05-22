package org.techtown.dietrecord;


import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

//import com.kakao.usermgmt.response.model.User;

public class DataAdapterUserFood
{
    protected static final String TAG = "DataAdapterUserFood";

    // TODO : TABLE 이름을 명시해야함
    protected String TABLE_NAME = "사용자식단";

    private final Context mContext;
    private SQLiteDatabase mDb;
    private DataBaseHelper mDbHelper;

    public DataAdapterUserFood(Context context)
    {
        this.mContext = context;
        mDbHelper = new DataBaseHelper(mContext);
    }

    public DataAdapterUserFood createDatabase() throws SQLException
    {
        try
        {
            mDbHelper.createDataBase();
        }
        catch (IOException mIOException)
        {
            Log.e(TAG, mIOException.toString() + "  UnableToCreateDatabase");
            throw new Error("UnableToCreateDatabase");
        }
        return this;
    }

    public DataAdapterUserFood open() throws SQLException
    {
        try
        {
            mDbHelper.openDataBase();
            mDbHelper.close();
            mDb = mDbHelper.getReadableDatabase();
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "open >>"+ mSQLException.toString());
            throw mSQLException;
        }
        return this;
    }
    public void setTable(String tableName){
        TABLE_NAME = tableName;
    }
    public void close()
    {
        mDbHelper.close();
    }

    public ArrayList<UserFood> getTableData() // 모두 불러오기
    {
        try
        {
            // Table 이름 -> antpool_bitcoin 불러오기
            String sql ="SELECT * FROM " + TABLE_NAME; //+ " WHERE 날짜 = '"+getDate()+"'";

            // 모델 넣을 리스트 생성
            ArrayList<UserFood> userList = new ArrayList<>();

            // TODO : 모델 선언
            UserFood user = null;

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null)
            {
                // 칼럼의 마지막까지
                while( mCur.moveToNext() ) {

                    // TODO : 커스텀 모델 생성
                    user = new UserFood();

                    // TODO : Record 기술
                    // id, name, account, privateKey, secretKey, Comment

                    //user.setId(mCur.getInt(0));
                    //user.setMeal(mCur.getString(1));
                    //user.setFoodKind(mCur.getString(2));
                    user.setFoodAmount(mCur.getFloat(3));
                    //user.setUNIT(mCur.getString(4));
                    user.setFoodCal(mCur.getFloat(5));
                    user.setCarb(mCur.getFloat(6));
                    user.setProt(mCur.getFloat(7));
                    user.setFat(mCur.getFloat(8));
                    user.setDate(mCur.getInt(9));

                    // 리스트에 넣기
                    userList.add(user);
                }

            }
            return userList;
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "getTestData >>"+ mSQLException.toString());
            throw mSQLException;
        }
    }

    public ArrayList<UserFood> getBreakfast()
    {
        try
        {
            // Table 이름 -> antpool_bitcoin 불러오기
            String sql ="SELECT * FROM " + TABLE_NAME + " WHERE 식사 = '아침' AND 날짜 = '"+getDate()+"'";

            // 모델 넣을 리스트 생성
            ArrayList<UserFood> userList = new ArrayList<>();

            // TODO : 모델 선언
            UserFood user = null;

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null)
            {
                // 칼럼의 마지막까지
                while( mCur.moveToNext() ) {

                    // TODO : 커스텀 모델 생성
                    user = new UserFood();

                    // TODO : Record 기술
                    //id,kind,amount,calories,unit
                    user.setId(mCur.getInt(0));
                    user.setFoodKind(mCur.getString(2));
                    user.setFoodAmount(mCur.getFloat(3));
                    user.setUNIT(mCur.getString(4));
                    user.setFoodCal(mCur.getFloat(5));

                    /*Cursor cur = mDb.rawQuery("SELECT * FROM 음식정보 WHERE 음식구분='"+mCur.getString(2)+"'", null);
                    if(cur!=null){
                        user.setUNIT(cur.getString(5));
                    }*/

                    // 리스트에 넣기
                    userList.add(user);
                }

            }
            return userList;
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "getTestData >>"+ mSQLException.toString());
            throw mSQLException;
        }
    }

    public ArrayList<UserFood> getLunch()
    {
        try
        {
            // Table 이름 -> antpool_bitcoin 불러오기
            String sql ="SELECT * FROM " + TABLE_NAME + " WHERE 식사 = '점심' AND 날짜 = '"+getDate()+"'";

            // 모델 넣을 리스트 생성
            ArrayList<UserFood> userList = new ArrayList<>();

            // TODO : 모델 선언
            UserFood user = null;

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null)
            {
                // 칼럼의 마지막까지
                while( mCur.moveToNext() ) {

                    // TODO : 커스텀 모델 생성
                    user = new UserFood();

                    // TODO : Record 기술
                    //id,kind,amount,calories,unit
                    user.setId(mCur.getInt(0));
                    user.setFoodKind(mCur.getString(2));
                    user.setFoodAmount(mCur.getFloat(3));
                    user.setUNIT(mCur.getString(4));
                    user.setFoodCal(mCur.getFloat(5));
                    // 리스트에 넣기
                    userList.add(user);
                }

            }
            return userList;
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "getTestData >>"+ mSQLException.toString());
            throw mSQLException;
        }
    }
    public ArrayList<UserFood> getDinner()
    {
        try
        {
            // Table 이름 -> antpool_bitcoin 불러오기
            String sql ="SELECT * FROM " + TABLE_NAME + " WHERE 식사 = '저녁' AND 날짜 = '"+getDate()+"'";

            // 모델 넣을 리스트 생성
            ArrayList<UserFood> userList = new ArrayList<>();

            // TODO : 모델 선언
            UserFood user = null;

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null)
            {
                // 칼럼의 마지막까지
                while( mCur.moveToNext() ) {

                    // TODO : 커스텀 모델 생성
                    user = new UserFood();

                    // TODO : Record 기술
                    //id,kind,amount,calories,unit
                    user.setId(mCur.getInt(0));
                    user.setFoodKind(mCur.getString(2));
                    user.setFoodAmount(mCur.getFloat(3));
                    user.setUNIT(mCur.getString(4));
                    user.setFoodCal(mCur.getFloat(5));
                    // 리스트에 넣기
                    userList.add(user);
                }

            }
            return userList;
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "getTestData >>"+ mSQLException.toString());
            throw mSQLException;
        }
    }

    public int getDate(){
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int date = year*10000 + month*100 + day;

        return date;
    }

}
