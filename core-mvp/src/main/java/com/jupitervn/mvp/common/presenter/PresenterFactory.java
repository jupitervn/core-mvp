package com.jupitervn.mvp.common.presenter;

/**
 * Created by Jupiter (vu.cao.duy@gmail.com) on 12/14/15.
 */
public interface PresenterFactory<P extends BaseViewPresenter> {
    P createPresenter();
}
