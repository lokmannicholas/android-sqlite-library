package sqlitedb_test.lokman.com.sample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.lokman.sqlitedb.SQLiteObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lokmannicholas on 13/9/16.
 */
class TestClass extends SQLiteObject{
    public int id=1;
    public String name="hi";
    String l="123";
    TestClassm[] ls = new TestClassm[1];
    Date ssD;
    public TestClass(String l){
        super();

    }
    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return this.name;
    }
    public void setLs(){
        ls[0]=new TestClassm();
        ssD = new Date();
    }

}