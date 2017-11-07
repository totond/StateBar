package yanzhikai.bar;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;

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
    private ListenRunnable mListenRunnable;
    private @State int mState;
    private ValueAnimator mColorAnimator;

    public static final int NONE = 0 , LISTENING = 1, LISTENING_ACTIVE = 2, MIC_OFF = 3, SYSTEM_ERROR = 4;
    @IntDef({NONE, LISTENING, LISTENING_ACTIVE, MIC_OFF,SYSTEM_ERROR})
    @Retention(RetentionPolicy.SOURCE)
    public  @interface State {}


    private
    @ColorInt
    int mBlockColor = getResources().getColor(R.color.colorLightBlue);
    private
    @ColorInt
    int mBackGroundColor = getResources().getColor(R.color.colorDeepBlue);

    private Paint mMaskPaint, mBlockPaint, mBackGroundPaint;

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

        mListenRunnable = new ListenRunnable();

        positionX = -mBlockWidth;

        mColorAnimator = ValueAnimator.ofInt(mBackGroundColor,mBlockColor);
        mColorAnimator.setDuration(5000);
        mColorAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mColorAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Log.d(TAG, "onAnimationUpdate: ");
                mBackGroundPaint.setColor((Integer) animation.getAnimatedValue());
                postInvalidate();
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMaskHeight = MeasureSpec.getSize(heightMeasureSpec);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d(TAG, "onLayout: ");
        mStateBlock1.layout(positionX, 0, positionX + mBlockWidth, mMaskHeight);
        mStateBlock2.layout(r - positionX - mBlockWidth, 0, r - positionX, mMaskHeight);
        mMidTransitionX = (getWidth() - mBlockWidth) / 2 - positionX;
    }

    private void initPaint() {
        mMaskPaint = new Paint();
        mMaskPaint.setColor(mBackGroundColor);


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
//        canvas.drawRect(0, bottom - mBarHeight, canvas.getWidth(), bottom, mMaskPaint);

        Log.d(TAG, "onDraw: ");
        listenMove();
//        canvas.drawRoundRect(positionX,bottom - mBarHeight,positionX + mBlockWidth, bottom,15,15,mBlockPaint);
//        canvas.drawRect(positionX,bottom - mBlockHeight,positionX + mBlockWidth,bottom, mMaskPaint);
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

    public void startActive(){
        mColorAnimator.start();
    }

    public void startListen() {
        getHandler().removeCallbacks(mListenRunnable);
        mStateBlock1.setTranslationX(0);
        listenMove();
    }

    private class ListenRunnable implements Runnable {

        @Override
        public void run() {
            listenMove();
        }
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
