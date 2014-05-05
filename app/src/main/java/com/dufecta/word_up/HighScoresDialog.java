package com.dufecta.word_up;


import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;



public class HighScoresDialog extends Dialog {

	private DBHelper mDbHelper;
	
	public HighScoresDialog(Context context) {
		super(context);
		mDbHelper = new DBHelper(context);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

    	setCanceledOnTouchOutside(true);
    	setTitle("high scores");
    	setContentView(R.layout.high_scores_dialog);

    	// need a local high score DB, with name(string) and score(int)
    	// initially, just have names be empty string or "-" and score be 0 in table
    	// table size of 10
    
    	List<HighScore_Entry> high_score_list = mDbHelper.fetchHighScores();
    	// order the highscore table by score, set names and scores accordingly
    	//List high_score_list = 
    	TextView name1 = (TextView) findViewById(R.id.name1);
    	TextView name2 = (TextView) findViewById(R.id.name2);
    	TextView name3 = (TextView) findViewById(R.id.name3);
    	TextView name4 = (TextView) findViewById(R.id.name4);
    	TextView name5 = (TextView) findViewById(R.id.name5);
    	TextView name6 = (TextView) findViewById(R.id.name6);
    	TextView name7 = (TextView) findViewById(R.id.name7);
    	TextView name8 = (TextView) findViewById(R.id.name8);
    	TextView name9 = (TextView) findViewById(R.id.name9);
    	TextView name10 = (TextView) findViewById(R.id.name10);
    	 	
    	TextView score1 = (TextView) findViewById(R.id.score1);
    	TextView score2 = (TextView) findViewById(R.id.score2);
    	TextView score3 = (TextView) findViewById(R.id.score3);
    	TextView score4 = (TextView) findViewById(R.id.score4);
    	TextView score5 = (TextView) findViewById(R.id.score5);
    	TextView score6 = (TextView) findViewById(R.id.score6);
    	TextView score7 = (TextView) findViewById(R.id.score7);
    	TextView score8 = (TextView) findViewById(R.id.score8);
    	TextView score9 = (TextView) findViewById(R.id.score9);
    	TextView score10 = (TextView) findViewById(R.id.score10);
    	
    	name1.setText(((HighScore_Entry)high_score_list.get(0)).getHighScoreName()); 
    	score1.setText(""+((HighScore_Entry)high_score_list.get(0)).getHighScoreValue()); 
    	
    	name2.setText(((HighScore_Entry)high_score_list.get(1)).getHighScoreName()); 
    	score2.setText(""+((HighScore_Entry)high_score_list.get(1)).getHighScoreValue()); 
    	
    	name3.setText(((HighScore_Entry)high_score_list.get(2)).getHighScoreName()); 
    	score3.setText(""+((HighScore_Entry)high_score_list.get(2)).getHighScoreValue()); 
    	
    	name4.setText(((HighScore_Entry)high_score_list.get(3)).getHighScoreName()); 
    	score4.setText(""+((HighScore_Entry)high_score_list.get(3)).getHighScoreValue()); 
    	
    	name5.setText(((HighScore_Entry)high_score_list.get(4)).getHighScoreName()); 
    	score5.setText(""+((HighScore_Entry)high_score_list.get(4)).getHighScoreValue()); 
    	
    	name6.setText(((HighScore_Entry)high_score_list.get(5)).getHighScoreName()); 
    	score6.setText(""+((HighScore_Entry)high_score_list.get(5)).getHighScoreValue()); 
    	
    	name7.setText(((HighScore_Entry)high_score_list.get(6)).getHighScoreName()); 
    	score7.setText(""+((HighScore_Entry)high_score_list.get(6)).getHighScoreValue()); 
    	
    	name8.setText(((HighScore_Entry)high_score_list.get(7)).getHighScoreName()); 
    	score8.setText(""+((HighScore_Entry)high_score_list.get(7)).getHighScoreValue()); 
    	
    	name9.setText(((HighScore_Entry)high_score_list.get(8)).getHighScoreName()); 
    	score9.setText(""+((HighScore_Entry)high_score_list.get(8)).getHighScoreValue()); 
    
    	name10.setText(((HighScore_Entry)high_score_list.get(9)).getHighScoreName()); 
    	score10.setText(""+((HighScore_Entry)high_score_list.get(9)).getHighScoreValue()); 
		
    	mDbHelper.close();
	}
}
