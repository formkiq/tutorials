/**
 * Copyright [2020] FormKiQ Inc. Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a copy of the License
 * at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.formkiq.tutorials.fileupload;

import java.util.Map;
import com.formkiq.stacks.client.FormKiqClient;
import com.formkiq.stacks.client.FormKiqClientConnection;
import com.formkiq.stacks.client.FormKiqClientV1;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;

public class FormKiqClientBuilder {

  /** Parameter Store Value: /formkiq/<appenvironment>/cognito/UserPoolClientId. */
  private static final String CLIENT_ID = "4b9bl9mkvbhpn0g0mjpj0hnhb7";

  /** Parameter Store Value: /formkiq/<appenvironment>/api/DocumentsHttpUrl. */
  private static final String HTTP_URL = "https://api-demo.tryformkiq.com/";

  private static CognitoIdentityProviderClient cognitoProvider = CognitoIdentityProviderClient
      .builder().credentialsProvider(AnonymousCredentialsProvider.create()).build();

  /**
   * Logins into Cognito and returns a Authentication token that is valid for 1 HR.
   * 
   * @param email User's Email address
   * @param password User's Password
   * 
   * @return Cognito ID Token
   */
  private static String loginToCognito(final String email, final String password) {

    InitiateAuthRequest req =
        InitiateAuthRequest.builder().clientId(CLIENT_ID).authFlow(AuthFlowType.USER_PASSWORD_AUTH)
            .authParameters(Map.of("USERNAME", email, "PASSWORD", password)).build();

    InitiateAuthResponse response = cognitoProvider.initiateAuth(req);
    return response.authenticationResult().idToken();
  }

  /**
   * Builds a {@link FormKiqClient} instance using a valid Cognito Email / Password.
   * 
   * @param email User's Email address
   * @param password User's Password
   * 
   * @return {@link FormKiqClient}
   */
  public static FormKiqClient build(final String email, final String password) {

    String token = loginToCognito(email, password);

    FormKiqClientConnection connection =
        new FormKiqClientConnection(HTTP_URL).cognitoIdToken(token);
    return new FormKiqClientV1(connection);
  }
}
