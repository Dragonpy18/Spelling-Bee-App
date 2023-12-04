package com.example.spell;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.Manifest;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context) {
        super(context, "Userdata.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create Table Userscores(name TEXT primary key, score INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("drop table if exists Userscores");
    }

    public void insertdata(String name, int score){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("score",score);
        db.insert("Userscores", null, contentValues);
    }
    public void updateData(String name, int score){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("score",score);
        db.update("Userscores", contentValues, "name=?", new String[]{name});
    }

    public int getScorebyName(String name){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT score FROM Userscores WHERE name = ?", new String[]{name});
        if (cursor.getCount()>0 && cursor.moveToFirst()){
            try{
                return cursor.getInt(0);
            }catch(Exception e){
                return -1;
            }
        }
        return -1;
    }

    public ArrayList<String> getMaxNames(){
        ArrayList<String> names = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM Userscores ORDER BY score DESC LIMIT 3" , null);
        while (cursor.moveToNext()){
            names.add(cursor.getString(0));
        }
        return names;
    }
    public boolean nameExists(String name){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Userscores WHERE name = ?", new String[]{name});
        return cursor.getCount() > 0 ;
    }


}
