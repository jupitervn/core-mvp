package com.jupitervn.mvp.common;

import com.jupitervn.mvp.common.presenter.BaseViewPresenter;
import com.jupitervn.mvp.common.presenter.PresenterStorage;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jupiter (vu.cao.duy@gmail.com) on 1/13/16.
 */
public class StubPresenterStorage implements PresenterStorage {
    public Map<Integer, BaseViewPresenter> presenterMap = new HashMap<>();
    public Map<BaseViewPresenter, Integer> presenterIdMap = new HashMap<>();

    @Override
    public BaseViewPresenter getPresenterWithId(String id) {
        return presenterMap.get(id);
    }

    @Override
    public void destroyPresenter(BaseViewPresenter presenter) {
        Integer id = presenterIdMap.remove(presenter);
        presenterMap.remove(id);
    }

    @Override
    public void putPresenter(BaseViewPresenter presenter) {
        presenterMap.put(presenter.hashCode(), presenter);
        presenterIdMap.put(presenter, presenter.hashCode());
    }

    @Override
    public String getIdOfPresenter(BaseViewPresenter presenter) {
        return String.valueOf(presenterIdMap.get(presenter));
    }
}
