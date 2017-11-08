package yanzhikai.bar;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import static yanzhikai.bar.StateBar.TAG;

/**
 * Created by yany on 2017/11/7.
 */

public class StateBlock extends View {
    private Paint mMaskPaint,mBlockPaint;
    private @ColorInt int mBlockColor = getResources().getColor(R.color.colorLightBlue);
    private @ColorInt int mBackGroundColor = getResources().getColor(R.color.colorTransparencyDeepBlue);
    private int mBlockHeight = 15;
    private StateBar mParent;
    private boolean mIsMasked = true;

    public StateBlock(Context context, StateBar stateBar) {
        super(context);
        mParent = stateBar;
        init();
    }


    private void init(){
        mBlockHeight = mParent.getBarHeight();
//        initPaint();
    }

    private void initPaint() {
        mBlockPaint = new Paint();
        mBlockPaint.setColor(mBlockColor);
        float[] points = {0,0.2f,0.8f,1};
        int[] colors = {mBackGroundColor,mBlockColor,mBlockColor,mBackGroundColor};
//        mBlockPaint.setColor(mBlockColor);
        mBlockPaint.setShader(new LinearGradient(0,0,getWidth(),0,colors,points, Shader.TileMode.CLAMP));

        mMaskPaint = new Paint();
        mMaskPaint.setColor(mBlockColor);
        mMaskPaint.setMaskFilter(new BlurMaskFilter(getHeight() - 10 , BlurMaskFilter.Blur.NORMAL));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initPaint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        canvas.drawRect(0,height - mBlockHeight,width,height,mBlockPaint);
        if (mIsMasked) {
            canvas.drawRect(20, height - 10, width - 20, height, mMaskPaint);
        }
        Log.d(TAG, "onDraw: block");
    }

    public void setIsMasked(boolean isMasked) {
        mIsMasked = isMasked;
    }
}
