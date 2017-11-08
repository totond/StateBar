package yanzhikai.bar;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
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
    private int mBlockWidth = 220, mBlockHeight = 15;
    private int mBarHeight = 15;
    private int positionX = 0;
    private int mMidTransitionX = 0;
    private StateBlock mStateBlock1, mStateBlock2;
    private @State int mCurrentState;
    private ValueAnimator mSpeakingAnimator,mThinkingAnimator,mListeningAnimator;
    private ValueAnimator mCurrentStateAnimator;
    private float mScale = 1;
    private int mIncrease = 10;

    public static final int NONE = 0 , LISTENING = 1, LISTENING_ACTIVE = 2,THINKING = 3,SPEAKING = 4, MIC_OFF = 5, SYSTEM_ERROR = 6;
    @IntDef({NONE, LISTENING, LISTENING_ACTIVE,THINKING,SPEAKING,MIC_OFF,SYSTEM_ERROR})
    @Retention(RetentionPolicy.SOURCE)
    public  @interface State {}

    private final @ColorInt int COLOR_BLOCK = getResources().getColor(R.color.colorLightBlue);
    private final @ColorInt int COLOR_LISTENING = getResources().getColor(R.color.colorDeepBlue);
    private final @ColorInt int COLOR_MIC_OFF = getResources().getColor(R.color.colorKhaki);
    private final @ColorInt int COLOR_SYS_ERROR = getResources().getColor(R.color.colorRed);


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
        addView(mStateBlock1);
        addView(mStateBlock2);
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

        mSpeakingAnimator = ValueAnimator.ofFloat(0,1);
        mSpeakingAnimator.setDuration(1000);
        mSpeakingAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mSpeakingAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mSpeakingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Log.d(TAG, "onAnimationUpdate: ");
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

        Log.d(TAG, "initAnimators: getWidth()" + (getWidth() + mBlockWidth / 4 - mBlockWidth));
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
                super.onAnimationEnd(animation);
                mThinkingAnimator.setRepeatCount(ValueAnimator.INFINITE);
                mSpeakingAnimator.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
//                if (mCurrentState != THINKING){
//                    mThinkingAnimator.cancel();
//                }
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
        mStateBlock1.layout(positionX, 0, positionX + mBlockWidth, mMaskHeight);
        mStateBlock2.layout(r - positionX - mBlockWidth, 0, r - positionX, mMaskHeight);

    }

    private void initPaint() {
        mMaskPaint = new Paint();
        mMaskPaint.setColor(mBackGroundColor);
        mMaskPaint.setMaskFilter(new BlurMaskFilter(mMaskHeight , BlurMaskFilter.Blur.NORMAL));


//        mBlockPaint = new Paint();
//        mBlockPaint.setColor(mBlockColor);

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
        mStateBlock1.layout(positionX - mIncrease, 0, positionX + mBlockWidth + mIncrease, mMaskHeight);
    }

    private void listenMove() {
//        if (positionX < getWidth() / 2){
//            positionX += 8;
//            requestLayout();
//        }
        float translationX = mStateBlock1.getTranslationX();
        if (translationX < mMidTransitionX) {
            translationX += 65;
            if (translationX > mMidTransitionX) {
                mStateBlock1.setTranslationX(mMidTransitionX);
                mStateBlock2.setTranslationX(-mMidTransitionX);
            } else {
                mStateBlock1.setTranslationX(translationX);
                mStateBlock2.setTranslationX(-translationX);
            }
            Log.d(TAG, "translationX: " + translationX);
            postInvalidate();
        }
    }

    public void setState(int nextState) {
        boolean shouldInterrupt = false;
        switch (nextState){
            case LISTENING:
            case LISTENING_ACTIVE:
            case THINKING:
            case SPEAKING:
                shouldInterrupt = false;
                break;
            case NONE:
            case MIC_OFF:
            case SYSTEM_ERROR:
                shouldInterrupt = true;
                break;

        }

        if (shouldInterrupt && mCurrentStateAnimator != null){
            mCurrentStateAnimator.cancel();
        }
        switch (mCurrentState){
            case NONE:

                break;
            case LISTENING:
                break;
            case LISTENING_ACTIVE:
                break;
            case THINKING:
                break;
            case SPEAKING:
                break;
            case MIC_OFF:
                break;
            case SYSTEM_ERROR:
                break;
        }
        this.mCurrentState = nextState;
    }

    public void startSpeak(){
        mSpeakingAnimator.start();
    }

    public void startListen() {
        mStateBlock1.setVisibility(VISIBLE);
        mStateBlock2.setVisibility(VISIBLE);
        mStateBlock1.setTranslationX(0);
        mStateBlock2.setTranslationX(0);
//        listenMove();
        mListeningAnimator.start();
    }

    public void startActive(){
//        if (mScale > 2.5){
//            mScale = 1;
//        }
        mStateBlock2.setVisibility(GONE);
        mStateBlock1.setIsMasked(false);
//        mScale += 0.2f;
//        mStateBlock1.setScaleX(mScale);

        mIncrease += 10;
        mStateBlock1.layout(positionX - mIncrease, 0, positionX + mBlockWidth + mIncrease, mMaskHeight);
        Log.d(TAG, "getWidth: " + mStateBlock1.getWidth());
    }

    public void startThink(){
        mStateBlock2.setVisibility(GONE);
        mStateBlock1.setIsMasked(false);
        mThinkingAnimator.start();
    }

    public void startMicOff(){
        mThinkingAnimator.setRepeatCount(0);
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
