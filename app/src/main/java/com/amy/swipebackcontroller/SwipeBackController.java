package com.amy.swipebackcontroller;

import android.animation.ArgbEvaluator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class SwipeBackController {

    public interface HandleXListener {
        /**
         * x是当前手指在屏幕中的位置,distanceX是手指移动过的位置,fractionX是x与屏幕的比例.<br/>
         * <p/>
         * X is the position of the current finger in the screen,
         * distanceX is the position where the finger has moved,
         * and fractionX is the ratio of x to the screen.<br/>
         *
         * @param x
         * @param distanceX
         * @param fractionX
         */
        void handleX(float x, float distanceX, float fractionX);
    }

    private Activity mActivity;

    /**
     * 是否将contentView的背景设置为截图
     * <p/>
     * as if set contentView background to screenShot.
     */
    private boolean isScreenShotBackground = false;
    /**
     * 设置 activity back动画的时间<br/>
     * <p/>
     * Set the time for the activity out animation<br/>
     */
    private int mAnimationBackDuration = 300;
    /**
     * 设置 activity out 动画的时间<br/>
     * <p/>
     * Set the time for the activity out animation<br/>
     */
    private int mAnimationOutDuration = 300;

    /**
     * 默认开始滑动的位置距离屏幕左边缘的距离<br/>
     * <p/>
     * Default The distance to start sliding from the left edge of the screen
     */
    private int mDefaultTouchThreshold = 60;

    private final List<HandleXListener> mHandleXListeners = new ArrayList<HandleXListener>();
    private final HandleXListener mTransXListener = new HandleXListener() {
        @Override
        public void handleX(float x, float distanceX, float fractionX) {
            handleView(x);
        }
    };
    private final HandleXListener mAlphaListener = new HandleXListener() {
        @Override
        public void handleX(float x, float distanceX, float fractionX) {
            if (isScreenShotBackground) {
                handleBackgroundAlpha(fractionX);
            } else {
                handleBackgroundColor(fractionX);
            }
        }
    };

    private Interpolator mOutAnimInterpolator = new DecelerateInterpolator();
    private Interpolator mBackAnimInterpolator = new DecelerateInterpolator();

    private int mScreenWidth;
    private int mScreenHeight;
    private int mTouchSlop;

    private int mScreenWidthThreshold;
    private int mScreenHeightThreshold;

    private boolean isMoving = false;
    private float mX;
    private float mInitX;
    private float mDistanceX;
    private float mFractionX;
    private float mY;
    private float mInitY;
    private float mFractionY;
    private float mDistanceY;

    /**
     * 窗口根布局<br/>
     * <p/>
     * window rootView decorView<br/>
     */
    private ViewGroup decorView;

    /**
     * content布局<br/>
     * <p/>
     * contentView<br/>
     */
    private FrameLayout contentView;

    /**
     * 用户添加的布局<br/>
     * <p/>
     * User setContentView(layout)<br/>
     */
    private ViewGroup userView;

    private final ArgbEvaluator mColorEvaluator = new ArgbEvaluator();
    private int mEvaluatorStart = Color.parseColor("#dd000000");
    private int mEvaluatorEnd = Color.parseColor("#00000000");

    private ValueAnimator mBackAnimator;
    private ValueAnimator mOutAnimator;

    private PropertyValuesHolder mXHolder;
    private PropertyValuesHolder mYHolder;
    private PropertyValuesHolder mDistanceXHolder;
    private PropertyValuesHolder mDistanceYHolder;
    private PropertyValuesHolder mFractionXHolder;
    private PropertyValuesHolder mFractionYHolder;

    private final String mXHolderName = "xHolder";
    private final String mYHolderName = "yHolder";
    private final String mDistanceXHolderName = "distanceXHolder";
    private final String mDistanceYHolderName = "distanceYHolder";
    private final String mFractionXHolderName = "fractionXHolder";
    private final String mFractionYHolderName = "fractionYHolder";

    private Drawable mDecorBackgroundDrawable = new ColorDrawable(Color.parseColor("#00ffffff"));

    private SwipeBackController(final Activity activity) {

        mActivity = activity;

        //Screen params
        mScreenWidth = activity.getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = activity.getResources().getDisplayMetrics().heightPixels;
        mTouchSlop = ViewConfiguration.get(activity).getScaledTouchSlop();

        //DecorView
        decorView = (ViewGroup) activity.getWindow().getDecorView();

        //Content and then getUserView
        contentView = (FrameLayout) activity.findViewById(android.R.id.content);
        userView = (ViewGroup) contentView.getChildAt(0);
        setScreenWidthThreshold(mScreenWidth / 2);
        initDefaultListeners();
    }

    private void createBackAnimator(
            float xStart,
            float xEnd,
            float distanceXStart,
            float distanceXEnd,
            float fractionXStart,
            float fractionXEnd) {
        mBackAnimator = new ValueAnimator();
        mXHolder = PropertyValuesHolder.ofFloat(mXHolderName, xStart, xEnd);
        mDistanceXHolder = PropertyValuesHolder.ofFloat(mDistanceXHolderName, distanceXStart, distanceXEnd);
        mFractionXHolder = PropertyValuesHolder.ofFloat(mFractionXHolderName, fractionXStart, fractionXEnd);

        mBackAnimator = ValueAnimator.ofPropertyValuesHolder(
                mXHolder,
                //        mYHolder,
                mDistanceXHolder,
                //        mDistanceYHolder
                mFractionXHolder
                //        mFractionYHolder
        );
        mBackAnimator.setDuration(mAnimationBackDuration);
        mBackAnimator.setInterpolator(mBackAnimInterpolator);
        mBackAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float x = (float) valueAnimator.getAnimatedValue(mXHolderName);
                float distanceX = (float) valueAnimator.getAnimatedValue(mDistanceXHolderName);
                float fractionX = (float) valueAnimator.getAnimatedValue(mFractionXHolderName);

                handleListeners(x, distanceX, fractionX);
            }
        });
    }

    private void createOutAnimator(
            float xStart,
            float xEnd,
            float distanceXStart,
            float distanceXEnd,
            float fractionXStart,
            float fractionXEnd) {
        mOutAnimator = new ValueAnimator();
        mXHolder = PropertyValuesHolder.ofFloat(mXHolderName, xStart, xEnd);
        mDistanceXHolder = PropertyValuesHolder.ofFloat(mDistanceXHolderName, distanceXStart, distanceXEnd);
        mFractionXHolder = PropertyValuesHolder.ofFloat(mFractionXHolderName, fractionXStart, fractionXEnd);
        mOutAnimator = ValueAnimator.ofPropertyValuesHolder(
                mXHolder,
                //mYHolder,
                mDistanceXHolder,
                //mDistanceYHolder,
                mFractionXHolder
                //mFractionYHolder
        );
        mOutAnimator.setDuration(mAnimationOutDuration);
        mOutAnimator.setInterpolator(mOutAnimInterpolator);
        mOutAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float x = (float) valueAnimator.getAnimatedValue(mXHolderName);
                float distanceX = (float) valueAnimator.getAnimatedValue(mDistanceXHolderName);
                float fractionX = (float) valueAnimator.getAnimatedValue(mFractionXHolderName);

                handleListeners(x, distanceX, fractionX);
            }
        });
    }

    /**
     * 默认接口可控制x右平移距离，alpha值。
     * <p/>
     * Default Listener is TransLateX and Alpha
     */
    private void initDefaultListeners() {

        //View TranslationX listener
        addHandleXListener(mTransXListener);

        //View BackgroundColor listener
        addHandleXListener(mAlphaListener);

    }

    /**
     * 执行所有回调方法
     * <p/>
     * run all listener callback
     *
     * @param x
     * @param distanceX
     * @param fractionX
     */
    private void handleListeners(float x, float distanceX, float fractionX) {
        for (HandleXListener xListener : mHandleXListeners) {
            xListener.handleX(x, distanceX, fractionX);
        }
    }

    /**
     * 控制userView的X位置<br/>
     * <p/>
     * The X position of the userView is controlled<br/>
     *
     * @param x
     */
    private void handleView(float x) {
        userView.setTranslationX(x);
        if (x >= mScreenWidth - 1) {
            mActivity.finish();
        }
    }

    /**
     * 控制背景透明度。<br/>
     * <p/>
     * Control background transparency.<br/>
     *
     * @param fractionX
     */
    private void handleBackgroundAlpha(float fractionX) {
        if (decorView.getChildCount() == 1) {
            handleBackgroundColor(fractionX);
        } else {
            decorView.getChildAt(0).setAlpha(fractionX);
        }
    }

    /**
     * 控制背景颜色和透明度。<br/>
     * <p/>
     * Controls background color and transparency.<br/>
     *
     * @param fractionX
     */
    private void handleBackgroundColor(float fractionX) {
        int colorValue = (int) mColorEvaluator.evaluate(
                fractionX,
                mEvaluatorStart,
                mEvaluatorEnd);

        contentView.setBackgroundColor(colorValue);
    }

    private void updateTouchParams(MotionEvent event) {
        mX = event.getRawX();
        mY = event.getRawY();
        mFractionX = mX / mScreenWidth;
        mFractionY = mY / mScreenHeight;
        mDistanceX = mX - mInitX;
        mDistanceY = mY - mInitY;
    }

    /************************************      API      ************************************/

    /**
     * 是否要将contentView的背景设置为一张截图，如果是，截图会造成内存损耗，而且设置alpha会对性能产生影响;
     * 如果不是，需要将activity 的 theme 加上 windowIsTranslucent = true, 但这样做会让你的 activity 转场动画失效, 但是对性能影响较小。
     * 用 {@link #setDecorBackgroundDrawable(Drawable)}去设置你的背景,这个背景图将最为你的decorView的一个子View.
     * 如果将param 设为true这个选项，你也需要将你的activity theme中的所有 open animation exit animation 等设为@null。
     * <br/>
     * 最后，跟你的产品经理撕逼去吧。
     * <p/>
     * whether to set background of contentView as a screen shoot, if true,
     * that screenShot will cause large memory cost, and also when you change alpha will make this significant performance problem
     * {@link android.view.View#setAlpha} , if false , you need to add a theme item like this windowIsTranslucent = true, but this will make your
     * activity enter or exit anim useless, but not have performance problem，this background will as a child view to your decor view.
     * <br/>
     * At the last , Go and fight with your PM.
     * use {@link #setDecorBackgroundDrawable(Drawable)}to set your decor background
     *
     * @param screenShotBackground - 默认值是false , default value is false.
     */
    public SwipeBackController enableScreenShotBackground(boolean screenShotBackground) {
        isScreenShotBackground = screenShotBackground;
        return this;
    }

    /**
     * 设置activity 退出动画的插值器
     * <p/>
     * set activity finish animator interpolator.
     *
     * @param outAnimInterpolator
     * @return
     */
    public SwipeBackController setOutAnimInterpolator(Interpolator outAnimInterpolator) {
        mOutAnimInterpolator = outAnimInterpolator;
        return this;
    }

    /**
     * 设置activity回弹的interpolator
     * <p/>
     * set activity swipe back interpolator
     *
     * @param backAnimInterpolator
     * @return
     */
    public SwipeBackController setBackAnimInterpolator(Interpolator backAnimInterpolator) {
        mBackAnimInterpolator = backAnimInterpolator;
        return this;
    }

    /**
     * 设置距离屏幕左边缘的像素值为触发右滑退出的阀值。
     * 默认值是屏幕宽度的一半。
     * <p>
     * Setting the pixel value to the left edge of
     * the screen is the threshold for triggering the right-slide exit.
     * default value is screenWidth/2
     *
     * @param screenWidthThreshold
     */
    public SwipeBackController setScreenWidthThreshold(int screenWidthThreshold) {
        if (mScreenWidthThreshold > mScreenWidth || mScreenWidthThreshold < 0) {
            throw new IllegalArgumentException("Your threshold is fucked, put a value  0 < value < screenWidth ");
        }
        mScreenWidthThreshold = screenWidthThreshold;
        return this;
    }

    /**
     * 设置触发右滑的阀值, 参数为距离左边缘的距离
     * <p/>
     * Sets the threshold of the distance from the left edge for triggering the right skid,
     *
     * @param defaultTouchThreshold
     * @return
     */
    public SwipeBackController setDefaultTouchThreshold(int defaultTouchThreshold) {
        if (mDefaultTouchThreshold > mScreenWidth || mDefaultTouchThreshold < 0) {
            throw new IllegalArgumentException("Your threshold is fucked, put a value  0 < value < screenWidth ");
        }
        mDefaultTouchThreshold = defaultTouchThreshold;
        return this;
    }

    /**
     * 设置evaluator的startColorId
     * <p/>
     * set evaluator start color<br/>
     * evaluatorStartColor = R.color.*<br/>
     * default is #dd000000<br/>
     *
     * @param evaluatorStartColor
     */
    public SwipeBackController setEvaluatorStart(int evaluatorStartColor) {
        mEvaluatorStart = mActivity.getResources().getColor(evaluatorStartColor);
        return this;
    }

    /**
     * 设置evaluator的endColorId
     * <p/>
     * set evaluator start color<br/>
     * evaluatorEndColor = R.color.*<br/>
     * default is #00000000<br/>
     *
     * @param evaluatorEndColor
     */
    public SwipeBackController setEvaluatorEnd(int evaluatorEndColor) {
        mEvaluatorEnd = mActivity.getResources().getColor(evaluatorEndColor);
        return this;
    }

    /**
     * 可在你的activity 中添加如下代码<br/>
     * Add the following code to your activity:<br/>
     * <p/>
     * <pre>
     * public boolean onTouchEvent(MotionEvent ev) {
     *      if (swipeBackController.processEvent(ev)) {
     *           return true;
     *      } else {
     *           return super.onTouchEvent(ev);
     *      }
     * }
     * </pre>
     *
     * @param event
     * @return
     */
    public boolean processEvent(MotionEvent event) {

        if (mOutAnimator != null && mOutAnimator.isRunning()
                || mBackAnimator != null && mBackAnimator.isRunning()) {
            return true;
        }

        int pointId = -1;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mInitX = event.getRawX();
                mInitY = event.getRawY();
                mX = event.getRawX();
                mY = event.getRawY();
                pointId = event.getPointerId(0);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                updateTouchParams(event);

                if (!isMoving) {
                    float dx = Math.abs(mDistanceX);
                    float dy = Math.abs(mDistanceY);
                    if (dx > mTouchSlop && dx > dy && mInitX < mDefaultTouchThreshold) {
                        isMoving = true;
                    }
                } else {
                    handleListeners(mX, mDistanceX, mFractionX);
                }

                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                updateTouchParams(event);

                if (isMoving && Math.abs(userView.getTranslationX()) >= 0) {
                    if (mDistanceX >= mScreenWidthThreshold) {
                        createOutAnimator(
                                mX,
                                mScreenWidth,
                                mDistanceX,
                                mDistanceX + mScreenWidth - mX,
                                mFractionX,
                                1f
                        );
                        mOutAnimator.start();
                    } else {
                        createBackAnimator(
                                mX,
                                0f,
                                mDistanceX,
                                0f,
                                mFractionX,
                                0f
                        );
                        mBackAnimator.start();
                    }
                    isMoving = false;
                }

                mInitX = 0;
                mInitY = 0;

                break;
            }
        }
        return true;
    }

    /**
     * 添加自定义的handleX接口 默认有两个口 {@link #handleView(float)} ()}  .<br/>
     * <p/>
     * Add a custom handleX interface.<br/>
     *
     * @param xListener
     */
    public void addHandleXListener(HandleXListener xListener) {
        if (xListener == null) {
            throw new NullPointerException("the xListener is null");
        } else {
            mHandleXListeners.add(xListener);
        }
    }

    /**
     * 清除所有除了左平移，alpha变化的listeners.<br/>
     * <p/>
     * Except for all the listeners except the left shift, the alpha change.<br/>
     *
     * @see #mTransXListener
     * @see #mAlphaListener
     */
    public void clearHandleXListener() {
        removeHandleXListener(mTransXListener);
        removeHandleXListener(mAlphaListener);
    }

    /**
     * 移除自定义的handleX接口<br/>
     * <p/>
     * Remove a custom handleX interface.<br/>
     *
     * @param xListener
     */
    public void removeHandleXListener(HandleXListener xListener) {
        if (mHandleXListeners.contains(xListener)) {
            mHandleXListeners.remove(xListener);
        } else {
            throw new IllegalArgumentException("The listener you remove not exists");
        }
    }

    /**
     * 设置窗口根布局的colorId
     * <p/>
     * set DecorViewBackground color<br/>
     * colorId = R.color.*<br/>
     * default is #00ffffff<br/>
     *
     * @param colorId
     */
    public SwipeBackController setDecorBackgroundColor(int colorId) {
        mDecorBackgroundDrawable = new ColorDrawable(mActivity.getResources().getColor(colorId));
        return this;
    }

    /**
     * 设置窗口根布局的背景图片，这张图将以子view的模式加到decorView中
     * <p/>
     * Sets the background image of the decorView, this drawable will be a child ImageView of decorView.
     *
     * @param decorBackgroundDrawable
     */
    public SwipeBackController setDecorBackgroundDrawable(Drawable decorBackgroundDrawable) {
        mDecorBackgroundDrawable = decorBackgroundDrawable;
        final ImageView bgImageView = new ImageView(mActivity);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        decorView.addView(bgImageView, 0, params);
        bgImageView.setBackground(mDecorBackgroundDrawable);
        return this;
    }

    /**
     * 设置 activity back 动画的时间<br/>
     * <p/>
     * Sets the time for the activity back animation<br/>
     * defaultDuration = 300<br/>
     *
     * @param animationDuration
     * @return
     */
    public SwipeBackController setAnimationBackDuration(int animationDuration) {
        mAnimationBackDuration = animationDuration;
        return this;
    }

    /**
     * 设置 activity out 动画的时间<br/>
     * <p/>
     * Sets the time for the activity out animation<br/>
     * defaultDuration = 300<br/>
     *
     * @param animationDuration
     * @return
     */
    public SwipeBackController setAnimationOutDuration(int animationDuration) {
        mAnimationOutDuration = animationDuration;
        return this;
    }

    /**
     * build 一个新 SwipeBackController对象，这里设置你要设置的activity.<br/>
     * <p/>
     * Build a new SwipeBackController, here you want to set the activity.<br/>
     *
     * @param activity
     * @return
     */
    public static SwipeBackController build(final Activity activity) {
        return new SwipeBackController(activity);
    }

}