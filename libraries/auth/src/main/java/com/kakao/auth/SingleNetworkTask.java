/*
  Copyright 2014-2017 Kakao Corp.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package com.kakao.auth;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import com.kakao.auth.AuthService.AgeAuthStatus;
import com.kakao.auth.ageauth.DefaultAgeAuthService;
import com.kakao.auth.authorization.accesstoken.AccessToken;
import com.kakao.auth.authorization.accesstoken.AccessTokenManager;
import com.kakao.auth.authorization.authcode.AuthCodeManager;
import com.kakao.auth.helper.StartActivityWrapper;
import com.kakao.auth.network.response.InsufficientScopeException;
import com.kakao.auth.network.response.AuthorizedApiResponse.SessionClosedException;
import com.kakao.network.ErrorResult;
import com.kakao.network.INetwork;
import com.kakao.network.IRequest;
import com.kakao.network.KakaoNetworkImpl;
import com.kakao.network.NetworkTask;
import com.kakao.network.response.ResponseBody;
import com.kakao.network.response.ResponseData;
import com.kakao.network.response.ResponseStringConverter;
import com.kakao.util.helper.log.Logger;

/**
 * Class used for network requests that require logged in state.
 *
 * @author leo.shin
 */
@Deprecated
public class SingleNetworkTask extends NetworkTask {

    @Deprecated
    public SingleNetworkTask() {
        super(new KakaoNetworkImpl());
    }

    @Deprecated
    public SingleNetworkTask(INetwork network) {
        super(network);
    }

    /**
     * Session??? ????????? ???????????? open????????? ??????????????? ????????? ??????.
     * @return true is succeed, false is otherwise.
     */
    private static boolean checkApiSession() {
        Session session = Session.getCurrentSession();
        // 1. session check.
        if (session.isOpened()) {
            return true;
        }

        Logger.i("access token expired... trying to refresh access token...");
        // 2. request accessToken with refreshToken.
        // 3. update original request authorization accessToken value on header.
        if (session.getTokenInfo().hasRefreshToken()) {
            try {
                AccessTokenManager.Factory.getInstance().refreshAccessToken(session.getTokenInfo().getRefreshToken(), session.getAccessTokenCallback()).get();
                return true;
            } catch (Exception e) {
                Logger.e("exception: " + e.toString());
                return false;
            }
        }

        return false;
    }

    private AccessToken requestScopesUpdateBlocking(final AuthType authType, final Activity topActivity, final ResponseBody result) throws Exception {
        List<String> requiredScopes = null;
        if (result.has(StringSet.required_scopes)) {
            try {
                requiredScopes = ResponseStringConverter.IDENTITY_CONVERTER.convertList(result.getJson().optJSONArray(StringSet.required_scopes));
            } catch (ResponseBody.ResponseBodyException e) {
                throw new InsufficientScopeException(result);
            }
        }
        return requestScopesUpdate(authType, topActivity, requiredScopes);
    }

