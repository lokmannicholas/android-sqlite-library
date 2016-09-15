package com.lokman.sqlitedb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Created by lokmannicholas on 11/9/16.
 */
public  class SQLiteObject {
    private static SQLiteDB databaseHelper= null;
    private long sqlite_storeID=0;
    private String[] table_fields;
    private Field[] fields;
    private Context context;
    protected SQLiteObject(){
        int count=0;
        fields = this.getClass().getDeclaredFields();
        table_fields = new String[fields.length];
        for(Field field : fields){
            table_fields[count++] = field.getName();
        }

    }


    public long getSID(){
        return this.sqlite_storeID;
    }
    public void initSQLite(Context context){
        this.context = context;
        databaseHelper= new SQLiteDB(context,this.getClass().getSimpleName(),fields);
        databaseHelper.createTable();
    }
    public void release(){
        databaseHelper.dropTable();
        databaseHelper = null;
        sqlite_storeID = 0;
        table_fields= null;
        fields = null;
    }
    public boolean save() throws JSONException {
        if(databaseHelper==null)return false;

        ContentValues mContentValues = new ContentValues();
        JSONObject mJSONObject = this.toJSON(this);
        Iterator<String> iter = mJSONObject.keys();

        while (iter.hasNext()) {
            String key = iter.next();
                if(Arrays.asList(table_fields).contains(key)){
                    Object value = mJSONObject.get(key);
                    if (value.getClass().equals(Bitmap.class)) {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        ((Bitmap)value).compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                        byte[] byteArray = byteArrayOutputStream .toByteArray();
                        value = Base64.encodeToString(byteArray, Base64.DEFAULT);

                        mContentValues.put(key, String.valueOf(value));
                    }else if (value.getClass().equals(Integer.class)){
                        mContentValues.put(key, Integer.valueOf((Integer) value));
                    }else if (value.getClass().equals(List.class) || value.getClass().equals(ArrayList.class)){
                        JSONArray jsonarry = new JSONArray();
                        for (Object i : (List<Object>) value){
                            jsonarry.put(toJSON(i));
                        }
                        mContentValues.put(key,jsonarry.toString());
                    }else if (value.getClass().isArray()){
                        JSONArray jsonarry = new JSONArray();
                        for (Object i : (Object[]) value){
                            jsonarry.put(toJSON(i));
                        }

                        mContentValues.put(key,jsonarry.toString());
                    }else {
                        mContentValues.put(key,String.valueOf(value));
                    }

                }
        }

        sqlite_storeID =  databaseHelper.insert(mContentValues);
        return sqlite_storeID>0?true:false;
    }

    public String load(){
        if(databaseHelper==null)return null;
        Cursor single_cursor =  databaseHelper.load(this.sqlite_storeID);

        int totalColumn = single_cursor.getColumnCount();
        JSONObject rowObject = new JSONObject();
        for (int i = 0; i < totalColumn; i++) {
            if (single_cursor.getColumnName(i) != null) {
                try {
                    if(single_cursor.getClass().equals(String.class))
                    rowObject.put(single_cursor.getColumnName(i),
                            single_cursor.getString(i));
                } catch (Exception e) {

                }
            }
        }
        return rowObject.toString();
    }

    public boolean delete(){
        if(databaseHelper==null)return false;
        return databaseHelper.delete(this.sqlite_storeID);
    }

    //get plan data
    public static List<Object> getList(int limit,Class<? extends SQLiteObject> cls){
        List<Object> list_of_result = new ArrayList<>();
        Cursor multi_cursor = databaseHelper.list(limit);
        while(multi_cursor.moveToNext()){
            int totalColumn = multi_cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            for (int i = 0; i < totalColumn; i++) {
                if (multi_cursor.getColumnName(i) != null) {
                    try {
//                        rowObject.put(multi_cursor.getColumnName(i),
//                                multi_cursor.getString(i));
                    } catch (Exception e) {

                    }
                }
            }

            list_of_result.add(convertFromJSON(rowObject.toString(),cls));
        }

        return list_of_result;
    }


    public String toJSON(){

        return this.toJSON(this).toString();
    }
    public JSONObject toJSON(Object vObject){
        JSONObject jobject = new JSONObject();
        try{
            for(Field field : vObject.getClass().getDeclaredFields()){
                field.setAccessible(true);
                Object value = field.get(vObject);
                if (value.getClass().equals(Bitmap.class)) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ((Bitmap)value).compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream .toByteArray();
                    value = Base64.encodeToString(byteArray, Base64.DEFAULT);
                    jobject.put(field.getName(), value);
                }else if (value.getClass().equals(Integer.class)){
                    jobject.put(field.getName(), Integer.valueOf((Integer) value));
                }else if (value.getClass().equals(List.class) || value.getClass().equals(ArrayList.class)){
                    JSONArray jsonarry = new JSONArray();
                    for (Object i : (List<Object>) value){
                        jsonarry.put(toJSON(i));
                    }

                    jobject.put(field.getName(), jsonarry);
                }else if (value.getClass().isArray()){
                    JSONArray jsonarry = new JSONArray();
                    for (Object i : (Object[]) value){
                        jsonarry.put(toJSON(i));
                    }

                    jobject.put(field.getName(), jsonarry);
                }else {
                    jobject.put(field.getName(), String.valueOf(value));
                }
            }
        }catch(Exception exp){

        }



        return jobject;
    }
    public static Object convertFromJSON(String JSON,Class<? extends SQLiteObject> cls){

        return  createObject(JSON,cls);

    }

    public static  Object createObject(String JSON,Class<? extends SQLiteObject> cls){
        try{
            //create an object by class  and json
            Class<?> clazz = Class.forName(cls.getName());
            Constructor<?> ctor = clazz.getConstructor(String.class);
            Object object = ctor.newInstance(cls.getDeclaredFields());
            return object;
        }catch (Exception exp){

        }
        return null;
    }
}
