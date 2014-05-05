package com.dufecta.word_up;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class Input extends EditText implements TextWatcher
{
    
    public interface InputReady {
        public void goToNextInput();
        public void goToPreviousInput();
    }
    
    private InputReady readyListener;

    public Input(Context context, InputReady ir) {
        super(context);
        
        setFocusable(true);
        setTextSize(40);
        setTypeface(Typeface.MONOSPACE);
        setGravity(Gravity.CENTER);
        setInputType(InputType.TYPE_CLASS_TEXT);
        setPadding(0,0,0,0);
        
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.width = 125;

        setLayoutParams(params);
        readyListener = ir;
        setOnEditorActionListener(oeaL);
    }

    private static OnEditorActionListener oeaL = new OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

      return true;
      }
   };

    private char[] alphabet = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};

   @Override
   public void beforeTextChanged(java.lang.CharSequence charSequence, int i, int i1, int i2){

   }

   @Override
   public void onTextChanged(java.lang.CharSequence charSequence, int start, int before, int count){

      if(count > 0)
      {
         readyListener.goToNextInput();
      }
//      if ((c = event.getMatch(alphabet)) != '\0') {
//            if (this.getText().length() > 0) return false;
//
//            setText(String.valueOf(c));
//            setSelection(1);
//
//            readyListener.goToNextInput();
//            return true;
//        }
//        else if (keyCode == KeyEvent.KEYCODE_DEL) {
//            if (this.getText().length() == 0) {
//                readyListener.goToPreviousInput();
//                return false;
//            }
//            else if (this.getText().length() == 1) {
//                setText("");
//                return false;
//            }
//            return true;
//        }
//        else if (keyCode == KeyEvent.KEYCODE_BACK) {
//            return true;
//        }
//        else return false;
   }

   @Override
   public void afterTextChanged(android.text.Editable editable){

   }
    
}
