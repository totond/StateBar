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
import android.view.View;

/**
 * Created by yany on 2017/11/6.
 */

public class StateBar extends View {
    public static final String TAG = "StateBar";
    private Context mContext;

    private @ColorInt int mBlockColor = getResources().getColor(R.color.colorLightBlue);
    private @ColorInt int mBlackColor = getResources().getColor(R.color.colorDeepBlue);

    private Paint mBlockPaint;

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

    private void init(Context context){
        mContext = context;
        setLayerType(LAYER_TYPE_NONE,null);

        initPaint();
    }

    private void initPaint(){
        mBlockPaint = new Paint();
        mBlockPaint.setColor(mBlockColor);
//        mBlockPaint.setShadowLayer(200,0,0, mBlockColor);
//        mBlockPaint.setShader(new LinearGradient(0,100,0,200,Color.argb(255,8,89,127),mBlockColor, Shader.TileMode.CLAMP));
        mBlockPaint.setMaskFilter(new BlurMaskFilter(50, BlurMaskFilter.Blur.NORMAL));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(100,100,200,200,mBlockPaint);
    }
}
