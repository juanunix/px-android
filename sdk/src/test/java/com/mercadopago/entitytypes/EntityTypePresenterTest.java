package com.mercadopago.entitytypes;

import com.mercadopago.callbacks.OnSelectedCallback;
import com.mercadopago.constants.Sites;
import com.mercadopago.mocks.EntityTypes;
import com.mercadopago.mocks.Identifications;
import com.mercadopago.mocks.PaymentMethods;
import com.mercadopago.model.EntityType;
import com.mercadopago.model.Site;
import com.mercadopago.presenters.EntityTypePresenter;
import com.mercadopago.providers.EntityTypeProvider;
import com.mercadopago.util.EntityTypesUtil;
import com.mercadopago.views.EntityTypeActivityView;

import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Created by marlanti on 7/5/17.
 */

public class EntityTypePresenterTest {

    @Test
    public void whenIssuersAreNullThenGetIssuersAndShow() {
        MockedView mockedView = new MockedView();
        MockedProvider provider = new MockedProvider();

        Site site = new Site("MCO", "COP");
        List<EntityType> entityTypes = EntityTypes.getEntityTypesBySite(site);

        provider.setResponse(entityTypes);


        EntityTypePresenter presenter = new EntityTypePresenter();
        presenter.setPublicKey("PUBLIC_KEY");
        presenter.setSite(site);
        presenter.setIdentification(Identifications.getIdentificationMCO());
        presenter.setPaymentMethod(PaymentMethods.getPaymentMethodOff());

        presenter.attachView(mockedView);
        presenter.attachResourcesProvider(provider);

        presenter.initialize();

//        mockedView.simulateEntityTypeSelection(0);

        assertTrue(mockedView.entityTypesShown);
        assertTrue(mockedView.headerShown);
//        assertEquals(entityTypes.get(0), mockedView.selectedEntityType);
//        assertTrue(mockedView.finishWithResult);
        assertFalse(mockedView.errorShown);
        assertFalse(provider.emptyEntityTypesErrorGotten);
    }

    private class MockedProvider implements EntityTypeProvider {

        private boolean emptyEntityTypesErrorGotten = false;
        private List<EntityType> successfulResponse;
        private boolean shouldFail;


        @Override
        public List<EntityType> getEntityTypesBySite(Site site) {

            List<EntityType> list = null;

            if(shouldFail){
                list = null;
            }else{
                list = successfulResponse;
            }

            return list;
        }

        @Override
        public String getStandardErrorMessage() {
            this.emptyEntityTypesErrorGotten = true;
            return null;
        }

        @Override
        public String getEmptyEntityTypesErrorMessage() {
            this.emptyEntityTypesErrorGotten = true;
            return null;
        }

        public void setResponse(List<EntityType> response) {
            this.successfulResponse = response;
            this.shouldFail = false;
        }
    }


    private class MockedView implements EntityTypeActivityView{

        private boolean entityTypesShown = false;
        private boolean headerShown = false;
        private boolean loadingViewShown = false;
        private boolean errorShown = false;
        private boolean finishWithResult = false;
        private EntityType selectedEntityType;
        private OnSelectedCallback<Integer> entityTypeSelectionCallback;


        @Override
        public void onValidStart() {

        }

        @Override
        public void onInvalidStart(String message) {

        }

        @Override
        public void showEntityTypes(List<EntityType> entityTypesList, OnSelectedCallback<Integer> onSelectedCallback) {
            this.entityTypeSelectionCallback = onSelectedCallback;
            this.entityTypesShown = true;
        }

        @Override
        public void showErrorView(String message, String errorDetail) {
            this.errorShown = true;
        }

        @Override
        public void showHeader() {
            this.headerShown = true;
        }

        @Override
        public void showLoadingView() {
            this.loadingViewShown = true;
        }

        @Override
        public void stopLoadingView() {
            this.loadingViewShown = false;
        }

        @Override
        public void finishWithResult(EntityType entityType) {
            this.finishWithResult = true;
            this.selectedEntityType = entityType;
        }

        private void simulateEntityTypeSelection(int index) {
            entityTypeSelectionCallback.onSelected(index);
        }
    }
}
