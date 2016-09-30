package com.lokman.sqlitedb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;
import com.google.gson.Gson;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by lokmannicholas on 11/9/16.
 */
public class SQLiteDB extends SQLiteOpenHelper{
    private  Field[] fields;
    private static final int DATABASE_VERSION = 7;
    private static final String DATABASE_NAME = "mydata.db";
    private String table_name = "";
    private String[] table_fields;


    public SQLiteDB(Context context, String tablename,Field[] fields) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.table_name = tablename;
        this.fields = fields;
    }

    public void createTable(){
        try{

            Field[] fields= this.fields;
            String [] table_field = new String[fields.length];
            table_fields = new String[fields.length+1];
            int count =0;
            for(Field field : fields){
                table_fields[count] = field.getName();
                table_field[count]= String.format(" %s %s ",field.getName(),SQLiteType.getStoreType(field.getGenericType()));
                count++;
            }
            table_fields[table_fields.length] = "sqlite_storeID";
            String allFields = Arrays.toString(table_field);
            String sql =String.format(" CREATE TABLE IF NOT EXISTS %s ( sqlite_storeID INTEGER PRIMARY KEY , %s );",this.table_name, allFields.substring(1,allFields.length()-2));


            this.getWritableDatabase().execSQL(sql);
        }catch(Exception exp){

        }

    }
    public long insert(ContentValues mContentValues){
        return this.getWritableDatabase().insert(this.table_name, null,
                mContentValues);
    }
    public long update(ContentValues mContentValues,long id){
        String [] where_arg= new String[1];
        where_arg[0] = String.valueOf(id);
        return this.getWritableDatabase().update(this.table_name,
                mContentValues, "sqlite_storeID = ?", where_arg);
    }
    public Cursor load(long id){
        String [] selection_args= new String[1];
        selection_args[0] = String.valueOf(id);


        Cursor cursor =  this.getReadableDatabase().query(this.table_name,table_fields,"sqlite_storeID = ?",selection_args,null,null,"sqlite_storeID","1");

        if(cursor.moveToFirst()){
            return cursor;
        }
        return null;
    }
    public Cursor list(int limit){

        Cursor cursor =  this.getReadableDatabase().query(this.table_name,table_fields,null,null,null,null,"sqlite_storeID",limit>0?String.valueOf(limit):null);

        if(cursor!=null){
            return cursor;
        }
        return null;
    }
    public boolean delete(long id){
        String [] where_arg= new String[1];
        where_arg[0] = String.valueOf(id);
        return this.getWritableDatabase().delete(this.table_name, "sqlite_storeID = ?",
                where_arg) >0;
    }
    public void onCreate(android.database.sqlite.SQLiteDatabase db) {
        createTable();
    }
    public void onUpgrade(android.database.sqlite.SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        dropTable();
        onCreate(db);
    }
    public void dropTable(){
        this.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + this.table_name);
    }
    public void onDowngrade(android.database.sqlite.SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
