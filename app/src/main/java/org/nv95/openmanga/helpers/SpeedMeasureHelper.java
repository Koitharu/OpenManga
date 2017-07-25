package org.nv95.openmanga.helpers;

import java.util.Locale;
import java.util.Stack;

/**
 * Created by admin on 25.07.17.
 */

public class SpeedMeasureHelper {

    private long mStaredAt;
    private final Stack<Float> mSpeedStack;

    public SpeedMeasureHelper() {
        mSpeedStack = new Stack<Float>(){
            private static final long serialVersionUID = 1L;
            public Float push(Float item) {
                if (this.size() == 3) {
                    this.removeElementAt(0);
                }
                return super.push(item);
            }
        };
        init();
    }

    public void init() {
        mSpeedStack.clear();
        reset();
    }

    public void reset() {
        mStaredAt = System.currentTimeMillis();
    }

    public double measure(long contentLength) {
        long time = System.currentTimeMillis() - mStaredAt;
        float speed = contentLength / (time / 1000f); //Bps
        mSpeedStack.push(speed);
        return speed;
    }

    public double getAverageSpeed() {
        float sum = 0;
        for (Float o : mSpeedStack) {
            sum += o;
        }
        return sum / (double)mSpeedStack.size();
    }

    public double getLastSpeed() {
        return mSpeedStack.isEmpty() ? 0 : mSpeedStack.peek();
    }

    public static CharSequence formatSpeed(double kbps) {
        if (kbps >= 1024) {
            return String.format(Locale.getDefault(), "%.2f Mb/s", kbps / 1024D);
        }
        return String.format(Locale.getDefault(), "%.0f Kb/s", kbps);
    }
}
