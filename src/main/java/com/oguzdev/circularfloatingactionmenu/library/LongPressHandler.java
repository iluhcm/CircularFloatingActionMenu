package com.oguzdev.circularfloatingactionmenu.library;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Created by xingli on 9/7/15.
 * 
 * A long press event detector.
 */
public class LongPressHandler implements View.OnTouchListener {
    private static final String TAG = LongPressHandler.class.getSimpleName();

    // Default long press time threshold.
    private static final long LONG_PRESS_TIME_THRESHOLD = 500;
    // Long press event message handler.
    private Handler mHandler = new Handler();
    // The long press time threshold.
    private long mPressTimeThreshold;
    // Record start point and end point to judge whether user has moved while performing long press event.
    private DoublePoint mTouchStartPoint = new DoublePoint();
    private DoublePoint mTouchEndPoint = new DoublePoint();
    // The long press thread.
    private final LongPressThread mLongPressThread = new LongPressThread();
    // Inset in pixels to look for touchable content when the user touches the edge of the screen.
    private final float mTouchSlop;
    // The long press callback.
    private OnLongPressListener listener;

    public LongPressHandler(View view) {
        this(view, LONG_PRESS_TIME_THRESHOLD);
    }

    public LongPressHandler(View view, long holdTime) {
        view.setOnTouchListener(this);
        mTouchSlop = ViewConfiguration.get(view.getContext()).getScaledEdgeSlop();
        mPressTimeThreshold = holdTime;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mTouchStartPoint.set(event.getRawX(), event.getRawY());
                addLongPressCallback();
                break;
            case MotionEvent.ACTION_MOVE:
                mTouchEndPoint.set(event.getRawX(), event.getRawY());
                // If user is pressing and dragging, then we make a callback.
                if (mLongPressThread.mLongPressing) {
                    if (listener != null) {
                        return listener.onLongPressed(event);
                    }
                    break;
                }
                // If user has moved before activating long press event, then the event should be reset.
                if (calculateDistanceBetween(mTouchStartPoint, mTouchEndPoint) > mTouchSlop) {
                    resetLongPressEvent();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mLongPressThread.mLongPressing) {
                    resetLongPressEvent();
                    // Must set true and left the child know we have handled this event.
                    return true;
                }
            default:
                resetLongPressEvent();
                break;
        }
        return false;
    }

    public void setOnLongPressListener(OnLongPressListener listener) {
        this.listener = listener;
    }

    /**
     * Reset the long press event.
     */
    private void resetLongPressEvent() {
        if (mLongPressThread.mAdded) {
            mHandler.removeCallbacks(mLongPressThread);
            mLongPressThread.mAdded = false;
        }
        mLongPressThread.mLongPressing = false;
    }

    /**
     * Add long press event handler.
     */
    private void addLongPressCallback() {
        if (!mLongPressThread.mAdded) {
            mLongPressThread.mLongPressing = false;
            mHandler.postDelayed(mLongPressThread, mPressTimeThreshold);
            mLongPressThread.mAdded = true;
        }
    }

    /**
     * Calculate distance between two point.
     * 
     * @param before previous point
     * @param after next point
     * @return the distance
     */
    private double calculateDistanceBetween(DoublePoint before, DoublePoint after) {
        return Math.sqrt(Math.pow((before.x - after.x), 2) + Math.pow((before.y - after.y), 2));
    }

    /**
     * Judge whether the long press event happens.
     *
     * The time threshold of default activated event is {@see LongPressHandler#LONG_PRESS_TIME_THRESHOLD}
     */
    private static class LongPressThread implements Runnable {
        // A flag to set whether the long press event happens.
        boolean mLongPressing = false;
        // A flag to set whether this thread has been added to the handler.
        boolean mAdded = false;

        @Override
        public void run() {
            mLongPressing = true;
        }
    }

    public interface OnLongPressListener {
        boolean onLongPressed(MotionEvent event);
    }

    class DoublePoint {
        public double x;
        public double y;

        public void set(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

}
