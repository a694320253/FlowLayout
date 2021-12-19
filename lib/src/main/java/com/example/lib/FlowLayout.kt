package com.example.lib

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import kotlin.math.max
import kotlin.math.min

class FlowLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    // 子元素水平间距
    var itemHorizontalSpacing: Int = 0
        set(value) {
            field = value
            requestLayout()
        }
    // 子元素竖直间距
    var itemVerticalSpacing: Int = 0
        set(value) {
            field = value
            requestLayout()
        }
    /**
     * 记录每一行所有的子 View 的集合
     */
//    private val allLineViews = ArrayList<ArrayList<View>>()
    /**
     * 所有行的行高的集合
     */
    private val lineHeights = ArrayList<Int>()

    var lineVerticalGravity: Int = LINE_VERTICAL_GRAVITY_CENTER_VERTICAL
        set(value) {
            field = value
            requestLayout()
        }
    var mode: Int = MODE_LIMIT_MAX_COUNT
    var maxLines: Int = Int.MAX_VALUE
        set(value) {
            mode = MODE_LIMIT_MAX_LINE
            field = value
            requestLayout()
        }
    var maxCount: Int = Int.MAX_VALUE
        set(value) {
            mode = MODE_LIMIT_MAX_COUNT
            field = value
            requestLayout()
        }

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout)
        lineVerticalGravity = ta.getInt(R.styleable.FlowLayout_flowlayout_line_vertical_gravity, LINE_VERTICAL_GRAVITY_CENTER_VERTICAL)
        Log.d(TAG, "init: lineVerticalGravity=$lineVerticalGravity")
        // 默认值为 Int.MAX_VALUE，表示不限制行数
        maxLines = ta.getInt(R.styleable.FlowLayout_android_maxLines, Int.MAX_VALUE)
        Log.d(TAG, "init: maxLines=$maxLines")
        maxCount = ta.getInt(R.styleable.FlowLayout_maxCount, Int.MAX_VALUE)
        itemHorizontalSpacing = ta.getDimensionPixelSize(R.styleable.FlowLayout_itemHorizontalSpacing, 0)
        itemVerticalSpacing = ta.getDimensionPixelSize(R.styleable.FlowLayout_itemVerticalSpacing, 0)
        ta.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//        allLineViews.clear()
        lineHeights.clear()
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        Log.d(TAG, "onMeasure: widthMeasureSpec=${MeasureSpec.toString(widthMeasureSpec)}, heightMeasureSpec=${MeasureSpec.toString(heightMeasureSpec)}")
        // 获取流式布局允许的最大宽度
        val maxWidth = if (widthMode != MeasureSpec.UNSPECIFIED) widthSize else Int.MAX_VALUE
        Log.i(TAG,"最大宽度--${maxWidth}")
        var lineWidth = 0 // 行宽
        var maxLineWidth = 0 // 最大行宽
        var lineHeight = 0 // 行高
        var totalHeight = 0 // 总高度
        val childCount = getChildCount()
