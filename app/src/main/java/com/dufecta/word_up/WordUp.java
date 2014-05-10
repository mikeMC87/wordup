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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class WordUp extends Activity implements Input.InputReady, /*shakeoff ShakeListener.Shook,*/ DialogInterface.OnDismissListener
{

   //shakeoff//private ShakeListener shake_listener; test
   //shakeoff//private SensorManager m_sensorManager;

   private static final int GAME_TIME = 26;

   private static final int MENU_QUIT = 1;
   private static final int MENU_HINT = 2;
   private static final int MENU_GIVE_UP = 3;
   private static final int CORRECT = 1;
   private static final int WRONG = 2;
   private static final int REPEAT = 3;
   public static final String PLAY = "PLAY";
   public static final int HINT_PENALTY = -25;
   public static final int SKIP_WORD_PENALTY = -100;

   private int points = 0;
   private int seconds = GAME_TIME;

   private Thread timerThread = null;

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


   private Handler updateHandler = new Handler()
   {
      public void handleMessage(Message msg)
      {
         switch (msg.what)
         {
            case 0:
               timeLabel.setText(String.valueOf(--seconds));
               if (seconds <= 10)
               {
                  timeLabel.setTextColor(Color.RED);
                  ((TextView) findViewById(R.id.seconds_label)).setTextColor(Color.RED);
               }
               if (seconds == 0)
               {
                  // kill timer thread and set color of text back to black
                  timerThread.interrupt();
//                         timerThread..stop();
                  timerThread = null;
                  game_is_now_over();
               }
               break;
         }
      }
   };

   private void game_is_now_over()
   {
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
      gameOverDialog.setPositiveButton("play again", new OnClickListener()
      {
         @Override
         public void onClick(DialogInterface dialog, int which)
         {
            reset_game();
         }
      });
      gameOverDialog.setNeutralButton("show words", new OnClickListener()
      {
         @Override
         public void onClick(DialogInterface dialog, int which)
         {
            giveUp();
         }
      });
      gameOverDialog.setNegativeButton("quit", new OnClickListener()
      {
         @Override
         public void onClick(DialogInterface dialog, int which)
         {
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

   private void reset_game()
   {
      if (playMode.equals(PLAY))
      {
         isGameOver = false;

         timeLabel.setTextColor(Color.parseColor("#333333"));
         ((TextView) findViewById(R.id.seconds_label)).setTextColor(Color.parseColor("#333333"));

         showNextWord();

         timerThread = new Thread(new secondCountDownRunner());
         timerThread.start();

         seconds = GAME_TIME;
         points = 0;

         timeLabel.setText(String.valueOf(seconds));
         pointsLabel.setText("0");
      } else
      {
         //timeLabel
         findViewById(R.id.time_table).setVisibility(View.GONE);
         showNextWord();
      }
   }

   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      //requestWindowFeature(Window.FEATURE_NO_TITLE);
      setContentView(R.layout.main);


      //InputMethodManager inputManager = (InputMethodManager)
      //getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE);

      //inputManager.showSoftInput(findViewById(R.layout.main), 0);
      //naaa

      //Intent softkb = new Intent(this, SoftKeyboard.class);
      //startService(softkb);
      //shakeoff//shake_listener = new ShakeListener(this);

      //shakeoff// m_sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
      //shakeoff//  m_sensorManager.registerListener(shake_listener,
      //shakeoff//    SensorManager.SENSOR_ACCELEROMETER,
      //shakeoff//    SensorManager.SENSOR_DELAY_GAME);

      dbHelper = new DBHelper(this);
      try
      {
         dbHelper.copy_db_from_assets(this);
      } catch (IOException e)
      {
         // TODO Auto-generated catch block
      }

      words = new HashSet<String>();
      guessedWords = new HashSet<String>();

      textLayout = (LinearLayout) findViewById(R.id.text_layout);
      numWordsView = (TextView) findViewById(R.id.num_words);
      findViewById(R.id.next_word).setOnClickListener(new View.OnClickListener()
      {
         public void onClick(View v)
         {
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
   public void onStart()
   {
      super.onStart();
   }

   @Override
   public void onStop()
   {
      super.onStop();
      if (timerThread != null)
      {
         timerThread.interrupt();
         timerThread = null;
      }
      dbHelper.close();
   }

   /* Creates the menu items */
   @Override
   public boolean onCreateOptionsMenu(Menu menu)
   {
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
   public boolean onOptionsItemSelected(MenuItem item)
   {
      super.onOptionsItemSelected(item);

      switch (item.getItemId())
      {
         case MENU_QUIT:
            finish();
            return true;
         case MENU_GIVE_UP:
            giveUp();
            return true;
         case MENU_HINT:
            Input current = (Input) textLayout.findFocus();

            for (int i = 0; i < word.length; i++)
            {
               if (word[i] == current)
               {
                  current.setText(String.valueOf(((String) (words.toArray()[0])).charAt(i)));
               }
            }

            updatePoints(HINT_PENALTY);

            Input next = (Input) textLayout.focusSearch(current, View.FOCUS_RIGHT);
            if (next == null)
            {
               verify();
               return true;
            }
            next.requestFocus();
            return true;
      }
      return false;
   }

   @Override
   public void onDismiss(DialogInterface dialog)
   {
      if (isGameOver)
      {
         game_is_now_over();
      } else
      {
         skipWord();
      }
   }

   public void giveUp()
   {
      wordListDialog = new Dialog(this);
      wordListDialog.setCanceledOnTouchOutside(true);
      wordListDialog.setTitle(wordPattern.replace("_", " _ "));
      wordListDialog.setContentView(R.layout.word_list_dialog);

      TextView hit_words = (TextView) wordListDialog.findViewById(R.id.hit_list_dialog);
      TextView miss_words = (TextView) wordListDialog.findViewById(R.id.miss_list_dialog);
      //all_words.layout(10, 10, 10, 10);

      String[] hit_word_array = new String[guessedWords.size()];
      String[] miss_word_array = new String[words.size()];

      int index = 0;
      for (String s : guessedWords)
      {
         hit_word_array[index++] = s;
      }
      index = 0;
      for (String s : words)
      {
         miss_word_array[index++] = s;
      }
      Arrays.sort(hit_word_array);
      Arrays.sort(miss_word_array);
      for (String s : hit_word_array)
      {
         hit_words.setText(hit_words.getText() + s + "\n");
      }
      for (String s : miss_word_array)
      {
         miss_words.setText(miss_words.getText() + s + "\n");
      }


      wordListDialog.show();
      wordListDialog.setOnDismissListener(this);
   }

   private void skipWord()
   {
      updatePoints(SKIP_WORD_PENALTY);
      showNextWord();
   }

   public void showNextWord()
   {
      // clear out the old word and list
      textLayout.removeAllViews();
      wordPattern = "";
      hintWord = "";
      correctTable.setText("");
      words.clear();
      guessedWords.clear();

      Random rand = new Random();
      int word_length = rand.nextInt(3) + 4;
      totalWordLength = word_length;

      // get common word from DB
      String common = dbHelper.get_common_word(word_length);

      //Toast.makeText(this, common, Toast.LENGTH_SHORT).show();

      // init vars
      word = new Object[word_length];
      Input input;
      Block block;

      // decide how many inputs (blanks) to have
      int num_inputs, num_blocks;
      if (word_length < 6) {
         num_inputs = 2;
      }
      else {
         num_inputs = 3;
      }
      num_blocks = word_length - num_inputs;

      // create the inputs and blocks
      boolean first_input = true;
      int index = 0;

      while (num_inputs != 0 || num_blocks != 0)
      {
         boolean flag = rand.nextBoolean();

         if ((flag && num_inputs != 0) || num_blocks == 0)
         {
            hintWord += common.charAt(index);
            wordPattern += "_";
            input = new Input(this, this);

            if (first_input)
            {
               first = input;
               first.requestFocus();
               first.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
               first_input = false;
            }
            word[index++] = input;
            textLayout.addView(input);
            num_inputs--;
         } else if ((!flag && num_blocks != 0) || num_inputs == 0)
         {
            hintWord += common.charAt(index);
            wordPattern += common.charAt(index);
            block = new Block(this, common.charAt(index));
            word[index++] = block;
            textLayout.addView(block);
            num_blocks--;
         }
      }

      numWords = totalWords = dbHelper.get_num_words_from_pattern(wordPattern);
      numWordsView.setText(String.valueOf(numWords) + "\nwords\nleft");

      ArrayList<String> word_list = dbHelper.get_words_from_pattern(wordPattern);
      for (String s : word_list)
      {
         words.add(s);
      }
   }

   @Override
   protected void onPause()
   {
      super.onPause();
      //shakeoff//m_sensorManager.unregisterListener(shake_listener);
   }

   @Override
   protected void onResume()
   {
      super.onResume();
      //shakeoff// m_sensorManager.registerListener(shake_listener,
      //shakeoff//	  SensorManager.SENSOR_ACCELEROMETER,
      //shakeoff//      SensorManager.SENSOR_DELAY_GAME);

      Intent intent = getIntent();
      if (intent != null && intent.getAction().equals("com.dufecta.word_up.GAME"))
      {
         playMode = intent.getStringExtra("MODE");
         //Toast.makeText(this, playMode, Toast.LENGTH_SHORT).show();
      }
      reset_game();
   }

   @Override
   public void goToNextInput()
   {
      Input next = (Input) textLayout.focusSearch(textLayout.findFocus(), View.FOCUS_RIGHT);
      if (next == null)
      {
         verify();
         return;
      }
      next.requestFocus();
   }

   @Override
   public void goToPreviousInput()
   {
      Input prev = (Input) textLayout.focusSearch(textLayout.findFocus(), View.FOCUS_LEFT);
      if (prev == null) return;
      prev.setText("");
      prev.requestFocus();
   }

   private void verify()
   {
      //Toast.makeText(this, "end", Toast.LENGTH_SHORT).show();
      String whole_word = "";
      for (Object obj : word)
      {
         TextView letter = (TextView) obj;
         whole_word += letter.getText();
      }

      if (dbHelper.check_word(whole_word))
      {
         if (guessedWords.contains(whole_word))
         {
            showAlertMessage(REPEAT, "repeat: " + whole_word);
            clear_inputs();
            return;
         }

         updatePoints(totalWordLength*10);

         ScrollView scrolly = (ScrollView) findViewById(R.id.textAreaScroller);
         scrolly.smoothScrollBy(0, 10000);

         correctTable.setText(correctTable.getText() + whole_word + "  ");

         numWordsView.setText(String.valueOf(--numWords) + "\nwords\nleft");

         words.remove(whole_word);
         guessedWords.add(whole_word);

         showAlertMessage(CORRECT, whole_word);
         clear_inputs();

         if (numWords == 0)
         {
            updatePoints(50 * totalWords);
            showNextWord();
         }

         seconds += 6;
         if (seconds >= 10)
         {
            timeLabel.setTextColor(Color.parseColor("#333333"));
            ((TextView) findViewById(R.id.seconds_label)).setTextColor(Color.parseColor("#333333"));
         }
      } else
      {
         showAlertMessage(WRONG, whole_word);
         clear_inputs();
      }
   }

   //shakeoff//  @Override
   //shakeoff//public void shook() {
   //shakeoff//      skip_word();
   //shakeoff//  }

   private void updatePoints(int add)
   {
      points += add;
      pointsLabel.setText(String.valueOf(points));
   }

   private void clear_inputs()
   {
      for (Object obj : word)
      {
         if (obj instanceof Input)
         {
            Input input = (Input) obj;
            input.setText("");
         }
      }
      first.requestFocus();
      first.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
   }

   public void showAlertMessage(int alert_type, String word)
   {
      // cancel the old toast
      mToast.cancel();

      //set the text in the view
      TextView message = (TextView) toastView.findViewById(R.id.toast_message);
      message.setText(word);

      ImageView image = (ImageView) toastView.findViewById(R.id.toast_image);

      switch (alert_type)
      {
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
   public void drop_db()
   {
      dbHelper.drop_db();
   }

   public void fill_db()
   {
      FileInputStream fis = null;
      try
      {
         fis = this.openFileInput("6letter.txt");
      } catch (FileNotFoundException e)
      {
         e.printStackTrace();
      }
      File file = new File("/data/data/com.dufecta.word_up/files/6letter.txt");
      InputStreamReader in = new InputStreamReader(fis);
      char[] contents = new char[(int) file.length()];
      try
      {
         in.read(contents);
      } catch (IOException e)
      {
         e.printStackTrace();
      }
      String buf = new String(contents);
      String[] strings = buf.split("\n");
      for (String s : strings)
      {
         dbHelper.add_word(s);
      }
   }

   public void fill_common_db()
   {
      FileInputStream fis = null;
      try
      {
         fis = this.openFileInput("common.txt");
      } catch (FileNotFoundException e)
      {
         e.printStackTrace();
      }
      File file = new File("/data/data/com.dufecta.word_up/files/common.txt");
      InputStreamReader in = new InputStreamReader(fis);
      char[] contents = new char[(int) file.length()];
      try
      {
         in.read(contents);
      } catch (IOException e)
      {
         e.printStackTrace();
      }
      String buf = new String(contents);
      String[] strings = buf.split("\n");
      for (String s : strings)
      {
         int length = s.length();
         if (length >= 5 && length <= 7)
            dbHelper.add_common_word(s);
      }
   }

   private class secondCountDownRunner implements Runnable
   {
      public void run()
      {
         while (!Thread.currentThread().isInterrupted())
         {
            Message m = new Message();
            m.what = 0;
            WordUp.this.updateHandler.sendMessage(m);
            try
            {
               Thread.sleep(1000);
            } catch (InterruptedException e)
            {
               Thread.currentThread().interrupt();
            }
         }
      }
   }

}

