package com.markduenas.android.aquickim;

import android.content.Context;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ViewFlipper;

public class MyGestureDetector extends SimpleOnGestureListener {
	
	// viewFlipper and animations
	private ViewFlipper viewFlipper;
	private Animation slideLeftIn;
	private Animation slideLeftOut;
	private Animation slideRightIn;
	private Animation slideRightOut;
	private Context appContext;

	//swipe gesture constants
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	
	public MyGestureDetector(Context c, ViewFlipper v) {
		appContext = c;
		viewFlipper = (ViewFlipper) v;
		slideLeftIn = AnimationUtils.loadAnimation(c, R.anim.slide_left_in);
		slideLeftOut = AnimationUtils.loadAnimation(c, R.anim.slide_left_out);
		slideRightIn = AnimationUtils.loadAnimation(c, R.anim.slide_right_in);
		slideRightOut = AnimationUtils.loadAnimation(c, R.anim.slide_right_out);
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
			return false;
		} 
		else 
		{
			try { 
				// right to left swipe
				if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					if(canFlipRight()){
						viewFlipper.setInAnimation(slideLeftIn);
						viewFlipper.setOutAnimation(slideLeftOut); 
						viewFlipper.showNext();
						((aquickim)appContext).setNextContact();
					} else { 
						return false;
					}
				//left to right swipe
				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					if(canFlipLeft()){
						viewFlipper.setInAnimation(slideRightIn);
						viewFlipper.setOutAnimation(slideRightOut); 
						viewFlipper.showPrevious();
						((aquickim)appContext).setPreviousContact();
					} else {
						return false;
					}
				} 
			} catch (Exception e) {
				// 	nothing
			}
			return true;
		}
	}

	private boolean canFlipLeft() {
		return ((aquickim)appContext).canFlipLeft();
	}

	private boolean canFlipRight() {
		return ((aquickim)appContext).canFlipRight();
	} 
}
