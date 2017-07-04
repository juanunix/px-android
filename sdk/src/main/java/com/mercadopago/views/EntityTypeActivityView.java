package com.mercadopago.views;

import com.mercadopago.callbacks.OnSelectedCallback;
import com.mercadopago.model.ApiException;
import com.mercadopago.model.EntityType;
import com.mercadopago.mvp.MvpView;

import java.util.List;

/**
 * Created by marlanti on 3/3/17.
 */

public interface EntityTypeActivityView extends MvpView {
    void onValidStart();
    void onInvalidStart(String message);
    void showEntityTypes(List<EntityType> entityTypes, OnSelectedCallback<Integer> dpadSelectionCallback);
    void showErrorView(String message, String errorDetail);
    void showHeader();
    void showLoadingView();
    void stopLoadingView();
    void finishWithResult(EntityType entityType);
}
