package yanzhikai.bar;

/**
 * Created by yany on 2017/11/9.
 */

public interface StateListener {
    void onStateChanged(@StateBar.State int nextState);
}
