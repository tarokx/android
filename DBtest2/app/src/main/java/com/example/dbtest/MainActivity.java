package com.example.dbtest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.textview);

        helper = new DatabaseHelper(this);
        try {
            helper.createDatabase();
        }
        catch (IOException e) {
            throw new Error("Unable to create database");
        }
        StringBuilder builder = new StringBuilder();

        SQLiteDatabase db = helper.getWritableDatabase();
/*
        ContentValues cv = new ContentValues();
        cv.put("seq", 1);
        cv.put("long", getToday());
        cv.put("address", "1");
        db.insert("addressbook", "11",cv);
*/


        String sql = "select * from addressbook";

        try {
            Cursor cursor = db.rawQuery(sql, null);
            while(cursor.moveToNext()) {
                builder.append(cursor.getInt(0) + " ");
                builder.append(cursor.getString(1) + " ");
                builder.append(cursor.getString(2) + "\n");
            }
        } finally {
            db.close();
        }
        textView.setText(builder.toString());
    }
    private String getToday() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        return sdf.format(date);
    }
}