package org.techtown.dietrecord;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.kakao.usermgmt.response.model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DataAdapter
{
    Calendar calendar;
    protected static final String TAG = "DataAdapter";

    // TODO : TABLE 이름을 명시해야함
    protected String TABLE_NAME = "사용자운동";

    private final Context mContext;
    private SQLiteDatabase mDb;
    private DataBaseHelper mDbHelper;

    public DataAdapter(Context context)
    {
        this.mContext = context;
        mDbHelper = new DataBaseHelper(mContext);
    }

    public DataAdapter createDatabase() throws SQLException
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

    public DataAdapter open() throws SQLException
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

    public ArrayList<ExerciseData> getTableData()
    {
        try
        {
            calendar = Calendar.getInstance();
            Integer date =calendar.get(Calendar.YEAR)*10000+(calendar.get(Calendar.MONTH)+1)*100+calendar.get(Calendar.DATE);
            System.out.println("date : "+date);
            // Table 이름 -> antpool_bitcoin 불러오기
            String sql ="SELECT * FROM " + TABLE_NAME +" WHERE 날짜="+date;

            // 모델 넣을 리스트 생성
            ArrayList<ExerciseData> userList = new ArrayList<>();

            // TODO : 모델 선언
            ExerciseData user = null;

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null)
            {
                // 칼럼의 마지막까지
                while( mCur.moveToNext() ) {

                    // TODO : 커스텀 모델 생성
                    user = new ExerciseData("걷기","상","10");

                    // TODO : Record 기술
                    // id, name, account, privateKey, secretKey, Comment
                    user.setExercise(mCur.getString(1));
                    user.setPower(mCur.getString(2));
                    user.setTime(Integer.toString(mCur.getInt(3)));
                    user.setCalories(Integer.toString(mCur.getInt(4)));


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

}
