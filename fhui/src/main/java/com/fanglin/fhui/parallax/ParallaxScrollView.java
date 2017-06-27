package com.fanglin.fhui.parallax;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.fanglin.fhui.R;

import java.util.ArrayList;

public class ParallaxScrollView extends ScrollView {

    private static final int DEFAULT_PARALLAX_VIEWS = 1;
    private static final float DEFAULT_INNER_PARALLAX_FACTOR = 1.9F;
    private static final float DEFAULT_PARALLAX_FACTOR = 1.9F;
    private int numOfParallaxViews = DEFAULT_PARALLAX_VIEWS;
    private float innerParallaxFactor = DEFAULT_PARALLAX_FACTOR;
    private float parallaxFactor = DEFAULT_PARALLAX_FACTOR;
    private ArrayList<ParallaxedView> parallaxedViews = new ArrayList<>();
    private ViewGroup mHeaderContainer;
    private View mColorLayer;
    private View mHeaderContent;
    private OnEnableStickyViewListener onEnableStickyViewListener;
    private int mColor;

    public ParallaxScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public ParallaxScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ParallaxScrollView(Context context) {
        super(context);
    }

    protected void init(Context context, AttributeSet attrs) {
        TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable.ParallaxScroll);
        this.parallaxFactor = typeArray.getFloat(R.styleable.ParallaxScroll_parallax_factor, DEFAULT_PARALLAX_FACTOR);
        this.innerParallaxFactor = typeArray.getFloat(R.styleable.ParallaxScroll_inner_parallax_factor, DEFAULT_INNER_PARALLAX_FACTOR);
        this.numOfParallaxViews = typeArray.getInt(R.styleable.ParallaxScroll_parallax_views_num, DEFAULT_PARALLAX_VIEWS);
        typeArray.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        makeViewsParallax();

        mHeaderContainer = (ViewGroup) findViewById(R.id.header_container);
        mColorLayer = new View(getContext());
        mColorLayer.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mHeaderContainer.addView(mColorLayer, 2);

        setHeaderContent(LayoutInflater.from(getContext()).inflate(R.layout.header_content, this, false));
        ViewGroup headerContentContainer = (ViewGroup) findViewById(R.id.header_content_container);
        headerContentContainer.addView(getHeaderContent());
    }

    private void makeViewsParallax() {
        if (getChildCount() > 0 && getChildAt(0) instanceof ViewGroup) {
            ViewGroup viewsHolder = (ViewGroup) ((ViewGroup) getChildAt(0)).getChildAt(0);
            int numOfParallaxViews = Math.min(this.numOfParallaxViews, viewsHolder.getChildCount());
            for (int i = 0; i < numOfParallaxViews; i++) {
                View child = viewsHolder.getChildAt(i);
                if (child.getId() == R.id.header_content_container) {
                    continue;
                }

                ParallaxedView parallaxedView = new ScrollViewParallaxedItem(child);
                parallaxedViews.add(parallaxedView);
            }
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);


        int headerHeight = mHeaderContainer.getHeight();
        headerHeight = headerHeight - getHeaderContent().getHeight();


        System.out.println("t = " + t);
        float factor = parallaxFactor;
        for (ParallaxedView parallaxedView : parallaxedViews) {
            parallaxedView.setOffset((float) t / factor);
            factor *= innerParallaxFactor;
        }

        t = t < 0 ? 0 : t;
        float p = (float) t / headerHeight;
        if (p > 1) {
            p = 1;
        }

        if (p == 1) {
            enableSticky(true);
        } else {
            enableSticky(false);
        }

        int i = (int) (0xff * p);
        System.out.println("i = " + i);
        int color = (i << 24) + getColor();
        mColorLayer.setBackgroundColor(color);
    }

    private int getColor() {
        return mColor & 0x00ffffff;
    }

    private void enableSticky(boolean b) {
        if (onEnableStickyViewListener != null) {
            onEnableStickyViewListener.onEnableStikyView(b);
        }
    }

    public OnEnableStickyViewListener getOnEnableStickyViewListener() {
        return onEnableStickyViewListener;
    }

    public void setOnEnableStickyViewListener(OnEnableStickyViewListener onEnableStickyViewListener) {
        this.onEnableStickyViewListener = onEnableStickyViewListener;
    }

    public void setColor(int c) {
        mColor = c;
    }

    public View getHeaderContent() {
        return mHeaderContent;
    }

    public void setHeaderContent(View headerContent) {
        mHeaderContent = headerContent;
    }

    protected class ScrollViewParallaxedItem extends ParallaxedView {

        public ScrollViewParallaxedItem(View view) {
            super(view);
        }

        @Override
        protected void translatePreICS(View view, float offset) {
            view.offsetTopAndBottom((int) offset - lastOffset);
            lastOffset = (int) offset;
        }
    }

    public interface OnEnableStickyViewListener {
        void onEnableStikyView(boolean enable);
    }
}
