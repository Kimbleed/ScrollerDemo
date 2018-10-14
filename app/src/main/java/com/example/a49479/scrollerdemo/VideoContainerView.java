package com.example.a49479.scrollerdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * Created by 49479 on 2018/7/2.
 */

public class VideoContainerView extends LinearLayout {

    //是否双点触控
    private int mode = MODE_SINGLE_POINTER;
    private static final int MODE_SINGLE_POINTER = 0;
    private static final int MODE_TWO_POINTER = 1;
    private static final int MODE_NO_OPERATION = -1;

    //上一次旋转的角度(起始角度)
    private int mOldDegree;
    //当前旋转角度
    private int mDegree;

    //上一次缩放的比例(起始比例)
    private float mOldScale = 1f;
    //当前缩放比例
    private float mScale = 1f;

    private float mOldTranX, mOldTranY;
    private float mTransX, mTransY;

    private int mWidth, mHeight;

    private float mStartDownX, mStartDownY;

    private float mRawDistance;

    private float mCurrentDistance;

    private Scroller mScroller;

    //两点触控时，初始两点的中点的坐标
    private float mRawTwoPointCenterX, mRawTwoPointCenterY;
    //两点触控时，当前两点的中点的坐标
    private float mCurrentTwoPointCenterX, mCurrentTwoPointCenterY;
    //两点触控时，当前两点中的一点的坐标
    private float mRawTwoPointSideX, mRawTwoPointSideY;

    private Matrix mMatrix;

    private GestureDetector mGestureDetector;

    private Paint mPaint;

    public VideoContainerView(Context context) {
        super(context);
        init();
    }

    public VideoContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mScroller = new Scroller(getContext());
        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });
        mMatrix = new Matrix();
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        matrix(canvas);
        super.dispatchDraw(canvas);
        mPaint.setColor(Color.RED);
        canvas.drawCircle(mRawTwoPointCenterX, mRawTwoPointCenterY, 20, mPaint);
        mPaint.setColor(Color.BLUE);
        canvas.drawCircle(mWidth / 2, mHeight / 2, 20, mPaint);
        mPaint.setColor(Color.YELLOW);
        canvas.drawCircle(0, 0, 20, mPaint);
        mPaint.setColor(Color.GREEN);
        float beishu = 1 / mScale;
        canvas.drawCircle(-mRawTwoPointCenterX * (beishu - 1), -mRawTwoPointCenterY * (beishu - 1), 40, mPaint);


    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        mCurrentTwoPointCenterX = mWidth / 2;
        mCurrentTwoPointCenterY = mHeight / 2;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i("CustomTouch", "action:" + event.getAction());
