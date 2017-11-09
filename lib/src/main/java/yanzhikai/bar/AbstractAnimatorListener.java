package yanzhikai.bar;

import android.animation.Animator;

/**
 * Created by yany on 2017/11/8.
 */

public abstract class AbstractAnimatorListener implements Animator.AnimatorListener {
    public boolean isCancel;

    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {
        isCancel = false;
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        isCancel = true;
    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }
}
