package com.jupitervn.mvp.common.presenter;

/**
 * Created by Jupiter (vu.cao.duy@gmail.com) on 12/14/15.
 */
public interface PresenterStorage {
    BaseViewPresenter getPresenterWithId(String id);
    void destroyPresenter(BaseViewPresenter presenter);

    void putPresenter(BaseViewPresenter presenter);

    String getIdOfPresenter(BaseViewPresenter presenter);
}