//        mGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                mode = MODE_SINGLE_POINTER;

                mRawDistance = 0f;
                mCurrentDistance = 0f;

                mOldScale = mScale;

                mOldDegree = mDegree;

                mOldTranX = mTransX;
                mOldTranY = mTransY;

                check();
                break;
            case MotionEvent.ACTION_DOWN:
                //平移起点
                mStartDownX = event.getX();
                mStartDownY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == MODE_SINGLE_POINTER) {
                    //平移处理
                    setTranslateMotionEvent(event);

                    postInvalidate();

                } else if (mode == MODE_TWO_POINTER) {

                    //处理旋转
                    setRotateMotionEvent(event);

                    //处理缩放
                    setScaleMotionEvent(event);

                    postInvalidate();
                }
                break;
            case 261:          // 第二个手指按下事件
                mode = MODE_TWO_POINTER;
                //缩放起点
                mRawDistance = distance(event);

                //旋转起点
                mRawTwoPointCenterX = twoPointCenterX(event);
                mRawTwoPointCenterY = twoPointCenterY(event);
                mRawTwoPointSideX = event.getX(0);
                mRawTwoPointSideY = event.getY(0);
                mCurrentTwoPointCenterX = twoPointCenterX(event);
                mCurrentTwoPointCenterY = twoPointCenterY(event);
                break;
            case 262:
            case MotionEvent.ACTION_POINTER_UP:
                mode = MODE_NO_OPERATION;
                break;

        }
        return super.onTouchEvent(event);
    }

    private void matrix(Canvas canvas) {
        canvas.setMatrix(mMatrix);
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

    /**
     * 获取角度
     *
     * @param centerX
     * @param centerY
     * @param xInView
     * @param yInView
     * @return
     */
    public static int getRotationBetweenLines(float centerX, float centerY, float xInView, float yInView) {
        double rotation = 0;

        double k1 = (double) (centerY - centerY) / (centerX * 2 - centerX);
        double k2 = (double) (yInView - centerY) / (xInView - centerX);
        double tmpDegree = Math.atan((Math.abs(k1 - k2)) / (1 + k1 * k2)) / Math.PI * 180;

        if (xInView > centerX && yInView < centerY) {  //第一象限
            rotation = 90 - tmpDegree;
        } else if (xInView > centerX && yInView > centerY) //第二象限
        {
            rotation = 90 + tmpDegree;
        } else if (xInView < centerX && yInView > centerY) { //第三象限
            rotation = 270 - tmpDegree;
        } else if (xInView < centerX && yInView < centerY) { //第四象限
            rotation = 270 + tmpDegree;
        } else if (xInView == centerX && yInView < centerY) {
            rotation = 0;
        } else if (xInView == centerX && yInView > centerY) {
            rotation = 180;
        }

        return (int) rotation;
    }

    private void setTranslateMotionEvent(MotionEvent event) {
        float tranX = event.getX() - mStartDownX + mOldTranX;
        float tranY = event.getY() - mStartDownY + mOldTranY;
        float dstTransX = tranX - mTransX;
        float dstTransY = tranY - mTransY;
        mTransX = tranX;
        mTransY = tranY;
        mMatrix.postTranslate(dstTransX, dstTransY);
    }

    private void postTranslate(float x,float y){

    }

    private void setRotateMotionEvent(MotionEvent event) {
        mCurrentTwoPointCenterX = twoPointCenterX(event);
        mCurrentTwoPointCenterY = twoPointCenterY(event);

        int degree = mOldDegree + getRotationBetweenLines(mCurrentTwoPointCenterX, mCurrentTwoPointCenterY, event.getX(0), event.getY(0)) - getRotationBetweenLines(mRawTwoPointCenterX, mRawTwoPointCenterY, mRawTwoPointSideX, mRawTwoPointSideY);
        int dstDegree = degree - mDegree;
        mDegree = degree;
        mMatrix.postRotate(dstDegree, mRawTwoPointCenterX, mRawTwoPointCenterY);
    }

    private void setScaleMotionEvent(MotionEvent event) {
        mCurrentDistance = distance(event);
        float scale = mOldScale * mCurrentDistance / mRawDistance;
        float dstScale = scale / mScale;
        mScale = scale;
        mMatrix.postScale(dstScale, dstScale, mRawTwoPointCenterX, mRawTwoPointCenterY);
    }

    private void check() {
        Log.i("check", "mDegree:" + mDegree);
        checkRotate();
        checkScale();
        checkTranslate();

        postInvalidate();
    }

    private void checkRotate() {
        mDegree = mDegree % 360;
        if (mDegree % 90 != 0) {
            int dstDegree = 0;
            for (int i = -4; i * 90 < 360; i++) {
                int start = i * 90;
                int end = (i + 1) * 90;
                if (start < mDegree && mDegree < end) {
                    dstDegree = (Math.abs(end - mDegree) > Math.abs(mDegree - start) ? (start - mDegree) : (end - mDegree));
                }
            }
            Log.i("check", "postDegree:" + dstDegree);
            mMatrix.postRotate(dstDegree, mRawTwoPointCenterX, mRawTwoPointCenterY);
            mDegree = mDegree + dstDegree;
            mOldDegree = mDegree;
        }
    }

    private void checkScale() {
        if (mScale > 2) {
            float dstScale = 2 / mScale;
            mMatrix.postScale(dstScale, dstScale);
            mScale = 2;
        } else if (mScale < 0.5) {
            float dstScale = 0.5f / mScale;
            mMatrix.postScale(dstScale, dstScale);
            mScale = 0.5f;
        }
    }

    private void checkTranslate() {
        float dstX = 0;
        float dstY = 0;


        float beishu = 1 / mScale;
        float x = -mRawTwoPointCenterX * (beishu - 1);
        float y = -mRawTwoPointCenterY * (beishu - 1);


        dstX = x*mScale-mTransX;

        dstY = y*mScale-mTransY;

        Log.i("checkTranslate", "zuobiao:" + x + "\t" + y);
        Log.i("checkTranslate", "tran:" + mTransX / mScale + "\t" + mTransY / mScale);
        Log.i("checkTranslate", "yuan:" + (mWidth * mScale - mRawTwoPointCenterX) + "\t" + ((mHeight * mScale) - mRawTwoPointCenterY));

        mTransX += dstX;
        mTransY += dstY;
        mOldTranX = mTransX;
        mOldTranY = mTransY;

        mMatrix.postTranslate(dstX, dstY);
    }
}
