package com.mercadopago.providers;

import com.mercadopago.model.EntityType;
import com.mercadopago.model.Site;
import com.mercadopago.mvp.ResourcesProvider;

import java.util.List;

/**
 * Created by marlanti on 3/21/17.
 */

public interface EntityTypeProvider extends ResourcesProvider {
    List<EntityType> getEntityTypesBySite(Site site);

    String getStandardErrorMessage();

    String getEmptyEntityTypesErrorMessage();

}
