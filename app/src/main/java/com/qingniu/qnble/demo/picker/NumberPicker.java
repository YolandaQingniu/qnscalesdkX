package com.qingniu.qnble.demo.picker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.NumberKeyListener;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.qingniu.qnble.demo.R;

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * author: yolanda-XY
 * date: 2018/3/30
 * package_name: com.qingniu.qnble.demo.picker
 * description: ${TODO}
 */

public class NumberPicker extends LinearLayout {
    // 在控件上显示的个数.
    private static final int SELECTOR_WHEEL_ITEM_COUNT = 5;
    // 长按默认的间隔时间
    private static final long DEFAULT_LONG_PRESS_UPDATE_INTERVAL = 300;
    // 中间选择的索引位置
    private static final int SELECTOR_MIDDLE_ITEM_INDEX = SELECTOR_WHEEL_ITEM_COUNT / 2;

    private static final int SELECTOR_MAX_FLING_VELOCITY_ADJUSTMENT = 8;
    // 调整选择器的时间.
    private static final int SELECTOR_ADJUSTMENT_DURATION_MILLIS = 800;
    // 滚动的持续时间.
    private static final int SNAP_SCROLL_DURATION = 500;
    /**
     * The strength of fading in the top and bottom while drawing the selector.
     */
    private static final float TOP_AND_BOTTOM_FADING_EDGE_STRENGTH = 0.9f;
    // 默认选择器的分屏高度
    private static final int UNSCALED_DEFAULT_SELECTION_DIVIDER_HEIGHT = 2;
    // 默认两个选项的间隔.
    private static final int UNSCALED_DEFAULT_SELECTION_DIVIDERS_DISTANCE = 48;
    // 默认的布局id
    private static final int DEFAULT_LAYOUT_RESOURCE_ID = 0;
    // 固定大小的默认值设置.
    private static final int SIZE_UNSPECIFIED = -1;

    private int backgroundID = -1;

    private static boolean isAddZero = true;

    private int color;
    public void initThemeColor(int themeColor){
        this.color = themeColor;
        mSelectorWheelPaint.setColor(this.color);
    }

    /**
     * 使用自定义NumberPicker格式回调使用两位数分钟 如“01”的字符串。保持一个静态格式等是最有效的
     * 的方式来做到这一点;它避免了在每次调用创建临时对象 格式（）。
     */
    private static class TwoDigitFormatter implements NumberPicker.Formatter {
        final StringBuilder mBuilder = new StringBuilder();

        char mZeroDigit;
        java.util.Formatter mFmt;

        final Object[] mArgs = new Object[1];

        TwoDigitFormatter() {
            final Locale locale = Locale.getDefault();
            init(locale);
        }

        private void init(Locale locale) {
            mFmt = createFormatter(locale);
            mZeroDigit = getZeroDigit(locale);
        }

        public String format(int value) {
            final Locale currentLocale = Locale.getDefault();
            if (mZeroDigit != getZeroDigit(currentLocale)) {
                init(currentLocale);
            }
            mArgs[0] = value;
            mBuilder.delete(0, mBuilder.length());
            if(isAddZero){
                mFmt.format("%02d", mArgs);
            }else{
                mFmt.format("%2d", mArgs);
            }

            return mFmt.toString();
        }

        private static char getZeroDigit(Locale locale) {
            // 获得用于零的字符
            return new DecimalFormatSymbols(locale).getZeroDigit();
        }

        private java.util.Formatter createFormatter(Locale locale) {
            return new java.util.Formatter(mBuilder, locale);
        }
    }

    private static final TwoDigitFormatter sTwoDigitFormatter = new TwoDigitFormatter();

    public static final Formatter getTwoDigitFormatter() {
        return sTwoDigitFormatter;
    }

    /**
     * 增加按钮
     */
    private final ImageButton mIncrementButton;

    /**
     * 减少按钮
     */
    private final ImageButton mDecrementButton;

    /**
     * 显示的输入的当前值
     */
    private final EditText mInputText;

    /**
     * 连个选择器之间的距离
     */
    private final int mSelectionDividersDistance;

    /**
     * 控件的最小高度
     */
    private final int mMinHeight;

    /**
     * 控件的最大高度.
     */
    private final int mMaxHeight;

    /**
     * 控件的最小宽度.
     */
    private final int mMinWidth;

    /**
     * 控件的最大宽度.
     */
    private int mMaxWidth;

    /**
     * 标记计算的最大宽度 Flag whether to compute the max width.
     */
    private final boolean mComputeMaxWidth;

    /**
     * 显示文本的大小.
     */
    private final int mTextSize;

    /**
     * 选择器中文本显示高度.
     */
    private int mSelectorTextGapHeight;

    /**
     * 存放所有显示数据的数组.
     */
    private String[] mDisplayedValues;

    /**
     * 最小值
     */
    private int mMinValue;

    /**
     * 最大值
     */
    private int mMaxValue;

    /**
     * 当前值
     */
    private int mValue;

    /**
     * 监听当前值的变化情况
     */
    private OnValueChangeListener mOnValueChangeListener;

    /**
     * 滚动状态的更改通知.
     */
    private OnScrollListener mOnScrollListener;

    /**
     * 格式化显示当前的值.
     */
    private Formatter mFormatter;

    /**
     * 长按时更新的速度值:300 The speed for updating the value form long press.
     */
    private long mLongPressUpdateInterval = DEFAULT_LONG_PRESS_UPDATE_INTERVAL;

    /**
     * 在选择器中缓存字符串标识的指标 Cache for the string representation of selector indices.
     */
    private final SparseArray<String> mSelectorIndexToStringCache = new SparseArray<String>();

    /**
     * 选择器显示值的数组 大小自定义 :这里改成了5
     */
    private final int[] mSelectorIndices = new int[SELECTOR_WHEEL_ITEM_COUNT];

    /**
     * 选择器的画笔 The {@link Paint} for drawing the selector.
     */
    private final Paint mSelectorWheelPaint;

    /**
     * 虚拟按钮(增加和减少) The {@link Drawable} for pressed virtual
     * (increment/decrement) buttons.
     */
    private final Drawable mVirtualButtonPressedDrawable;

    /**
     * 选择器的元素高度(文本+间隙) The bottom of a selector element (text + gap).
     */
    private int mSelectorElementHeight;

    /**
     * 滚动选择器的初始偏移量.
     */
    private int mInitialScrollOffset = Integer.MIN_VALUE;

    /**
     * The current offset of the scroll selector.
     */
    private int mCurrentScrollOffset;

    /**
     *
     * 负责滑动选择器 The {@link Scroller} responsible for flinging the selector.
     */
    private final Scroller mFlingScroller;

    /**
     * 负责调调整选择器 The {@link Scroller} responsible for adjusting the selector.
     */
    private final Scroller mAdjustScroller;

    /**
     * The previous Y coordinate while scrolling the selector.
     */
    private int mPreviousScrollerY;

    /**
     * Handle to the reusable command for setting the input text selection.
     */
    private SetSelectionCommand mSetSelectionCommand;

    /**
     * Handle to the reusable command for changing the current value from long
     * press by threes.
     */
    private ChangeCurrentByOneFromLongPressCommand mChangeCurrentByOneFromLongPressCommand;

    /**
     * Command for beginning an edit of the current value via IME on long press.
     */
    private BeginSoftInputOnLongPressCommand mBeginSoftInputOnLongPressCommand;

    /**
     * The Y normalposition of the last down event.
     */
    private float mLastDownEventY;

    /**
     * The createdTime of the last down event.
     */
    private long mLastDownEventTime;

    /**
     * The Y normalposition of the last down or move event.
     */
    private float mLastDownOrMoveEventY;

    /**
     * Determines speed during touch scrolling.
     */
    private VelocityTracker mVelocityTracker;

    /**
     * @see ViewConfiguration#getScaledTouchSlop()
     */
    private int mTouchSlop;

    /**
     * @see ViewConfiguration#getScaledMinimumFlingVelocity()
     */
    private int mMinimumFlingVelocity;

    /**
     * @see ViewConfiguration#getScaledMaximumFlingVelocity()
     */
    private int mMaximumFlingVelocity;

    /**
     * Flag whether the selector should wrap around.
     */
    private boolean mWrapSelectorWheel;

    /**
     * The back ground color used to optimize scroller fading.
     */
    private final int mSolidColor;

    /**
     * Flag whether this widget has a selector wheel.
     */
    private final boolean mHasSelectorWheel;

    /**
     * Divider for showing item to be selected while scrolling
     */
    private final Drawable mSelectionDivider;

    /**
     * The bottom of the selection divider.
     */
    private final int mSelectionDividerHeight;

    /**
     * The current scroll state of the number sportHour.
     */
    private int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;

    /**
     * Flag whether to ignore move events - we ignore such when we show in IME
     * to prevent the content from scrolling.
     */
    private boolean mIgonreMoveEvents;

    /**
     * Flag whether to show soft input on tap.
     */
    private boolean mShowSoftInputOnTap;

    /**
     * The top of the top selection divider.
     */
    private int mTopSelectionDividerTop;

    /**
     * The bottom of the bottom selection divider.
     */
    private int mBottomSelectionDividerBottom;

    /**
     * The virtual serverId of the last hovered child.
     */
    private int mLastHoveredChildVirtualViewId;

    /**
     * Whether the increment virtual button is pressed.
     */
    private boolean mIncrementVirtualButtonPressed;

    /**
     * Whether the decrement virtual button is pressed.
     */
    private boolean mDecrementVirtualButtonPressed;

    /**
     * Provider to report to clients the semantic structure of this widget.
     */
    private SupportAccessibilityNodeProvider mAccessibilityNodeProvider;

    /**
     * Helper class for managing pressed state of the virtual buttons.
     */
    private final PressedStateHelper mPressedStateHelper;

