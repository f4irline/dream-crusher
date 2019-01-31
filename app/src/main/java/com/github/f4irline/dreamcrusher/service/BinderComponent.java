package com.github.f4irline.dreamcrusher.service;

import android.os.Binder;

/**
 * A base class for the remotable object.
 */
public class BinderComponent extends Binder {

    private LottoService service;

    public BinderComponent (LottoService service) {
        this.service = service;
    }

    /**
     * Returns the service so it's possible to invoke
     * all the services methods.
     *
     * @return the service which is bound.
     */
    public LottoService getService() {
        return this.service;
    }

}
