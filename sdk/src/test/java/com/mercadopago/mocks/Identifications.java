package com.mercadopago.mocks;

import com.mercadopago.model.Identification;
import com.mercadopago.util.JsonUtil;
import com.mercadopago.utils.ResourcesUtil;

/**
 * Created by marlanti on 7/5/17.
 */

public class Identifications {

    public static Identification getIdentificationMCO() {
        String json = ResourcesUtil.getStringResource("identification_MCO.json");
        return JsonUtil.getInstance().fromJson(json, Identification.class);
    }

}
