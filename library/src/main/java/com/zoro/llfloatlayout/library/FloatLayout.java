package com.zoro.llfloatlayout.library;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class FloatLayout extends LinearLayout{

    List<Integer> lineHeights = new ArrayList<>();
    List<Integer> lineWidths = new ArrayList<>();
    int contentHeight;

    int lines = -1;
    View moreDetailsView;
    int indexBeforeDetailView = 0;
    boolean showDetailView = false;

    public FloatLayout(Context context) {
        this(context, null);
    }

    public FloatLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("NewApi")
    public FloatLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FloatLayout);
        lines = a.getInt(R.styleable.FloatLayout_lines, -1);
    }



    public void setLines(int lines) {
        this.lines = lines;
    }

    public void setMoreDetailsView(View moreDetailsView) {
        this.moreDetailsView = moreDetailsView;
        if(moreDetailsView != null){
            this.addView(moreDetailsView);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width = 0, height = 0;
        int count = getChildCount();
        int x = getPaddingLeft();
        int y = getPaddingTop();
        int maxLineWidth = 0;
        int maxHeight = 0;
        int lineHeight = 0;
        lineHeights.clear();
        lineWidths.clear();
        width = getWidthBeforeMeasure(widthMode, widthSize);

        int moreDetailsViewWidth = 0;
        if(moreDetailsView != null){
            indexBeforeDetailView = 0;
            LayoutParams lp = (LayoutParams) moreDetailsView.getLayoutParams();
            measureChildWithMargins(moreDetailsView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            if(lp != null){
                moreDetailsViewWidth = moreDetailsView.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            }
        }
        int curLine = 1;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (child.getVisibility() == GONE ) {
                continue;
            }
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            x += childWidth;
            if (x <= width - getPaddingRight()) {
                // 不满一行，接在后面。记录此行最高的孩子的高度。
                if (childHeight > lineHeight) {
                    lineHeight = childHeight;
                }
            } else {
                lineHeights.add(lineHeight);
                lineWidths.add(x - childWidth);
                maxLineWidth = Math.max(maxLineWidth, x - childWidth + getPaddingRight());
                // 满了一行，另起一行。
                x = getPaddingLeft() + childWidth;
                y += lineHeight;
                curLine++;
                lineHeight = childHeight;
            }
            //隐藏超出行数的子 View
            if(lines > 0 && !child.equals(moreDetailsView)){
                if(curLine > lines){
                    setGoneForOutLineViews();
                    if(moreDetailsView != null){
                        if(moreDetailsView.getParent() != null){
                            ViewGroup parent = (ViewGroup)moreDetailsView.getParent();
                            parent.removeView(moreDetailsView);
                        }
                        this.addView(moreDetailsView);
                        showDetailView = true;
                    }
                    break;
                }else if(curLine < lines || (curLine == lines && x + moreDetailsViewWidth <= width - getPaddingRight())){
                    indexBeforeDetailView = i;

                }
            }
        }
        //moreDetailView
        if(lines <= 0 || !showDetailView){
            setMoreDetailViewVisibleState(false);
        }else{
            setMoreDetailViewVisibleState(true);
        }
        lineHeights.add(lineHeight);
        lineWidths.add(x);
        maxLineWidth = Math.max(maxLineWidth, x + getPaddingRight());
        maxHeight = y + lineHeight + getPaddingBottom();
        contentHeight = maxHeight;
        width = getWidthAfterMeasure(widthMode, width, maxLineWidth);
        height = getHeightAfterMeasure(heightMode, heightSize, height, maxHeight);
        setMeasuredDimension(width, height);
    }

    private void setGoneForOutLineViews(){
        int count = getChildCount();
        for(int i = indexBeforeDetailView + 1; i < count; i++){
            if(!getChildAt(i).equals(moreDetailsView)){
                getChildAt(i).setVisibility(GONE);
            }
        }
    }

    private void setMoreDetailViewVisibleState(boolean visible){
        if(moreDetailsView != null){
            moreDetailsView.setVisibility(visible ? VISIBLE : GONE);
        }
    }

    private int getHeightAfterMeasure(int heightMode, int heightSize, int height, int maxHeight) {
        switch (heightMode) {
            case MeasureSpec.AT_MOST:
                height = Math.min(heightSize, maxHeight);
            case MeasureSpec.UNSPECIFIED:
                height = maxHeight;
                break;
            case MeasureSpec.EXACTLY:
                height = heightSize;
                break;
        }
        return height;
    }

    private int getWidthAfterMeasure(int widthMode, int width, int maxLineWidth) {
        switch (widthMode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                width = Math.min(width, maxLineWidth);
                break;
            default:
                break;
        }
        return width;
    }

    private int getWidthBeforeMeasure(int widthMode, int widthSize) {
        int width;
        switch (widthMode) {
            case MeasureSpec.UNSPECIFIED:
                // 想多宽就多宽
                width = Integer.MAX_VALUE;
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
            default:
                width = widthSize;
                break;
        }
        return width;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int count = getChildCount();
        int x = getPaddingLeft();
        int y = getPaddingTop();
        int lineHeight = 0;
        int lineWidth = 0;
        int line = 0;
        int offsetTop;
        int offsetLeft;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            x += childWidth;
            if (x <= getMeasuredWidth() - getPaddingRight()) {
            } else {
                // 满了一行，另起一行。
                line++;
                x = getPaddingLeft() + childWidth;
                y += lineHeight;
            }

            if (line < lineHeights.size()) {
                lineHeight = lineHeights.get(line);
            }
            if (line < lineWidths.size()) {
                lineWidth = lineWidths.get(line);
            }
            int horizontalGravity = 0;
            int gravity = 0;
            try {
                Field field = LinearLayout.class.getDeclaredField("mGravity");
                field.setAccessible(true);
                gravity = field.getInt((LinearLayout) this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            horizontalGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
            offsetLeft = getOffsetLeft(lineWidth, horizontalGravity);
            int parentVerticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;
            final int verticalGravity = (lp.gravity < 0 ? gravity : lp.gravity) & Gravity.VERTICAL_GRAVITY_MASK;
            offsetTop = getOffsetTop(lineHeight, childHeight, parentVerticalGravity, verticalGravity);
            child.layout(x - childWidth + lp.leftMargin + offsetLeft, y + lp.topMargin + offsetTop, x - lp.rightMargin
                    + offsetLeft, y + childHeight - lp.bottomMargin + offsetTop);
        }
    }

    private int getOffsetTop(int lineHeight, int childHeight, int parentVerticalGravity, final int verticalGravity) {
        int offsetTop;
        switch (verticalGravity) {
            case Gravity.BOTTOM:
                offsetTop = lineHeight - childHeight;
                break;
            case Gravity.CENTER_VERTICAL:
                offsetTop = (lineHeight - childHeight) / 2;
                break;
            case Gravity.TOP:
            default:
                offsetTop = 0;
                break;
        }
        switch (parentVerticalGravity) {
            case Gravity.BOTTOM:
                offsetTop += getMeasuredHeight() - contentHeight;
                break;
            case Gravity.CENTER_VERTICAL:
                offsetTop += (getMeasuredHeight() - contentHeight) / 2;
                break;
            case Gravity.TOP:
            default:
                break;
        }
        return offsetTop;
    }

    private int getOffsetLeft(int lineWidth, int horizontalGravity) {
        int offsetLeft;
        switch (horizontalGravity) {
            case Gravity.RIGHT:
                offsetLeft = getMeasuredWidth() - lineWidth - getPaddingRight();
                break;
            case Gravity.CENTER_HORIZONTAL:
                offsetLeft = (getMeasuredWidth() - lineWidth - getPaddingRight()) / 2;
                break;
            case Gravity.LEFT:
            default:
                offsetLeft = 0;
                break;
        }
        return offsetLeft;
    }

}