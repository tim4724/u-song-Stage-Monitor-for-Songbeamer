package com.tim.usong;

import io.dropwizard.Configuration;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.DefaultServerFactory;

public class USongConfiguration extends Configuration {

    public USongConfiguration() {
        //Not perfect, but I want to get rid of the configuration yaml and I want the server to run on port 80
        ((HttpConnectorFactory) ((DefaultServerFactory) getServerFactory())
                .getApplicationConnectors().get(0)).setPort(80);
    }

    private AppConfigHolder app = new AppConfigHolder();

    AppConfigHolder getAppConfig() {
        return app;
    }

    public void setApp(AppConfigHolder app) {
        this.app = app;
    }

    public class AppConfigHolder {
        public String songDir;
    }
}