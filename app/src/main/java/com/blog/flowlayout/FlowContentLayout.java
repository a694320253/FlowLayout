package com.blog.flowlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FlowContentLayout extends RelativeLayout {

    private FlowLayout mBackFlowLayout;

    private int mLastIndex = 0;
    private FlowLayout mFontFlowLayout;
    private List<String> list = new ArrayList<>();
    private View upView;
    private View downView;


    public FlowContentLayout(Context context) {
        this(context,null);
    }

    public FlowContentLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FlowContentLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.flow_content_layout,this);
        upView = LayoutInflater.from(context).inflate(R.layout.view_item_fold_up, this, false);
        upView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mBackFlowLayout.setFoldState(true);
                mFontFlowLayout.setFoldState(true);
                refreshViews();
            }
        });
        downView = LayoutInflater.from(context).inflate(R.layout.view_item_fold_down, this, false);
        downView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mBackFlowLayout.setFoldState(false);
                mFontFlowLayout.setFoldState(false);
                refreshViews();
            }
        });
        mBackFlowLayout = findViewById(R.id.mFlowLayout);
        mBackFlowLayout.setFlowContentLayout(this);
        mFontFlowLayout = findViewById(R.id.mFontFlowLayout);
        mFontFlowLayout.setUpFoldView(upView);
        mFontFlowLayout.setDownFoldView(downView);
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mBackFlowLayout.setFlowContentLayout(null);
    }

    /**
     * 这里把隐藏的幕后计算布局加入view先计算
     * @param list
     */
    public void addViews(@NotNull List<String> list) {
        mLastIndex = 0;
        this.list.clear();
        this.list.addAll(list);
        mBackFlowLayout.addViews(list);
    }

    /**
     * 相同的数据重新刷新
     */
    private void refreshViews(){
        if(list != null && list.size() > 0){
            mLastIndex = 0;
            mBackFlowLayout.addViews(list);
        }
    }

    /**
     * 幕后布局计算后的最大折叠位置
     * @param foldState
     * @param index
     * @param flag 是否需要加入向上或者向下按钮
     * @param lineWidthUsed
     */
    public void foldIndex(boolean foldState, int index, boolean flag, int lineWidthUsed) {

        Log.d("tagtag",index+"---foldIndex--"+flag);
        if(mLastIndex != index){//防止多次调用
            Log.d("tagtag","---2222222--");
            mLastIndex = index;
            //添加外部真正的布局
            if(flag){
                List<String> list = new ArrayList<>();
                for (int x = 0; x < index; x++) {
                    list.add(FlowContentLayout.this.list.get(x));
                }
                list.add("@@");
                mFontFlowLayout.addViews(list);
            }else{
                List<String> list = new ArrayList<>();
                for (int x = 0; x < FlowContentLayout.this.list.size(); x++) {
                    list.add(FlowContentLayout.this.list.get(x));
                }
                mFontFlowLayout.addViews(list);
            }
        }
    }

    public int getUpViewWidth() {
        if(upView != null){
            int measuredWidth = upView.getMeasuredWidth();
            Log.d("tagtag","--"+measuredWidth);
            return 138;
        }
        return 0;
    }

    /**
     * 删除全部后转态恢复
     */
    public void releaseState(){
        mBackFlowLayout.setFoldState(true);
        mFontFlowLayout.setFoldState(true);
    }
}

