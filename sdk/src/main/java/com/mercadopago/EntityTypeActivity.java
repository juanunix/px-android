package com.mercadopago;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.mercadopago.adapters.EntityTypesAdapter;
import com.mercadopago.callbacks.OnSelectedCallback;
import com.mercadopago.controllers.CheckoutTimer;
import com.mercadopago.customviews.MPTextView;
import com.mercadopago.listeners.RecyclerItemClickListener;
import com.mercadopago.model.EntityType;
import com.mercadopago.model.Identification;
import com.mercadopago.model.IdentificationType;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.Site;
import com.mercadopago.mptracker.MPTracker;
import com.mercadopago.observers.TimerObserver;
import com.mercadopago.preferences.DecorationPreference;
import com.mercadopago.presenters.EntityTypePresenter;
import com.mercadopago.providers.EntityTypeProviderImpl;
import com.mercadopago.uicontrollers.FontCache;
import com.mercadopago.uicontrollers.card.CardRepresentationModes;
import com.mercadopago.uicontrollers.card.IdentificationCardView;
import com.mercadopago.util.ColorsUtil;
import com.mercadopago.util.ErrorUtil;
import com.mercadopago.util.JsonUtil;
import com.mercadopago.util.LayoutUtil;
import com.mercadopago.util.ScaleUtil;
import com.mercadopago.views.EntityTypeActivityView;

import java.util.List;

/**
 * Created by marlanti on 3/3/17.
 */

public class EntityTypeActivity extends MercadoPagoBaseActivity implements EntityTypeActivityView, TimerObserver {

    private static final String DECORATION_PREFERENCE_BUNDLE = "mDecorationPreference";
    private static final String LOW_RES_BUNDLE = "mLowResActive";
    private static final String PRESENTER_BUNDLE = "mEntityTypePresenter";

    protected EntityTypePresenter mEntityTypePresenter;
    protected Activity mActivity;

    //View controls
    protected EntityTypesAdapter mEntityTypesAdapter;
    protected RecyclerView mEntityTypesRecyclerView;
    protected DecorationPreference mDecorationPreference;
    //ViewMode
    protected boolean mLowResActive;
    //Low Res View
    protected Toolbar mLowResToolbar;
    protected MPTextView mLowResTitleToolbar;
    protected MPTextView mTimerTextView;
    //Normal View
    protected CollapsingToolbarLayout mCollapsingToolbar;
    protected AppBarLayout mAppBar;
    protected FrameLayout mCardContainer;
    protected Toolbar mNormalToolbar;
    protected IdentificationCardView mIdentificationCardView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;

