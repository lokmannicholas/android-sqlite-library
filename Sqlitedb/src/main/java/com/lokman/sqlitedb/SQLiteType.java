package com.lokman.sqlitedb;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lokmannicholas on 13/9/16.
 */
public class SQLiteType {
    public final static SimpleDateFormat dateformate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    public final static String getStoreType(Type type){
        if(type == String.class){
            return "TEXT";
        }else if(type == int.class){
            return "INTEGER";
        }else if(type == boolean.class){
            return "NUMERIC";
        }else if(type == float.class){
            return "REAL";
        }else if(type == double.class){
            return "REAL";
        }else if(type == Date.class){
            return "TEXT";
        }

        return "TEXT";
    }
}
