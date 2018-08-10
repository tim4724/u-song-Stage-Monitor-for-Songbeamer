package com.tim.usong.view;

import com.tim.usong.resource.StatusResource;
import io.dropwizard.views.View;

import java.util.*;

public class StatusView extends View {
    private final ResourceBundle messages;
    private final StatusResource.Status status;

    public StatusView(Locale locale, StatusResource.Status status) {
        super("status.ftl");
        this.messages = ResourceBundle.getBundle("MessagesBundle", locale);
        this.status = status;
    }

    public StatusResource.Status getStatus() {
        return status;
    }

    public ResourceBundle getMessages() {
        return messages;
    }

}