package com.blog.flowlayout;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class FlowLayout extends ViewGroup {

    /**
     * 水平距离
     */
    private int mHorizontalSpacing =10;

    private static final int MAX_LINE = Integer.MAX_VALUE;//从0开始计数
    private static final int MIN_LINE = 0;//从0开始计数
    private FlowContentLayout mFlowContentLayout;
    private boolean foldState = true;
    private View upFoldView;
    private View downFoldView;
    private int mWidth;

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setFlowContentLayout(FlowContentLayout mFlowContentLayout) {
        this.mFlowContentLayout = mFlowContentLayout;
    }

    public void setFoldState(boolean foldState) {
        this.foldState = foldState;
    }

    public void setUpFoldView(View upFoldView) {
        this.upFoldView = upFoldView;
    }

    public void setDownFoldView(View downFoldView) {
        this.downFoldView = downFoldView;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = getWidth();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mFlowContentLayout!=null){
            Log.d("TAGTAG","onMeasure");
        }
        //获取mode 和 size
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        final int layoutWidth = widthSize - getPaddingLeft() - getPaddingRight();
        //判断如果布局宽度抛去左右padding小于0，也不能处理了
        if (layoutWidth <= 0) {
            return;
        }

        //这里默认宽高默认值默认把左右，上下padding加上
        int width = getPaddingLeft() + getPaddingRight();
        int height = getPaddingTop() + getPaddingBottom();

        //初始一行的宽度
        int lineWidth = 0;
        //初始一行的高度
        int lineHeight = 0;

        //测量子View
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        int beforewh = 0;
        int beforelineWidth = 0;
        int childWidth, childHeight;
        //行数
        int line = 0;
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View view = getChildAt(i);
            //这里需要先判断子view是否被设置了GONE
            if (view.getVisibility() == GONE) {
                continue;
            }
            childWidth = view.getMeasuredWidth();
            childHeight = view.getMeasuredHeight();

            //第一行
            if (i == 0) {
                lineWidth = getPaddingLeft() + getPaddingRight() + childWidth;
                lineHeight = childHeight;
            } else {
                //判断是否需要换行
                //换行
                if (lineWidth + mHorizontalSpacing + childWidth > widthSize) {
                    line++;//行数增加
                    // 取最大的宽度
                    beforelineWidth=lineWidth;
                    width = Math.max(lineWidth, width);
                    //重新开启新行，开始记录
                    lineWidth = getPaddingLeft() + getPaddingRight() + childWidth;
                    //叠加当前高度，
                    height += lineHeight;
                    //开启记录下一行的高度
                    lineHeight = childHeight;
                    if(mFlowContentLayout != null){
                        if(foldState && line > MIN_LINE){
//                            Log.d("TAGTAG",beforewh+"--beforelineWidth---"+beforelineWidth+"--"+widthSize);
                            if (beforelineWidth+mHorizontalSpacing+mFlowContentLayout.getUpViewWidth()<=widthSize){
                                callBack(foldState,i, true,lineWidth);
                            }else {
                                callBack(foldState,((beforelineWidth - beforewh+mFlowContentLayout.getUpViewWidth() > widthSize)?i-2:i-1), true,lineWidth);
                            }
                            if (mFlowContentLayout!=null){
                                Log.d("TAGTAG","break");
                            }
                            break;
                        }
                    }
                }
                //不换行
                else {
                    lineWidth = lineWidth + mHorizontalSpacing + childWidth;
                    lineHeight = Math.max(lineHeight, childHeight);
                }
            }
            // 如果是最后一个，则将当前记录的最大宽度和当前lineWidth做比较
            if (i == count - 1) {
                width = Math.max(width, lineWidth);
                height += lineHeight;
            }
            beforewh=childWidth;
        }
        if (mFlowContentLayout!=null){
            Log.d("TAGTAG","-------");
        }
        //根据计算的值重新设置
        if(mFlowContentLayout == null){
            setMeasuredDimension(widthMode == MeasureSpec.EXACTLY ? widthSize : width,
                    heightMode == MeasureSpec.EXACTLY ? heightSize : height);
        }else{
            setMeasuredDimension(widthMode == MeasureSpec.EXACTLY ? widthSize : width,
                    0);
        }

        if(foldState && (line >= 0 && line <= MIN_LINE)){
            callBack(foldState,getChildCount(),false,lineWidth);
        }
        if(!foldState && line >= 0){
            if(mFlowContentLayout != null){
                int upViewWidth = mFlowContentLayout.getUpViewWidth() + mHorizontalSpacing;
                if(lineWidth > (mWidth - upViewWidth) && line == MAX_LINE){
                    callBack(foldState,getChildCount() - 1,true,lineWidth);
                }else{
                    callBack(foldState,getChildCount(),true,lineWidth);
                }
            }else{
                callBack(foldState,getChildCount(),true,lineWidth);
            }

        }
    }

    /**
     * 超过最大数的回调
     * @param foldState
     * @param index 最大数的位置。
     * @param b
     * @param lineWidthUsed
     */
    private void callBack(boolean foldState, int index, boolean b, int lineWidthUsed) {
        if(mFlowContentLayout != null){
            mFlowContentLayout.foldIndex(foldState,index,b,lineWidthUsed);
        }
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int layoutWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        if (layoutWidth <= 0) {
            return;
        }
        int childWidth, childHeight;
        //需要加上top padding
        int top = getPaddingTop();
//        final int[] wh = getMaxWidthHeight();
        int lineHeight = 0;
//        int line = 0;
        //左对齐
        //左侧需要先加上左边的padding
        int left = getPaddingLeft();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View view = getChildAt(i);
            //这里一样判断下显示状态
            if (view.getVisibility() == GONE) {
                continue;
            }
            //自适宽高
            childWidth = view.getMeasuredWidth();
            childHeight = view.getMeasuredHeight();
            //第一行开始摆放
            if (i == 0) {
                view.layout(left, top, left + childWidth, top + childHeight);
                lineHeight = childHeight;
            } else {
                //判断是否需要换行
                if (left + mHorizontalSpacing + childWidth > layoutWidth + getPaddingLeft()) {
//                    line++;
                    //重新起行
                    left = getPaddingLeft();
                    top = top + lineHeight;
                    lineHeight = childHeight;
                } else {
                    left = left + mHorizontalSpacing;
                    lineHeight = Math.max(lineHeight, childHeight);
                }
                view.layout(left, top, left + childWidth, top + childHeight);
            }
            //累加left
            left += childWidth;
        }
    }


    public void addViews(List<String> list){
        removeAllViews();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        for (int x = 0; x< list.size(); x++) {
            String s = list.get(x);
            if(TextUtils.equals("@@",s)){
                if(foldState){
                    if(downFoldView != null){
//                        Utils.removeFromParent(downFoldView);
                        addView(downFoldView,layoutParams);
                    }
                }else{
                    if(upFoldView != null){
//                        Utils.removeFromParent(upFoldView);
                        addView(upFoldView,layoutParams);
                    }
                }
            }else{
                addTextView(s,layoutParams);
            }

        }
    }



    private void addTextView(String s, LinearLayout.LayoutParams layoutParams){
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setPadding(0,IntExtensionsKt.dp2px(8),0,0);
        linearLayout.setLayoutParams(layoutParams);
        TextView tv = new TextView(getContext());
        tv.setPadding(IntExtensionsKt.dp2px(6), IntExtensionsKt.dp2px(2), IntExtensionsKt.dp2px(6), IntExtensionsKt.dp2px(2));
        tv.setText(s);
        tv.setSingleLine();
        tv.setBackgroundResource(R.drawable.myshape);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,13);
        tv.setTextColor(getResources().getColor(R.color.ff666666));
        tv.setEllipsize(TextUtils.TruncateAt.END);
        GradientDrawable drawable= (GradientDrawable) tv.getBackground();
        drawable.setStroke(1, Color.parseColor("#00f00f"));
        drawable.setColor(Color.YELLOW);
        linearLayout.addView(tv,new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
        addView(linearLayout,layoutParams);
    }
}


