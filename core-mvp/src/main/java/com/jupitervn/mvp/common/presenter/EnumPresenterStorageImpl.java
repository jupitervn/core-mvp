package com.jupitervn.mvp.common.presenter;

import java.util.HashMap;

/**
 * Created by Jupiter (vu.cao.duy@gmail.com) on 1/25/16.
 */
public class EnumPresenterStorageImpl implements PresenterStorage {
    private HashMap<String, BaseViewPresenter> idToPresenter = new HashMap<>();
    private HashMap<BaseViewPresenter, String> presenterToId = new HashMap<>();

    @Override
    public BaseViewPresenter getPresenterWithId(String id) {
        return idToPresenter.get(id);
    }

    @Override
    public void destroyPresenter(BaseViewPresenter presenter) {
        idToPresenter.remove(presenterToId.remove(presenter));
    }

    @Override
    public void putPresenter(BaseViewPresenter presenter) {
        String id = presenter.getClass().getSimpleName() + "/" + System.nanoTime() + "/" + (int)(Math.random() * Integer.MAX_VALUE);
        idToPresenter.put(id, presenter);
        presenterToId.put(presenter, id);
    }

    @Override
    public String getIdOfPresenter(BaseViewPresenter presenter) {
        return presenterToId.get(presenter);
    }
}