        if (savedInstanceState == null) {
            createPresenter();
            configurePresenter();
            getActivityParameters();
            setTheme();
            analyzeLowRes();
            setContentView();
            mEntityTypePresenter.validateActivityParameters();
        }

    }

    private void createPresenter() {
        if (mEntityTypePresenter == null) {
            mEntityTypePresenter = new EntityTypePresenter();
        }
    }

    private void configurePresenter() {
        if(mEntityTypePresenter != null){
            mEntityTypePresenter.attachView(this);
            EntityTypeProviderImpl resourcesProvider = new EntityTypeProviderImpl(this);
            mEntityTypePresenter.attachResourcesProvider(resourcesProvider);
        }
    }

    private void setTheme() {
        if (isCustomColorSet()) {
            setTheme(R.style.Theme_MercadoPagoTheme_NoActionBar);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(DECORATION_PREFERENCE_BUNDLE, JsonUtil.getInstance().toJson(mDecorationPreference));
        outState.putString(PRESENTER_BUNDLE, JsonUtil.getInstance().toJson(mEntityTypePresenter));
        outState.putBoolean(LOW_RES_BUNDLE, mLowResActive);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mEntityTypePresenter =  JsonUtil.getInstance().fromJson(savedInstanceState.getString(PRESENTER_BUNDLE), EntityTypePresenter.class);
            mDecorationPreference = JsonUtil.getInstance().fromJson(savedInstanceState.getString(DECORATION_PREFERENCE_BUNDLE), DecorationPreference.class);
            mLowResActive = savedInstanceState.getBoolean(LOW_RES_BUNDLE);
            setTheme();
            analyzeLowRes();
            setContentView();
            mEntityTypePresenter.validateActivityParameters();
        }

    }

    private boolean isCustomColorSet() {
        return mDecorationPreference != null && mDecorationPreference.hasColors();
    }

    private void getActivityParameters() {

        String publicKey = getIntent().getStringExtra("merchantPublicKey");
        mDecorationPreference = JsonUtil.getInstance().fromJson(getIntent().getStringExtra("decorationPreference"), DecorationPreference.class);

        PaymentMethod paymentMethod = JsonUtil.getInstance().fromJson(
                this.getIntent().getStringExtra("paymentMethod"), PaymentMethod.class);
        Identification identification = JsonUtil.getInstance().fromJson(this.getIntent().getStringExtra("identification"), Identification.class);
        IdentificationType identificationType = JsonUtil.getInstance().fromJson(this.getIntent().getStringExtra("identificationType"), IdentificationType.class);
        Site site = JsonUtil.getInstance().fromJson(this.getIntent().getStringExtra("site"), Site.class);

        mEntityTypePresenter.setSite(site);
        mEntityTypePresenter.setPaymentMethod(paymentMethod);
        mEntityTypePresenter.setPublicKey(publicKey);
        mEntityTypePresenter.setIdentification(identification);
        mEntityTypePresenter.setIdentificationType(identificationType);
    }

    public void analyzeLowRes() {
        if (mEntityTypePresenter.isCardInfoAvailable()) {
            this.mLowResActive = ScaleUtil.isLowRes(this);
        } else {
            this.mLowResActive = true;
        }
    }

    public void setContentView() {
        if (mLowResActive) {
            setContentViewLowRes();
        } else {
            setContentViewNormal();
        }
    }

    @Override
    public void onValidStart() {
        MPTracker.getInstance().trackScreen("ENTITY_TYPE_ACTIVITY", "2", mEntityTypePresenter.getPublicKey(),
                BuildConfig.VERSION_NAME, this);
        initializeControls();
        initialize();
        mEntityTypePresenter.initialize();
    }

    private void initialize() {
        loadViews();
        hideHeader();
        decorate();
        showTimer();
    }

    @Override
    public void showEntityTypes(List<EntityType> entityTypesList, OnSelectedCallback<Integer> onSelectedCallback){
        initializeAdapter(onSelectedCallback);
        mEntityTypesAdapter.addResults(entityTypesList);
    }

    private void showTimer() {
        if (CheckoutTimer.getInstance().isTimerEnabled()) {
            CheckoutTimer.getInstance().addObserver(this);
            mTimerTextView.setVisibility(View.VISIBLE);
            mTimerTextView.setText(CheckoutTimer.getInstance().getCurrentTime());
        }
    }

    @Override
    public void onInvalidStart(String message) {
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    private void setContentViewLowRes() {
        setContentView(R.layout.mpsdk_activity_entity_type_lowres);
    }

    private void setContentViewNormal() {
        setContentView(R.layout.mpsdk_activity_entity_type_normal);
    }

    private void initializeControls() {
        mEntityTypesRecyclerView = (RecyclerView) findViewById(R.id.mpsdkActivityEntityTypeView);
        mTimerTextView = (MPTextView) findViewById(R.id.mpsdkTimerTextView);

        if (mLowResActive) {
            mLowResToolbar = (Toolbar) findViewById(R.id.mpsdkRegularToolbar);
            mLowResTitleToolbar = (MPTextView) findViewById(R.id.mpsdkTitle);

            if (CheckoutTimer.getInstance().isTimerEnabled()) {
                Toolbar.LayoutParams marginParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                marginParams.setMargins(0, 0, 0, 0);
                mLowResTitleToolbar.setLayoutParams(marginParams);
                mLowResTitleToolbar.setTextSize(17);
                mTimerTextView.setTextSize(15);
            }

            mLowResToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            mLowResToolbar.setVisibility(View.VISIBLE);
        } else {
            mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.mpsdkCollapsingToolbar);
            mAppBar = (AppBarLayout) findViewById(R.id.mpsdkEntityTypeAppBar);
            mCardContainer = (FrameLayout) findViewById(R.id.mpsdkActivityCardContainer);
            mNormalToolbar = (Toolbar) findViewById(R.id.mpsdkRegularToolbar);
            mNormalToolbar.setVisibility(View.VISIBLE);

            mNormalToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    private void loadViews() {
        if (mLowResActive) {
            loadLowResViews();
        } else {
            loadNormalViews();
        }
    }

    private void initializeAdapter(OnSelectedCallback<Integer> onSelectedCallback) {
        mEntityTypesAdapter = new EntityTypesAdapter(this, onSelectedCallback);
        initializeAdapterListener(mEntityTypesAdapter, mEntityTypesRecyclerView);
    }

    private void initializeAdapterListener(RecyclerView.Adapter adapter, RecyclerView view) {
        view.setAdapter(adapter);
        view.setLayoutManager(new LinearLayoutManager(this));
        view.addOnItemTouchListener(new RecyclerItemClickListener(this,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        mEntityTypePresenter.onItemSelected(position);
                    }
                }));
    }

    @Override
    public void showErrorView(String message, String errorDetail) {
        ErrorUtil.startErrorActivity(mActivity, message, errorDetail, false);
    }

    private void loadLowResViews() {
        loadToolbarArrow(mLowResToolbar);
        mLowResTitleToolbar.setText(getString(R.string.mpsdk_entity_types_title));
        if (FontCache.hasTypeface(FontCache.CUSTOM_REGULAR_FONT)) {
            mLowResTitleToolbar.setTypeface(FontCache.getTypeface(FontCache.CUSTOM_REGULAR_FONT));
        }
    }

    private void loadNormalViews() {
        loadToolbarArrow(mNormalToolbar);
        mNormalToolbar.setTitle(getString(R.string.mpsdk_entity_types_title));
        setCustomFontNormal();

        mIdentificationCardView = new IdentificationCardView(mActivity, CardRepresentationModes.MEDIUM_SIZE, mEntityTypePresenter.getIdentification());


        mIdentificationCardView.setIdentificationType(mEntityTypePresenter.getIdentificationType());

        mIdentificationCardView.inflateInParent(mCardContainer, true);
        mIdentificationCardView.initializeControls();
        mIdentificationCardView.draw();
        mIdentificationCardView.show();

    }

    private void loadToolbarArrow(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                    finish();
                }
            });
        }
    }

    private void setCustomFontNormal() {
        if (FontCache.hasTypeface(FontCache.CUSTOM_REGULAR_FONT)) {
            mCollapsingToolbar.setCollapsedTitleTypeface(FontCache.getTypeface(FontCache.CUSTOM_REGULAR_FONT));
            mCollapsingToolbar.setExpandedTitleTypeface(FontCache.getTypeface(FontCache.CUSTOM_REGULAR_FONT));
        }
    }

    private void decorate() {
        if (isDecorationEnabled()) {
            if (mLowResActive) {
                decorateLowRes();
            } else {
                decorateNormal();
            }
        }
    }

    private boolean isDecorationEnabled() {
        return mDecorationPreference != null && mDecorationPreference.hasColors();
    }

    private void decorateLowRes() {
        ColorsUtil.decorateLowResToolbar(mLowResToolbar, mLowResTitleToolbar, mDecorationPreference,
                getSupportActionBar(), this);
        if (mTimerTextView != null) {
            ColorsUtil.decorateTextView(mDecorationPreference, mTimerTextView, this);
        }
    }

    private void decorateNormal() {
        ColorsUtil.decorateNormalToolbar(mNormalToolbar, mDecorationPreference, mAppBar,
                mCollapsingToolbar, getSupportActionBar(), this);
        if (mTimerTextView != null) {
            ColorsUtil.decorateTextView(mDecorationPreference, mTimerTextView, this);
        }

        mIdentificationCardView.decorateCardBorder(mDecorationPreference.getLighterColor());
    }

    @Override
    public void showHeader() {
        if (mLowResActive) {
            mLowResToolbar.setVisibility(View.VISIBLE);
        } else {
            String title = getString(R.string.mpsdk_entity_types_title);
            mNormalToolbar.setTitle(title);
            setCustomFontNormal();
        }
    }

    private void hideHeader() {
        if (mLowResActive) {
            mLowResToolbar.setVisibility(View.GONE);
        } else {
            mNormalToolbar.setTitle("");
        }
    }

    @Override
    public void showLoadingView() {
        mEntityTypesRecyclerView.setVisibility(View.GONE);
        LayoutUtil.showProgressLayout(this);
    }

    @Override
    public void stopLoadingView() {
        mEntityTypesRecyclerView.setVisibility(View.VISIBLE);
        LayoutUtil.showRegularLayout(this);
    }

    @Override
    public void finishWithResult(EntityType entityType) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("entityType", JsonUtil.getInstance().toJson(entityType));
        setResult(RESULT_OK, returnIntent);
        finish();
        overridePendingTransition(R.anim.mpsdk_hold, R.anim.mpsdk_hold);
    }

    @Override
    public void onBackPressed() {
        MPTracker.getInstance().trackEvent("ENTITY_TYPE_ACTIVITY", "BACK_PRESSED", "2", mEntityTypePresenter.getPublicKey(),
                BuildConfig.VERSION_NAME, this);
        Intent returnIntent = new Intent();
        returnIntent.putExtra("backButtonPressed", true);
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ErrorUtil.ERROR_REQUEST_CODE) {
            resolveErrorRequest(resultCode, data);
        }
    }

    private void resolveErrorRequest(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            mEntityTypePresenter.recoverFromFailure();
        } else {
            setResult(resultCode, data);
            finish();
        }
    }

    @Override
    public void onTimeChanged(String timeToShow) {
        mTimerTextView.setText(timeToShow);
    }

    @Override
    public void onFinish() {
        this.finish();
    }
}
