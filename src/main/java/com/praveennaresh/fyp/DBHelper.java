package com.praveennaresh.fyp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Praveen Naresh
 * Created on 23-Feb-16.
 * Database Helper class for all database methods
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "mydb.db";
    public static final String LINK_TABLE = "link";
    public static final String LINK_COLUMN_ID = "id";
    public static final String LINK_COLUMN_LINK = "link";
    public static final String LINK_COLUMN_TITLE = "title";

    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }

    //Create database and tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS link " +
                        "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "title TEXT," +
                        "link TEXT)"
        );
        db.execSQL("CREATE TABLE IF NOT EXISTS search_result" +
                        " (id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "text TEXT)"
        );
        db.execSQL("CREATE TABLE IF NOT EXISTS news_category" +
                        "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "category TEXT," +
                        "score INTEGER)"
        );

        //news categories
        db.execSQL("INSERT INTO news_category(category,score) VALUES('Business',0)");
        db.execSQL("INSERT INTO news_category(category,score) VALUES('Entertainment',0)");
        db.execSQL("INSERT INTO news_category(category,score) VALUES('Fashion',0)");
        db.execSQL("INSERT INTO news_category(category,score) VALUES('Finance',0)");
        db.execSQL("INSERT INTO news_category(category,score) VALUES('Food',0)");
        db.execSQL("INSERT INTO news_category(category,score) VALUES('Global',0)");
        db.execSQL("INSERT INTO news_category(category,score) VALUES('Health',0)");
        db.execSQL("INSERT INTO news_category(category,score) VALUES('Home',0)");
        db.execSQL("INSERT INTO news_category(category,score) VALUES('Men',0)");
        db.execSQL("INSERT INTO news_category(category,score) VALUES('Parents',0)");
        db.execSQL("INSERT INTO news_category(category,score) VALUES('Sports',0)");
        db.execSQL("INSERT INTO news_category(category,score) VALUES('Technology',0)");
        db.execSQL("INSERT INTO news_category(category,score) VALUES('US',0)");
        db.execSQL("INSERT INTO news_category(category,score) VALUES('Women',0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS link");
        db.execSQL("DROP TABLE IF EXISTS search_result");
        onCreate(db);
    }

    //insert saved link
    public String insertLink(String title, String link)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        if(!validate(title))
        {
            ContentValues values = new ContentValues();
            values.put("title",title);
            values.put("link", link);
            db.insert("link", null, values);
            return "Link added successful";
        }
        return "failed";
    }

    //save search result
    public void insertSearchResult(String query)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        try
        {
            values.put("text",query);
            db.insert("search_result",null,values);
        }
        catch (Exception e)
        {
            Log.d("DB ERROR",e.getMessage());
        }
    }

    //get the last two searched results
    public String[] getLastSearchResult()
    {
        String result[] = new String[2];
        int counter = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT category FROM news_category " +
                        "ORDER BY score DESC LIMIT 2",
                null
        );
        if(cursor.getCount() > 0)
        {
            while(cursor.moveToNext())
            {
                result[counter] = cursor.getString(cursor.getColumnIndex("category"));
                counter++;
            }
            cursor.close();
        }
        else
        {
            result = null;
        }
        return result;
    }

    //delete saved link based on id
    public void deleteLink(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(LINK_TABLE,LINK_COLUMN_ID+"="+id,null);
    }

    //check if title exists before saving
    public boolean validate(String title)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + LINK_TABLE
                        + " WHERE " + LINK_COLUMN_TITLE
                        + " LIKE '" + title + "'"
                , null
        );
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    //get all saved links
    public ArrayList<SavedLinks> getSavedLinks()
    {
        ArrayList<SavedLinks> list = new ArrayList<SavedLinks>();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery(
                "SELECT * FROM link"
                ,null
        );

        while (res.moveToNext())
        {
            SavedLinks model = new SavedLinks(
                    res.getInt(res.getColumnIndex(LINK_COLUMN_ID)),
                    res.getString(res.getColumnIndex(LINK_COLUMN_TITLE)),
                    res.getString(res.getColumnIndex(LINK_COLUMN_LINK))
            );
            list.add(model);
        }
        res.close();
        return list;
    }

    //add score to the recommended category
    public void addRecommendation(String category)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("UPDATE news_category" +
                            " SET score = score + 1 " +
                            "WHERE category LIKE '%"+category+"%'"
            );
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}
