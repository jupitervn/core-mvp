package com.jupiter.mvp.test.common;

import com.jupitervn.mvp.common.presenter.BaseViewPresenter;
import com.jupitervn.mvp.common.view.BaseAppcompatActivityView;

import android.os.Bundle;
import android.support.annotation.Nullable;


/**
 * Created by Jupiter (vu.cao.duy@gmail.com) on 1/13/16.
 */
public class NoPresenterAppCompatActivityViewForTest extends BaseAppcompatActivityView<BaseViewPresenter> {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
