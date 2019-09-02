package com.tim.usong.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class TutorialFrame {
    private TutorialFrame() {
    }

    public static void showTutorial() {
        ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");
        String title = messages.getString("tutorial");
        String url = "http://localhost/tutorial";
        Preferences prefs = Preferences.userNodeForPackage(TutorialFrame.class).node("tutorial");
        int style = SWT.ON_TOP | SWT.SHELL_TRIM;

        new WebFrame(title, url, prefs, 1024, 800, 12, style) {
            @Override
            public void onBeforeOpen(Shell shell) {
                Button button = new Button(shell, SWT.FLAT);
                GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
                gridData.heightHint = 32;
                gridData.widthHint= 96;
                button.setLayoutData(gridData);
                button.setBackground(new Color(Display.getCurrent(), 0, 0, 0));
                button.setText(messages.getString("done"));
                button.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        shell.close();
                    }
                });
            }
        };
    }
}
