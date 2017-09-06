package com.teliver.sdk.util;

import android.os.Binder;

final class ServiceBinder extends Binder {

    private TService service;

    ServiceBinder(TService service) {
        this.service = service;
    }

    public TService getService() {
        return service;
    }

}
