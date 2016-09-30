package com.lokman.sqlitedb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
    //not checked
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
                        mContentValues.put(key, String.valueOf(value));
                    }else
                    //Integer
                    if (value.getClass().equals(Integer.class)){
                        mContentValues.put(key, Integer.valueOf((Integer) value));
                    }
                    //List
                    else if (value.getClass().equals(List.class) || value.getClass().equals(ArrayList.class)){
                        JSONArray jsonarry = new JSONArray();
                        for (Object i : (List<Object>) value){
                            jsonarry.put(toJSON(i));
                        }
                        mContentValues.put(key,jsonarry.toString());
                    }
                    //Array
                    else if (value.getClass().isArray()){
                        JSONArray jsonarry = new JSONArray();
                        for (Object i : (Object[]) value){
                            jsonarry.put(toJSON(i));
                        }

                        mContentValues.put(key,jsonarry.toString());
                    }
                    //Date
                    else if(value.getClass().equals(Date.class)){

                        mContentValues.put(key, SQLiteType.dateformate.format(value));
                    }
                    //String Float Double Date
                    else if (value.getClass().equals(String.class) || value.getClass().equals(Float.class) || value.getClass().equals(Double.class)){
                        mContentValues.put(key,String.valueOf(value));
                    }

                }
        }

        sqlite_storeID =  databaseHelper.insert(mContentValues);
        return sqlite_storeID>0?true:false;
    }
    //not checked
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

    //not checked
    public static List<Object> getList(int limit,Class<? extends SQLiteObject> cls){
        List<Object> list_of_result = new ArrayList<>();
        Cursor multi_cursor = databaseHelper.list(limit);
        while(multi_cursor.moveToNext()){
            int totalColumn = multi_cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            for (int i = 0; i < totalColumn; i++) {
                if (multi_cursor.getColumnName(i) != null) {
                    try {
                        rowObject.put(multi_cursor.getColumnName(i),
                                multi_cursor.getString(i));
                    } catch (Exception e) {

                    }
                }
            }

//            list_of_result.add(convertFromJSON(rowObject.toString(),cls));
        }

        return list_of_result;
    }


    public String toJSON(){

        return this.toJSON(this).toString();
    }
    private static boolean isSupportFieldType(Class<?> cls){
        List<Class<?>> class_type = Arrays.asList(
                List.class,ArrayList.class,Integer.class,Double.class,Float.class,String.class,Date.class,Bitmap.class
        );
        return class_type.contains(cls)?true:false;
    }

    private JSONObject jsonValueTransfer(Object object_value) throws JSONException{

        if (object_value.getClass().equals(Bitmap.class)) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ((Bitmap)object_value).compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream .toByteArray();
            String base64Value = Base64.encodeToString(byteArray, Base64.DEFAULT);

            return new JSONObject("{\"type\":\""+object_value.getClass().getName()+"\",\"value\":\""+base64Value+"\"}");
        }else if (object_value.getClass().equals(Integer.class)){
            return new JSONObject("{\"type\":\""+object_value.getClass().getName() +"\",\"value\":"+Integer.valueOf((Integer) object_value)+"}");
        }else if (object_value.getClass().equals(List.class) || object_value.getClass().equals(ArrayList.class)){
                JSONArray jsonarry = new JSONArray();
                for (Object i : (List<Object>) object_value){
                    jsonarry.put(toJSON(i));
                }
            return new JSONObject("{\"type\":\""+object_value.getClass().getName() +"\",\"value\":"+jsonarry.toString()+"}");

        }else if (object_value.getClass().isArray()){
                JSONArray jsonarry = new JSONArray();
                for (Object i : (Object[]) object_value){
                    jsonarry.put(toJSON(i));
                }
            return new JSONObject("{\"type\":\""+object_value.getClass().getComponentType().getName() +"\",\"value\":"+jsonarry.toString()+"}");
        }
        else if(object_value.getClass().equals(Date.class)){
            return new JSONObject("{\"type\":\""+object_value.getClass().getName()+"\",\"value\":\""+SQLiteType.dateformate.format(object_value)+"\"}");
        }
        else if (object_value.getClass().equals(String.class) || object_value.getClass().equals(Float.class) || object_value.getClass().equals(Double.class)){
            return new JSONObject("{\"type\":\""+object_value.getClass().getName()+"\",\"value\":\""+String.valueOf(object_value)+"\"}");
        }
        return new JSONObject();
    }
    public JSONObject toJSON(Object vObject){


        JSONObject jobject = new JSONObject();
        try{
            for(Field field : vObject.getClass().getDeclaredFields()){
                field.setAccessible(true);
                Object field_value = field.get(vObject);
                if(isSupportFieldType(field_value.getClass()) || field_value.getClass().isArray()){
                    jobject.put(field.getName(), jsonValueTransfer(field_value));
                }//
            }
        }catch(Exception exp){

        }



        return jobject;
    }
    public static Object convertFromJSON(String JSON,Class<? extends SQLiteObject> cls) {

        Gson gson = new Gson();
        try{
            String jsonString = SerializableConversion(new JSONObject(JSON)).toString();
            Object obj =  gson.fromJson(jsonString, cls);
            return objectConstructing(obj,cls,new JSONObject(JSON));
        }catch (JSONException jsonexp){
            Log.d("JSONException",jsonexp.getMessage());
        }
        return null;
    }
    private static Object objectConstructing(Object obj,Class<?> cls,JSONObject jsonobj){
        try{
            for(Field field : cls.getDeclaredFields()){
                field.setAccessible(true);
                Object field_value = field.get(obj);
                if(isSupportFieldType(field.getType()) ){
                    Object object_value =((JSONObject)jsonobj.get(field.getName())).get("value");
                    if (field.getType().equals(Bitmap.class)) {
                            //onvert base 64 to bit map
                    }else if (field.getType().equals(Integer.class)){
                        field.setInt(obj, Integer.valueOf((Integer) object_value));
                    }
                    else if(field.getType().equals(Date.class)){
                        try{
                            field.set(obj, SQLiteType.dateformate.parse(String.valueOf(object_value)));
                        }catch(Exception exp){

                        }
                    }
                    else if (field.getType().equals(String.class)){
                        field.set(obj, String.valueOf(object_value));
                    }
                    else if (field.getType().equals(Float.class)){
                        field.setFloat(obj, Float.valueOf(String.valueOf(object_value)));
                    }
                    else if (field.getType().equals(Double.class)){
                        field.setDouble(obj, Double.valueOf(String.valueOf(object_value)));
                    }
                    else if (field.getType().equals(List.class) || field.getType().equals(ArrayList.class)){
                        JSONArray jarray = jsonobj.getJSONArray(field.getName());
                        List<Object> fields_array_value = field_value==null?new ArrayList<>():(List)field_value;
                        for(int i=0;i<fields_array_value.size();i++){
                            fields_array_value.set(i ,
                                    objectConstructing(fields_array_value.get(i),field_value.getClass().getComponentType().getClass(),jarray.getJSONObject(i))
                            );
                        }
                        field.set(obj, fields_array_value);
                    }
                }//
                else if( field.getType().isArray()){
                    JSONArray jarray = ((JSONObject)jsonobj.get(field.getName())).getJSONArray("value");
                    Object[] fields_array_value = (Object[])field_value;
                    for(int i=0;i<fields_array_value.length;i++){
                        fields_array_value[i] = objectConstructing(fields_array_value[i], field.getType(),jarray.getJSONObject(i));
                    }
                    field.set(obj, fields_array_value);
                }
            }
        }catch(Exception exp){
            Log.d("exception",exp.getMessage());
        }
        return obj;
    }

    private static JSONObject SerializableConversion(JSONObject json){
        JSONObject json_result = new JSONObject();
        Iterator<String> keys = json.keys();
        while(keys.hasNext()){
            String key = keys.next();
            try{
                JSONObject field_value = json.getJSONObject(key);
                String type = field_value.getString("type");
                try{
                    Class<?> cls = Class.forName(type);
                    if(cls.equals(List.class) || cls.equals(ArrayList.class) || field_value.optJSONArray("value")!=null){
                        JSONArray jarray = field_value.getJSONArray("value");
                        JSONArray jarray_result = new JSONArray();
                        for(int i =0 ; i<jarray.length();i++){
                            jarray_result.put(SerializableConversion(jarray.getJSONObject(i)));
                        }
                        json_result.put(key,jarray_result);
                    }else if(cls instanceof Serializable){
                        json_result.put(key,field_value.getJSONObject("value"));
                    }
                }catch(ClassNotFoundException cnf_exp){
                    Log.d("ClassNotFoundException",cnf_exp.getMessage());
                }
            }catch(JSONException json_exp){
                Log.d("JSONException",json_exp.getMessage());
            }
        }
        return json_result;
    }
}
