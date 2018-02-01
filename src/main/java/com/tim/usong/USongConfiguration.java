package com.tim.usong;

import io.dropwizard.Configuration;

public class USongConfiguration extends Configuration {
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