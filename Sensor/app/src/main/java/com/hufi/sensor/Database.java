package com.hufi.sensor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class Database {
    Context context;
    private String dbName = "Sensor.db";
    private String dbTable = "DiaChi";
    private String dbTableSpeechToText = "SpeechToText";

    public Database(Context context)
    {
        this.context = context;
    }

    public SQLiteDatabase openDB() {
        //return SQLiteDatabase.openOrCreateDatabase(dbName,null);
        return context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null);
    }
    /*
    public SQLiteDatabase openDB() {
        String path = Environment.getExternalStorageDirectory().getPath()+"/"+dbName;
        return SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.CREATE_IF_NECESSARY);
    }*/

    public void closeDB(SQLiteDatabase db) {
        db.close();
    }

    public void createTable() {
        SQLiteDatabase db = openDB();

        String sql = "create table if not exists " + dbTable + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT, " +
                "diachi TEXT ) ";
        db.execSQL(sql);

        sql = "create table if not exists " + dbTableSpeechToText + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT, " +
                "text TEXT ) ";
        db.execSQL(sql);

        closeDB(db);
    }

    public ArrayList<DiaChi> getDiaChiAll()	{
        SQLiteDatabase db =	openDB();
        ArrayList<DiaChi>	arr =	new	ArrayList<>();
        String	sql =	"select	*	from " + dbTable;
        Cursor csr =	db.rawQuery(sql,	null);
        if	(csr !=	null)	{
            if	(csr.moveToLast())	{
                do	{
                    String date = csr.getString(1);
                    String diachi = csr.getString(2);
                    arr.add(new	DiaChi(date, diachi));
                }	while	(csr.moveToPrevious());
            } }
        closeDB(db);
        return	arr;
    }

    public boolean insertDiaChi(DiaChi b) {
        boolean flag = false;
        SQLiteDatabase db = openDB();
        ContentValues cv = new ContentValues();
        cv.put("date", b.getDate());
        cv.put("diachi", b.getDiaChi());
        flag = db.insert(dbTable, null, cv) > 0;
        closeDB(db);
        return flag;
    }

    public void deleteDiaChiAll() {
        SQLiteDatabase db = openDB();
        db.delete(dbTable, null, null);
        db.close();
    }

    public ArrayList<SpeechToTextHistoryClass> getSpeechToTextHistoryAll()	{
        SQLiteDatabase db =	openDB();
        ArrayList<SpeechToTextHistoryClass>	arr =	new	ArrayList<>();
        String	sql =	"select	*	from " + dbTableSpeechToText;
        Cursor csr =	db.rawQuery(sql,	null);
        if	(csr !=	null)	{
            if	(csr.moveToLast())	{
                do	{
                    String date = csr.getString(1);
                    String text = csr.getString(2);
                    arr.add(new	SpeechToTextHistoryClass(date, text));
                }	while	(csr.moveToPrevious());
            } }
        closeDB(db);
        return	arr;
    }

    public boolean insertSpeechToTextHistory(SpeechToTextHistoryClass b) {
        boolean flag = false;
        SQLiteDatabase db = openDB();
        ContentValues cv = new ContentValues();
        cv.put("date", b.getDate());
        cv.put("text", b.getText());
        flag = db.insert(dbTableSpeechToText, null, cv) > 0;
        closeDB(db);
        return flag;
    }

    public void deleteSpeechToTextHistoryAll() {
        SQLiteDatabase db = openDB();
        db.delete(dbTableSpeechToText, null, null);
        db.close();
    }
}
