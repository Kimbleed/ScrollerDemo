package com.example.a49479.scrollerdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * Created by 49479 on 2017/10/17.
 */

public class FreeTransformContainerView2 extends LinearLayout {

    //是否双点触控
    private int mode = MODE_SINGLE_POINTER;
    private static final int MODE_SINGLE_POINTER = 0;
    private static final int MODE_TWO_POINTER = 1;
    private static final int MODE_NO_OPERATION = -1;

    //上一次缩放的比例(起始比例)
    private float mOldScale = 1f;
    //当前缩放比例
    private float mScale = 1f;

    private float mRawDistance;
    private float mCurrentDistance;


    private int mWidth,mHeight;

    private float mStartDownX,mStartDownY;

    private int lastX,lastY;

    private Scroller mScroller ;

    public FreeTransformContainerView2(Context context) {
        super(context);
        init();
    }

    public FreeTransformContainerView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FreeTransformContainerView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mScroller = new Scroller(getContext());
    }

    @Override
    public void computeScroll() {
        Log.i("scroller1","computeScroll start");
        if(mScroller.computeScrollOffset()){
            Log.i("scroller1","computeScroll scroll to " +"   "+mScroller.getCurrX()+mScroller.getCurrY());
            scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i("CustomTouch","event x & y"+event.getX()+"   "+event.getY());
        switch(event.getAction()){
            case MotionEvent.ACTION_UP:
                Log.i("CustomTouch","up");

                mode = MODE_SINGLE_POINTER;

                mOldScale = mScale;
                smoothScrollTo(0,0);
                break;
            case MotionEvent.ACTION_DOWN:
                mStartDownX = event.getX();
                mStartDownY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i("CustomTouch","scrollBy "+((int)mStartDownY-(int)event.getY()));
                if(mode == MODE_SINGLE_POINTER) {
                    scrollBy((int) mStartDownX - (int) event.getX(), (int) mStartDownY - (int) event.getY());
                    mStartDownX = event.getX();
                    mStartDownY = event.getY();
                    Log.i("CustomTouch", "scroll x & y" + getScrollX() + "   " + getScrollY());
                }
                else if(mode == MODE_TWO_POINTER){
                    setScaleMotionEvent(event);
                    postInvalidate();
                }
                break;
            case 261:          // 第二个手指按下事件
                mode = MODE_TWO_POINTER;
                //缩放起点
                mRawDistance = distance(event);

                break;
            case 262:
            case MotionEvent.ACTION_POINTER_UP:
                mode = MODE_NO_OPERATION;
                break;
        }
        return super.onTouchEvent(event);
    }


    private void setScaleMotionEvent(MotionEvent event) {
        mCurrentDistance = distance(event);
        float scale = mOldScale * mCurrentDistance / mRawDistance;
        mScale = scale;
    }

    /**
     * 两点距离
     *
     * @param event
     * @return
     */
    private float distance(MotionEvent event) {
        float dx = event.getX(1) - event.getX(0);
        float dy = event.getY(1) - event.getY(0);

        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * 两点触控时，两点中点X坐标
     *
     * @param event
     * @return
     */
    private float twoPointCenterX(MotionEvent event) {
        float dx = event.getX(1) + event.getX(0);

        return dx / 2;
    }

    /**
     * 两点触控时，两点中点Y坐标
     *
     * @param event
     * @return
     */
    private float twoPointCenterY(MotionEvent event) {
        float dy = event.getY(1) + event.getY(0);

        return dy / 2;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.scale(mScale,mScale,mWidth/2,mHeight/2);
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public void smoothScrollTo(int destX,int destY) {
        int scrollX = getScrollX();
        int deltaX = destX - scrollX;
        int scrollY = getScrollY();
        int deltaY = destY - scrollY;
        Log.i("scroller1","startScroll");
        mScroller.startScroll(scrollX,scrollY,deltaX,deltaY,500);
        invalidate();
    }

}
