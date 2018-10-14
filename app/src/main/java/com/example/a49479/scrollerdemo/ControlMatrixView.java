package com.example.a49479.scrollerdemo;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.AccelerateInterpolator;
import android.widget.RelativeLayout;

public class ControlMatrixView extends RelativeLayout {
    public ControlMatrixView(Context context) {
        super(context);
        init();
    }

    public ControlMatrixView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ControlMatrixView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private boolean mInit = false;
    private float mWidth, mHeight;

    /**
     * 简单初始化
     */
    private void init() {
        mMatrix = new Matrix();
    }

    /**
     * 变换矩阵
     */
    private Matrix mMatrix;

    /**
     * 触摸的模式： 无 / 单指 / 双指
     */
    private int mTouchMode = TOUCH_MODE_NO_OPERATION;
    private static final int TOUCH_MODE_SINGLE_POINTER = 0;
    private static final int TOUCH_MODE_TWO_POINTER = 1;
    private static final int TOUCH_MODE_NO_OPERATION = -1;

    /**
     * 平移前的位置
     */
    private PointF mLastPoint;
    /**
     * 总共平移量
     */
    private float mTotalTranslateX, mTotalTranslateY;

    /**
     * 缩放前的两指间距离
     */
    private float mLastDst;
    private float mFactor = 1;
    private static float FACTOR_MAX = 2;
    private static float FACTOR_MIN = 1;
    private PointF mMidPoint;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (!mInit) {
            mWidth = getMeasuredWidth();
            mHeight = getMeasuredHeight();
            mInit = true;
        }
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        //拦截该控件以及其子控件的 总canvas
        canvas.setMatrix(mMatrix);
        super.dispatchDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:

                //平移复位
                float limitX = (mFactor - 1) * mWidth / 2;
                if (Math.abs(mTotalTranslateX) > limitX) {
                    ValueAnimator animator = ValueAnimator.ofFloat(mTotalTranslateX, mTotalTranslateX>0?limitX:-limitX);
                    animator.setDuration(100);
                    animator.setInterpolator(new AccelerateInterpolator());
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float value = (float) animation.getAnimatedValue();
                            postTranslateDst(value - mTotalTranslateX, 0);
                            mTotalTranslateX = value;
                            postInvalidate();
                        }
                    });
                    animator.start();
                }

                float limitY = (mFactor - 1) * mHeight / 2;
                if(Math.abs(mTotalTranslateY)>limitY) {
                    ValueAnimator animator2 = ValueAnimator.ofFloat(mTotalTranslateY, mTotalTranslateY>0?limitY:-limitY);
                    animator2.setDuration(100);
                    animator2.setInterpolator(new AccelerateInterpolator());
                    animator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float value = (float) animation.getAnimatedValue();
                            postTranslateDst(0, value - mTotalTranslateY);
                            mTotalTranslateY = value;
                            postInvalidate();
                        }
                    });
                    animator2.start();
                }
//                postTranslateDst(-mTotalTranslateX,-mTotalTranslateY);

                break;
            case MotionEvent.ACTION_DOWN:
                mLastPoint = new PointF(event.getX(), event.getY());
                mTouchMode = TOUCH_MODE_SINGLE_POINTER;
                break;
            case 261:
                mTouchMode = TOUCH_MODE_TWO_POINTER;
                mLastDst = getDistanceBetweenTwoPoint(event);
                mMidPoint = getMidPointBetweenTwoPoint(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTouchMode == TOUCH_MODE_SINGLE_POINTER) {
                    //平移
                    postTranslateMotionEvent(event);
                } else if (mTouchMode == TOUCH_MODE_TWO_POINTER) {
                    //缩放
                    postScaleMotionEvent(event);
                }
                break;

            case 262:
            case MotionEvent.ACTION_POINTER_UP:
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 矩阵变换：平移(叠加)
     *
     * @param event 以MotionEvent(的 触控点坐标) 作参数
     */
    private void postTranslateMotionEvent(MotionEvent event) {
        float dstTransX = event.getX() - mLastPoint.x;
        float dstTransY = event.getY() - mLastPoint.y;
        mLastPoint = new PointF(event.getX(), event.getY());
        Log.i("ControlMatrix", "per dst:" + dstTransX + "-" + dstTransY);
        mMatrix.postTranslate(dstTransX, dstTransY);
        mTotalTranslateX += dstTransX;
        mTotalTranslateY += dstTransY;
        Log.i("ControlMatrix", "total dst:" + mTotalTranslateX + "-" + mTotalTranslateY);
        postInvalidate();
    }

    /**
     * 矩阵变换：平移(叠加)
     *
     * @param dstTransX
     * @param dstTransY
     */
    private void postTranslateDst(float dstTransX, float dstTransY) {
        mMatrix.postTranslate(dstTransX, dstTransY);
    }

    /**
     * 矩阵变换：平移(设置)
     *
     * @param x
     * @param y
     */
    private void setTranslate(float x, float y) {
        mMatrix.setTranslate(x, y);
    }


    /**
     * 矩阵变换：缩放
     */
    private void postScaleMotionEvent(MotionEvent event) {
        if (event.getPointerCount() != 2) {
            return;
        }
        float dst = getDistanceBetweenTwoPoint(event);
        float scale = dst / mLastDst;
        mLastDst = dst;
        Log.i("scale", scale + " - " + event.getPointerCount());
        Log.i("mFactor", mFactor + "");
        float factor = mFactor * scale;

        if(factor >=FACTOR_MIN && factor <=FACTOR_MAX ) {
            mFactor = factor;
            mMatrix.postScale(scale, scale, mWidth / 2+mTotalTranslateX, mHeight / 2 +mTotalTranslateY);
            postInvalidate();
        }
    }

    private void setScale(float scale) {
        mMatrix.setScale(scale, scale, mWidth / 2, mHeight / 2);
    }

    /**
     * 两点距离
     *
     * @param event
     * @return
     */
    private float getDistanceBetweenTwoPoint(MotionEvent event) {
        if (event.getPointerCount() != 2) {
            return 1f;
        }
        Log.i("pointer", event.getPointerCount() + "");
        float dx = event.getX(1) - event.getX(0);
        float dy = event.getY(1) - event.getY(0);

        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private PointF getMidPointBetweenTwoPoint(MotionEvent event) {
        float x = event.getX(1) + event.getX(0) / 2;
        float y = (event.getY(1) + event.getY(0)) / 2;
        return new PointF(x, y);
    }
}

