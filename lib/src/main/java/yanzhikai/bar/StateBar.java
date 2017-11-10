package yanzhikai.bar;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by yany on 2017/11/6.
 */

public class StateBar extends ViewGroup {
    public static final String TAG = "StateBar";
    private Context mContext;
    //Bar的模糊半径
    private int mMaskHeight = 30;
    //中间蓝色块的宽度
    private int mBlockWidth = 220;
    //Bar的主要部分高度
    private int mBarHeight = 15;
    //用于控制蓝色块的属性
    private int positionX = 0, mMidTransitionX = 0;
    //蓝色块
    private StateBlock mStateBlock1, mStateBlock2, mMidBlock;
    //当前状态
    private @State int mCurrentState;
    //一些动画
    private ValueAnimator mSpeakingAnimator,mThinkingAnimator,mListeningAnimator,mColorAnimator,mAlphaOutAnimator,mAlphaInAnimator;
    //控制Active状态时候的属性
    private int mIncrease = 10;
    //状态回调
    private StateListener mStateListener;

    //状态定义
    public static final int IDLE = 0 , LISTENING = 1, LISTENING_ACTIVE = 2,THINKING = 3,SPEAKING = 4, MIC_OFF = 5, SYSTEM_ERROR = 6;
    @IntDef({IDLE, LISTENING, LISTENING_ACTIVE,THINKING,SPEAKING,MIC_OFF,SYSTEM_ERROR})
    @Retention(RetentionPolicy.SOURCE)
    public  @interface State {}

    //颜色
    private final @ColorInt int COLOR_BLOCK = getResources().getColor(R.color.colorLightBlue);
    private final @ColorInt int COLOR_LISTENING = getResources().getColor(R.color.colorDeepBlue);
    private final @ColorInt int COLOR_MIC_OFF = getResources().getColor(R.color.colorKhaki);
    private final @ColorInt int COLOR_SYS_ERROR = getResources().getColor(R.color.colorRed);
    private @ColorInt int mStartColor,mEndColor;

    private @ColorInt int mBlockColor = COLOR_BLOCK;
    private @ColorInt int mBackGroundColor = COLOR_LISTENING;

    //画笔，mMaskPaint暂不使用
    private Paint mMaskPaint, mBackGroundPaint;

    public StateBar(Context context) {
        super(context);
        init(context);
    }

