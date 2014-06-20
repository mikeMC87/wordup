package com.dufecta.word_up;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class WordUp extends Activity implements Input.InputReady, DialogInterface.OnDismissListener {

    public static final String PLAY = "PLAY";
    public static final int HINT_PENALTY = -25;
    public static final int SKIP_WORD_PENALTY = -100;
    private static final int GAME_TIME = 26;
    private int seconds = GAME_TIME;
    private static final int MENU_QUIT = 1;
    private static final int MENU_HINT = 2;
    private static final int MENU_GIVE_UP = 3;
    private static final int CORRECT = 1;
    private static final int WRONG = 2;
    private static final int REPEAT = 3;
    private int points = 0;
    private Thread timerThread = null;
    private Handler updateHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    timeLabel.setText(String.valueOf(--seconds));
                    if (seconds <= 10) {
                        timeLabel.setTextColor(Color.RED);
                        ((TextView) findViewById(R.id.seconds_label)).setTextColor(Color.RED);
                    }
                    if (seconds == 0) {
                        // kill timer thread and set color of text back to black
                        timerThread.interrupt();
//                         timerThread..stop();
                        timerThread = null;
                        gameOver();
                    }
                    break;
            }
        }
    };
    private TextView timeLabel;
    private TextView pointsLabel;
    private String playMode = "";
    private Toast mToast = null;
    private View toastView;
    private LinearLayout textLayout;
    private Object[] word = null;
    private int totalWordLength;
    private Input first = null;
    private String wordPattern = "";
    private String hintWord = "";
    private TextView numWordsView;
    private TextView correctTable;
    private Dialog wordListDialog = null;
    private Set<String> words;
    private Set<String> guessedWords;
    private int numWords = 0;
    private int totalWords = 0;
    private DBHelper dbHelper;
    // timer stuff
    private Builder gameOverDialog = null;
    private boolean isGameOver = false;
    private boolean notSubmitted = true;

    private void gameOver() {
        isGameOver = true;

        // text to show
        TextView game_over = new TextView(WordUp.this);
        game_over.setText("score: " + points);
        game_over.setTextColor(Color.WHITE);
        game_over.setTextSize(16);
        game_over.setTypeface(Typeface.MONOSPACE);
        game_over.setGravity(Gravity.CENTER);

        gameOverDialog = new AlertDialog.Builder(WordUp.this);
        gameOverDialog.setTitle("game over");
        gameOverDialog.setView(game_over);
        gameOverDialog.setPositiveButton("play again", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                resetGame();
            }
        });
        gameOverDialog.setNeutralButton("show words", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                giveUp();
            }
        });
        gameOverDialog.setNegativeButton("quit", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        //TODO fix high score DB not found??
        //check for high score here
//        if(points > dbHelper.getLowScore() && notSubmitted)
//        {
//        	Log.e("compare", ""+points+" "+dbHelper.getLowScore());
//        	CongratsScoreDialog new_score_dialog = new CongratsScoreDialog(this);
//
//        	new_score_dialog.setScore(points);
//        	new_score_dialog.show();
//        	notSubmitted = false;
//        	new_score_dialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
//    					public void onDismiss(DialogInterface dialog) {
//    						gameOverDialog.show();
//    					}
//        	});
//        }
//        else{
        gameOverDialog.show();
//        }
    }

    private void resetGame() {
        if (playMode.equals(PLAY)) {
            isGameOver = false;

            timeLabel.setTextColor(Color.parseColor("#333333"));
            ((TextView) findViewById(R.id.seconds_label)).setTextColor(Color.parseColor("#333333"));

            showNextWord();

            timerThread = new Thread(new countdownClock());
            timerThread.start();

            seconds = GAME_TIME;
            points = 0;

            timeLabel.setText(String.valueOf(seconds));
            pointsLabel.setText("0");
        } else {
            //timeLabel
            findViewById(R.id.time_table).setVisibility(View.GONE);
            showNextWord();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        dbHelper = new DBHelper(this);
        try {
            dbHelper.copy_db_from_assets(this);
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }

        words = new HashSet<String>();
        guessedWords = new HashSet<String>();

        textLayout = (LinearLayout) findViewById(R.id.text_layout);
        numWordsView = (TextView) findViewById(R.id.num_words);
        findViewById(R.id.next_word).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                skipWord();
            }
        });

        correctTable = (TextView) findViewById(R.id.correct_table);
        correctTable.setTextSize(40);

        timeLabel = (TextView) findViewById(R.id.time_label);
        timeLabel.setText(String.valueOf(seconds));
        pointsLabel = (TextView) findViewById(R.id.points_label);
        pointsLabel.setText("0");

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        toastView = inflater.inflate(R.layout.toast_layout, null);
        mToast = new Toast(this);
        mToast.setGravity(Gravity.BOTTOM, 0, 0);
        mToast.setView(toastView);
        mToast.setDuration(Toast.LENGTH_SHORT);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (timerThread != null) {
            timerThread.interrupt();
            timerThread = null;
        }
        dbHelper.close();
    }

    /* Creates the menu items */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, MENU_QUIT, 0, "Quit")
            .setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        menu.add(0, MENU_GIVE_UP, 0, "Give Up")
            .setIcon(android.R.drawable.ic_menu_help);
        menu.add(0, MENU_HINT, 0, "Hint")
            .setIcon(android.R.drawable.ic_menu_view);
        return true;
    }

    /* Handles item selections */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case MENU_QUIT:
                finish();
                return true;
            case MENU_GIVE_UP:
                giveUp();
                return true;
            case MENU_HINT:
                Input current = (Input) textLayout.findFocus();

                for (int i = 0; i < word.length; i++) {
                    if (word[i] == current) {
                        current.setText(String.valueOf(((String) (words.toArray()[0])).charAt(i)));
                    }
                }

                updatePoints(HINT_PENALTY);

                Input next = (Input) textLayout.focusSearch(current, View.FOCUS_RIGHT);
                if (next == null) {
                    verify();
                } else {
                    next.requestFocus();
                }
                return true;
        }
        return false;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (isGameOver) {
            gameOver();
        } else {
            skipWord();
        }
    }

    public void giveUp() {
        wordListDialog = new Dialog(this);
        wordListDialog.setCanceledOnTouchOutside(true);
        wordListDialog.setTitle(wordPattern.replace("_", " _ "));
        wordListDialog.setContentView(R.layout.word_list_dialog);

        TextView correctWordsView = (TextView) wordListDialog.findViewById(R.id.hit_list_dialog);
        TextView missedWordsView = (TextView) wordListDialog.findViewById(R.id.miss_list_dialog);

        String[] correctWords = guessedWords.toArray(new String[guessedWords.size()]);
        String[] missedWords = words.toArray(new String[words.size()]);

        Arrays.sort(correctWords);
        Arrays.sort(missedWords);

        for (String s : correctWords) {
            correctWordsView.setText(correctWordsView.getText() + s + "\n");
        }
        for (String s : missedWords) {
            missedWordsView.setText(missedWordsView.getText() + s + "\n");
        }

        wordListDialog.show();
        wordListDialog.setOnDismissListener(this);
    }

    private void skipWord() {
        updatePoints(SKIP_WORD_PENALTY);
        showNextWord();
    }

    public void showNextWord() {
        // clear out the old word and list
        textLayout.removeAllViews();
        wordPattern = "";
        hintWord = "";
        correctTable.setText("");
        words.clear();
        guessedWords.clear();

        Random rand = new Random();
        int wordLength = rand.nextInt(3) + 4;
        totalWordLength = wordLength;

        // get common word from DB
        String common = dbHelper.get_common_word(wordLength);

        //Toast.makeText(this, common, Toast.LENGTH_SHORT).show(); ???

        // init vars
        word = new Object[wordLength];
        Input input;
        Block block;

        // decide how many inputs (blanks) to have
        int numInputs, numBlocks;
        if (wordLength < 6) {
            numInputs = 2;
        } else {
            numInputs = 3;
        }
        numBlocks = wordLength - numInputs;

        // create the inputs and blocks
        boolean isFirstInput = true;
        int index = 0;

        while (numInputs != 0 || numBlocks != 0) {
            boolean flag = rand.nextBoolean();

            if ((flag && numInputs != 0) || numBlocks == 0) {
                hintWord += common.charAt(index);
                wordPattern += "_";
                input = new Input(this, this);

                if (isFirstInput) {
                    first = input;
                    first.requestFocus();
                    first.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                    isFirstInput = false;
                }
                word[index++] = input;
                textLayout.addView(input);
                numInputs--;
            } else if ((!flag && numBlocks != 0) || numInputs == 0) {
                hintWord += common.charAt(index);
                wordPattern += common.charAt(index);
                block = new Block(this, common.charAt(index));
                word[index++] = block;
                textLayout.addView(block);
                numBlocks--;
            }
        }

        numWords = totalWords = dbHelper.get_num_words_from_pattern(wordPattern);
        numWordsView.setText(String.valueOf(numWords) + "\nwords\nleft");

        List<String> wordList = dbHelper.getWordsFromPattern(wordPattern);
        for (String s : wordList) {
            words.add(s);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        if (intent != null && intent.getAction().equals("com.dufecta.word_up.GAME")) {
            playMode = intent.getStringExtra("MODE");
            //Toast.makeText(this, playMode, Toast.LENGTH_SHORT).show();
        }
        resetGame();
    }

    @Override
    public void goToNextInput() {
        Input next = (Input) textLayout.focusSearch(textLayout.findFocus(), View.FOCUS_RIGHT);
        if (next == null) {
            verify();
            return;
        }
        next.requestFocus();
    }

    @Override
    public void goToPreviousInput() {
        Input prev = (Input) textLayout.focusSearch(textLayout.findFocus(), View.FOCUS_LEFT);
        if (prev == null) {
            return;
        }
        prev.setText("");
        prev.requestFocus();
    }

    private void verify() {
        //Toast.makeText(this, "end", Toast.LENGTH_SHORT).show();
        String wholeWord = "";
        for (Object obj : word) {
            TextView letter = (TextView) obj;
            wholeWord += letter.getText();
        }

        if (dbHelper.checkWord(wholeWord)) {
            if (guessedWords.contains(wholeWord)) {
                showAlertMessage(REPEAT, "repeat: " + wholeWord);
                cleartInputs();
                return;
            }

            updatePoints(totalWordLength * 10);

            ScrollView scrolly = (ScrollView) findViewById(R.id.textAreaScroller);
            scrolly.smoothScrollBy(0, 10000);

            correctTable.setText(correctTable.getText() + wholeWord + "  ");

            numWordsView.setText(String.valueOf(--numWords) + "\nwords\nleft");

            words.remove(wholeWord);
            guessedWords.add(wholeWord);

            showAlertMessage(CORRECT, wholeWord);
            cleartInputs();

            if (numWords == 0) {
                updatePoints(50 * totalWords);
                showNextWord();
            }

            seconds += 6;
            if (seconds >= 10) {
                timeLabel.setTextColor(Color.parseColor("#333333"));
                ((TextView) findViewById(R.id.seconds_label)).setTextColor(Color.parseColor("#333333"));
            }
        } else {
            showAlertMessage(WRONG, wholeWord);
            cleartInputs();
        }
    }

    private void updatePoints(int pointsToAdd) {
        points += pointsToAdd;
        pointsLabel.setText(String.valueOf(points));
    }

    private void cleartInputs() {
        for (Object obj : word) {
            if (obj instanceof Input) {
                Input input = (Input) obj;
                input.setText("");
            }
        }
        first.requestFocus();
        first.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
    }

    public void showAlertMessage(int alert_type, String word) {
        // cancel the old toast
        mToast.cancel();

        //set the text in the view
        TextView message = (TextView) toastView.findViewById(R.id.toast_message);
        message.setText(word);

        ImageView image = (ImageView) toastView.findViewById(R.id.toast_image);

        switch (alert_type) {
            case CORRECT:
                image.setImageResource(R.drawable.correct);
                break;
            case WRONG:
                image.setImageResource(R.drawable.wrong);
                break;
            case REPEAT:
                image.setImageResource(R.drawable.repeat);
                break;
        }

        mToast.show();
    }

    // Not used normally - just for DB stuff
    public void drop_db() {
        dbHelper.drop_db();
    }

    private class countdownClock implements Runnable {
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                Message m = new Message();
                m.what = 0;
                WordUp.this.updateHandler.sendMessage(m);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

}

