/*******************************************************************************
 * Copyright (c) 2016, 2020 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.authentication.shiro.realm;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.eclipse.kapua.service.authentication.ApiKeyCredentials;
import org.eclipse.kapua.service.authentication.UsernamePasswordCredentials;
import org.eclipse.kapua.service.authentication.credential.Credential;
import org.eclipse.kapua.service.authentication.credential.CredentialType;
import org.eclipse.kapua.service.user.User;
import org.springframework.security.crypto.bcrypt.BCrypt;

/**
 * {@link ApiKeyCredentials} credential matcher implementation
 *
 * @since 1.0
 */
public class UserPassCredentialsMatcher implements CredentialsMatcher {

    @Override
    public boolean doCredentialsMatch(AuthenticationToken authenticationToken, AuthenticationInfo authenticationInfo) {

        //
        // Token data
        UsernamePasswordCredentials token = (UsernamePasswordCredentials) authenticationToken;
        String tokenUsername = token.getUsername();
        String tokenPassword = token.getPassword();
        String tokenAuthenticationCode = token.getAuthenticationCode();

        //
        // Info data
        LoginAuthenticationInfo info = (LoginAuthenticationInfo) authenticationInfo;
        User infoUser = (User) info.getPrincipals().getPrimaryPrincipal();
        Credential infoCredential = (Credential) info.getCredentials();  // TODO: it would be better to have a list of credentials...
        Credential authenticationKey = info.getAuthenticationKeyCredential();

        //
        // Match token with info
        boolean credentialMatch = false;
        if (tokenUsername.equals(infoUser.getName()) && CredentialType.PASSWORD.equals(infoCredential.getCredentialType()) && BCrypt.checkpw(tokenPassword, infoCredential.getCredentialKey())) {

            // FIXME: problem here: if the tokenAuthenticationCode!=null but the authenticationKey==null, the user will enter!

            if (authenticationKey == null) {
                credentialMatch = true;
                // FIXME: if true cache token password for authentication performance improvement

            } else {
                // 2FA match here
                boolean isCodeValid;
                GoogleAuthenticatorConfig googleAuthenticatorConfig = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                        .setWindowSize(100)  // TODO: make a configuration property with this value
                        .build();
                GoogleAuthenticator ga = new GoogleAuthenticator(googleAuthenticatorConfig);
                isCodeValid = ga.authorize(authenticationKey.getCredentialKey(), Integer.parseInt(tokenAuthenticationCode));
                return isCodeValid;

                // TODO: trust code is not implemented
//                // Trust machine - if trust is required
//                if (isCodeValid) {
//                    if (token.isTrustReq()) {
//                        s_logger.info("User with Trust Machine required: {}", token.getUsername());
//
//                        EdcTrustAuthUtils.enableTrust(user);
//                        isCodeValid = true;
//                    }
//                }

                // TODO: in case of scratch codes
//                if(!isCodeValid) {
//                    List<String> sc = user.getScratchCodes();
//                    ListIterator<String> i = sc.listIterator();
//                    while(i.hasNext()){
//                        String code = i.next();
//                        if (BCrypt.checkpw( edcToken.getAuthenticationCode(), code)) {
//                            isCodeValid = true;
//                            sc.remove(code);
//                            user.setAlreadyEncriptedScratchCodes(sc);
//                            UserService userService = locator.getUserService();
//                            userService.removeScratchCode(user.getId(), code);
//                            break;
//                        }
//                    }
//                }

                // TODO: trust code not implemented (this was another else in case authenticationKey is null but the machine is trusted)
//          else {
//              // if authentication code is null - we check the trust_key
//              if (token.getTrustKey()!=null) {
//                  // check trust machine authentication on the server side
//                  if (user.getTrustKey()!=null) {
//                      s_logger.debug("Login User with Trust Machine: {} {}", token.getUsername(), token.getTrustKey());
//                      if (token.getTrustKey().equals(user.getTrustKey())) {
//                          // trust_key is valid
//                          isCodeValid=true;
//                          s_logger.info("Trust key valid for {} {}", token.getUsername(), token.getTrustKey());
//                      } else {
//                          // trust key is invalid
//                          s_logger.info("Error during authetication: trust machine code doesn't match for user: {}", token.getUsername());
//                          ice = new IncorrectCredentialsException();
//                      }
//                  }
//              }
//          }

            }

        }

        return credentialMatch;
    }

}