//        var lineViews = ArrayList<View>()
        var lineCount = 0
        var measuredChildCount = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                // 测量子 View
                val lp = child.layoutParams as MarginLayoutParams
                val childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                    getPaddingLeft() + getPaddingRight(), lp.width)
                val childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                    getPaddingTop() + getPaddingBottom(), lp.height)
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
                // 获取子 View 的测量宽/高
                val childMeasuredWidth = child.getMeasuredWidth()
                val childMeasuredHeight = child.getMeasuredHeight()
                val actualChildWidth = childMeasuredWidth + lp.leftMargin + lp.rightMargin
                val actualChildHeight = childMeasuredHeight + lp.topMargin + lp.bottomMargin
                val actualItemHorizontalSpacing = if (lineWidth == 0) 0 else itemHorizontalSpacing
                if (lineWidth + actualItemHorizontalSpacing + actualChildWidth <= maxWidth - getPaddingLeft() - getPaddingRight()) {
                    // 在本行还可以放置一个子 View
                    lineWidth += actualItemHorizontalSpacing + actualChildWidth
                    // 行高为一行中所有子 View 最高的那一个
                    lineHeight = max(lineHeight, actualChildHeight)
//                    lineViews.add(child)
                } else {
                    // 在本行不可以放置一个子 View，需要换行
                    if (lineCount == maxLines && mode == MODE_LIMIT_MAX_LINE) {
                        break
                    }
                    maxLineWidth = max(lineWidth, maxLineWidth)
                    lineCount++
                    totalHeight += lineHeight + if (lineCount == 1) 0 else itemVerticalSpacing
                    lineHeights.add(lineHeight)
//                    allLineViews.add(lineViews)
                    lineWidth = actualChildWidth
                    lineHeight = actualChildHeight
//                    lineViews = ArrayList<View>()
//                    lineViews.add(child)
                }
                measuredChildCount++
            }

            if (i == childCount - 1 || (measuredChildCount == maxCount && mode == MODE_LIMIT_MAX_COUNT)) {
                if (lineCount == maxLines && mode == MODE_LIMIT_MAX_LINE) {
                    break
                }
                maxLineWidth = max(lineWidth, maxLineWidth)
                lineCount++
                totalHeight += lineHeight + if (lineCount == 1) 0 else itemVerticalSpacing
                lineHeights.add(lineHeight)
//                allLineViews.add(lineViews)
                if (measuredChildCount == maxCount && mode == MODE_LIMIT_MAX_COUNT) {
                    break
                }
            }
        }
        maxLineWidth += getPaddingLeft() + getPaddingRight()
        totalHeight += getPaddingTop() + getPaddingBottom()
        Log.d(TAG, "onMeasure: lineCount=$lineCount")
        val measuredWidth = if (widthMode == MeasureSpec.EXACTLY) widthSize else maxLineWidth
        val measuredHeight = when(heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> Math.min(heightSize, totalHeight)
            else -> totalHeight
        }
        Log.d(TAG, "onMeasure: measuredWidth=$measuredWidth, measuredHeight=$measuredHeight")
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        // 子元素的左上角横坐标
        var childLeft = getPaddingLeft()
        // 子元素的左上角纵坐标
        var childTop = getPaddingTop()
        val childCount = getChildCount()
        // 行数索引
        var lineIndex = 0
        var layoutChildCount = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                val lp = child.layoutParams as MarginLayoutParams
                childLeft += lp.leftMargin
                val childMeasuredWidth = child.getMeasuredWidth()
                val childMeasuredHeight = child.getMeasuredHeight()
                if (childLeft + childMeasuredWidth > getMeasuredWidth()) {
                    // 需要换行了
                    // 更新 childTop，作为下一行子元素的左上角纵坐标
                    childTop += lineHeights[lineIndex] + itemVerticalSpacing
                    // 更新 childLeft，作为下一行子元素的左上角横坐标
                    childLeft = getPaddingLeft()
                    lineIndex++
                }
                if (lineIndex + 1 > maxLines && mode == MODE_LIMIT_MAX_LINE) {
                    child.layout(0,0,0,0)
                } else if (layoutChildCount >= maxCount && mode == MODE_LIMIT_MAX_COUNT) {
                    child.layout(0,0,0,0)
                } else {
                    val offsetTop = getOffsetTop(lineHeights[lineIndex], child)
                    // 确定子元素的位置
                    child.layout(
                        childLeft, childTop + lp.topMargin + offsetTop, childLeft + childMeasuredWidth,
                        childTop + lp.topMargin + offsetTop + childMeasuredHeight
                    )
                    layoutChildCount++
                    // 更新 childLeft，作为该行下一个子元素的左上角横坐标
                    childLeft += childMeasuredWidth + lp.rightMargin + itemHorizontalSpacing
                }
            }
        }
    }

    private fun getOffsetTop(lineHeight: Int, child: View): Int {
        val lp = child.layoutParams as MarginLayoutParams
        val childMeasuredHeight = child.getMeasuredHeight()
        val childMeasuredHeightWithMargin = childMeasuredHeight + lp.topMargin + lp.bottomMargin
        return when (lineVerticalGravity) {
            LINE_VERTICAL_GRAVITY_TOP -> 0
            LINE_VERTICAL_GRAVITY_CENTER_VERTICAL -> (lineHeight - childMeasuredHeightWithMargin) / 2
            LINE_VERTICAL_GRAVITY_BOTTOM -> lineHeight - childMeasuredHeightWithMargin
            else -> {
                throw IllegalArgumentException("unknown lineVerticalGravity value: $lineVerticalGravity")
            }
        }
    }

    // 当通过 addView(View) 方法添加子元素，并且子元素没有设置布局参数时，会调用此方法来生成默认的布局参数
    // 这里重写返回 MarginLayoutParams 对象，是为了在获取子元素的 LayoutParams 对象时，可以正常强转为 MarginLayoutParams
    override fun generateDefaultLayoutParams(): LayoutParams {
        return MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    // 检查传入的布局参数是否符合某个条件
    override fun checkLayoutParams(p: LayoutParams?): Boolean {
        return p is MarginLayoutParams
    }

    // addViewInner 中调用，但是布局参数类型无法通过 checkLayoutParams() 判断时，会走这个方法。
    override fun generateLayoutParams(p: LayoutParams?): LayoutParams {
        return MarginLayoutParams(p)
    }

    // 当通过 xml 添加时，会走这个方法获取子 View 的布局参数
    // 但是，默认的实现只会从 AttributeSet 里解析 layout_width 和 layout_height 这两个属性
    // 这里重写的目的是解析 margin 属性。
    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(getContext(), attrs)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState =  super.onSaveInstanceState()
        val ss = SavedState(superState)
        ss.maxCount = maxCount
        Log.d(TAG, "onSaveInstanceState: maxCount=$maxCount")
        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        maxCount = ss.maxCount
        Log.d(TAG, "onRestoreInstanceState: maxCount=$maxCount")
    }

    class SavedState : BaseSavedState {
        var maxCount = Int.MAX_VALUE

        constructor(superState: Parcelable?): super(superState)

        constructor(parcel: Parcel) : super(parcel) {
            maxCount = parcel.readInt()
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel.writeInt(maxCount)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }

    }

    companion object {
        private const val TAG = "FlowLayout"
        const val LINE_VERTICAL_GRAVITY_TOP = 0
        const val LINE_VERTICAL_GRAVITY_CENTER_VERTICAL = 1
        const val LINE_VERTICAL_GRAVITY_BOTTOM = 2

        const val MODE_LIMIT_MAX_LINE = 0
        const val MODE_LIMIT_MAX_COUNT = 1
    }
}