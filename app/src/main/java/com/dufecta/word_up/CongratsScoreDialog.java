package com.dufecta.word_up;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class CongratsScoreDialog extends Dialog {

    private DBHelper mDbHelper;
    private int points;

    public CongratsScoreDialog(Context context) {
        super(context);
        mDbHelper = new DBHelper(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.high_scores_dialog);

        setContentView(R.layout.congrats_hi_score_dialog);
        setTitle("new high score! (" + points + ")");

        Button submit_btn = (Button) findViewById(R.id.submit_btn);
        submit_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText name_entry = (EditText) findViewById(R.id.enter_name);
                String name = name_entry.getText().toString();
                mDbHelper.addHighScore(name, points);

                mDbHelper.close();
                CongratsScoreDialog.this.dismiss();

            }
        });


    }

    public void setScore(int in_points) {
        points = in_points;
    }
}
