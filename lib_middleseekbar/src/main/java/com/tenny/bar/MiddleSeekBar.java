package com.tenny.bar;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Property;
import android.view.MotionEvent;
import android.view.View;

/**
 * 高仿Instagram图片加编辑效果（亮度、对比度等）的拖动条，
 * 以中间为起点，向左滑动递减，向右滑动递增
 * 拖动条的进度实际是0-100，通过变换显示成-100到100
 */


public class MiddleSeekBar extends View {

    private static final float THUMB_SHADOW = 3.5f;
    private static final float Y_OFFSET = 1.75f;
    private static final int KEY_SHADOW_COLOR = 0x1E000000;
    private static final float HIT_SCOPE_RATIO = 1.2f;

    private int mThumbRadius;
    private int mThumbColor;
    private int mProgressColor;
    private int mProgressBackgroundColor;
    private int mProgressWidth;
    private int mTextColor;
    private int mTextSize;


    private Point mStartPoint = new Point();//progress 起点
    private float mThumbRatio = 0.5f;

    //这个方框为活动圆的作用区域
    private RectF mThumbRect = new RectF();


    private int mProgressLength;
    private int mThumbShadowRadius;
    private int mYOffset;

    //活动圆顶上，提示进度的文字
    private String mPromptString;


    private Paint mProgressPaint = new Paint();
    private Paint mThumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);


    private onSeekBarChangeListener mOnSeekBarChangeListener;

    public MiddleSeekBar(Context context) {
        this(context, null, 0);
    }

    public MiddleSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiddleSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }


    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MiddleSeekBar, defStyleAttr, R.style.MiddleSeekBar);
        mThumbRadius = a.getDimensionPixelSize(R.styleable.MiddleSeekBar_MB_ThumbRadius, 0);
        mThumbColor = a.getColor(R.styleable.MiddleSeekBar_MB_ThumbColor, 0);
        mTextColor = a.getColor(R.styleable.MiddleSeekBar_MB_TextColor, 0);
        mProgressColor = a.getColor(R.styleable.MiddleSeekBar_MB_ProgressColor, 0);
        mProgressBackgroundColor = a.getColor(R.styleable.MiddleSeekBar_MB_ProgressBackgroundColor, 0);
        mTextSize = a.getDimensionPixelSize(R.styleable.MiddleSeekBar_MB_TextSize, 0);
        mProgressWidth = a.getDimensionPixelOffset(R.styleable.MiddleSeekBar_MB_ProgressWidth, 0);
        a.recycle();

        //初始化提示文字画笔
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
        //初始化进度条画笔
        mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(mProgressWidth);
        //初始化活动圆画笔
        mThumbShadowRadius = (int) (getResources().getDisplayMetrics().density * THUMB_SHADOW);
        mYOffset = (int) (getResources().getDisplayMetrics().density * Y_OFFSET);
        if(!isInEditMode()) {
            ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_SOFTWARE, mThumbPaint);
            mThumbPaint.setShadowLayer(mThumbShadowRadius, 0, mYOffset, KEY_SHADOW_COLOR);
        }
        mThumbPaint.setColor(mThumbColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        //suggestHeight算出拖动条的高度
        int suggestHeight = (int) (3 * mThumbRadius + mThumbShadowRadius - fontMetrics.top + fontMetrics.bottom + getPaddingTop() + getPaddingBottom());
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                resolveSizeAndState(suggestHeight, heightMeasureSpec, 0));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //设置最左边的起始点
        mStartPoint.set(getPaddingLeft() + mThumbRadius, h - getPaddingBottom() - mThumbRadius - mThumbShadowRadius);
        mProgressLength = w - getPaddingLeft() - getPaddingRight() - 2 * mThumbRadius;
        //设置活动圆作用区域的长方形
        mThumbRect.set(mStartPoint.x + mProgressLength * mThumbRatio - HIT_SCOPE_RATIO * mThumbRadius, mStartPoint.y - HIT_SCOPE_RATIO * mThumbRadius,
                mStartPoint.x + mProgressLength * mThumbRatio + HIT_SCOPE_RATIO * mThumbRadius, mStartPoint.y + HIT_SCOPE_RATIO * mThumbRadius);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int y = mStartPoint.y;
        int firstX = mStartPoint.x;//最左边点
        int secondX = mStartPoint.x + mProgressLength/2;//中间点
        int thirdX = (int) mThumbRect.centerX();//活动圆所在点

        //draw progress将进度条分成三段，前段没有到达时显示背景色，中段显示进度颜色，后端没有到达时显示背景色
        mProgressPaint.setColor(mProgressBackgroundColor);
        canvas.drawLine(firstX, y, secondX<thirdX?secondX:thirdX, y, mProgressPaint);
        mProgressPaint.setColor(mProgressColor);
        if (secondX < thirdX){
            canvas.drawLine(secondX, y, thirdX, y, mProgressPaint);
        }else {
            canvas.drawLine(thirdX, y, secondX, y, mProgressPaint);
        }
        mProgressPaint.setColor(mProgressBackgroundColor);
        canvas.drawLine(thirdX>secondX?thirdX:secondX, y, firstX + mProgressLength, y, mProgressPaint);

        //画活动圆
        canvas.drawCircle(thirdX, y, mThumbRadius, mThumbPaint);

        //画提示文字
        mPromptString = ratio2String(mThumbRatio);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(mPromptString, mThumbRect.centerX(), mThumbRect.top - mThumbRadius, mTextPaint);
    }


    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.thumbRatio = mThumbRatio;
        return ss;
    }


    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            mThumbRatio = ((SavedState) state).thumbRatio;
            super.onRestoreInstanceState(((SavedState) state).getSuperState());
        }else {
            super.onRestoreInstanceState(state);
        }
    }

    private static final int INVALID_POINTER = -1;

    static class SelectInfo {
        int pointerId = INVALID_POINTER;
        boolean isCaptured = false;

        void invalid() {
            pointerId = INVALID_POINTER;
            isCaptured = false;
        }
    }

    private SelectInfo selectInfo = new SelectInfo();

    /**
     * reset to default state
     */
    public void reset(){
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, mThumbProperty,new float[]{0.5f});
        objectAnimator.setDuration(200);
        objectAnimator.start();
    }

    public void setProgress(int progress){
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, mThumbProperty,new float[]{progress/100f});
        objectAnimator.setDuration(200);
        objectAnimator.start();
    }

    public int getProgress(){
        return (int) (mThumbRatio *100);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //判断触摸点在活动圆方形作用域内
                if (mThumbRect.contains(x, y)) {
                    selectInfo.pointerId = event.getPointerId(0);
                    selectInfo.isCaptured = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (selectInfo.isCaptured) {
                    if (moveThumb(x)) return false;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                selectInfo.invalid();
                break;
        }
        if (selectInfo.isCaptured) {
            if(mOnSeekBarChangeListener!=null){
                mOnSeekBarChangeListener.onProgressChanged(this, mThumbRatio);
            }
            invalidate();
        }
        //让父View的 onInterceptTouchEvent(MotionEvent)失效
        if(ViewCompat.isAttachedToWindow(this)){
            getParent().requestDisallowInterceptTouchEvent(selectInfo.isCaptured);
        }
        return selectInfo.isCaptured;
    }


    private boolean moveThumb(float x) {
        if (x > mStartPoint.x) {
            mThumbRatio = (x - mStartPoint.x) / mProgressLength;
            if (mThumbRatio > 1) {
                mThumbRatio = 1;
                return true;
            }
            float offsetX = x - mThumbRect.centerX();
            //活动圆位置进行偏移调整
            mThumbRect.offset(offsetX, 0);
        }
        return false;
    }


    protected String ratio2String(float ratio) {
        float current = ratio*202 - 101;
        if (Math.abs(current) < 0.99){//扩大零点范围
            return "";
        }else if (Math.abs(current) > 100){//扩大正负100的范围
            if (current > 0){
                return "100";
            }else {
                return "-100";
            }
        }else {
            return String.valueOf(Math.round(current));
        }
    }

    public void setOnSeekBarChangeListener(onSeekBarChangeListener onSeekBarChangeListener) {
        mOnSeekBarChangeListener = onSeekBarChangeListener;
    }

    public interface onSeekBarChangeListener {
        void onProgressChanged(MiddleSeekBar doubleSeekBar, float secondThumbRatio);
    }



    public static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
        float thumbRatio;

        public SavedState(Parcel source) {
            super(source);
            thumbRatio = source.readFloat();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeFloat(thumbRatio);
        }
    }

    private Property<MiddleSeekBar,Float> mThumbProperty = new Property<MiddleSeekBar, Float>(Float.class,"thumbRatio") {
        @Override
        public Float get(MiddleSeekBar object) {
            return object.mThumbRatio;
        }

        @Override
        public void set(MiddleSeekBar object, Float value) {
            object.mThumbRatio = value;
            object.mThumbRect.set(mStartPoint.x + mProgressLength * mThumbRatio - HIT_SCOPE_RATIO * mThumbRadius, mStartPoint.y - HIT_SCOPE_RATIO * mThumbRadius,
                    mStartPoint.x + mProgressLength * mThumbRatio + HIT_SCOPE_RATIO * mThumbRadius, mStartPoint.y + HIT_SCOPE_RATIO * mThumbRadius);
            postInvalidate();
        }
    };
}
