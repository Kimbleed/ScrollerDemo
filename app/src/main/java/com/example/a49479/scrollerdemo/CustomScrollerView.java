package com.example.a49479.scrollerdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by 49479 on 2017/10/17.
 */

public class CustomScrollerView extends View {

    private int mWidth,mHeight;

    private float mStartDownX,mStartDownY;

    private int lastX,lastY;

    private Bitmap bitmap;

    private Paint mPaint ;

    public CustomScrollerView(Context context) {
        super(context);
    }

    public CustomScrollerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomScrollerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        if(bitmap == null) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.super_man);
//            bitmap = compressImg(bitmap, mWidth, mHeight);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i("CustomTouch","event x & y"+event.getX()+"   "+event.getY());
        switch(event.getAction()){
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_DOWN:
                mStartDownX = event.getX();
                mStartDownY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                // 方法 1
                //因为getParent.scrollBy 导致 this View 位置移动，eventGetX 与 方法2 中不一样
//                ((View)getParent()).scrollBy((int)mStartDownX-(int)event.getX(),(int)mStartDownY-(int)event.getY());


                //方法 2
                //每次MOVE ，或许会导致 View重新绘制，执行onDraw  ，如果在onDraw中生成图片（耗时操作），会导致卡顿
                Log.i("CustomTouch","scrollBy "+((int)mStartDownY-(int)event.getY()));
                scrollBy((int)mStartDownX-(int)event.getX(),(int)mStartDownY-(int)event.getY());
                mStartDownX = event.getX();
                mStartDownY = event.getY();
                Log.i("CustomTouch","scroll x & y"+getScrollX()+"   "+getScrollY());
                break;
        }
        return super.onTouchEvent(event);
    }

//    public boolean onTouchEvent(MotionEvent event) {
//        int x = (int) event.getX();
//        int y = (int) event.getY();
//        Log.i("CustomTouch","event x & y"+event.getX()+"   "+event.getY());
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                lastX = (int) event.getX();
//                lastY = (int) event.getY();
//                break;
//            case MotionEvent.ACTION_MOVE:
//                int offsetX = x - lastX;
//                int offsetY = y - lastY;
//
//                ((View) getParent()).scrollBy(0, -offsetY);
//                Log.i("CustomTouch","scroll x & y"+((View)getParent()).getScrollX()+"   "+((View)getParent()).getScrollY());
//                break;
//        }
//
//        return true;
//    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.i("CustomDraw","view w & h    " + bitmap.getWidth() +"    "+bitmap.getHeight());
        Rect rect = new Rect(0,0,bitmap.getWidth() ,bitmap.getHeight());
        Log.i("CustomDraw","bitmap w & h    " + bitmap.getWidth() +"    "+bitmap.getHeight());
        RectF rectF = new RectF(0,0,bitmap.getWidth(),bitmap.getHeight());
        canvas.drawBitmap(bitmap,rect,rectF,mPaint);
//        drawText(canvas,mPaint,"Fuck");
    }

    //画文字
    private void drawText(Canvas canvas, Paint p, String msg) {
        p.setTextSize(50);

        float value = p.measureText(msg);

        canvas.drawText(msg, mWidth /2 -value/2, mHeight / 2 + 12, p);

    }

    public static Bitmap compressImg(Bitmap bitmap, double newWidth, double newHeight) {
        float width = bitmap.getWidth();
        float height = bitmap.getHeight();
        //创建操作图片用的Matrix对象
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, (int) width, (int) height, matrix, true);
        return newBitmap;
    }
}
