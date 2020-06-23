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
package org.eclipse.kapua.app.console.module.authentication.client.tabs.credentials;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Image;
import org.eclipse.kapua.app.console.module.api.client.resources.icons.IconSet;
import org.eclipse.kapua.app.console.module.api.client.resources.icons.KapuaIcon;
import org.eclipse.kapua.app.console.module.api.client.ui.dialog.InfoDialog;
import org.eclipse.kapua.app.console.module.api.client.util.DialogUtils;
import org.eclipse.kapua.app.console.module.api.client.util.FailureHandler;
import org.eclipse.kapua.app.console.module.authentication.client.messages.ConsoleCredentialMessages;

import java.util.Date;

public class AuthenticationKeyConfirmationDialog extends InfoDialog {

    protected static final ConsoleCredentialMessages CRED_MSGS = GWT.create(ConsoleCredentialMessages.class);

    private final Image barcodeImage;

    public AuthenticationKeyConfirmationDialog(String authKey, String userName, String accountName) {
        super(CRED_MSGS.dialogConfirmationAuthenticationKey(), new KapuaIcon(IconSet.KEY),
                new SafeHtmlBuilder().appendEscapedLines(CRED_MSGS.dialogAddConfirmationAuthenticationKey2()).toSafeHtml().asString());
        setStyleAttribute("background-color", "#F0F0F0");
        setBodyStyle("background-color: #F0F0F0");

        barcodeImage = new Image(); // QR Code Image
        try {
            Date date = new Date();

            StringBuilder sb = new StringBuilder();
            sb.append("image/2FAQRcode?");

            sb.append("username=")
                    .append(userName)
                    .append("&accountName=")  // FIXME: not sure that we also need the account name
                    .append(accountName)
                    .append("&key=")
                    .append(authKey)
                    .append("&timestamp= ")  // this is only used to avoid that images are taken by the browser cache instead of being generated again
                    .append(date.getTime());

            barcodeImage.setUrl(sb.toString());
        } catch (Exception e) {
            FailureHandler.handle(e);
        }

        barcodeImage.addLoadHandler(new LoadHandler() {

            @Override
            public void onLoad(LoadEvent event) {
                barcodeImage.setVisible(true);
            }
        });
        add(barcodeImage);

        DialogUtils.resizeDialog(this, 350, 200);
        // TODO: Should scratch code generation be performed inside the authentication key generation, or should be called form here with a dedicated callback?
    }

    // TODO: implement scratch codes
//    private void printScratchCodes(List<String> scratchCodes) {
//        StringBuilder sb = new StringBuilder();
//        sb.append("Scratch Codes:<br/><br/>");
//        for (String code : scratchCodes) {
//            sb.append(code).append("</br>");
//        }
//        scratchCodesArea.setText(sb.toString());
//    }
}
