package com.dufecta.word_up;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.widget.TextView;

public class Block extends TextView {

    public Block(Context context, char c) {
        super(context);
        setText(String.valueOf(c));
       setTextSize(40);
        setTypeface(Typeface.MONOSPACE);
        setGravity(Gravity.CENTER);
        //setWidth(40);
//        Drawable left = Drawable.createFromPath("/wordup/app/src/main/res/drawable/icon.png");
//        setCompoundDrawablesWithIntrinsicBounds(left,null,null,null);
    }

}
