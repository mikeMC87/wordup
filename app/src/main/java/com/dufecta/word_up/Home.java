package com.dufecta.word_up;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Home extends Activity{

	  @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.home);
	        
	        ((Button)findViewById(R.id.play_btn)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent("com.dufecta.word_up.GAME");
                    intent.putExtra("MODE", "PLAY");
                    startActivity(intent);
                }
	        });
	        
	        ((Button)findViewById(R.id.practice_btn)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent("com.dufecta.word_up.GAME");
                    intent.putExtra("MODE", "PRACTICE");
                    startActivity(intent);
                }
            });
	        
	        ((Button)findViewById(R.id.settings_btn)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                	SettingsDialog settings_dialog = new SettingsDialog(Home.this);

                	settings_dialog.show();
                	settings_dialog.setOnDismissListener(new DialogInterface.OnDismissListener(){

    					public void onDismiss(DialogInterface dialog) {
  
    					}
                	});
                }
                
            });
	        ((Button)findViewById(R.id.high_scores_btn)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                	HighScoresDialog high_scores_dialog = new HighScoresDialog(Home.this);

                	high_scores_dialog.show();
                	high_scores_dialog.setOnDismissListener(new DialogInterface.OnDismissListener(){

    					public void onDismiss(DialogInterface dialog) {
    						
    					}
                	});
                }
                
            });
	  }
}
