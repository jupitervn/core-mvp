package com.jupitervn.mvp;

import com.jupitervn.mvp.common.presenter.PresenterLifecycle;
import com.jupitervn.mvp.common.view.BaseFragmentView;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static org.mockito.Mockito.mock;

/**
 * Created by Jupiter (vu.cao.duy@gmail.com) on 1/19/16.
 */
public class BaseFragmentViewForTest extends BaseFragmentView {

    public PresenterLifecycle presenterLifecycle;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenterLifecycle = mock(PresenterLifecycle.class);
        setPresenterLifecycle(presenterLifecycle);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return new TextView(getContext());
    }

}
