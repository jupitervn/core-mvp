package com.jupiter.mvp.test.common;

import com.jupitervn.mvp.common.presenter.BaseViewPresenter;
import com.jupitervn.mvp.common.presenter.PresenterLifecycle;
import com.jupitervn.mvp.common.view.BaseAppcompatActivityView;

import android.os.Bundle;
import android.support.annotation.Nullable;

import static org.mockito.Mockito.mock;


/**
 * Created by Jupiter (vu.cao.duy@gmail.com) on 1/13/16.
 */
public class AppCompatActivityViewForTest extends BaseAppcompatActivityView<BaseViewPresenter> {

    private PresenterLifecycle mockPresenterLifecycle = mock(PresenterLifecycle.class);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPresenterLifecycle(mockPresenterLifecycle);
    }

    public PresenterLifecycle getPresenterLifecycle() {
        return mockPresenterLifecycle;
    }


}