    /**
     * The keycode of the last handled DPAD down event.
     */
    private int mLastHandledDownDpadKeyCode = -1;

    /**
     * The description of the current value.
     */
    private String label = "";

    /**
     * Interface to listen for changes of the current value.
     */
    public interface OnValueChangeListener {

        void onValueChange(NumberPicker picker, int oldVal, int newVal,
                           EditText editText);
    }

    /**
     * Interface to listen for the sportHour scroll state.
     */
    public interface OnScrollListener {

        /**
         * The view is not scrolling.
         */
        int SCROLL_STATE_IDLE = 0;

        /**
         * The user is scrolling using touch, and his finger is still on the
         * screen.
         */
        int SCROLL_STATE_TOUCH_SCROLL = 1;

        /**
         * The user had previously been scrolling using touch and performed a
         * fling.
         */
        int SCROLL_STATE_FLING = 2;
        void onScrollStateChange(NumberPicker view, int scrollState);
    }

    /**
     * Interface used to format current value into a string for presentation.
     */
    public interface Formatter {

        String format(int value);
    }

    public NumberPicker(Context context) {
        this(context, null);
    }

    public NumberPicker(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.numberPickerStyle);
    }

    public NumberPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);

        // process style attributes
        TypedArray attributesArray = context.obtainStyledAttributes(attrs,
                R.styleable.NumberPicker, defStyle, 0);
        final int layoutResId = attributesArray.getResourceId(
                R.styleable.NumberPicker_internalLayout,
                DEFAULT_LAYOUT_RESOURCE_ID);

        mHasSelectorWheel = (layoutResId != DEFAULT_LAYOUT_RESOURCE_ID);

        mSolidColor = attributesArray.getColor(
                R.styleable.NumberPicker_solidColor, 0);

        mSelectionDivider = attributesArray
                .getDrawable(R.styleable.NumberPicker_selectionDivider);

        final int defSelectionDividerHeight = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                UNSCALED_DEFAULT_SELECTION_DIVIDER_HEIGHT, getResources()
                        .getDisplayMetrics());
        mSelectionDividerHeight = attributesArray.getDimensionPixelSize(
                R.styleable.NumberPicker_selectionDividerHeight,
                defSelectionDividerHeight);

        final int defSelectionDividerDistance = (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        UNSCALED_DEFAULT_SELECTION_DIVIDERS_DISTANCE,
                        getResources().getDisplayMetrics());
        mSelectionDividersDistance = attributesArray.getDimensionPixelSize(
                R.styleable.NumberPicker_selectionDividersDistance,
                defSelectionDividerDistance);

        mMinHeight = attributesArray.getDimensionPixelSize(
                R.styleable.NumberPicker_internalMinHeight, SIZE_UNSPECIFIED);

        mMaxHeight = attributesArray.getDimensionPixelSize(
                R.styleable.NumberPicker_internalMaxHeight, SIZE_UNSPECIFIED);
        if (mMinHeight != SIZE_UNSPECIFIED && mMaxHeight != SIZE_UNSPECIFIED
                && mMinHeight > mMaxHeight) {
            throw new IllegalArgumentException("minHeight > maxHeight");
        }

        mMinWidth = attributesArray.getDimensionPixelSize(
                R.styleable.NumberPicker_internalMinWidth, SIZE_UNSPECIFIED);

        mMaxWidth = attributesArray.getDimensionPixelSize(
                R.styleable.NumberPicker_internalMaxWidth, SIZE_UNSPECIFIED);
        if (mMinWidth != SIZE_UNSPECIFIED && mMaxWidth != SIZE_UNSPECIFIED
                && mMinWidth > mMaxWidth) {
            throw new IllegalArgumentException("minWidth > maxWidth");
        }

        mComputeMaxWidth = (mMaxWidth == SIZE_UNSPECIFIED);

        mVirtualButtonPressedDrawable = attributesArray
                .getDrawable(R.styleable.NumberPicker_virtualButtonPressedDrawable);

        attributesArray.recycle();

        mPressedStateHelper = new PressedStateHelper();

        // By default Linearlayout that we extend is not drawn. This is
        // its drawCubic() method is not called but dispatchDraw() is called
        // directly (see ViewGroup.drawChild()). However, this class uses
        // the fading edge effect implemented by View and we need our
        // drawCubic() method to be called. Therefore, we declare we will drawCubic.
        setWillNotDraw(!mHasSelectorWheel);

        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(layoutResId, this, true);

        OnClickListener onClickListener = new OnClickListener() {
            public void onClick(View v) {
                hideSoftInput();
                mInputText.clearFocus();
                if (v.getId() == R.id.np__increment) {
                    changeValueByOne(true);
                } else {
                    changeValueByOne(false);
                }
            }
        };

        OnLongClickListener onLongClickListener = new OnLongClickListener() {
            public boolean onLongClick(View v) {
                hideSoftInput();
                mInputText.clearFocus();
                if (v.getId() == R.id.np__increment) {
                    postChangeCurrentByOneFromLongPress(true, 0);
                } else {
                    postChangeCurrentByOneFromLongPress(false, 0);
                }
                return true;
            }
        };

        // increment button
        if (!mHasSelectorWheel) {
            mIncrementButton = (ImageButton) findViewById(R.id.np__increment);
            mIncrementButton.setOnClickListener(onClickListener);
            mIncrementButton.setOnLongClickListener(onLongClickListener);
        } else {
            mIncrementButton = null;
        }

        // decrement button
        if (!mHasSelectorWheel) {
            mDecrementButton = (ImageButton) findViewById(R.id.np__decrement);
            mDecrementButton.setOnClickListener(onClickListener);
            mDecrementButton.setOnLongClickListener(onLongClickListener);
        } else {
            mDecrementButton = null;
        }

        // input text
        mInputText = (EditText) findViewById(R.id.np__numberpicker_input);
        mInputText.setFocusable(false);
        mInputText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        mInputText.setImeOptions(EditorInfo.IME_ACTION_DONE);

        // initialize constants
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity()
                / SELECTOR_MAX_FLING_VELOCITY_ADJUSTMENT;
        mTextSize = (int) mInputText.getTextSize() + 1;

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(mTextSize);

        paint.setColor(color);
        mSelectorWheelPaint = paint;
        mFlingScroller = new Scroller(getContext(), null, true);
        mAdjustScroller = new Scroller(getContext(),
                new DecelerateInterpolator(2.5f));

        updateInputTextView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (getImportantForAccessibility() == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
                setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        if (!mHasSelectorWheel) {
            super.onLayout(changed, left, top, right, bottom);
            return;
        }
        final int msrdWdth = getMeasuredWidth();
        final int msrdHght = getMeasuredHeight();

        // Input text centered horizontally.
        final int inptTxtMsrdWdth = mInputText.getMeasuredWidth();
        final int inptTxtMsrdHght = mInputText.getMeasuredHeight();
        final int inptTxtLeft = (msrdWdth - inptTxtMsrdWdth) / 2;
        final int inptTxtTop = (msrdHght - inptTxtMsrdHght) / 2;
        final int inptTxtRight = inptTxtLeft + inptTxtMsrdWdth;
        final int inptTxtBottom = inptTxtTop + inptTxtMsrdHght;
        mInputText.layout(inptTxtLeft, inptTxtTop, inptTxtRight, inptTxtBottom);
        if(getBackgroundID() != 0){
            mInputText.setBackground(getResources().getDrawable(getBackgroundID()));
        }


        if (changed) {
            initializeSelectorWheel();
            initializeFadingEdges();
            mTopSelectionDividerTop = (getHeight() - mSelectionDividersDistance)
                    / 2 - mSelectionDividerHeight;
            mBottomSelectionDividerBottom = mTopSelectionDividerTop + 2
                    * mSelectionDividerHeight + mSelectionDividersDistance;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!mHasSelectorWheel) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        // Try greedily to fit the max width and bottom.
        final int newWidthMeasureSpec = makeMeasureSpec(widthMeasureSpec,
                mMaxWidth);
        final int newHeightMeasureSpec = makeMeasureSpec(heightMeasureSpec,
                mMaxHeight);
        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec);
        // Flag if we are measured with width or bottom less than the respective
        // min.
        final int widthSize = resolveSizeAndStateRespectingMinSize(mMinWidth,
                getMeasuredWidth(), widthMeasureSpec);
        final int heightSize = resolveSizeAndStateRespectingMinSize(mMinHeight,
                getMeasuredHeight(), heightMeasureSpec);
        setMeasuredDimension(widthSize, heightSize);
    }

    private boolean moveToFinalScrollerPosition(Scroller scroller) {
        scroller.forceFinished(true);
        int amountToScroll = scroller.getFinalY() - scroller.getCurrY();
        int futureScrollOffset = (mCurrentScrollOffset + amountToScroll)
                % mSelectorElementHeight;
        int overshootAdjustment = mInitialScrollOffset - futureScrollOffset;
        if (overshootAdjustment != 0) {
            if (Math.abs(overshootAdjustment) > mSelectorElementHeight / 2) {
                if (overshootAdjustment > 0) {
                    overshootAdjustment -= mSelectorElementHeight;
                } else {
                    overshootAdjustment += mSelectorElementHeight;
                }
            }
            amountToScroll += overshootAdjustment;
            scrollBy(0, amountToScroll);
            return true;
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!mHasSelectorWheel || !isEnabled()) {
            return false;
        }
        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                removeAllCallbacks();
                // mInputText.setVisibility(View.INVISIBLE);
                mLastDownOrMoveEventY = mLastDownEventY = event.getY();
                mLastDownEventTime = event.getEventTime();
                mIgonreMoveEvents = false;
                mShowSoftInputOnTap = false;
                // Handle pressed state before any state change.
                if (mLastDownEventY < mTopSelectionDividerTop) {
                    if (mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                        mPressedStateHelper
                                .buttonPressDelayed(PressedStateHelper.BUTTON_DECREMENT);
                    }
                } else if (mLastDownEventY > mBottomSelectionDividerBottom) {
                    if (mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                        mPressedStateHelper
                                .buttonPressDelayed(PressedStateHelper.BUTTON_INCREMENT);
                    }
                }
                getParent().requestDisallowInterceptTouchEvent(true);
                if (!mFlingScroller.isFinished()) {
                    mFlingScroller.forceFinished(true);
                    mAdjustScroller.forceFinished(true);
                    onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
                } else if (!mAdjustScroller.isFinished()) {
                    mFlingScroller.forceFinished(true);
                    mAdjustScroller.forceFinished(true);
                } else if (mLastDownEventY < mTopSelectionDividerTop) {
                    hideSoftInput();
                    postChangeCurrentByOneFromLongPress(false,
                            ViewConfiguration.getLongPressTimeout());
                } else if (mLastDownEventY > mBottomSelectionDividerBottom) {
                    hideSoftInput();
                    postChangeCurrentByOneFromLongPress(true,
                            ViewConfiguration.getLongPressTimeout());
                } else {
                    mShowSoftInputOnTap = true;
                    postBeginSoftInputOnLongPressCommand();
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || !mHasSelectorWheel) {
            return false;
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                if (mIgonreMoveEvents) {
                    break;
                }
                float currentMoveY = event.getY();
                if (mScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    int deltaDownY = (int) Math.abs(currentMoveY - mLastDownEventY);
                    if (deltaDownY > mTouchSlop) {
                        removeAllCallbacks();
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
                    }
                } else {
                    int deltaMoveY = (int) ((currentMoveY - mLastDownOrMoveEventY));
                    scrollBy(0, deltaMoveY);
                    invalidate();
                }
                mLastDownOrMoveEventY = currentMoveY;
            }
            break;
            case MotionEvent.ACTION_UP: {
                removeBeginSoftInputCommand();
                removeChangeCurrentByOneFromLongPress();
                mPressedStateHelper.cancel();
                VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                int initialVelocity = (int) velocityTracker.getYVelocity();
                if (Math.abs(initialVelocity) > mMinimumFlingVelocity) {
                    fling(initialVelocity);
                    onScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
                } else {
                    int eventY = (int) event.getY();
                    int deltaMoveY = (int) Math.abs(eventY - mLastDownEventY);
                    @SuppressWarnings("unused")
                    long deltaTime = event.getEventTime() - mLastDownEventTime;
                    @SuppressWarnings("unused")
                    long tapTimeout = ViewConfiguration.getTapTimeout();
                    if (deltaMoveY <= mTouchSlop) { // && deltaTime <
                        // ViewConfiguration.getTapTimeout())
                        // {
                        if (mShowSoftInputOnTap) {
                            mShowSoftInputOnTap = false;
                            showSoftInput();
                        } else {
                            int selectorIndexOffset = (eventY / mSelectorElementHeight)
                                    - SELECTOR_MIDDLE_ITEM_INDEX;
                            if (selectorIndexOffset > 0) {
                                changeValueByOne(true);
                                mPressedStateHelper
                                        .buttonTapped(PressedStateHelper.BUTTON_INCREMENT);
                            } else if (selectorIndexOffset < 0) {
                                changeValueByOne(false);
                                mPressedStateHelper
                                        .buttonTapped(PressedStateHelper.BUTTON_DECREMENT);
                            }
                        }
                    } else {
                        ensureScrollWheelAdjusted();
                    }
                    onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
            break;
        }
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                removeAllCallbacks();
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                removeAllCallbacks();
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_UP:
                if (!mHasSelectorWheel) {
                    break;
                }
                switch (event.getAction()) {
                    case KeyEvent.ACTION_DOWN:
                        if (mWrapSelectorWheel
                                || (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) ? getValue() < getMaxValue()
                                : getValue() > getMinValue()) {
                            requestFocus();
                            mLastHandledDownDpadKeyCode = keyCode;
                            removeAllCallbacks();
                            if (mFlingScroller.isFinished()) {
                                changeValueByOne(keyCode == KeyEvent.KEYCODE_DPAD_DOWN);
                            }
                            return true;
                        }
                        break;
                    case KeyEvent.ACTION_UP:
                        if (mLastHandledDownDpadKeyCode == keyCode) {
                            mLastHandledDownDpadKeyCode = -1;
                            return true;
                        }
                        break;
                }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent event) {
        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                removeAllCallbacks();
                break;
        }
        return super.dispatchTrackballEvent(event);
    }

    @Override
    protected boolean dispatchHoverEvent(MotionEvent event) {
        if (!mHasSelectorWheel) {
            return super.dispatchHoverEvent(event);
        }

        if (((AccessibilityManager) getContext().getSystemService(
                Context.ACCESSIBILITY_SERVICE)).isEnabled()) {
            final int eventY = (int) event.getY();
            final int hoveredVirtualViewId;
            if (eventY < mTopSelectionDividerTop) {
                hoveredVirtualViewId = AccessibilityNodeProviderImpl.VIRTUAL_VIEW_ID_DECREMENT;
            } else if (eventY > mBottomSelectionDividerBottom) {
                hoveredVirtualViewId = AccessibilityNodeProviderImpl.VIRTUAL_VIEW_ID_INCREMENT;
            } else {
                hoveredVirtualViewId = AccessibilityNodeProviderImpl.VIRTUAL_VIEW_ID_INPUT;
            }
            final int action = event.getAction() & MotionEvent.ACTION_MASK;
            SupportAccessibilityNodeProvider provider = getSupportAccessibilityNodeProvider();

            switch (action) {
                case MotionEvent.ACTION_HOVER_ENTER: {
                    provider.sendAccessibilityEventForVirtualView(
                            hoveredVirtualViewId,
                            AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
                    mLastHoveredChildVirtualViewId = hoveredVirtualViewId;
                    provider.performAction(hoveredVirtualViewId,
                            AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
                }
                break;
                case MotionEvent.ACTION_HOVER_MOVE: {
                    if (mLastHoveredChildVirtualViewId != hoveredVirtualViewId
                            && mLastHoveredChildVirtualViewId != View.NO_ID) {
                        provider.sendAccessibilityEventForVirtualView(
                                mLastHoveredChildVirtualViewId,
                                AccessibilityEvent.TYPE_VIEW_HOVER_EXIT);
                        provider.sendAccessibilityEventForVirtualView(
                                hoveredVirtualViewId,
                                AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
                        mLastHoveredChildVirtualViewId = hoveredVirtualViewId;
                        provider.performAction(hoveredVirtualViewId,
                                AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS,
                                null);
                    }
                }
                break;
                case MotionEvent.ACTION_HOVER_EXIT: {
                    provider.sendAccessibilityEventForVirtualView(
                            hoveredVirtualViewId,
                            AccessibilityEvent.TYPE_VIEW_HOVER_EXIT);
                    mLastHoveredChildVirtualViewId = View.NO_ID;
                }
                break;
            }
        }
        return false;
    }

    @Override
    public void computeScroll() {
        Scroller scroller = mFlingScroller;
        if (scroller.isFinished()) {
            scroller = mAdjustScroller;
            if (scroller.isFinished()) {
                return;
            }
        }
        scroller.computeScrollOffset();
        int currentScrollerY = scroller.getCurrY();
        if (mPreviousScrollerY == 0) {
            mPreviousScrollerY = scroller.getStartY();
        }
        scrollBy(0, currentScrollerY - mPreviousScrollerY);
        mPreviousScrollerY = currentScrollerY;
        if (scroller.isFinished()) {
            onScrollerFinished(scroller);
        } else {
            invalidate();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!mHasSelectorWheel) {
            mIncrementButton.setEnabled(enabled);
        }
        if (!mHasSelectorWheel) {
            mDecrementButton.setEnabled(enabled);
        }
        mInputText.setEnabled(enabled);
    }

    @Override
    public void scrollBy(int x, int y) {
        int[] selectorIndices = mSelectorIndices;
        if (!mWrapSelectorWheel && y > 0
                && selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX] <= mMinValue) {
            mCurrentScrollOffset = mInitialScrollOffset;
            return;
        }
        if (!mWrapSelectorWheel && y < 0
                && selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX] >= mMaxValue) {
            mCurrentScrollOffset = mInitialScrollOffset;
            return;
        }
        mCurrentScrollOffset += y;
        while (mCurrentScrollOffset - mInitialScrollOffset > mSelectorTextGapHeight) {
            mCurrentScrollOffset -= mSelectorElementHeight;
            decrementSelectorIndices(selectorIndices);
            setValueInternal(selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX], true);
            if (!mWrapSelectorWheel
                    && selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX] <= mMinValue) {
                mCurrentScrollOffset = mInitialScrollOffset;
            }
        }
        while (mCurrentScrollOffset - mInitialScrollOffset < -mSelectorTextGapHeight) {
            mCurrentScrollOffset += mSelectorElementHeight;
            incrementSelectorIndices(selectorIndices);
            setValueInternal(selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX], true);
            if (!mWrapSelectorWheel
                    && selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX] >= mMaxValue) {
                mCurrentScrollOffset = mInitialScrollOffset;
            }
        }
    }

    @Override
    public int getSolidColor() {
        return mSolidColor;
    }

    /**
     * Sets the lcListener to be notified on change of the current value.
     *
     * @param onValueChangedListener
     *            The lcListener.
     */
    public void setOnValueChangedListener(
            OnValueChangeListener onValueChangedListener) {
        mOnValueChangeListener = onValueChangedListener;
    }

    /**
     * Set lcListener to be notified for scroll state changes.
     *
     * @param onScrollListener
     *            The lcListener.
     */
    public void setOnScrollListener(OnScrollListener onScrollListener) {
        mOnScrollListener = onScrollListener;
    }

    /**
     * Set the formatter to be used for formatting the current value.
     * <planData>
     * Note: If you have provided alternative values for the values this
     * formatter is never invoked.
     * </planData>
     *
     * @param formatter
     *            The formatter object. If formatter is <code>null</code>,
     *            {@link String#valueOf(int)} will be used.
     * @see #setDisplayedValues(String[])
     */
    public void setFormatter(Formatter formatter) {
        if (formatter == mFormatter) {
            return;
        }
        mFormatter = formatter;
        initializeSelectorWheelIndices();
        updateInputTextView();
    }

    public void setCurValue(int value) {
        setValueInternal(value, false);
    }

    /**
     * 显示键盘数输入的文本信息
     */
    private void showSoftInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            if (mHasSelectorWheel) {
                mInputText.setVisibility(View.VISIBLE);
            }
            mInputText.requestFocus();
            inputMethodManager.showSoftInput(mInputText, 0);
        }
    }

    /**
     * 如果有输入源就隐藏键盘
     */
    private void hideSoftInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null
                && inputMethodManager.isActive(mInputText)) {
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            if (mHasSelectorWheel) {
                // mInputText.setVisibility(View.INVISIBLE);
                mInputText.clearFocus();
            }
        }
    }

    /**
     * 如果没有规定大小就计算出最大的宽度.
     */
    private void tryComputeMaxWidth() {
        if (!mComputeMaxWidth) {
            return;
        }
        int maxTextWidth = 0;
        if (mDisplayedValues == null) {
            float maxDigitWidth = 0;
            for (int i = 0; i <= 9; i++) {
                final float digitWidth = mSelectorWheelPaint
                        .measureText(formatNumberWithLocale(i));
                if (digitWidth > maxDigitWidth) {
                    maxDigitWidth = digitWidth;
                }
            }
            int numberOfDigits = 0;
            int current = mMaxValue;
            while (current > 0) {
                numberOfDigits++;
                current = current / 10;
            }
            maxTextWidth = (int) (numberOfDigits * maxDigitWidth);
        } else {
            final int valueCount = mDisplayedValues.length;
            for (int i = 0; i < valueCount; i++) {
                final float textWidth = mSelectorWheelPaint
                        .measureText(mDisplayedValues[i]);
                if (textWidth > maxTextWidth) {
                    maxTextWidth = (int) textWidth;
                }
            }
        }
        maxTextWidth += mInputText.getPaddingLeft()
                + mInputText.getPaddingRight();
        if (mMaxWidth != maxTextWidth) {
            if (maxTextWidth > mMinWidth) {
                mMaxWidth = maxTextWidth;
            } else {
                mMaxWidth = mMinWidth;
            }
            invalidate();
        }
    }

    /**
     * 判断选择器是否到底端或者是top
     *
     * @return
     */
    public boolean getWrapSelectorWheel() {
        return mWrapSelectorWheel;
    }

    /**
     * 判断是否需要换行
     *
     * @param wrapSelectorWheel
     */
    public void setWrapSelectorWheel(boolean wrapSelectorWheel) {
        final boolean wrappingAllowed = (mMaxValue - mMinValue) >= mSelectorIndices.length;
        if ((!wrapSelectorWheel || wrappingAllowed)
                && wrapSelectorWheel != mWrapSelectorWheel) {
            mWrapSelectorWheel = wrapSelectorWheel;
        }
    }

    /**
     * 设置增加或者减少时的速度
     *
     * @param intervalMillis
     */
    public void setOnLongPressUpdateInterval(long intervalMillis) {
        mLongPressUpdateInterval = intervalMillis;
    }

    /**
     * 获取选择器中的值
     *
     * @return
     */
    public int getValue() {

        return mValue;
    }

    /**
     * 获取最小值
     *
     * @return
     */
    public int getMinValue() {
        return mMinValue;
    }

    /**
     * 设置最小值
     *
     * @param minValue
     */
    public void setMinValue(int minValue) {
        if (mMinValue == minValue) {
            return;
        }
        if (minValue < 0) {
            throw new IllegalArgumentException("minValue must be >= 0");
        }
        mMinValue = minValue;
        if (mMinValue > mValue) {
            mValue = mMinValue;
        }
        boolean wrapSelectorWheel = mMaxValue - mMinValue > mSelectorIndices.length;
        setWrapSelectorWheel(wrapSelectorWheel);
        initializeSelectorWheelIndices();
        updateInputTextView();
        tryComputeMaxWidth();
        invalidate();
    }

    /**
     * 返回最大值
     *
     * @return
     */
    public int getMaxValue() {
        return mMaxValue;
    }

    /**
     * 设置最大值
     *
     * @param maxValue
     */
    public void setMaxValue(int maxValue) {
        if (mMaxValue == maxValue) {
            return;
        }
        if (maxValue < 0) {
            throw new IllegalArgumentException("maxValue must be >= 0");
        }
        mMaxValue = maxValue;
        if (mMaxValue < mValue) {
            mValue = mMaxValue;
        }
        boolean wrapSelectorWheel = mMaxValue - mMinValue > mSelectorIndices.length;
        setWrapSelectorWheel(wrapSelectorWheel);
        initializeSelectorWheelIndices();
        updateInputTextView();
        tryComputeMaxWidth();
        invalidate();
    }

    /**
     * 获取将要显示的值.
     *
     * @return The displayed values.
     */
    public String[] getDisplayedValues() {
        return mDisplayedValues;
    }

    /**
     * 设置要显示的值
     *
     * @param displayedValues
     */
    public void setDisplayedValues(String[] displayedValues) {
        if (mDisplayedValues == displayedValues) {
            return;
        }
        mDisplayedValues = displayedValues;
        if (mDisplayedValues != null) {
            // 允许文本输入也不是单一的数字
            mInputText.setRawInputType(InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        } else {
            mInputText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        }
        updateInputTextView();
        initializeSelectorWheelIndices();
        tryComputeMaxWidth();
    }

    @Override
    protected float getTopFadingEdgeStrength() {
        return TOP_AND_BOTTOM_FADING_EDGE_STRENGTH;
    }

    @Override
    protected float getBottomFadingEdgeStrength() {
        return TOP_AND_BOTTOM_FADING_EDGE_STRENGTH;
    }

    @Override
    protected void onDetachedFromWindow() {
        removeAllCallbacks();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!mHasSelectorWheel) {
            super.onDraw(canvas);
            return;
        }
        mInputText.setVisibility(View.GONE);
        float x = (getRight() - getLeft()) / 2;
        float y = mCurrentScrollOffset;

        // 绘制虚拟按钮按下的状态(如果需要的话)
        if (mVirtualButtonPressedDrawable != null
                && mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            if (mDecrementVirtualButtonPressed) {
                // mVirtualButtonPressedDrawable.setState(PRESSED_STATE_SET);
                mVirtualButtonPressedDrawable
                        .setState(PRESSED_ENABLED_STATE_SET);
                mVirtualButtonPressedDrawable.setBounds(0, 0, getRight(),
                        mTopSelectionDividerTop);
                mVirtualButtonPressedDrawable.draw(canvas);
            }
            if (mIncrementVirtualButtonPressed) {
                // mVirtualButtonPressedDrawable.setState(PRESSED_STATE_SET);
                mVirtualButtonPressedDrawable
                        .setState(PRESSED_ENABLED_STATE_SET);
                mVirtualButtonPressedDrawable.setBounds(0,
                        mBottomSelectionDividerBottom, getRight(), getBottom());
                mVirtualButtonPressedDrawable.draw(canvas);
            }
        }

        // 绘制选择轮
        int[] selectorIndices = mSelectorIndices;
        for (int i = 0; i < selectorIndices.length; i++) {
            int selectorIndex = selectorIndices[i];
            String scrollSelectorValue = mSelectorIndexToStringCache
                    .get(selectorIndex);

            // 不绘制的中间item，如果输入是可见的，因为在输入仅示出如果车轮是静态的，
            // 它覆盖了中间item。否则，如果用户开始编辑通过输入法的文本，他可能会看到混合用新的旧的值暗淡的版本。
            if (i != SELECTOR_MIDDLE_ITEM_INDEX
                    || mInputText.getVisibility() != VISIBLE) {
                canvas.drawText(scrollSelectorValue, x, y, mSelectorWheelPaint);
            }
            y += mSelectorElementHeight;
        }
        // 绘制选择分屏器
        if (mSelectionDivider != null) {
            // drawCubic the top divider
            int topOfTopDivider = mTopSelectionDividerTop;
            int bottomOfTopDivider = topOfTopDivider + mSelectionDividerHeight;
            mSelectionDivider.setBounds(0, topOfTopDivider, getRight(),
                    bottomOfTopDivider);
            mSelectionDivider.draw(canvas);

            // drawCubic the bottom divider
            int bottomOfBottomDivider = mBottomSelectionDividerBottom;
            int topOfBottomDivider = bottomOfBottomDivider
                    - mSelectionDividerHeight;
            mSelectionDivider.setBounds(0, topOfBottomDivider, getRight(),
                    bottomOfBottomDivider);
            mSelectionDivider.draw(canvas);
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(NumberPicker.class.getName());
        event.setScrollable(true);
        event.setScrollY((mMinValue + mValue) * mSelectorElementHeight);
        event.setMaxScrollY((mMaxValue - mMinValue) * mSelectorElementHeight);
    }

    @Override
    public AccessibilityNodeProvider getAccessibilityNodeProvider() {
        if (!mHasSelectorWheel) {
            return super.getAccessibilityNodeProvider();
        }
        if (mAccessibilityNodeProvider == null) {
            mAccessibilityNodeProvider = new SupportAccessibilityNodeProvider();
        }
        return mAccessibilityNodeProvider.mProvider;
    }

    /**
     * 制造一个规范的措施，尽可能的使用最大值
     *
     * @param measureSpec
     * @param maxSize
     * @return
     */
    private int makeMeasureSpec(int measureSpec, int maxSize) {
        if (maxSize == SIZE_UNSPECIFIED) {
            return measureSpec;
        }
        final int size = MeasureSpec.getSize(measureSpec);
        final int mode = MeasureSpec.getMode(measureSpec);
        switch (mode) {
            case MeasureSpec.EXACTLY:
                return measureSpec;
            case MeasureSpec.AT_MOST:
                return MeasureSpec.makeMeasureSpec(Math.min(size, maxSize),
                        MeasureSpec.EXACTLY);
            case MeasureSpec.UNSPECIFIED:
                return MeasureSpec.makeMeasureSpec(maxSize, MeasureSpec.EXACTLY);
            default:
                throw new IllegalArgumentException("Unknown measure mode: " + mode);
        }
    }

    /**
     * 实用调和所需的规模和状态，所施加的限制 由MeasureSpec。试图尊重最小的尺寸，除非一个不同的大小 受局限造成的。
     *
     * @参数MINSIZE 最小所需的尺寸。 @参数measuredSize 当前测量的大小。
     * @参数measureSpec 当前测量规范。 返回：该解决规模和状态。
     */
    private int resolveSizeAndStateRespectingMinSize(int minSize,
                                                     int measuredSize, int measureSpec) {
        if (minSize != SIZE_UNSPECIFIED) {
            final int desiredWidth = Math.max(minSize, measuredSize);
            return resolveSizeAndState(desiredWidth, measureSpec, 0);
        } else {
            if(measuredSize == 0){
                measuredSize = 600;
            }
            return measuredSize;
        }
    }

    /**
     * 实用调和所需的规模和状态，所施加的限制 由MeasureSpec。将所需的大小，除非一个不同的大小 受局限造成的。返回值是一个复合的整数， 用在
     * {@link#MEASURED_SIZE_MASK}位解决大小和 任选的位{@link#MEASURED_STATE_TOO_SMALL}
     * 如果所得到的集 尺寸比图希望成为的尺寸。
     *
     * @参数规格 有多大的意见希望成为
     * @参数measureSpec 父的制约 所定义@return尺寸信息的位掩码 {@link#MEASURED_SIZE_MASK}和
     *                {@link#MEASURED_STATE_TOO_SMALL} 。
     */
    public static int resolveSizeAndState(int size, int measureSpec,
                                          int childMeasuredState) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = size;
                break;
            case MeasureSpec.AT_MOST:
                if (specSize < size) {
                    result = specSize | MEASURED_STATE_TOO_SMALL;
                } else {
                    result = size;
                }
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result | (childMeasuredState & MEASURED_STATE_MASK);
    }

    /**
     * 重设选择指数和清除这些指标的缓存字符串表示形式。
     */
    private void initializeSelectorWheelIndices() {
        mSelectorIndexToStringCache.clear();
        int[] selectorIndices = mSelectorIndices;
        int current = getValue();
        for (int i = 0; i < mSelectorIndices.length; i++) {
            int selectorIndex = current + (i - SELECTOR_MIDDLE_ITEM_INDEX);
            if (mWrapSelectorWheel) {
                selectorIndex = getWrappedSelectorIndex(selectorIndex);
            }
            selectorIndices[i] = selectorIndex;
            ensureCachedScrollSelectorValue(selectorIndices[i]);
        }
    }

    /**
     * 设置当前的number值
     *
     * @param current
     * @param notifyChange
     *            :是否需要通知
     */
    private void setValueInternal(int current, boolean notifyChange) {
        if (mValue == current) {
            return;
        }
        // Wrap around the values if we go past the start or end
        if (mWrapSelectorWheel) {
            current = getWrappedSelectorIndex(current);
        } else {
            current = Math.max(current, mMinValue);
            current = Math.min(current, mMaxValue);
        }
        int previous = mValue;
        mValue = current;
        updateInputTextView();
        if (notifyChange) {
            notifyChange(previous, current);
        }
        initializeSelectorWheelIndices();
        invalidate();
    }

    /**
     * 更改当前值一个是增量或减量的基础上 通行证的说法。减少当前值。
     *
     * @参数增量 真递增，虚假递减。
     */
    private void changeValueByOne(boolean increment) {
        if (mHasSelectorWheel) {
            // mInputText.setVisibility(View.INVISIBLE);
            mInputText.clearFocus();
            if (!moveToFinalScrollerPosition(mFlingScroller)) {
                moveToFinalScrollerPosition(mAdjustScroller);
            }
            mPreviousScrollerY = 0;
            if (increment) {
                mFlingScroller.startScroll(0, 0, 0, -mSelectorElementHeight,
                        SNAP_SCROLL_DURATION);
            } else {
                mFlingScroller.startScroll(0, 0, 0, mSelectorElementHeight,
                        SNAP_SCROLL_DURATION);
            }
            invalidate();
        } else {
            if (increment) {
                setValueInternal(mValue + 1, true);
            } else {
                setValueInternal(mValue - 1, true);
            }
        }
    }

    private void initializeSelectorWheel() {
        initializeSelectorWheelIndices();
        int[] selectorIndices = mSelectorIndices;
        int totalTextHeight = selectorIndices.length * mTextSize;
        float totalTextGapHeight = (getBottom() - getTop()) - totalTextHeight;
        float textGapCount = selectorIndices.length;
        mSelectorTextGapHeight = (int) (totalTextGapHeight / textGapCount + 0.5f);
        mSelectorElementHeight = mTextSize + mSelectorTextGapHeight;
        // 确保中间项被定位在相同的文本
        // mInputText
        int editTextTextPosition = mInputText.getBaseline()
                + mInputText.getTop();
        mInitialScrollOffset = editTextTextPosition
                - (mSelectorElementHeight * SELECTOR_MIDDLE_ITEM_INDEX);
        mCurrentScrollOffset = mInitialScrollOffset;
        updateInputTextView();
    }

    private void initializeFadingEdges() {
        setVerticalFadingEdgeEnabled(true);
        setFadingEdgeLength((getBottom() - getTop() - mTextSize) / 2);
    }

    // 给定完成后调用的回调函数
    private void onScrollerFinished(Scroller scroller) {
        if (scroller == mFlingScroller) {
            if (!ensureScrollWheelAdjusted()) {
                updateInputTextView();
            }
            onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
        } else {
            if (mScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                updateInputTextView();
            }
        }
    }

    /**
     * Handles transition to a given: 处理过渡到一个给定的
     */
    private void onScrollStateChange(int scrollState) {
        if (mScrollState == scrollState) {
            return;
        }
        mScrollState = scrollState;
        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollStateChange(this, scrollState);
        }
    }

    /**
     * Flings the selector with the given <code>velocityY</code>.
     */
    private void fling(int velocityY) {
        mPreviousScrollerY = 0;

        if (velocityY > 0) {
            mFlingScroller
                    .fling(0, 0, 0, velocityY, 0, 0, 0, Integer.MAX_VALUE);
        } else {
            mFlingScroller.fling(0, Integer.MAX_VALUE, 0, velocityY, 0, 0, 0,
                    Integer.MAX_VALUE);
        }

        invalidate();
    }

    /**
     * @return The wrapped index <code>selectorIndex</code> value.
     */
    private int getWrappedSelectorIndex(int selectorIndex) {
        if (selectorIndex > mMaxValue) {
            return mMinValue + (selectorIndex - mMaxValue)
                    % (mMaxValue - mMinValue) - 1;
        } else if (selectorIndex < mMinValue) {
            return mMaxValue - (mMinValue - selectorIndex)
                    % (mMaxValue - mMinValue) + 1;
        }
        return selectorIndex;
    }

    /**
     * 递增的<码> selectorIndices</代码>的字符串表示形式 将被显示在选择器。
     */
    private void incrementSelectorIndices(int[] selectorIndices) {
        for (int i = 0; i < selectorIndices.length - 1; i++) {
            selectorIndices[i] = selectorIndices[i + 1];
        }
        int nextScrollSelectorIndex = selectorIndices[selectorIndices.length - 2] + 1;
        if (mWrapSelectorWheel && nextScrollSelectorIndex > mMaxValue) {
            nextScrollSelectorIndex = mMinValue;
        }
        selectorIndices[selectorIndices.length - 1] = nextScrollSelectorIndex;
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex);
    }

    /**
     * 递减<码> selectorIndices</代码>的字符串表示形式 将被显示在选择器。
     */
    private void decrementSelectorIndices(int[] selectorIndices) {
        for (int i = selectorIndices.length - 1; i > 0; i--) {
            selectorIndices[i] = selectorIndices[i - 1];
        }
        int nextScrollSelectorIndex = selectorIndices[1] - 1;
        if (mWrapSelectorWheel && nextScrollSelectorIndex < mMinValue) {
            nextScrollSelectorIndex = mMaxValue;
        }
        selectorIndices[0] = nextScrollSelectorIndex;
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex);
    }

    /**
     * 确保我们给定的<码>缓存的字符串表示形式 selectorIndex</ code>的，以避免相同的字符串的多个实例。
     */
    private void ensureCachedScrollSelectorValue(int selectorIndex) {
        SparseArray<String> cache = mSelectorIndexToStringCache;
        String scrollSelectorValue = cache.get(selectorIndex);
        if (scrollSelectorValue != null) {
            return;
        }
        if (selectorIndex < mMinValue || selectorIndex > mMaxValue) {
            scrollSelectorValue = "";
        } else {
            if (mDisplayedValues != null) {
                int displayedValueIndex = selectorIndex - mMinValue;
                scrollSelectorValue = mDisplayedValues[displayedValueIndex];
            } else {
                scrollSelectorValue = formatNumber(selectorIndex);
            }
        }
        cache.put(selectorIndex, scrollSelectorValue);
    }

    private String formatNumber(int value) {
        return (mFormatter != null) ? mFormatter.format(value)
                : formatNumberWithLocale(value);
    }

    @SuppressWarnings("unused")
    private void validateInputTextView(View v) {
        String str = String.valueOf(((TextView) v).getText());
        if (TextUtils.isEmpty(str)) {
            // Restore to the old value as we don't allow empty values
            updateInputTextView();
        } else {
            // Check the new value and ensure it's in range
            int current = getSelectedPos(str.toString());
            setValueInternal(current, true);
        }
    }

    /**
     * Updates the view of this NumberPicker. If displayValues were specified in
     * the string corresponding to the index specified by the current value will
     * be returned. Otherwise, the formatter specified in {@link #setFormatter}
     * will be used to format the number.
     *
     * @return Whether the text was updated.
     */
    private boolean updateInputTextView() {
        String text = (mDisplayedValues == null) ? formatNumber(mValue)
                : mDisplayedValues[mValue - mMinValue];
        if (!TextUtils.isEmpty(text)
                && !text.equals(mInputText.getText().toString())) {
            if (mDisplayedValues == null) {
                mInputText.setText(sTwoDigitFormatter.format(Integer
                        .valueOf(text)) + " " + label);
            } else {
                mInputText.setText(text + " " + label);
            }
            mInputText.setTextColor(color);
            mInputText.setTextSize(22);

            return true;
        }

        return false;
    }

    /**
     * Notifies the lcListener, if registered, of a change of the value of this
     * NumberPicker.
     */
    private void notifyChange(int previous, int current) {

        if (mOnValueChangeListener != null) {
            mOnValueChangeListener.onValueChange(this, previous, mValue,
                    mInputText);
        }
    }

    /**
     * Posts a command for changing the current value by threes.
     *
     * @param increment
     *            Whether to increment or decrement the value.
     */
    private void postChangeCurrentByOneFromLongPress(boolean increment,
                                                     long delayMillis) {
        if (mChangeCurrentByOneFromLongPressCommand == null) {
            mChangeCurrentByOneFromLongPressCommand = new ChangeCurrentByOneFromLongPressCommand();
        } else {
            removeCallbacks(mChangeCurrentByOneFromLongPressCommand);
        }
        mChangeCurrentByOneFromLongPressCommand.setStep(increment);
        postDelayed(mChangeCurrentByOneFromLongPressCommand, delayMillis);
    }

    /**
     * Removes the command for changing the current value by threes.
     */
    private void removeChangeCurrentByOneFromLongPress() {
        if (mChangeCurrentByOneFromLongPressCommand != null) {
            removeCallbacks(mChangeCurrentByOneFromLongPressCommand);
        }
    }

    /**
     * Posts a command for beginning an edit of the current value via IME on
     * long press.
     */
    private void postBeginSoftInputOnLongPressCommand() {
        if (mBeginSoftInputOnLongPressCommand == null) {
            mBeginSoftInputOnLongPressCommand = new BeginSoftInputOnLongPressCommand();
        } else {
            removeCallbacks(mBeginSoftInputOnLongPressCommand);
        }
        postDelayed(mBeginSoftInputOnLongPressCommand,
                ViewConfiguration.getLongPressTimeout());
    }

    /**
     * Removes the command for beginning an edit of the current value via IME.
     */
    private void removeBeginSoftInputCommand() {
        if (mBeginSoftInputOnLongPressCommand != null) {
            removeCallbacks(mBeginSoftInputOnLongPressCommand);
        }
    }

    /**
     * Removes all pending callback from the message queue.
     */
    private void removeAllCallbacks() {
        if (mChangeCurrentByOneFromLongPressCommand != null) {
            removeCallbacks(mChangeCurrentByOneFromLongPressCommand);
        }
        if (mSetSelectionCommand != null) {
            removeCallbacks(mSetSelectionCommand);
        }
        if (mBeginSoftInputOnLongPressCommand != null) {
            removeCallbacks(mBeginSoftInputOnLongPressCommand);
        }
        mPressedStateHelper.cancel();
    }

    /**
     * @return The selected index given its displayed <code>value</code>.
     */
    private int getSelectedPos(String value) {
        if (mDisplayedValues == null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
            }
        } else {
            for (int i = 0; i < mDisplayedValues.length; i++) {
                value = value.toLowerCase();
                if (value.toLowerCase().startsWith(mDisplayedValues[i])) {
                    return mMinValue + i;
                }
            }

            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {

            }
        }
        return mMinValue;
    }

    /**
     * Posts an {@link SetSelectionCommand} from the given <code>selectionStart
     * </code> to <code>selectionEnd</code>.
     */
    private void postSetSelectionCommand(int selectionStart, int selectionEnd) {
        if (mSetSelectionCommand == null) {
            mSetSelectionCommand = new SetSelectionCommand();
        } else {
            removeCallbacks(mSetSelectionCommand);
        }
        mSetSelectionCommand.mSelectionStart = selectionStart;
        mSetSelectionCommand.mSelectionEnd = selectionEnd;
        post(mSetSelectionCommand);
    }

    /**
     * The numbers accepted by the input text's {@link LayoutInflater.Filter}
     */
    private static final char[] DIGIT_CHARACTERS = new char[] {
            // Latin digits are the common case
            '0', '1', '2', '3', '4', '5', '6', '7', '8',
            '9',
            // Arabic-Indic
            '\u0660', '\u0661', '\u0662', '\u0663', '\u0664', '\u0665',
            '\u0666', '\u0667', '\u0668', '\u0669',
            // Extended Arabic-Indic
            '\u06f0', '\u06f1', '\u06f2', '\u06f3', '\u06f4', '\u06f5',
            '\u06f6', '\u06f7', '\u06f8', '\u06f9' };

    /**
     * Filter for accepting only valid indices or prefixes of the string
     * representation of valid indices.
     */
    class InputTextFilter extends NumberKeyListener {
        public int getInputType() {
            return InputType.TYPE_CLASS_TEXT;
        }

        @Override
        protected char[] getAcceptedChars() {
            return DIGIT_CHARACTERS;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            if (mDisplayedValues == null) {
                CharSequence filtered = super.filter(source, start, end, dest,
                        dstart, dend);
                if (filtered == null) {
                    filtered = source.subSequence(start, end);
                }

                String result = String.valueOf(dest.subSequence(0, dstart))
                        + filtered + dest.subSequence(dend, dest.length());

                if ("".equals(result)) {
                    return result;
                }
                int val = getSelectedPos(result);

				/*
				 * Ensure the user can't type in a value greater than the max
				 * allowed. We have to allow less than min as the user might
				 * want to doDelete some numbers and then type a new number.
				 */
                if (val > mMaxValue) {
                    return "";
                } else {
                    return filtered;
                }
            } else {
                CharSequence filtered = String.valueOf(source.subSequence(
                        start, end));
                if (TextUtils.isEmpty(filtered)) {
                    return "";
                }
                String result = String.valueOf(dest.subSequence(0, dstart))
                        + filtered + dest.subSequence(dend, dest.length());
                String str = String.valueOf(result).toLowerCase();
                for (String val : mDisplayedValues) {
                    String valLowerCase = val.toLowerCase();
                    if (valLowerCase.startsWith(str)) {
                        postSetSelectionCommand(result.length(), val.length());
                        return val.subSequence(dstart, val.length());
                    }
                }
                return "";
            }
        }
    }

    /**
     * Ensures that the scroll wheel is adjusted i.e. there is no offset and the
     * middle element is in the middle of the widget.
     *
     * @return Whether an adjustment has been made.
     */
    private boolean ensureScrollWheelAdjusted() {
        // adjust to the closest value
        int deltaY = mInitialScrollOffset - mCurrentScrollOffset;
        if (deltaY != 0) {
            mPreviousScrollerY = 0;
            if (Math.abs(deltaY) > mSelectorElementHeight / 2) {
                deltaY += (deltaY > 0) ? -mSelectorElementHeight
                        : mSelectorElementHeight;
            }
            mAdjustScroller.startScroll(0, 0, 0, deltaY,
                    SELECTOR_ADJUSTMENT_DURATION_MILLIS);
            invalidate();
            return true;
        }
        return false;
    }

    class PressedStateHelper implements Runnable {
        public static final int BUTTON_INCREMENT = 1;
        public static final int BUTTON_DECREMENT = 2;

        private final int MODE_PRESS = 1;
        private final int MODE_TAPPED = 2;

        private int mManagedButton;
        private int mMode;

        public void cancel() {
            mMode = 0;
            mManagedButton = 0;
            NumberPicker.this.removeCallbacks(this);
            if (mIncrementVirtualButtonPressed) {
                mIncrementVirtualButtonPressed = false;
                invalidate(0, mBottomSelectionDividerBottom, getRight(),
                        getBottom());
            }
            mDecrementVirtualButtonPressed = false;
            if (mDecrementVirtualButtonPressed) {
                invalidate(0, 0, getRight(), mTopSelectionDividerTop);
            }
        }

        public void buttonPressDelayed(int button) {
            cancel();
            mMode = MODE_PRESS;
            mManagedButton = button;
            NumberPicker.this.postDelayed(this,
                    ViewConfiguration.getTapTimeout());
        }

        public void buttonTapped(int button) {
            cancel();
            mMode = MODE_TAPPED;
            mManagedButton = button;
            NumberPicker.this.post(this);
        }

        @Override
        public void run() {
            switch (mMode) {
                case MODE_PRESS: {
                    switch (mManagedButton) {
                        case BUTTON_INCREMENT: {
                            mIncrementVirtualButtonPressed = true;
                            invalidate(0, mBottomSelectionDividerBottom, getRight(),
                                    getBottom());
                        }
                        break;
                        case BUTTON_DECREMENT: {
                            mDecrementVirtualButtonPressed = true;
                            invalidate(0, 0, getRight(), mTopSelectionDividerTop);
                        }
                    }
                }
                break;
                case MODE_TAPPED: {
                    switch (mManagedButton) {
                        case BUTTON_INCREMENT: {
                            if (!mIncrementVirtualButtonPressed) {
                                NumberPicker.this.postDelayed(this,
                                        ViewConfiguration.getPressedStateDuration());
                            }
                            mIncrementVirtualButtonPressed ^= true;
                            invalidate(0, mBottomSelectionDividerBottom, getRight(),
                                    getBottom());
                        }
                        break;
                        case BUTTON_DECREMENT: {
                            if (!mDecrementVirtualButtonPressed) {
                                NumberPicker.this.postDelayed(this,
                                        ViewConfiguration.getPressedStateDuration());
                            }
                            mDecrementVirtualButtonPressed ^= true;
                            invalidate(0, 0, getRight(), mTopSelectionDividerTop);
                        }
                    }
                }
                break;
            }
        }
    }

    /**
     * 命令用于设置输入文本选择.
     */
    class SetSelectionCommand implements Runnable {
        private int mSelectionStart;

        private int mSelectionEnd;

        public void run() {
            mInputText.setSelection(mSelectionStart, mSelectionEnd);
        }
    }

    /**
     * Command for changing the current value from a long press by threes.
     */
    class ChangeCurrentByOneFromLongPressCommand implements Runnable {
        private boolean mIncrement;

        private void setStep(boolean increment) {
            mIncrement = increment;
        }

        @Override
        public void run() {
            changeValueByOne(mIncrement);
            postDelayed(this, mLongPressUpdateInterval);
        }
    }

    /**
     * @hide
     */
    public static class CustomEditText extends EditText {

        public CustomEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public void onEditorAction(int actionCode) {
            super.onEditorAction(actionCode);
            if (actionCode == EditorInfo.IME_ACTION_DONE) {
                clearFocus();
            }
        }

    }

    /**
     * Command for beginning soft input on long press.
     */
    class BeginSoftInputOnLongPressCommand implements Runnable {

        @Override
        public void run() {
            showSoftInput();
            mIgonreMoveEvents = true;
        }
    }

    private SupportAccessibilityNodeProvider getSupportAccessibilityNodeProvider() {
        return new SupportAccessibilityNodeProvider();
    }

    class SupportAccessibilityNodeProvider {

        AccessibilityNodeProviderImpl mProvider;

        private SupportAccessibilityNodeProvider() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mProvider = new AccessibilityNodeProviderImpl();
            }
        }

        public boolean performAction(int virtualViewId, int action,
                                     Bundle arguments) {
            if (mProvider != null) {
                return mProvider
                        .performAction(virtualViewId, action, arguments);
            }

            return false;
        }

        public void sendAccessibilityEventForVirtualView(int virtualViewId,
                                                         int eventType) {
            if (mProvider != null)
                mProvider.sendAccessibilityEventForVirtualView(virtualViewId,
                        eventType);
        }
    }

    /**
     * Class for managing virtual view tree rooted at this sportHour.
     */
    class AccessibilityNodeProviderImpl extends AccessibilityNodeProvider {
        private static final int UNDEFINED = Integer.MIN_VALUE;

        private static final int VIRTUAL_VIEW_ID_INCREMENT = 1;

        private static final int VIRTUAL_VIEW_ID_INPUT = 2;

        private static final int VIRTUAL_VIEW_ID_DECREMENT = 3;

        private final Rect mTempRect = new Rect();

        private final int[] mTempArray = new int[2];

        private int mAccessibilityFocusedView = UNDEFINED;

        @Override
        public AccessibilityNodeInfo createAccessibilityNodeInfo(
                int virtualViewId) {
            switch (virtualViewId) {
                case View.NO_ID:
                    return createAccessibilityNodeInfoForNumberPicker(getScrollX(),
                            getScrollY(), getScrollX() + (getRight() - getLeft()),
                            getScrollY() + (getBottom() - getTop()));
                case VIRTUAL_VIEW_ID_DECREMENT:
                    return createAccessibilityNodeInfoForVirtualButton(
                            VIRTUAL_VIEW_ID_DECREMENT,
                            getVirtualDecrementButtonText(), getScrollX(),
                            getScrollY(), getScrollX() + (getRight() - getLeft()),
                            mTopSelectionDividerTop + mSelectionDividerHeight);
                case VIRTUAL_VIEW_ID_INPUT:
                    return createAccessibiltyNodeInfoForInputText();
                case VIRTUAL_VIEW_ID_INCREMENT:
                    return createAccessibilityNodeInfoForVirtualButton(
                            VIRTUAL_VIEW_ID_INCREMENT,
                            getVirtualIncrementButtonText(),
                            getScrollX(),
                            mBottomSelectionDividerBottom - mSelectionDividerHeight,
                            getScrollX() + (getRight() - getLeft()), getScrollY()
                                    + (getBottom() - getTop()));
            }
            return super.createAccessibilityNodeInfo(virtualViewId);
        }

        @Override
        public List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText(
                String searched, int virtualViewId) {
            if (TextUtils.isEmpty(searched)) {
                return Collections.emptyList();
            }
            String searchedLowerCase = searched.toLowerCase();
            List<AccessibilityNodeInfo> result = new ArrayList<AccessibilityNodeInfo>();
            switch (virtualViewId) {
                case View.NO_ID: {
                    findAccessibilityNodeInfosByTextInChild(searchedLowerCase,
                            VIRTUAL_VIEW_ID_DECREMENT, result);
                    findAccessibilityNodeInfosByTextInChild(searchedLowerCase,
                            VIRTUAL_VIEW_ID_INPUT, result);
                    findAccessibilityNodeInfosByTextInChild(searchedLowerCase,
                            VIRTUAL_VIEW_ID_INCREMENT, result);
                    return result;
                }
                case VIRTUAL_VIEW_ID_DECREMENT:
                case VIRTUAL_VIEW_ID_INCREMENT:
                case VIRTUAL_VIEW_ID_INPUT: {
                    findAccessibilityNodeInfosByTextInChild(searchedLowerCase,
                            virtualViewId, result);
                    return result;
                }
            }
            return super.findAccessibilityNodeInfosByText(searched,
                    virtualViewId);
        }

        @Override
        public boolean performAction(int virtualViewId, int action,
                                     Bundle arguments) {
            switch (virtualViewId) {
                case View.NO_ID: {
                    switch (action) {
                        case AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView != virtualViewId) {
                                mAccessibilityFocusedView = virtualViewId;
                                // requestAccessibilityFocus();
                                performAccessibilityAction(
                                        AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS,
                                        null);
                                return true;
                            }
                        }
                        return false;
                        case AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView == virtualViewId) {
                                mAccessibilityFocusedView = UNDEFINED;
                                // clearAccessibilityFocus();
                                performAccessibilityAction(
                                        AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS,
                                        null);
                                return true;
                            }
                            return false;
                        }
                        case AccessibilityNodeInfo.ACTION_SCROLL_FORWARD: {
                            if (NumberPicker.this.isEnabled()
                                    && (getWrapSelectorWheel() || getValue() < getMaxValue())) {
                                changeValueByOne(true);
                                return true;
                            }
                        }
                        return false;
                        case AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD: {
                            if (NumberPicker.this.isEnabled()
                                    && (getWrapSelectorWheel() || getValue() > getMinValue())) {
                                changeValueByOne(false);
                                return true;
                            }
                        }
                        return false;
                    }
                }
                break;
                case VIRTUAL_VIEW_ID_INPUT: {
                    switch (action) {
                        case AccessibilityNodeInfo.ACTION_FOCUS: {
                            if (NumberPicker.this.isEnabled()
                                    && !mInputText.isFocused()) {
                                return mInputText.requestFocus();
                            }
                        }
                        break;
                        case AccessibilityNodeInfo.ACTION_CLEAR_FOCUS: {
                            if (NumberPicker.this.isEnabled() && mInputText.isFocused()) {
                                mInputText.clearFocus();
                                return true;
                            }
                            return false;
                        }
                        case AccessibilityNodeInfo.ACTION_CLICK: {
                            if (NumberPicker.this.isEnabled()) {
                                showSoftInput();
                                return true;
                            }
                            return false;
                        }
                        case AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView != virtualViewId) {
                                mAccessibilityFocusedView = virtualViewId;
                                sendAccessibilityEventForVirtualView(
                                        virtualViewId,
                                        AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
                                mInputText.invalidate();
                                return true;
                            }
                        }
                        return false;
                        case AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView == virtualViewId) {
                                mAccessibilityFocusedView = UNDEFINED;
                                sendAccessibilityEventForVirtualView(
                                        virtualViewId,
                                        AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
                                mInputText.invalidate();
                                return true;
                            }
                        }
                        return false;
                        default: {
                            return mInputText.performAccessibilityAction(action,
                                    arguments);
                        }
                    }
                }
                return false;
                case VIRTUAL_VIEW_ID_INCREMENT: {
                    switch (action) {
                        case AccessibilityNodeInfo.ACTION_CLICK: {
                            if (NumberPicker.this.isEnabled()) {
                                NumberPicker.this.changeValueByOne(true);
                                sendAccessibilityEventForVirtualView(virtualViewId,
                                        AccessibilityEvent.TYPE_VIEW_CLICKED);
                                return true;
                            }
                        }
                        return false;
                        case AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView != virtualViewId) {
                                mAccessibilityFocusedView = virtualViewId;
                                sendAccessibilityEventForVirtualView(
                                        virtualViewId,
                                        AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
                                invalidate(0, mBottomSelectionDividerBottom,
                                        getRight(), getBottom());
                                return true;
                            }
                        }
                        return false;
                        case AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView == virtualViewId) {
                                mAccessibilityFocusedView = UNDEFINED;
                                sendAccessibilityEventForVirtualView(
                                        virtualViewId,
                                        AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
                                invalidate(0, mBottomSelectionDividerBottom,
                                        getRight(), getBottom());
                                return true;
                            }
                        }
                        return false;
                    }
                }
                return false;
                case VIRTUAL_VIEW_ID_DECREMENT: {
                    switch (action) {
                        case AccessibilityNodeInfo.ACTION_CLICK: {
                            if (NumberPicker.this.isEnabled()) {
                                final boolean increment = (virtualViewId == VIRTUAL_VIEW_ID_INCREMENT);
                                NumberPicker.this.changeValueByOne(increment);
                                sendAccessibilityEventForVirtualView(virtualViewId,
                                        AccessibilityEvent.TYPE_VIEW_CLICKED);
                                return true;
                            }
                        }
                        return false;
                        case AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView != virtualViewId) {
                                mAccessibilityFocusedView = virtualViewId;
                                sendAccessibilityEventForVirtualView(
                                        virtualViewId,
                                        AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
                                invalidate(0, 0, getRight(), mTopSelectionDividerTop);
                                return true;
                            }
                        }
                        return false;
                        case AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView == virtualViewId) {
                                mAccessibilityFocusedView = UNDEFINED;
                                sendAccessibilityEventForVirtualView(
                                        virtualViewId,
                                        AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
                                invalidate(0, 0, getRight(), mTopSelectionDividerTop);
                                return true;
                            }
                        }
                        return false;
                    }
                }
                return false;
            }
            return super.performAction(virtualViewId, action, arguments);
        }

        public void sendAccessibilityEventForVirtualView(int virtualViewId,
                                                         int eventType) {
            switch (virtualViewId) {
                case VIRTUAL_VIEW_ID_DECREMENT: {
                    if (hasVirtualDecrementButton()) {
                        sendAccessibilityEventForVirtualButton(virtualViewId,
                                eventType, getVirtualDecrementButtonText());
                    }
                }
                break;
                case VIRTUAL_VIEW_ID_INPUT: {
                    sendAccessibilityEventForVirtualText(eventType);
                }
                break;
                case VIRTUAL_VIEW_ID_INCREMENT: {
                    if (hasVirtualIncrementButton()) {
                        sendAccessibilityEventForVirtualButton(virtualViewId,
                                eventType, getVirtualIncrementButtonText());
                    }
                }
                break;
            }
        }

        private void sendAccessibilityEventForVirtualText(int eventType) {
            if (((AccessibilityManager) getContext().getSystemService(
                    Context.ACCESSIBILITY_SERVICE)).isEnabled()) {
                AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
                mInputText.onInitializeAccessibilityEvent(event);
                mInputText.onPopulateAccessibilityEvent(event);
                event.setSource(NumberPicker.this, VIRTUAL_VIEW_ID_INPUT);
                requestSendAccessibilityEvent(NumberPicker.this, event);
            }
        }

        private void sendAccessibilityEventForVirtualButton(int virtualViewId,
                                                            int eventType, String text) {
            if (((AccessibilityManager) getContext().getSystemService(
                    Context.ACCESSIBILITY_SERVICE)).isEnabled()) {
                AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
                event.setClassName(Button.class.getName());
                event.setPackageName(getContext().getPackageName());
                event.getText().add(text);
                event.setEnabled(NumberPicker.this.isEnabled());
                event.setSource(NumberPicker.this, virtualViewId);
                requestSendAccessibilityEvent(NumberPicker.this, event);
            }
        }

        private void findAccessibilityNodeInfosByTextInChild(
                String searchedLowerCase, int virtualViewId,
                List<AccessibilityNodeInfo> outResult) {
            switch (virtualViewId) {
                case VIRTUAL_VIEW_ID_DECREMENT: {
                    String text = getVirtualDecrementButtonText();
                    if (!TextUtils.isEmpty(text)
                            && text.toString().toLowerCase()
                            .contains(searchedLowerCase)) {
                        outResult
                                .add(createAccessibilityNodeInfo(VIRTUAL_VIEW_ID_DECREMENT));
                    }
                }
                return;
                case VIRTUAL_VIEW_ID_INPUT: {
                    CharSequence text = mInputText.getText();
                    if (!TextUtils.isEmpty(text)
                            && text.toString().toLowerCase()
                            .contains(searchedLowerCase)) {
                        outResult
                                .add(createAccessibilityNodeInfo(VIRTUAL_VIEW_ID_INPUT));
                        return;
                    }
                    CharSequence contentDesc = mInputText.getText();
                    if (!TextUtils.isEmpty(contentDesc)
                            && contentDesc.toString().toLowerCase()
                            .contains(searchedLowerCase)) {
                        outResult
                                .add(createAccessibilityNodeInfo(VIRTUAL_VIEW_ID_INPUT));
                        return;
                    }
                }
                break;
                case VIRTUAL_VIEW_ID_INCREMENT: {
                    String text = getVirtualIncrementButtonText();
                    if (!TextUtils.isEmpty(text)
                            && text.toString().toLowerCase()
                            .contains(searchedLowerCase)) {
                        outResult
                                .add(createAccessibilityNodeInfo(VIRTUAL_VIEW_ID_INCREMENT));
                    }
                }
                return;
            }
        }

        private AccessibilityNodeInfo createAccessibiltyNodeInfoForInputText() {
            AccessibilityNodeInfo info = mInputText
                    .createAccessibilityNodeInfo();
            info.setSource(NumberPicker.this, VIRTUAL_VIEW_ID_INPUT);
            if (mAccessibilityFocusedView != VIRTUAL_VIEW_ID_INPUT) {
                info.addAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
            }
            if (mAccessibilityFocusedView == VIRTUAL_VIEW_ID_INPUT) {
                info.addAction(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
            }
            return info;
        }

        private AccessibilityNodeInfo createAccessibilityNodeInfoForVirtualButton(
                int virtualViewId, String text, int left, int top, int right,
                int bottom) {
            AccessibilityNodeInfo info = AccessibilityNodeInfo.obtain();
            info.setClassName(Button.class.getName());
            info.setPackageName(getContext().getPackageName());
            info.setSource(NumberPicker.this, virtualViewId);
            info.setParent(NumberPicker.this);
            info.setText(text);
            info.setClickable(true);
            info.setLongClickable(true);
            info.setEnabled(NumberPicker.this.isEnabled());
            Rect boundsInParent = mTempRect;
            boundsInParent.set(left, top, right, bottom);
            // TODO info.setVisibleToUser(isVisibleToUser(boundsInParent));
            info.setBoundsInParent(boundsInParent);
            Rect boundsInScreen = boundsInParent;
            int[] locationOnScreen = mTempArray;
            getLocationOnScreen(locationOnScreen);
            boundsInScreen.offset(locationOnScreen[0], locationOnScreen[1]);
            info.setBoundsInScreen(boundsInScreen);

            if (mAccessibilityFocusedView != virtualViewId) {
                info.addAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
            }
            if (mAccessibilityFocusedView == virtualViewId) {
                info.addAction(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
            }
            if (NumberPicker.this.isEnabled()) {
                info.addAction(AccessibilityNodeInfo.ACTION_CLICK);
            }

            return info;
        }

        private AccessibilityNodeInfo createAccessibilityNodeInfoForNumberPicker(
                int left, int top, int right, int bottom) {
            AccessibilityNodeInfo info = AccessibilityNodeInfo.obtain();
            info.setClassName(NumberPicker.class.getName());
            info.setPackageName(getContext().getPackageName());
            info.setSource(NumberPicker.this);

            if (hasVirtualDecrementButton()) {
                info.addChild(NumberPicker.this, VIRTUAL_VIEW_ID_DECREMENT);
            }
            info.addChild(NumberPicker.this, VIRTUAL_VIEW_ID_INPUT);
            if (hasVirtualIncrementButton()) {
                info.addChild(NumberPicker.this, VIRTUAL_VIEW_ID_INCREMENT);
            }

            info.setParent((View) getParentForAccessibility());
            info.setEnabled(NumberPicker.this.isEnabled());
            info.setScrollable(true);

            if (mAccessibilityFocusedView != View.NO_ID) {
                info.addAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
            }
            if (mAccessibilityFocusedView == View.NO_ID) {
                info.addAction(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
            }
            if (NumberPicker.this.isEnabled()) {
                if (getWrapSelectorWheel() || getValue() < getMaxValue()) {
                    info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                }
                if (getWrapSelectorWheel() || getValue() > getMinValue()) {
                    info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
                }
            }

            return info;
        }

        private boolean hasVirtualDecrementButton() {
            return getWrapSelectorWheel() || getValue() > getMinValue();
        }

        private boolean hasVirtualIncrementButton() {
            return getWrapSelectorWheel() || getValue() < getMaxValue();
        }

        private String getVirtualDecrementButtonText() {
            int value = mValue - 1;
            if (mWrapSelectorWheel) {
                value = getWrappedSelectorIndex(value);
            }
            if (value >= mMinValue) {
                return (mDisplayedValues == null) ? formatNumber(value)
                        : mDisplayedValues[value - mMinValue];
            }
            return null;
        }

        private String getVirtualIncrementButtonText() {
            int value = mValue + 1;
            if (mWrapSelectorWheel) {
                value = getWrappedSelectorIndex(value);
            }
            if (value <= mMaxValue) {
                return (mDisplayedValues == null) ? formatNumber(value)
                        : mDisplayedValues[value - mMinValue];
            }
            return null;
        }
    }

    static private String formatNumberWithLocale(int value) {
        return String.format(Locale.getDefault(), "%d", value);
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getBackgroundID() {
        return backgroundID;
    }

    public void setBackgroundID(int backgroundID) {
        this.backgroundID = backgroundID;
    }

    public static void setAddZero(boolean isAddZero) {
        NumberPicker.isAddZero = isAddZero;
    }

}
