package yanzhikai.bar;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
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
    private int mMaskHeight = 30;
    private int mBlockWidth = 220;
    private int mBarHeight = 15;
    private int positionX = 0;
    private int mMidTransitionX = 0;
    private StateBlock mStateBlock1, mStateBlock2, mMidBlock;
    private @State int mCurrentState;
    private ValueAnimator mSpeakingAnimator,mThinkingAnimator,mListeningAnimator,mColorAnimator,mAlphaOutAnimator,mAlphaInAnimator;
    private ValueAnimator mCurrentStateAnimator;
    private int mIncrease = 10;

    public static final int IDLE = 0 , LISTENING = 1, LISTENING_ACTIVE = 2,THINKING = 3,SPEAKING = 4, MIC_OFF = 5, SYSTEM_ERROR = 6;
    @IntDef({IDLE, LISTENING, LISTENING_ACTIVE,THINKING,SPEAKING,MIC_OFF,SYSTEM_ERROR})
    @Retention(RetentionPolicy.SOURCE)
    public  @interface State {}

    private final @ColorInt int COLOR_BLOCK = getResources().getColor(R.color.colorLightBlue);
    private final @ColorInt int COLOR_LISTENING = getResources().getColor(R.color.colorDeepBlue);
    private final @ColorInt int COLOR_MIC_OFF = getResources().getColor(R.color.colorKhaki);
    private final @ColorInt int COLOR_SYS_ERROR = getResources().getColor(R.color.colorRed);
    private @ColorInt int mStartColor,mEndColor;


    private
    @ColorInt
    int mBlockColor = COLOR_BLOCK;
    private
    @ColorInt
    int mBackGroundColor = COLOR_LISTENING;
    private Paint mMaskPaint, mBackGroundPaint;

    public StateBar(Context context) {
        super(context);
        init(context);
    }

    public StateBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StateBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
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
        mMidBlock.setVisibility(GONE);
        initPaint();
        setWillNotDraw(false);

        positionX = -mBlockWidth;


//        initAnimators();
    }

    private void initAnimators() {
        mListeningAnimator = ValueAnimator.ofInt(0,mMidTransitionX);
        mListeningAnimator.setInterpolator(new LinearInterpolator());
        mListeningAnimator.setDuration(400);
        mListeningAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int tx = (int) animation.getAnimatedValue();
                Log.d(TAG, "onAnimationUpdate: tx" + tx);
                mStateBlock1.setTranslationX(tx);
                mStateBlock2.setTranslationX(-tx);
                postInvalidate();
            }
        });
        mListeningAnimator.addListener(new AbstractAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mStateBlock2.setVisibility(GONE);
            }
        });

        mThinkingAnimator = ValueAnimator.ofInt(0,getWidth() + mBlockWidth / 4 - mBlockWidth);
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
                mStateBlock1.setVisibility(GONE);
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
        mAlphaInAnimator.setDuration(1000);
        mAlphaInAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mBackGroundPaint.setAlpha((Integer) animation.getAnimatedValue());
            }
        });
        mAlphaInAnimator.addListener(new AbstractAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }
        });

        mAlphaOutAnimator = ValueAnimator.ofInt(255, 0);
        mAlphaOutAnimator.setDuration(1000);
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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        mMaskHeight = MeasureSpec.getSize(heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mMaskHeight = h;
        mMidTransitionX = (getWidth() - mBlockWidth) / 2 - positionX;
        initAnimators();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d(TAG, "onLayout: ");
        mStateBlock1.layout(positionX - l, 0, positionX - l + mBlockWidth, mMaskHeight);
        mStateBlock2.layout(r - positionX - mBlockWidth, 0, r - positionX, mMaskHeight);
        mMidBlock.layout((r - l - mBlockWidth) / 2, 0,(r - l + mBlockWidth) / 2, mMaskHeight );
    }

    private void initPaint() {
        mMaskPaint = new Paint();
        mMaskPaint.setColor(mBackGroundColor);
        mMaskPaint.setMaskFilter(new BlurMaskFilter(mMaskHeight , BlurMaskFilter.Blur.NORMAL));

        mBackGroundPaint = new Paint();
        mBackGroundPaint.setColor(mBackGroundColor);
        mBackGroundPaint.setStyle(Paint.Style.FILL);
        mBackGroundPaint.setMaskFilter(new BlurMaskFilter(mMaskHeight , BlurMaskFilter.Blur.SOLID));
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
                ,mMaskHeight);
    }

    public void startSpeak(){
        mMidBlock.setVisibility(GONE);
        mCurrentStateAnimator.cancel();
        mCurrentStateAnimator = mSpeakingAnimator;
        mCurrentStateAnimator.start();

        mCurrentState = SPEAKING;
    }

    public void startListen() {
        mStateBlock1.setVisibility(VISIBLE);
        mStateBlock2.setVisibility(VISIBLE);
        mStateBlock1.setTranslationX(0);
        mStateBlock2.setTranslationX(0);
        mMidBlock.setVisibility(GONE);
        mCurrentStateAnimator = mListeningAnimator;
        mCurrentStateAnimator.start();
        mCurrentState = LISTENING;
    }



    public void startActive(){
        showMidBlock();
        mMidBlock.setIsMasked(true);

        mCurrentState = LISTENING_ACTIVE;

        mIncrease += 10;
        blockIncrease();
    }

    public void startThink(){
        showMidBlock();
        mMidBlock.setIsMasked(false);
        mCurrentStateAnimator = mThinkingAnimator;
        mCurrentStateAnimator.start();

        mCurrentState = THINKING;
    }

    public void startSysError(){
        hideAll();
        mCurrentStateAnimator.cancel();
        mStartColor = mBackGroundPaint.getColor();
        mEndColor = COLOR_SYS_ERROR;
        mCurrentStateAnimator = mColorAnimator;
        mCurrentStateAnimator.start();

    }

    public void startMicOff(){
        hideAll();
        mCurrentStateAnimator.cancel();
        mStartColor = mBackGroundPaint.getColor();
        mEndColor = COLOR_MIC_OFF;
        mCurrentStateAnimator = mColorAnimator;
        mCurrentStateAnimator.start();
    }

    public void startIdle(){
        mCurrentStateAnimator.cancel();
        mCurrentStateAnimator = mAlphaOutAnimator;
        mCurrentStateAnimator.start();
    }

    public void nextState(){
        mThinkingAnimator.setRepeatCount(0);
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

    public void setIncrease(int increase) {
        this.mIncrease = increase;
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
