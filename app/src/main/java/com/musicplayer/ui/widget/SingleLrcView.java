package com.musicplayer.ui.widget;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.OverScroller;

import com.lauzy.freedom.library.Lrc;
import com.lauzy.freedom.library.LrcHelper;
import com.lauzy.freedom.library.R.drawable;
import com.lauzy.freedom.library.R.styleable;

import java.util.HashMap;
import java.util.List;

public class SingleLrcView extends View {
    private static final String DEFAULT_CONTENT = "Empty";
    private List<Lrc> mLrcData;
    private TextPaint mTextPaint;
    private String mDefaultContent;
    private int mCurrentLine;
    private float mOffset;
    private float mLastMotionX;
    private float mLastMotionY;
    private int mScaledTouchSlop;
    private OverScroller mOverScroller;
    private VelocityTracker mVelocityTracker;
    private int mMaximumFlingVelocity;
    private int mMinimumFlingVelocity;
    private float mLrcTextSize;
    private float mLrcLineSpaceHeight;
    private int mTouchDelay;
    private int mNormalColor;
    private int mCurrentPlayLineColor;
    private float mNoLrcTextSize;
    private int mNoLrcTextColor;
    private boolean isDragging;
    private boolean isUserScroll;
    private boolean isAutoAdjustPosition;
    private Drawable mPlayDrawable;
    private boolean isShowTimeIndicator;
    private Rect mPlayRect;
    private Paint mIndicatorPaint;
    private float mIndicatorLineWidth;
    private float mIndicatorTextSize;
    private int mCurrentIndicateLineTextColor;
    private int mIndicatorLineColor;
    private float mIndicatorMargin;
    private float mIconLineGap;
    private float mIconWidth;
    private float mIconHeight;
    private boolean isEnableShowIndicator;
    private int mIndicatorTextColor;
    private int mIndicatorTouchDelay;
    private HashMap<String, StaticLayout> mLrcMap;
    private Runnable mScrollRunnable;
    private Runnable mHideIndicatorRunnable;
    private HashMap<String, StaticLayout> mStaticLayoutHashMap;
    private SingleLrcView.OnPlayIndicatorLineListener mOnPlayIndicatorLineListener;

    boolean isDownAction;

    public SingleLrcView(Context context) {
        this(context, (AttributeSet)null);
    }

