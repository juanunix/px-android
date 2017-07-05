package com.mercadopago.mocks;

import com.mercadopago.constants.Sites;
import com.mercadopago.model.EntityType;
import com.mercadopago.model.Site;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by marlanti on 7/5/17.
 */

public class EntityTypes {

    private EntityTypes() {
    }

    private static final String INDIVIDUAL_FOR_PAYMENT = "individual";
    private static final String ASSOCIATION_FOR_PAYMENT = "association";

    public static List<EntityType> getEntityTypesBySite(Site site) {

        Map<String, List<EntityType>> entityTypesBySite = new HashMap<>();
        //MCO
        List<EntityType> entityTypesMCO = new ArrayList<>();

        entityTypesMCO.add(new EntityType(INDIVIDUAL_FOR_PAYMENT, "Natural"));
        entityTypesMCO.add(new EntityType(ASSOCIATION_FOR_PAYMENT, "Juridica"));

        entityTypesBySite.put(Sites.COLOMBIA.getId(), entityTypesMCO);

        List<EntityType> list = null;

        if (site != null) {
            entityTypesBySite.get(site.getId());
        }

        return list == null ? new ArrayList<EntityType>() : list;
    }
}
