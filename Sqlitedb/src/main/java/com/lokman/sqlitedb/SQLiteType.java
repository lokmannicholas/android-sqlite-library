package com.lokman.sqlitedb;

import java.lang.reflect.Type;

/**
 * Created by lokmannicholas on 13/9/16.
 */
public class SQLiteType {

    public static String getStoreType(Type type){
        if(type == String.class){
            return "TEXT";
        }else if(type == int.class){
            return "INTEGER";
        }else if(type == boolean.class){
            return "INTEGER";
        }else if(type == float.class){
            return "REAL";
        }else if(type == double.class){
            return "REAL";
        }

        return "TEXT";
    }
}