    public StateBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context,attrs);
        init(context);
    }

    public StateBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void initAttrs(Context context, AttributeSet attrs){
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.StateBar, 0, 0);
        mMaskHeight = typedArray.getDimensionPixelSize(R.styleable.StateBar_maskHeight,mMaskHeight);
        mBlockWidth = typedArray.getDimensionPixelSize(R.styleable.StateBar_blockWidth,mBlockWidth);
        mBarHeight = typedArray.getDimensionPixelSize(R.styleable.StateBar_barHeight,mBarHeight);
    }

    private void init(Context context) {
        mContext = context;
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        mStateBlock1 = new StateBlock(mContext, this);
        mStateBlock2 = new StateBlock(mContext, this);
        mMidBlock = new StateBlock(mContext,this);
        addView(mStateBlock1);
        addView(mStateBlock2);
        addView(mMidBlock);

        hideAll();

        setWillNotDraw(false);

        positionX = -mBlockWidth;

        initPaint();
    }

    private void initAnimators() {
        mListeningAnimator = ValueAnimator.ofInt(0,mMidTransitionX);
        mListeningAnimator.setInterpolator(new LinearInterpolator());
        mListeningAnimator.setDuration(400);
        mListeningAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int tx = (int) animation.getAnimatedValue();
                mStateBlock1.setTranslationX(tx);
                mStateBlock2.setTranslationX(-tx);
                postInvalidate();
            }
        });
        mListeningAnimator.addListener(new AbstractAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                startActive();
            }
        });

        mThinkingAnimator = ValueAnimator.ofInt(0,(getWidth() + mBlockWidth / 4) / 2);
        mThinkingAnimator.setDuration(1000);
        mThinkingAnimator.setRepeatMode(ValueAnimator.RESTART);
        mThinkingAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mThinkingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mIncrease = (int) animation.getAnimatedValue();
                Log.d(TAG, "onAnimationUpdate: " + mIncrease);
                blockIncrease();
            }
        });
        mThinkingAnimator.addListener(new AbstractAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //属性isCancel请看AbstractAnimatorListener
                if (!isCancel) {
                    Log.d(TAG, "onAnimationEnd: asdasd");
                    mThinkingAnimator.setRepeatCount(ValueAnimator.INFINITE);
                    startSpeak();
                }else {
                    super.onAnimationEnd(animation);
                }
            }
        });


        mSpeakingAnimator = ValueAnimator.ofFloat(0,1);
        mSpeakingAnimator.setDuration(1000);
        mSpeakingAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mSpeakingAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mSpeakingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int color = (int) new ArgbEvaluator().evaluate(animation.getAnimatedFraction(),mBlockColor,mBackGroundColor);
                mBackGroundPaint.setColor(color);
                postInvalidate();
            }
        });
        mSpeakingAnimator.addListener(new AbstractAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }
        });

        mColorAnimator = ValueAnimator.ofFloat(0,1);
        mColorAnimator.setDuration(1000);
        mColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int color = (int) new ArgbEvaluator().evaluate(animation.getAnimatedFraction(),mStartColor,mEndColor);
                mBackGroundPaint.setColor(color);
                postInvalidate();
            }
        });

        mAlphaInAnimator = ValueAnimator.ofInt(0, 255);
        mAlphaInAnimator.setDuration(600);
        mAlphaInAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mBackGroundPaint.setAlpha((Integer) animation.getAnimatedValue());
            }
        });
        mAlphaInAnimator.addListener(new AbstractAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startListen();
            }
        });

        mAlphaOutAnimator = ValueAnimator.ofInt(255, 0);
        mAlphaOutAnimator.setDuration(600);
        mAlphaOutAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mBackGroundPaint.setAlpha((Integer) animation.getAnimatedValue());
                postInvalidate();
            }
        });
        mAlphaOutAnimator.addListener(new AbstractAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                hideAll();
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mMidTransitionX = (getWidth() - mBlockWidth) / 2 - positionX;

        initAnimators();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d(TAG, "onLayout: ");
        mStateBlock1.layout(positionX - l, 0, positionX - l + mBlockWidth, getHeight());
        mStateBlock2.layout(r - positionX - mBlockWidth, 0, r - positionX, getHeight());
        mMidBlock.layout((r - l - mBlockWidth) / 2, 0,(r - l + mBlockWidth) / 2, getHeight() );
    }

    private void initPaint() {
//        mMaskPaint = new Paint();
//        mMaskPaint.setColor(mBackGroundColor);
//        mMaskPaint.setMaskFilter(new BlurMaskFilter(mMaskHeight , BlurMaskFilter.Blur.NORMAL));

        mBackGroundPaint = new Paint();
        mBackGroundPaint.setColor(mBackGroundColor);
        mBackGroundPaint.setAlpha(0);
        mBackGroundPaint.setStyle(Paint.Style.FILL);
        mBackGroundPaint.setMaskFilter(new BlurMaskFilter(mMaskHeight, BlurMaskFilter.Blur.SOLID));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int bottom = canvas.getHeight();
        canvas.drawRect(0, bottom - mBarHeight, canvas.getWidth(), bottom, mBackGroundPaint);
//        canvas.drawRect(0, bottom - 10, canvas.getWidth(), bottom, mMaskPaint);

        Log.d(TAG, "onDraw: ");
    }

    private void blockIncrease(){
        mMidBlock.layout(
                (getWidth() - mBlockWidth) / 2 - mIncrease
                ,0
                ,(getWidth() + mBlockWidth) / 2 + mIncrease
                ,getHeight());
    }

    private void startIdle(){
        cancelAllAnimator();
        mAlphaOutAnimator.start();
        if (mStateListener != null){
            mStateListener.onStateChanged(mCurrentState,IDLE);
        }
        mCurrentState = IDLE;
    }

    private void startListen() {
        mStateBlock1.setVisibility(VISIBLE);
        mStateBlock2.setVisibility(VISIBLE);
        mStateBlock1.setTranslationX(0);
        mStateBlock2.setTranslationX(0);
        mMidBlock.setVisibility(GONE);
        mListeningAnimator.start();
        if (mStateListener != null){
            mStateListener.onStateChanged(mCurrentState,LISTENING);
        }
        mCurrentState = LISTENING;
    }



    private void startActive(){
        showMidBlock();
        mMidBlock.setIsMasked(true);

        if (mStateListener != null){
            mStateListener.onStateChanged(mCurrentState, LISTENING_ACTIVE);
        }
        mCurrentState = LISTENING_ACTIVE;

    }

    private void startThink(){
        showMidBlock();
        mMidBlock.setIsMasked(false);

        mThinkingAnimator.start();
        if (mStateListener != null){
            mStateListener.onStateChanged(mCurrentState,THINKING);
        }
        mCurrentState = THINKING;
    }

    private void startSpeak(){
        mMidBlock.setVisibility(GONE);
        mSpeakingAnimator.start();

        if (mStateListener != null){
            mStateListener.onStateChanged(mCurrentState,SPEAKING);
        }
        mCurrentState = SPEAKING;
    }

    /**
     * 进入SYSTEM_ERROR状态
     */
    public void startSysError(){
        cancelAllAnimator();
        hideAll();
        mStartColor = mBackGroundPaint.getColor();
        mEndColor = COLOR_SYS_ERROR;
        mColorAnimator.start();

        if (mStateListener != null){
            mStateListener.onStateChanged(mCurrentState,SYSTEM_ERROR);
        }
        mCurrentState = SYSTEM_ERROR;

    }

    /**
     * 进入MIC_OFF状态
     */
    public void startMicOff(){
        if (mCurrentState == IDLE || mCurrentState == LISTENING
                || mCurrentState == LISTENING_ACTIVE || mCurrentState == SYSTEM_ERROR) {
            cancelAllAnimator();
            hideAll();
            mStartColor = mBackGroundPaint.getColor();
            mEndColor = COLOR_MIC_OFF;
            mColorAnimator.start();

            if (mStateListener != null) {
                mStateListener.onStateChanged(mCurrentState,MIC_OFF);
            }
            mCurrentState = MIC_OFF;
        }
    }

    /**
     * 进入下一个状态
     */
    public void nextState(){
        switch (mCurrentState){
            case IDLE:
                cancelAllAnimator();
                reset();
                mAlphaInAnimator.start();
                break;
            case LISTENING:
                break;
            case LISTENING_ACTIVE:
                startThink();
                break;
            case THINKING:
                mThinkingAnimator.setRepeatCount(0);
                break;
            case SPEAKING:
                startIdle();
                break;
            case MIC_OFF:
                startIdle();
                break;
            case SYSTEM_ERROR:
                startIdle();
                break;
        }
    }

    private void hideAll(){
        mStateBlock2.setVisibility(GONE);
        mStateBlock1.setVisibility(GONE);
        mMidBlock.setVisibility(GONE);
    }

    private void showMidBlock(){
        mStateBlock2.setVisibility(GONE);
        mStateBlock1.setVisibility(GONE);
        mMidBlock.setVisibility(VISIBLE);
    }

    private void reset(){
        mBackGroundPaint.setColor(mBackGroundColor);
        hideAll();
    }

    private void cancelAllAnimator(){
        mAlphaInAnimator.cancel();
        mAlphaOutAnimator.cancel();
        mListeningAnimator.cancel();
        mThinkingAnimator.cancel();
        mSpeakingAnimator.cancel();
        mColorAnimator.cancel();
    }
    public void setIncrease(int increase) {
        if (mCurrentState == LISTENING_ACTIVE){
            this.mIncrease = increase;
            blockIncrease();
//            mIncrease = 0;
//            blockIncrease();
        }
    }

    public void setStateListener(StateListener stateListener) {
        this.mStateListener = stateListener;
    }

    public int getMaskHeight() {
        return mMaskHeight;
    }

    public int getBarHeight() {
        return mBarHeight;
    }

    public int getmBlockWidth() {
        return mBlockWidth;
    }
}
