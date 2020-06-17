/*******************************************************************************
 * Copyright (c) 2020 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.app.console.core.client;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Status;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import org.eclipse.kapua.app.console.core.client.messages.ConsoleCoreMessages;
import org.eclipse.kapua.app.console.core.shared.model.authentication.GwtLoginCredential;
import org.eclipse.kapua.app.console.core.shared.service.GwtAuthorizationService;
import org.eclipse.kapua.app.console.core.shared.service.GwtAuthorizationServiceAsync;
import org.eclipse.kapua.app.console.module.api.client.messages.ConsoleMessages;
import org.eclipse.kapua.app.console.module.api.client.util.ConsoleInfo;
import org.eclipse.kapua.app.console.module.api.shared.model.session.GwtSession;

/**
 * Two Factor Authentication - Login second step: 2FA code and trust this machine request
 */
public class TwoFADialog extends Dialog {

    private static final ConsoleMessages MSGS = GWT.create(ConsoleMessages.class);
    private static final ConsoleCoreMessages CORE_MSGS = GWT.create(ConsoleCoreMessages.class);

    private final GwtAuthorizationServiceAsync gwtAuthorizationService = GWT.create(GwtAuthorizationService.class);

    protected TextField<String> code;
    //final CheckBox trustCheckbox;  // FIXME: no trustKey for the moment, add it later

    protected Button back;
    protected Button submit;
    protected Status status;

    private final LoginDialog loginDialog;

    public TwoFADialog(LoginDialog loginDialog) {
        this.loginDialog = loginDialog;

        FormLayout layout = new FormLayout();

        layout.setLabelWidth(90);
        layout.setDefaultWidth(160);
        setLayout(layout);

        setButtonAlign(HorizontalAlignment.LEFT);
        setButtons(""); // don't show OK button
        setIcon(IconHelper.createStyle("user"));

        setModal(false);
        setBodyBorder(true);
        setBodyStyle("padding: 8px;background: none");
        setWidth(300);
        setResizable(false);
        setClosable(false);

        KeyListener keyListener = new KeyListener() {

            @Override
            public void componentKeyUp(ComponentEvent event) {
                validate();
                if (event.getKeyCode() == 13) {
                    if (code.getValue() != null && code.getValue().trim().length() > 0) {
                        onSubmit();
                    }
                }
            }
        };

        Label title = new Label(MSGS.loginDialogTwoFaTitle());
        title.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        add(title);

        // 2FA presentation text
        add(new HTML("<br/>"));
        add(new Label(MSGS.login2FA()));
        add(new HTML("<br/>"));
        add(new Label(MSGS.login2FA1()));
        add(new HTML("<br/>"));

        // 2FA code
        code = new TextField<String>();
        code.setFieldLabel(MSGS.loginCode());
        code.addKeyListener(keyListener);
        add(code);

        // TODO: add checkBox for the trustKey (no trustKey for the moment, add it later)

        setFocusWidget(code);
    }

    protected void onSubmit() {
        status.show();
        getButtonBar().disable();

        // Login
        GwtLoginCredential credentials = new GwtLoginCredential(loginDialog.getUsername().getValue(), loginDialog.getPassword().getValue(), code.getValue());
        // TODO: set trustKey (no trustKey for the moment, add it later)

        gwtAuthorizationService.login(credentials, new AsyncCallback<GwtSession>() {
            @Override
            public void onFailure(Throwable caught) {
                ConsoleInfo.display(CORE_MSGS.loginError(), caught.getLocalizedMessage());
                reset();

                // Go back
                TwoFADialog.this.hide();
                loginDialog.resetDialog();
                loginDialog.show();
            }

            @Override
            public void onSuccess(final GwtSession gwtSession) {
                loginDialog.setCurrentSession(gwtSession);

                // TODO: If trust is checked we set the cookie for the first time (no trustKey for the moment, add it later)
                TwoFADialog.this.hide();
                loginDialog.callMainScreen();
                ConsoleInfo.hideInfo();
            }
        });
    }

    @Override
    protected void createButtons() {
        super.createButtons();

        status = new Status();
        status.setBusy(MSGS.waitMsg());
        status.hide();
        status.setAutoWidth(true);

        getButtonBar().add(status);
        getButtonBar().add(new FillToolItem());

        // login
        submit = new Button(CORE_MSGS.loginLogin());
        submit.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                onSubmit();
            }
        });

        // back
        back = new Button("Back");
        back.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                reset();
                TwoFADialog.this.hide();
                loginDialog.resetDialog(); // TODO: be careful, this resets everything
                loginDialog.show();
            }
        });

        // add the buttons
        addButton(back);
        addButton(submit);
    }

    protected boolean hasWellFormedCode(TextField<String> field) {
        return field.getValue() != null && field.getValue().length() > 0;
    }

    protected void validate() {
        submit.setEnabled(hasWellFormedCode(code));
    }

    private void reset() {
        code.reset();
        //code.enable();  // TODO: do we need the 'enable?'
        code.focus();
        // TODO: reset the trustKey (no trustKey for the moment, add it later)
        status.hide();
        getButtonBar().enable();
        validate();
    }

}
