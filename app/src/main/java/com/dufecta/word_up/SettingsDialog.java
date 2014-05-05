package com.dufecta.word_up;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SettingsDialog extends Dialog
{

    public SettingsDialog(Context context)
    {
        super(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setCanceledOnTouchOutside(true);
        setTitle("change settings");
        setContentView(R.layout.settings_dialog);

        Button saveButton = (Button) findViewById(R.id.save_settings_btn);
        saveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SettingsDialog.this.dismiss();
            }

        });

    }
}