    private AccessToken requestScopesUpdate(final AuthType authType, final Activity topActivity, final List<String> scopes) throws Exception {
        final AuthCodeManager getter = AuthCodeManager.Factory.getInstance();

        final AtomicReference<String> authCodeResult = new AtomicReference<String>();
        final AtomicReference<Exception> exception = new AtomicReference<Exception>();
        final CountDownLatch lock = new CountDownLatch(1);

        final AuthCodeCallback callback = new AuthCodeCallback() {
            @Override
            public void onAuthCodeReceived(String authCode) {
                authCodeResult.set(authCode);
                lock.countDown();
            }

            @Override
            public void onAuthCodeFailure(ErrorResult errorResult) {
                exception.set(errorResult.getException());
                lock.countDown();
            }
        };

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    // 4. Scope update Webview Dialog??????.(????????? ??????) - AuthCode ??????.
                    getter.requestAuthCodeWithScopes(authType, new StartActivityWrapper(topActivity), scopes, callback);
                } catch (Exception e) {
                    exception.set(e);
                    lock.countDown();
                }
            }
        });

        // scope??? ?????????????????? ????????????.
        // ???????????? ????????? ????????? ??????.
        try {
            lock.await();
        } catch (InterruptedException e) {
            exception.set(e);
        }

        if (exception.get() != null) {
            throw new InsufficientScopeException(exception.toString());
        }

        String authCode = authCodeResult.get();
        if (authCode == null) {
            throw new InsufficientScopeException("Failed to get authorization code while requesting dynamic scope update.");
        }

        AccessToken accessToken = null;
        try {
            accessToken = AccessTokenManager.Factory.getInstance().requestAccessTokenByAuthCode(authCode, Session.getCurrentSession().getAccessTokenCallback()).get();
        } catch (Exception e) {
            Logger.e(e);
            throw new InsufficientScopeException(e.toString());
        }

        return accessToken;
    }

    private static int getErrorCode(ResponseBody responseBody) {
        try {
            // 1. check scopes error.
            if (responseBody.has(StringSet.code)) {
                return responseBody.getInt(StringSet.code);
            }
        } catch (ResponseBody.ResponseBodyException e) {
            Logger.e("exception while getting error code: " + e.toString());
        }
        return ApiErrorCode.UNDEFINED_ERROR_CODE;
    }

    Activity getTopActivity() {
        Activity topActivity = KakaoSDK.getCurrentActivity();
        if (topActivity == null) {
            // 3???????????? retry?????? ????????? ??????????????? ????????? ???????????? ?????????.
            int retryCount = 0;
            while(topActivity == null && retryCount < 3) {
                try {
                    retryCount++;
                    Thread.sleep(500);
                    topActivity = KakaoSDK.getCurrentActivity();
                } catch (InterruptedException e) {
                }
            }
        }
        return topActivity;
    }

    AuthType getAuthType() {
        return AuthType.KAKAO_ACCOUNT;
    }

    private boolean handleApiError(ResponseData result) throws Exception {
        boolean retry = false;
        ResponseBody errResponseBody = new ResponseBody(result.getHttpStatusCode(), result.getData());
        if (getErrorCode(errResponseBody) == ApiErrorCode.INVALID_TOKEN_CODE) {
            Session session = Session.getCurrentSession();
            session.getTokenInfo().clearAccessToken();

            if (session.getTokenInfo().hasRefreshToken()) {
                AccessToken futureToken = AccessTokenManager.Factory.getInstance().refreshAccessToken(session.getTokenInfo().getRefreshToken(), session.getAccessTokenCallback()).get();
                if (futureToken != null) {
                    return futureToken.hasValidAccessToken();
                    // futureToken is null when refresh token also has expired.. Just log user out here.
                }
            }
        } else if (getErrorCode(errResponseBody) == ApiErrorCode.INVALID_SCOPE_CODE) {
            Activity topActivity = getTopActivity();
            AuthType authType = getAuthType();
            retry = requestScopesUpdateBlocking(authType, topActivity, errResponseBody).hasValidAccessToken();
        } else if (getErrorCode(errResponseBody) == ApiErrorCode.NEED_TO_AGE_AUTHENTICATION) {
            Activity topActivity = getTopActivity();
            int state = DefaultAgeAuthService.getInstance().requestAgeAuth(null, topActivity);
            retry = state == AgeAuthStatus.SUCCESS.getValue() || state == AgeAuthStatus.ALREADY_AGE_AUTHORIZED.getValue();
        }
        return retry;
    }

    /**
     * ?????? API ????????? ????????? ????????? ????????? ????????????.
     * Background Thread
     * 1. accessToken ???????????? ?????? ??? ???????????? ????????? refreshToken?????? accessToken??????.
     * 2. ????????? API ??????.
     *
     * UI Thread
     * 1. result??? scope error ??????.
     * 2. Scope update Webview Dialog??????.(????????? ??????)
     * 3. AuthCode?????? ??? accessToken??????.
     *
     * Backgound Thread
     * 1. ????????? accessToken??? ????????? ?????? retry.
     *
     * @param request API ??????.
     * @return request??? ?????? responseBody
     * @throws Exception if request fails
     */
    @Deprecated
    public synchronized ResponseData requestApi(final IRequest request) throws Exception {
        // 1. accessToken ???????????? ?????? ??? ???????????? ????????? refreshToken?????? accessToken??????.
        if (!checkApiSession()) {
            throw new SessionClosedException("Application Session is Closed.");
        }

        // 2. ????????? API ??????.
        ResponseData result = request(request);
        Logger.d("++ [%s]response : %s", result.getHttpStatusCode(), result.getStringData());
        if (result.getHttpStatusCode() != HttpURLConnection.HTTP_OK) {
            if (handleApiError(result)) {
                // ????????? accessToken??? ????????? ?????? retry.
                return requestApi(request);
            }
        }

        return result;
    }

    @Deprecated
    public synchronized ResponseBody requestAuth(IRequest request) throws Exception {
        ResponseData result = request(request);
        Logger.d("++ [%s]response : %s", result.getHttpStatusCode(), result.getStringData());
        return new ResponseBody(result.getHttpStatusCode(), result.getData());
    }
}
