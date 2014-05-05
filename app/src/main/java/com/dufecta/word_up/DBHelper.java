package com.dufecta.word_up;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBHelper {
    
    private static final String DATABASE_NAME = "WordUpDB";
    private static String DATABASE_PATH = "/data/data/com.dufecta.word_up/databases/";
    
    private static final String DATABASE_TABLE4 = "word_list4";
    private static final String DATABASE_TABLE5 = "word_list5";
    private static final String DATABASE_TABLE6 = "word_list6";
    private static final String DATABASE_TABLE_COMMON = "common_words";
    private static final String DATABASE_TABLE_HIGHSCORES = "high_scores";
    
    private static final String TABLE4_CREATE =
        "CREATE TABLE IF NOT EXISTS " +DATABASE_TABLE4 +" (word TEXT NOT NULL);";
    private static final String TABLE5_CREATE =
        "CREATE TABLE IF NOT EXISTS " +DATABASE_TABLE5 +" (word TEXT NOT NULL);";
    private static final String TABLE6_CREATE =
		"CREATE TABLE IF NOT EXISTS " +DATABASE_TABLE6 +" (word TEXT NOT NULL);";
    
    private static final String TABLE_COMMON_CREATE =
        "CREATE TABLE IF NOT EXISTS " +DATABASE_TABLE_COMMON +" (word TEXT NOT NULL);";
    
    private static final String TABLE_HIGHSCORES_CREATE = 
    	"CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE_HIGHSCORES +
    	" (name TEXT NOT NULL, score INTEGER);";
    
	public SQLiteDatabase db;
	
	public DBHelper(Context ctx) {
	    db = ctx.openOrCreateDatabase(DATABASE_NAME, 0, null);
		db.execSQL(TABLE4_CREATE);
		db.execSQL(TABLE5_CREATE);
		db.execSQL(TABLE6_CREATE);
		db.execSQL(TABLE_COMMON_CREATE);
		db.execSQL(TABLE_HIGHSCORES_CREATE);
		Cursor c = db.rawQuery("SELECT * FROM "+DATABASE_TABLE_HIGHSCORES, null);
		if(c.getCount() < 5)
		{
			Log.e("LOW COUNT", ""+c.getCount());
			initialAddHighScoreRows();
		}
		
	}
	
	public void drop_db() {
	    db.execSQL("DROP TABLE IF EXISTS " +DATABASE_TABLE4);
	    db.execSQL("DROP TABLE IF EXISTS " +DATABASE_TABLE5);
	    db.execSQL("DROP TABLE IF EXISTS " +DATABASE_TABLE6);
	    db.execSQL("DROP TABLE IF EXISTS " +DATABASE_TABLE_COMMON);
	    db.execSQL("DROP TABLE IF EXISTS " +DATABASE_TABLE_HIGHSCORES);
	    
	}
	
	public void close() {
	    db.close();
	}
	
	public void copy_db_from_assets(Context ctx) throws IOException {
	    drop_db();
	    
	    //Open your local db as the input stream
        InputStream myInput = ctx.getAssets().open(DATABASE_NAME);
 
        // Path to the just created empty db
        String outFileName = DATABASE_PATH + DATABASE_NAME;
 
        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);
 
        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }
 
        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
	}

    public void add_word(String word) {
        ContentValues initialValues = new ContentValues();
        initialValues.put("word", word);
        
        switch(word.length()) {
        case 4:
            db.insert(DATABASE_TABLE4, null, initialValues);
            break;
        case 5:
            db.insert(DATABASE_TABLE5, null, initialValues);
            break;
        case 6:
            db.insert(DATABASE_TABLE6, null, initialValues);
            break;
        }
    }
    
    public void add_common_word(String word) {
        ContentValues initialValues = new ContentValues();
        initialValues.put("word", word);
        
        db.insert(DATABASE_TABLE_COMMON, null, initialValues);
    }
    
    public String get_common_word(int word_length) {
        Random rand = new Random();
        String str = "", tmp;
        
        while (str.length() != word_length) {
            int row = rand.nextInt(1226);
            Log.i("DB", "this is row: " + row);
            Cursor c = db.rawQuery("SELECT * FROM " +DATABASE_TABLE_COMMON +" WHERE rowid = '"+ row +"'", null);
            c.moveToFirst();
            tmp = c.getString(0);
            str = tmp.trim();
            c.close();
        }
        return str;
    }
    
    public int get_num_words_from_pattern(String pattern) {
        String which_table = "";
        switch(pattern.length()) {
        case 4:
            which_table = DATABASE_TABLE4;
            break;
        case 5:
            which_table = DATABASE_TABLE5;
            break;
        case 6:
            which_table = DATABASE_TABLE6;
            break;
        }
        
        Cursor c = db.rawQuery("SELECT * FROM " +which_table +" WHERE word LIKE '"+ pattern +"'", null);
        
//        String str = "";
//        while (c.moveToNext()) {
//            str += c.getString(0) +", ";
//        }
//        return str;
        int count = c.getCount();
        c.close();
        return count;
    }
    
    public ArrayList<String> get_words_from_pattern(String pattern) {
        String which_table = "";
        switch(pattern.length()) {
        case 4:
            which_table = DATABASE_TABLE4;
            break;
        case 5:
            which_table = DATABASE_TABLE5;
            break;
        case 6:
            which_table = DATABASE_TABLE6;
            break;
        }
        
        Cursor c = db.rawQuery("SELECT * FROM " +which_table +" WHERE word LIKE '"+ pattern +"'", null);
        
        ArrayList<String> str_list = new ArrayList<String>();
        while (c.moveToNext()) {
            str_list.add(c.getString(0));
        }
        c.close();
        return str_list;
    }
    
    public boolean check_word(String word) {
        String which_table = "";
        switch(word.length()) {
        case 4:
            which_table = DATABASE_TABLE4;
            break;
        case 5:
            which_table = DATABASE_TABLE5;
            break;
        case 6:
            which_table = DATABASE_TABLE6;
            break;
        }
        
        Cursor c = db.rawQuery("SELECT * FROM " +which_table +" WHERE word = '"+ word +"'", null);
        int count = c.getCount();
        c.close();
        if (count == 0) return false;
        return true;
    }
    
    public void addHighScore(String in_name, int in_score)
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put("name", in_name);
        initialValues.put("score", in_score);
        
        db.insert(DATABASE_TABLE_HIGHSCORES, null, initialValues);	
                
    }
    public List<HighScore_Entry> fetchHighScores(){
        Cursor c = db.query(DATABASE_TABLE_HIGHSCORES, null, null, null, null, null, "score DESC", "10");
        
        List<HighScore_Entry> high_score_list = new ArrayList<HighScore_Entry>();
        
        c.moveToFirst();
        high_score_list.add(makeHighScoreFromCursor(c));
        while(c.moveToNext())
        {
        	high_score_list.add(makeHighScoreFromCursor(c));
        }
        
        //delete entries that are not top 10
        c.moveToPrevious();
        db.delete(DATABASE_TABLE_HIGHSCORES, "score < "+c.getInt(1), null);
        
        return high_score_list;
    }
    
    public HighScore_Entry makeHighScoreFromCursor(Cursor c){

    	HighScore_Entry new_score_entry = new HighScore_Entry();

    	new_score_entry.setHighScore(c.getString(c.getColumnIndex("name")), c.getInt(c.getColumnIndex("score")));
	
    	return new_score_entry;
    }
    
    public void initialAddHighScoreRows()
    {
    	for(int i=0; i<10; i++)
    	{
	    	ContentValues initialValues = new ContentValues();
	        initialValues.put("name", "---");
	        initialValues.put("score", 0);
	        
	       
	        db.insert(DATABASE_TABLE_HIGHSCORES, null, initialValues);
    	}
    }
    
    public int getLowScore()
    {
    	Cursor c = db.query(DATABASE_TABLE_HIGHSCORES, null, null, null, null, null, "score DESC", "10");
    	c.moveToLast();
    	return c.getInt(1);
    }
    
}
