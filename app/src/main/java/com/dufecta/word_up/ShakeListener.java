/*package com.dufecta.word_up;

import com.dufecta.word_up.Input.InputReady;

import android.content.Context;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.view.View;
import android.widget.Toast;

public class ShakeListener implements SensorListener {
    
    public interface Shook {
        public void shook();
    }
    
    private Shook shookListener;
    
    private float m_totalForcePrev; // stores the previous total force value

    public ShakeListener(Shook s) {
        shookListener = s;
        m_totalForcePrev = 0;
    }

    public void onAccuracyChanged(int arg0, int arg1) {
        // I have no desire to deal with the accuracy events
    }

    public void onSensorChanged(int sensor, float[] values) {
        if (sensor == SensorManager.SENSOR_ACCELEROMETER) {
            double forceThreshHold = 1.5f;

            double totalForce = 0.0f;
            totalForce += Math.pow(values[SensorManager.DATA_X] / SensorManager.GRAVITY_EARTH, 2.0);
            totalForce += Math.pow(values[SensorManager.DATA_Y] / SensorManager.GRAVITY_EARTH, 2.0);
            totalForce += Math.pow(values[SensorManager.DATA_Z] / SensorManager.GRAVITY_EARTH, 2.0);
            totalForce = Math.sqrt(totalForce);

            if ((totalForce < forceThreshHold) && (m_totalForcePrev > forceThreshHold)) {
                //Toast.makeText(getContext(), "SHAKE", Toast.LENGTH_SHORT).show();
                shookListener.shook();
            }

            m_totalForcePrev = (float) totalForce;
        }
    }

}
*/
