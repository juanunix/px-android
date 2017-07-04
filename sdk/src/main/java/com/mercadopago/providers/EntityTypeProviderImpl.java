package com.mercadopago.providers;

import android.content.Context;

import com.mercadopago.R;
import com.mercadopago.model.EntityType;
import com.mercadopago.model.Site;
import com.mercadopago.util.EntityTypesUtil;

import java.util.List;

/**
 * Created by marlanti on 3/21/17.
 */

public class EntityTypeProviderImpl implements EntityTypeProvider {

    private Context mContext;

    public EntityTypeProviderImpl(Context context) {
        this.mContext = context;
    }

    @Override
    public List<EntityType> getEntityTypesBySite(Site site) {

        return EntityTypesUtil.getEntityTypesBySite(site, mContext);
    }

    @Override
    public String getStandardErrorMessage() {
        return mContext.getString(R.string.mpsdk_standard_error_message);
    }

    @Override
    public String getEmptyEntityTypesErrorMessage() {
        return mContext.getString(R.string.mpsdk_entity_types_not_found_error_message);
    }
}
