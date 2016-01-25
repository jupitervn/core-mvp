package com.jupitervn.mvp.common.view;

/**
 * Created by SILONG on 12/27/15.
 */
public interface ViewWithProgressDialog {

  void showProgressDialog(int messageId);

  void showMessage(int messageId);

  void hideProgressDialog();

  void showProgressLoading();

  void hideProgressLoading();
}
