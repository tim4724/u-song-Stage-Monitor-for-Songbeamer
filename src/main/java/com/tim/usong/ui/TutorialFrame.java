package com.tim.usong.ui;

import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class TutorialFrame extends WebFrame {

    public TutorialFrame() {
        super("", "http://localhost/tutorial",
                Preferences.userNodeForPackage(TutorialFrame.class).node("tutorial"),
                1024, 800, 1);
        ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");
        setTitle(messages.getString("tutorial"));
    }
}
