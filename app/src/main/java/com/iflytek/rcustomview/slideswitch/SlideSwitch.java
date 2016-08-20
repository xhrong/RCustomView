/*
 * Copyright (C) 2015 Quinn Chen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iflytek.rcustomview.slideswitch;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.*;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import com.iflytek.rcustomview.R;


public class SlideSwitch extends View {

    public static final int SHAPE_RECT = 1;
    public static final int SHAPE_CIRCLE = 2;

    private static final int RIM_SIZE = 2;

    private static final int DEFAULT_OPEN_COLOR = Color.parseColor("#00ced1");
    private static final int DEFAULT_CLOSE_COLOR = Color.LTGRAY;
    private static final int DEFAULT_SLIDER_COLOR = Color.WHITE;

    // 5 attributes
    private int openColor;//打开状态下的颜色
    private int closeColor;//关闭状态下的颜色
    private int sliderColor;//滑块颜色
    private boolean isOpen;//是否打开状态
    private int shape;//状态状态：圆角矩形或矩形


    // varials of drawing
    private Paint paint;
    private Rect backRect;
    private Rect frontRect;
    private RectF frontCircleRect;
    private RectF backCircleRect;

    private int alpha;
    private int max_left;
    private int min_left;
    private int frontRect_left;
    private int frontRect_left_begin = RIM_SIZE;
    private int eventStartX;
    private int eventLastX;
    private int diffX = 0;
    private boolean slideable = true;

    private SlideListener listener;

    public interface SlideListener {
        void open();

        void close();
    }

    public SlideSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        listener = null;
        paint = new Paint();
        paint.setAntiAlias(true);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.slideswitch);
        openColor = a.getColor(R.styleable.slideswitch_openColor, DEFAULT_OPEN_COLOR);
        closeColor = a.getColor(R.styleable.slideswitch_closeColor, DEFAULT_CLOSE_COLOR);
        sliderColor = a.getColor(R.styleable.slideswitch_sliderColor, DEFAULT_SLIDER_COLOR);

        isOpen = a.getBoolean(R.styleable.slideswitch_isOpen, false);
        shape = a.getInt(R.styleable.slideswitch_shape, SHAPE_CIRCLE);
        a.recycle();
    }

    public SlideSwitch(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideSwitch(Context context) {
        this(context, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int defaultWidth = 80;
        int defaultHeight = 30;
//      int width = measureWidth(80, widthMeasureSpec);
//      int height = measureHeight(30, heightMeasureSpec);
        int width = measureDimension(defaultWidth, widthMeasureSpec);
        int height = measureDimension(defaultHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
        initDrawingVal();

    }


    //这种方式可以解决GOOGLE的BUG 63673，但是实现方式比较特殊，不建议用
    private int measureWidth(int defaultSize, int measureSpec) {
        int width = defaultSize;
        if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT) {

        } else if ((getLayoutParams().width == ViewGroup.LayoutParams.MATCH_PARENT)
                || (getLayoutParams().width == ViewGroup.LayoutParams.FILL_PARENT)) {
            width = MeasureSpec.getSize(measureSpec);
        } else {
            width = getLayoutParams().width;
        }
        return width;
    }

    //这种方式可以解决GOOGLE的BUG 63673，但是实现方式比较特殊，不建议用
    private int measureHeight(int defaultSize, int measureSpec) {
        int height = defaultSize;
        if (getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {

        } else if ((getLayoutParams().height == ViewGroup.LayoutParams.MATCH_PARENT)
                || (getLayoutParams().height == ViewGroup.LayoutParams.FILL_PARENT)) {
            height = MeasureSpec.getSize(measureSpec);
        } else {
            height = getLayoutParams().height;
        }
        return height;
    }

    //这种弄法在RelativeLayout下有个问题，即指定数值时，仍然被判定为AT_MOST，可能导致UI效果不是想要的样子。
    // 这是GOOGLE的BUG 63673：https://code.google.com/p/android/issues/detail?id=63673
    private int measureDimension(int defaultSize, int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = defaultSize;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(defaultSize, specSize);
            }
        }
        return result;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (shape == SHAPE_RECT) {//矩形
            paint.setColor(closeColor);
            canvas.drawRect(backRect, paint);
            paint.setColor(openColor);
            paint.setAlpha(alpha);
            canvas.drawRect(backRect, paint);
            frontRect.set(frontRect_left, RIM_SIZE, frontRect_left
                    + getMeasuredWidth() / 2 - RIM_SIZE, getMeasuredHeight()
                    - RIM_SIZE);
            paint.setColor(sliderColor);
            canvas.drawRect(frontRect, paint);

        } else {//圆角矩形
            int radius;
            radius = backRect.height() - RIM_SIZE;
            paint.setColor(closeColor);
            backCircleRect.set(backRect);
            canvas.drawRoundRect(backCircleRect, radius, radius, paint);
            paint.setColor(openColor);
            paint.setAlpha(alpha);
            canvas.drawRoundRect(backCircleRect, radius, radius, paint);
            frontRect.set(frontRect_left, RIM_SIZE, frontRect_left
                    + backRect.height() - 2 * RIM_SIZE, backRect.height()
                    - RIM_SIZE);
            frontCircleRect.set(frontRect);
            paint.setColor(sliderColor);
            canvas.drawRoundRect(frontCircleRect, radius, radius, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!slideable)
            return super.onTouchEvent(event);
        int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                eventStartX = (int) event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                eventLastX = (int) event.getRawX();
                diffX = eventLastX - eventStartX;
                int tempX = diffX + frontRect_left_begin;
                tempX = (tempX > max_left ? max_left : tempX);
                tempX = (tempX < min_left ? min_left : tempX);
                if (tempX >= min_left && tempX <= max_left) {
                    frontRect_left = tempX;
                    alpha = (int) (255 * (float) tempX / (float) max_left);
                    invalidateView();
                }
                break;
            case MotionEvent.ACTION_UP:
                int wholeX = (int) (event.getRawX() - eventStartX);
                frontRect_left_begin = frontRect_left;
                boolean toRight;
                toRight = (frontRect_left_begin > max_left / 2 ? true : false);
                if (Math.abs(wholeX) < 3) {
                    toRight = !toRight;
                }
                moveToDest(toRight);
                break;
            default:
                break;
        }
        return true;
    }


    public void setSlideListener(SlideListener listener) {
        this.listener = listener;
    }

    public void setState(boolean isOpen) {
        this.isOpen = isOpen;
        initDrawingVal();
        invalidateView();
    }

    public void setShapeType(int shapeType) {
        this.shape = shapeType;
    }

    public void setSlideable(boolean slideable) {
        this.slideable = slideable;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            this.isOpen = bundle.getBoolean("isOpen");
            state = bundle.getParcelable("instanceState");
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putBoolean("isOpen", this.isOpen);
        return bundle;
    }


    private void initDrawingVal() {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        backCircleRect = new RectF();
        frontCircleRect = new RectF();
        frontRect = new Rect();
        backRect = new Rect(0, 0, width, height);

        min_left = RIM_SIZE;

        if (shape == SHAPE_RECT)
            max_left = width / 2;
        else
            max_left = width - height + RIM_SIZE;

        if (isOpen) {
            frontRect_left = max_left;
            alpha = 255;
        } else {
            frontRect_left = RIM_SIZE;
            alpha = 0;
        }
        frontRect_left_begin = frontRect_left;

    }

    private void moveToDest(final boolean toRight) {
        ValueAnimator toDestAnim = ValueAnimator.ofInt(frontRect_left,
                toRight ? max_left : min_left);
        toDestAnim.setDuration(200);
        toDestAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        toDestAnim.start();
        toDestAnim.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                frontRect_left = (Integer) animation.getAnimatedValue();
                alpha = (int) (255 * (float) frontRect_left / (float) max_left);
                invalidateView();
            }
        });
        toDestAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (toRight) {
                    changeState(true);
                    frontRect_left_begin = max_left;
                } else {
                    changeState(false);
                    frontRect_left_begin = min_left;
                }
            }
        });
    }

    private void changeState(boolean isOpen) {
        if (this.isOpen != isOpen) {
            this.isOpen = isOpen;
            if (listener != null)
                if (isOpen) {
                    listener.open();
                } else {
                    listener.close();
                }
        }
    }

    private void invalidateView() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }
}
