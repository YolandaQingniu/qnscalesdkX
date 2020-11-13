package com.qingniu.qnble.demo.picker;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * author: yolanda-XY
 * date: 2018/3/30
 * package_name: com.qingniu.qnble.demo.picker
 * description: ${TODO}
 */

public class StringWheelView extends ScrollView {
    public static final String TAG = StringWheelView.class.getSimpleName();

    int initialY;

    Runnable scrollerTask = new Runnable() {

        public void run() {

            int newY = getScrollY();
            if (initialY == newY) { // stopped
                int scrollSpace = initialY;
                int remainder = scrollSpace % itemHeight;

                int scrollOffset;
                if (remainder > itemHeight / 2) {
                    selectedIndex = scrollSpace / itemHeight + 1;
                    scrollOffset = initialY - remainder + itemHeight;
                } else {
                    selectedIndex = scrollSpace / itemHeight;
                    scrollOffset = initialY - remainder;
                }
                smoothScrollTo(0, scrollOffset);

                onSelectedCallBack();
            } else {
                initialY = getScrollY();
                StringWheelView.this.postDelayed(scrollerTask, newCheck);
            }
        }
    };

    int newCheck = 50;


    public interface OnWheelViewListener {
        void onSelected(StringWheelView picker, int selectedIndex, String item);
    }

    private Context context;

    private LinearLayout views;
    boolean initShowFlag = false;
    int selectedColor = Color.parseColor("#0288ce");
    int unSelecetedColor = Color.parseColor("#bbbbbb");

    public StringWheelView(Context context) {
        super(context);
        init(context);
    }

    public StringWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StringWheelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    final List<String> items = new ArrayList<String>();

    public void setItems(List<String> list) {

        items.clear();
        items.addAll(list);
        if (selectedIndex >= items.size()) {
            selectedIndex = items.size() - 1;
        }
        // 前面和后面补全
        initShowFlag = true;
        initData();
    }

    int middleItemTop;
    int itemHeight = 0;
    float transform = 0;
    int selectedIndex = 0;

    private void init(Context context) {
        this.context = context;

        itemHeight = dip2px(35);
        this.setVerticalScrollBarEnabled(false);
        views = new LinearLayout(context);
        views.setOrientation(LinearLayout.VERTICAL);
        this.addView(views);

    }

    public void startScrollerTask() {

        initialY = getScrollY();
        this.postDelayed(scrollerTask, newCheck);
    }

    private void initData() {
        views.removeAllViews();
        for (String item : items) {
            views.addView(createView(item));
        }
        this.selectedIndex = 0;
        if (getHeight() > 0) {
            initShow();
        }
    }


    private TextView createView(String item) {
        TextView tv = new TextView(context);
        tv.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight));
        tv.setSingleLine(true);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tv.setText(item);
        tv.setGravity(Gravity.CENTER);

        return tv;
    }

    View createIdleView(int idleHeight) {
        View v = new View(context);
        v.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, idleHeight));
        return v;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        refreshItemView(t);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        middleItemTop = (h - itemHeight) / 2;
        transform = 0.7f / (h / itemHeight / 2);
        if (items.size() > 0) {
            //已经添加了数据
            initShow();
        }
    }


    public void setSelectedColor(int selectedColor) {
        this.selectedColor = selectedColor;
        this.unSelecetedColor = selectedColor;
    }

    void initShow() {
        if (getHeight() == 0 || !initShowFlag) {
            return;
        }
        initShowFlag = true;
        views.addView(createIdleView(middleItemTop));
        views.addView(createIdleView(middleItemTop), 0);
        if (selectedIndex == 0) {
            refreshItemView(0);
            return;
        }
        postDelayed(new Runnable() {
            @Override
            public void run() {
                int scrollY = selectedIndex * itemHeight;
                smoothScrollTo(0, scrollY);
                refreshItemView(scrollY);
            }
        }, 20);

    }

    private void refreshItemView(int y) {
        int remainder = y % itemHeight;

        int currentPosition;
        if (remainder > itemHeight / 2) {
            currentPosition = y / itemHeight + 1;
        } else {
            currentPosition = y / itemHeight;
        }

        currentPosition++;

        int childSize = views.getChildCount() - 1;
        for (int i = 1; i < childSize; i++) {
            TextView itemView = (TextView) views.getChildAt(i);
            if (null == itemView) {
                return;
            }
            if (currentPosition == i) {
                itemView.setTextColor(selectedColor);
                itemView.setAlpha(1);
                itemView.setScaleX(1);
                itemView.setScaleY(1);
            } else {
                float alpha = 1 - Math.abs(currentPosition - i) * transform;
                if (alpha < 0) {
                    alpha = 0;
                }
                itemView.setAlpha(alpha);
                itemView.setScaleX(alpha);
                itemView.setScaleY(alpha);
                itemView.setTextColor(unSelecetedColor);
            }
        }
    }

    /**
     * 选中回调
     */
    private void onSelectedCallBack() {
        if (null != onWheelViewListener && !items.isEmpty()) {
            onWheelViewListener.onSelected(this, selectedIndex, items.get(selectedIndex));
        }

    }

    public void setSelection(int position) {

        if (position >= items.size()) {
            position = items.size() - 1;
        }
        if (position < 0) {
            position = 0;
        }
        this.selectedIndex = position;
        if (initShowFlag) {
            smoothScrollTo(0, selectedIndex * itemHeight);
        }

    }

    public String getSelectedItem() {
        return items.get(selectedIndex);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }


    @Override
    public void fling(int velocityY) {
        super.fling(velocityY / 3);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {

            startScrollerTask();
        }
        return super.onTouchEvent(ev);
    }

    private OnWheelViewListener onWheelViewListener;

    public OnWheelViewListener getOnWheelViewListener() {
        return onWheelViewListener;
    }

    public void setOnWheelViewListener(OnWheelViewListener onWheelViewListener) {
        this.onWheelViewListener = onWheelViewListener;
    }

    private int dip2px(float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}