    public SingleLrcView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SingleLrcView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.isAutoAdjustPosition = true;
        this.isEnableShowIndicator = true;
        this.mLrcMap = new HashMap();
        this.mScrollRunnable = new Runnable() {
            public void run() {
                SingleLrcView.this.isUserScroll = false;
                SingleLrcView.this.scrollToPosition(SingleLrcView.this.mCurrentLine);
            }
        };
        this.mHideIndicatorRunnable = new Runnable() {
            public void run() {
                SingleLrcView.this.isShowTimeIndicator = false;
                SingleLrcView.this.invalidateView();
            }
        };
        this.mStaticLayoutHashMap = new HashMap();
        this.init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, styleable.LrcView);
        this.mLrcTextSize = typedArray.getDimension(styleable.LrcView_lrcTextSize, (float)this.sp2px(context, 15.0F));
        this.mLrcLineSpaceHeight = typedArray.getDimension(styleable.LrcView_lrcLineSpaceSize, (float)this.dp2px(context, 20.0F));
        this.mTouchDelay = typedArray.getInt(styleable.LrcView_lrcTouchDelay, 3500);
        this.mIndicatorTouchDelay = typedArray.getInt(styleable.LrcView_indicatorTouchDelay, 2500);
        this.mNormalColor = typedArray.getColor(styleable.LrcView_lrcNormalTextColor, -7829368);
        this.mCurrentPlayLineColor = typedArray.getColor(styleable.LrcView_lrcCurrentTextColor, -16776961);
        this.mNoLrcTextSize = typedArray.getDimension(styleable.LrcView_noLrcTextSize, (float)this.dp2px(context, 20.0F));
        this.mNoLrcTextColor = typedArray.getColor(styleable.LrcView_noLrcTextColor, -16777216);
        this.mIndicatorLineWidth = typedArray.getDimension(styleable.LrcView_indicatorLineHeight, (float)this.dp2px(context, 0.5F));
        this.mIndicatorTextSize = typedArray.getDimension(styleable.LrcView_indicatorTextSize, (float)this.sp2px(context, 13.0F));
        this.mIndicatorTextColor = typedArray.getColor(styleable.LrcView_indicatorTextColor, -7829368);
        this.mCurrentIndicateLineTextColor = typedArray.getColor(styleable.LrcView_currentIndicateLrcColor, -7829368);
        this.mIndicatorLineColor = typedArray.getColor(styleable.LrcView_indicatorLineColor, -7829368);
        this.mIndicatorMargin = typedArray.getDimension(styleable.LrcView_indicatorStartEndMargin, (float)this.dp2px(context, 5.0F));
        this.mIconLineGap = typedArray.getDimension(styleable.LrcView_iconLineGap, (float)this.dp2px(context, 3.0F));
        this.mIconWidth = typedArray.getDimension(styleable.LrcView_playIconWidth, (float)this.dp2px(context, 20.0F));
        this.mIconHeight = typedArray.getDimension(styleable.LrcView_playIconHeight, (float)this.dp2px(context, 20.0F));
        this.mPlayDrawable = typedArray.getDrawable(styleable.LrcView_playIcon);
        this.mPlayDrawable = this.mPlayDrawable == null ? ContextCompat.getDrawable(context, drawable.play_icon) : this.mPlayDrawable;
        typedArray.recycle();
        this.setupConfigs(context);
    }

    private void setupConfigs(Context context) {
        this.mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mMaximumFlingVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        this.mMinimumFlingVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
        this.mOverScroller = new OverScroller(context, new DecelerateInterpolator());
        this.mOverScroller.setFriction(0.1F);
        this.mTextPaint = new TextPaint();
        this.mTextPaint.setAntiAlias(true);
        this.mTextPaint.setTextAlign(Align.CENTER);
        this.mTextPaint.setTextSize(this.mLrcTextSize);
        this.mDefaultContent = "Empty";
        this.mIndicatorPaint = new Paint();
        this.mIndicatorPaint.setAntiAlias(true);
        this.mIndicatorPaint.setStrokeWidth(this.mIndicatorLineWidth);
        this.mIndicatorPaint.setColor(this.mIndicatorLineColor);
        this.mPlayRect = new Rect();
        this.mIndicatorPaint.setTextSize(this.mIndicatorTextSize);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            this.mPlayRect.left = (int)this.mIndicatorMargin;
            this.mPlayRect.top = (int)((float)(this.getHeight() / 2) - this.mIconHeight / 2.0F);
            this.mPlayRect.right = (int)((float)this.mPlayRect.left + this.mIconWidth);
            this.mPlayRect.bottom = (int)((float)this.mPlayRect.top + this.mIconHeight);
            this.mPlayDrawable.setBounds(this.mPlayRect);
        }

    }

    private int getLrcWidth() {
        return this.getWidth() - this.getPaddingLeft() - this.getPaddingRight();
    }

    private int getLrcHeight() {
        return this.getHeight();
    }

    private boolean isLrcEmpty() {
        return this.mLrcData == null || this.getLrcCount() == 0;
    }

    private int getLrcCount() {
        return this.mLrcData.size();
    }

    public void setLrcData(List<Lrc> lrcData) {
        this.resetView("Empty");
        this.mLrcData = lrcData;
        this.invalidate();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.isLrcEmpty()) {
            this.drawEmptyText(canvas);
        } else {
            int indicatePosition = this.getIndicatePosition();
            this.mTextPaint.setTextSize(this.mLrcTextSize);
            this.mTextPaint.setTextAlign(Align.CENTER);
            float y = (float)(this.getLrcHeight() / 2);
            float x = (float)(this.getLrcWidth() / 2 + this.getPaddingLeft());

            for(int i = 0; i < this.getLrcCount(); ++i) {
                if (i > 0) {
                    y += (this.getTextHeight(i - 1) + this.getTextHeight(i)) / 2.0F + this.mLrcLineSpaceHeight;
                }

                if (this.mCurrentLine == i) {
                    this.mTextPaint.setColor(this.mCurrentPlayLineColor);
                } else if (indicatePosition == i && this.isShowTimeIndicator) {
                    this.mTextPaint.setColor(this.mCurrentIndicateLineTextColor);
                } else {
                    this.mTextPaint.setColor(this.mNormalColor);
                }

                this.drawLrc(canvas, x, y, i);
            }

            if (this.isShowTimeIndicator) {
                this.mPlayDrawable.draw(canvas);
                long time = ((Lrc)this.mLrcData.get(indicatePosition)).getTime();
                float timeWidth = this.mIndicatorPaint.measureText(LrcHelper.formatTime(time));
                this.mIndicatorPaint.setColor(this.mIndicatorLineColor);
                canvas.drawLine((float)this.mPlayRect.right + this.mIconLineGap, (float)(this.getHeight() / 2), (float)this.getWidth() - timeWidth * 1.3F, (float)(this.getHeight() / 2), this.mIndicatorPaint);
                int baseX = (int)((float)this.getWidth() - timeWidth * 1.1F);
                float baseline = (float)(this.getHeight() / 2) - (this.mIndicatorPaint.descent() - this.mIndicatorPaint.ascent()) / 2.0F - this.mIndicatorPaint.ascent();
                this.mIndicatorPaint.setColor(this.mIndicatorTextColor);
                canvas.drawText(LrcHelper.formatTime(time), (float)baseX, baseline, this.mIndicatorPaint);
            }

        }
    }

    private void drawLrc(Canvas canvas, float x, float y, int i) {
        String text = ((Lrc)this.mLrcData.get(i)).getText();
        StaticLayout staticLayout = (StaticLayout)this.mLrcMap.get(text);
        if (staticLayout == null) {
            this.mTextPaint.setTextSize(this.mLrcTextSize);
            staticLayout = new StaticLayout(text, this.mTextPaint, this.getLrcWidth(), Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
            this.mLrcMap.put(text, staticLayout);
        }

        canvas.save();
        canvas.translate(x, y - (float)(staticLayout.getHeight() / 2) - this.mOffset);
        staticLayout.draw(canvas);
        canvas.restore();
    }

    private void drawEmptyText(Canvas canvas) {
        this.mTextPaint.setTextAlign(Align.CENTER);
        this.mTextPaint.setColor(this.mNoLrcTextColor);
        this.mTextPaint.setTextSize(this.mNoLrcTextSize);
        canvas.save();
        StaticLayout staticLayout = new StaticLayout(this.mDefaultContent, this.mTextPaint, this.getLrcWidth(), Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
        canvas.translate((float)(this.getLrcWidth() / 2 + this.getPaddingLeft()), (float)(this.getLrcHeight() / 2));
        staticLayout.draw(canvas);
        canvas.restore();
    }

    public void updateTime(long time) {
        if (!this.isLrcEmpty()) {
            int linePosition = this.getUpdateTimeLinePosition(time);
            if (this.mCurrentLine != linePosition) {
                this.mCurrentLine = linePosition;
                if (this.isUserScroll) {
                    this.invalidateView();
                    return;
                }

                ViewCompat.postOnAnimation(this, this.mScrollRunnable);
            }

        }
    }

    private int getUpdateTimeLinePosition(long time) {
        int linePos = 0;

        for(int i = 0; i < this.getLrcCount(); ++i) {
            Lrc lrc = (Lrc)this.mLrcData.get(i);
            if (time >= lrc.getTime()) {
                if (i == this.getLrcCount() - 1) {
                    linePos = this.getLrcCount() - 1;
                } else if (time < ((Lrc)this.mLrcData.get(i + 1)).getTime()) {
                    linePos = i;
                    break;
                }
            }
        }

        return linePos;
    }

    private void scrollToPosition(int linePosition) {
        float scrollY = this.getItemOffsetY(linePosition);
        ValueAnimator animator = ValueAnimator.ofFloat(new float[]{this.mOffset, scrollY});
        animator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                SingleLrcView.this.mOffset = (Float)animation.getAnimatedValue();
                SingleLrcView.this.invalidateView();
            }
        });
        animator.setDuration(300L);
        animator.start();
    }

    public int getIndicatePosition() {
        int pos = 0;
        float min = 3.4028235E38F;

        for(int i = 0; i < this.mLrcData.size(); ++i) {
            float offsetY = this.getItemOffsetY(i);
            float abs = Math.abs(offsetY - this.mOffset);
            if (abs < min) {
                min = abs;
                pos = i;
            }
        }

        return pos;
    }

    private float getItemOffsetY(int linePosition) {
        float tempY = 0.0F;

        for(int i = 1; i <= linePosition; ++i) {
            tempY += (this.getTextHeight(i - 1) + this.getTextHeight(i)) / 2.0F + this.mLrcLineSpaceHeight;
        }

        return tempY;
    }

    private float getTextHeight(int linePosition) {
        String text = ((Lrc)this.mLrcData.get(linePosition)).getText();
        StaticLayout staticLayout = (StaticLayout)this.mStaticLayoutHashMap.get(text);
        if (staticLayout == null) {
            this.mTextPaint.setTextSize(this.mLrcTextSize);
            staticLayout = new StaticLayout(text, this.mTextPaint, this.getLrcWidth(), Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
            this.mStaticLayoutHashMap.put(text, staticLayout);
        }

        return (float)staticLayout.getHeight();
    }

    private boolean overScrolled() {
        return this.mOffset > this.getItemOffsetY(this.getLrcCount() - 1) || this.mOffset < 0.0F;
    }

//    public boolean onTouchEvent(MotionEvent event) {
//
//        if (this.isLrcEmpty()) {
//            return super.onTouchEvent(event);
//        } else {
//            if (this.mVelocityTracker == null) {
//                this.mVelocityTracker = VelocityTracker.obtain();
//            }
//
//            this.mVelocityTracker.addMovement(event);
//            switch(event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                this.removeCallbacks(this.mScrollRunnable);
//                this.removeCallbacks(this.mHideIndicatorRunnable);
//                if (!this.mOverScroller.isFinished()) {
//                    this.mOverScroller.abortAnimation();
//                }
//
//                this.mLastMotionX = event.getX();
//                this.mLastMotionY = event.getY();
//                this.isUserScroll = true;
//                this.isDragging = false;
//                isDownAction = true;
//                break;
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_CANCEL:
//                Log.e("*LrcView","ACTION_CANCEL "+isDownAction);
//                if (!this.isDragging && (!this.isShowTimeIndicator || !this.onClickPlayButton(event))) {
//                    this.isShowTimeIndicator = false;
//                    this.invalidateView();
//                    this.performClick();
//                }
//                this.handleActionUp(event);
//                if (isDownAction && !onClickPlayButton(event)){
////                    return super.onTouchEvent(event);
//                    PlayActivity.isClickLrcView = true;
//                }
//                break;
//            case MotionEvent.ACTION_MOVE:
////                Log.e("*LrcView","滑动");
//                float moveY = event.getY() - this.mLastMotionY;
//                if (Math.abs(moveY) > (float)this.mScaledTouchSlop) {
//                    this.isDragging = true;
//                    this.isShowTimeIndicator = this.isEnableShowIndicator;
//                }
//
//                if (this.isDragging) {
//                    float maxHeight = this.getItemOffsetY(this.getLrcCount() - 1);
//                    if (this.mOffset < 0.0F || this.mOffset > maxHeight) {
//                        moveY /= 3.5F;
//                    }
//
//                    this.mOffset -= moveY;
//                    this.mLastMotionY = event.getY();
//                    this.invalidateView();
//                }
//                isDownAction = false;
//                break;
//            }
//
//            return true;
//        }
//    }

    private void handleActionUp(MotionEvent event) {
        if (this.isEnableShowIndicator) {
            ViewCompat.postOnAnimationDelayed(this, this.mHideIndicatorRunnable, (long)this.mIndicatorTouchDelay);
        }

        if (this.isShowTimeIndicator && this.mPlayRect != null && this.onClickPlayButton(event)) {
            this.isShowTimeIndicator = false;
            this.invalidateView();
            if (this.mOnPlayIndicatorLineListener != null) {
                this.mOnPlayIndicatorLineListener.onPlay(((Lrc)this.mLrcData.get(this.getIndicatePosition())).getTime(), ((Lrc)this.mLrcData.get(this.getIndicatePosition())).getText());
            }
        }

        if (this.overScrolled() && this.mOffset < 0.0F) {
            this.scrollToPosition(0);
            if (this.isAutoAdjustPosition) {
                ViewCompat.postOnAnimationDelayed(this, this.mScrollRunnable, (long)this.mTouchDelay);
            }

        } else if (this.overScrolled() && this.mOffset > this.getItemOffsetY(this.getLrcCount() - 1)) {
            this.scrollToPosition(this.getLrcCount() - 1);
            if (this.isAutoAdjustPosition) {
                ViewCompat.postOnAnimationDelayed(this, this.mScrollRunnable, (long)this.mTouchDelay);
            }

        } else {
            this.mVelocityTracker.computeCurrentVelocity(1000, (float)this.mMaximumFlingVelocity);
            float YVelocity = this.mVelocityTracker.getYVelocity();
            float absYVelocity = Math.abs(YVelocity);
            if (absYVelocity > (float)this.mMinimumFlingVelocity) {
                this.mOverScroller.fling(0, (int)this.mOffset, 0, (int)(-YVelocity), 0, 0, 0, (int)this.getItemOffsetY(this.getLrcCount() - 1), 0, (int)this.getTextHeight(0));
                this.invalidateView();
            }

            this.releaseVelocityTracker();
            if (this.isAutoAdjustPosition) {
                ViewCompat.postOnAnimationDelayed(this, this.mScrollRunnable, (long)this.mTouchDelay);
            }

        }
    }

    private boolean onClickPlayButton(MotionEvent event) {
        float left = (float)this.mPlayRect.left;
        float right = (float)this.mPlayRect.right;
        float top = (float)this.mPlayRect.top;
        float bottom = (float)this.mPlayRect.bottom;
        float x = event.getX();
        float y = event.getY();
        return this.mLastMotionX > left && this.mLastMotionX < right && this.mLastMotionY > top && this.mLastMotionY < bottom && x > left && x < right && y > top && y < bottom;
    }

    public void computeScroll() {
        super.computeScroll();
        if (this.mOverScroller.computeScrollOffset()) {
            this.mOffset = (float)this.mOverScroller.getCurrY();
            this.invalidateView();
        }

    }

    private void releaseVelocityTracker() {
        if (null != this.mVelocityTracker) {
            this.mVelocityTracker.clear();
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }

    }

    public void resetView(String defaultContent) {
        if (this.mLrcData != null) {
            this.mLrcData.clear();
        }

        this.mLrcMap.clear();
        this.mStaticLayoutHashMap.clear();
        this.mCurrentLine = 0;
        this.mOffset = 0.0F;
        this.isUserScroll = false;
        this.isDragging = false;
        this.mDefaultContent = defaultContent;
        this.removeCallbacks(this.mScrollRunnable);
        this.invalidate();
    }

    public boolean performClick() {
        return super.performClick();
    }

    public int dp2px(Context context, float dpVal) {
        return (int)TypedValue.applyDimension(1, dpVal, context.getResources().getDisplayMetrics());
    }

    public int sp2px(Context context, float spVal) {
        return (int)TypedValue.applyDimension(2, spVal, context.getResources().getDisplayMetrics());
    }

    public void pause() {
        this.isAutoAdjustPosition = false;
        this.invalidateView();
    }

    public void resume() {
        this.isAutoAdjustPosition = true;
        ViewCompat.postOnAnimationDelayed(this, this.mScrollRunnable, (long)this.mTouchDelay);
        this.invalidateView();
    }

    private void invalidateView() {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            this.invalidate();
        } else {
            this.postInvalidate();
        }

    }

    public void setOnPlayIndicatorLineListener(SingleLrcView.OnPlayIndicatorLineListener onPlayIndicatorLineListener) {
        this.mOnPlayIndicatorLineListener = onPlayIndicatorLineListener;
    }

    public void setEmptyContent(String defaultContent) {
        this.mDefaultContent = defaultContent;
        this.invalidateView();
    }

    public void setLrcTextSize(float lrcTextSize) {
        this.mLrcTextSize = lrcTextSize;
        this.invalidateView();
    }

    public void setLrcLineSpaceHeight(float lrcLineSpaceHeight) {
        this.mLrcLineSpaceHeight = lrcLineSpaceHeight;
        this.invalidateView();
    }

    public void setTouchDelay(int touchDelay) {
        this.mTouchDelay = touchDelay;
        this.invalidateView();
    }

    public void setNormalColor(@ColorInt int normalColor) {
        this.mNormalColor = normalColor;
        this.invalidateView();
    }

    public void setCurrentPlayLineColor(@ColorInt int currentPlayLineColor) {
        this.mCurrentPlayLineColor = currentPlayLineColor;
        this.invalidateView();
    }

    public void setNoLrcTextSize(float noLrcTextSize) {
        this.mNoLrcTextSize = noLrcTextSize;
        this.invalidateView();
    }

    public void setNoLrcTextColor(@ColorInt int noLrcTextColor) {
        this.mNoLrcTextColor = noLrcTextColor;
        this.invalidateView();
    }

    public void setIndicatorLineWidth(float indicatorLineWidth) {
        this.mIndicatorLineWidth = indicatorLineWidth;
        this.invalidateView();
    }

    public void setIndicatorTextSize(float indicatorTextSize) {
        this.mIndicatorPaint.setTextSize(indicatorTextSize);
        this.invalidateView();
    }

    public void setCurrentIndicateLineTextColor(int currentIndicateLineTextColor) {
        this.mCurrentIndicateLineTextColor = currentIndicateLineTextColor;
        this.invalidateView();
    }

    public void setIndicatorLineColor(int indicatorLineColor) {
        this.mIndicatorLineColor = indicatorLineColor;
        this.invalidateView();
    }

    public void setIndicatorMargin(float indicatorMargin) {
        this.mIndicatorMargin = indicatorMargin;
        this.invalidateView();
    }

    public void setIconLineGap(float iconLineGap) {
        this.mIconLineGap = iconLineGap;
        this.invalidateView();
    }

    public void setIconWidth(float iconWidth) {
        this.mIconWidth = iconWidth;
        this.invalidateView();
    }

    public void setIconHeight(float iconHeight) {
        this.mIconHeight = iconHeight;
        this.invalidateView();
    }

    public void setEnableShowIndicator(boolean enableShowIndicator) {
        this.isEnableShowIndicator = enableShowIndicator;
        this.invalidateView();
    }

    public Drawable getPlayDrawable() {
        return this.mPlayDrawable;
    }

    public void setPlayDrawable(Drawable playDrawable) {
        this.mPlayDrawable = playDrawable;
        this.mPlayDrawable.setBounds(this.mPlayRect);
        this.invalidateView();
    }

    public void setIndicatorTextColor(int indicatorTextColor) {
        this.mIndicatorTextColor = indicatorTextColor;
        this.invalidateView();
    }

    public interface OnPlayIndicatorLineListener {
        void onPlay(long var1, String var3);
    }
}
