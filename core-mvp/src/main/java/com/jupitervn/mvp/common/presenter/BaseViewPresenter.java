package com.jupitervn.mvp.common.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * A simple view presenter with all the lifecycle-related method
 * Created by Jupiter (vu.cao.duy@gmail.com) on 12/6/15.
 */
public interface BaseViewPresenter<V> {
    void setView(V view);
    void create(Bundle bundle);
    void onResume();
    void onPause(boolean isFinishing);
    @Nullable Bundle saveState();
    void onDestroy();
}
