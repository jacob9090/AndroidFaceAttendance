package com.fruitoftek.androidfaceattendance.surfingtime.services;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import org.apache.commons.lang3.StringUtils;
import java.util.List;
import com.fruitoftek.androidfaceattendance.surfingtime.dto.ApiAttLog;
import com.fruitoftek.androidfaceattendance.surfingtime.dto.ApiBioPhoto;
import com.fruitoftek.androidfaceattendance.surfingtime.dto.ApiCommand;
import com.fruitoftek.androidfaceattendance.surfingtime.dto.ApiCommandUpdate;
import com.fruitoftek.androidfaceattendance.surfingtime.dto.ApiInfoRequest;
import com.fruitoftek.androidfaceattendance.surfingtime.dto.ApiInfoResponse;
import com.fruitoftek.androidfaceattendance.surfingtime.dto.ApiUser;
import com.fruitoftek.androidfaceattendance.surfingtime.dto.TokenRequest;
import com.fruitoftek.androidfaceattendance.detection.env.Logger;
import com.fruitoftek.androidfaceattendance.surfingtime.dto.TokenResponse;
import com.fruitoftek.androidfaceattendance.surfingtime.restclient.RetryableRetrofitCall;
import com.fruitoftek.androidfaceattendance.surfingtime.restclient.SurfingTimeServiceRestClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class SurfingTimeService {
    private static final Logger LOGGER = new Logger();
    private static String TAG = "SurfingTimeService";
    private SurfingTimeServiceRestClient surfingTimeServiceRestClient;
    private Context context;
    private static final int MAX_FAILS_FOR_DISABLING = 3;
    private int failsCount = 0;

    private String baseUrl = "";
    private String clientId = "";
    private String clientSecret = "";
    private String deviceSn = "";
    private String authToken = "";

    public SurfingTimeService(Context context) {
        this.context = context;
    }

    public ApiInfoResponse info(ApiInfoRequest apiInfoRequest) throws Exception {
        return genericRetryableHttpRequest(authHeader -> surfingTimeServiceRestClient.info(authHeader, apiInfoRequest));
    }

    public void syncAttendanceLogs(List<ApiAttLog> apiAttLogs) throws Exception {
        genericRetryableHttpRequest(authHeader -> surfingTimeServiceRestClient.upsertAttLogs(authHeader, apiAttLogs));
    }

    public void syncUser(int userId, ApiUser apiUser) throws Exception {
        genericRetryableHttpRequest(authHeader -> surfingTimeServiceRestClient.upsertUser(authHeader, userId, apiUser));
    }

    public void syncUserBioPhoto(int userId, ApiBioPhoto apiBioPhoto) throws Exception {
        genericRetryableHttpRequest(authHeader -> surfingTimeServiceRestClient.upsertBioPhotoForUser(authHeader, userId, apiBioPhoto));
    }

    public List<ApiCommand> getCommands() throws Exception {
        return genericRetryableHttpRequest(authHeader -> surfingTimeServiceRestClient.getCommands(authHeader));
    }

    public void pushCommandsUpdates(List<ApiCommandUpdate> apiCommandUpdates) throws Exception {
        genericRetryableHttpRequest(authHeader -> surfingTimeServiceRestClient.updateCommands(authHeader, apiCommandUpdates));
    }

    public ApiUser getUserById(int id) throws Exception {
        return genericRetryableHttpRequest(authHeader -> surfingTimeServiceRestClient.getUserById(authHeader, id));
    }

    public ApiBioPhoto getBioPhotoForUser(int id) throws Exception {
        return genericRetryableHttpRequest(authHeader -> surfingTimeServiceRestClient.getBioPhotoForUser(authHeader, id));
    }

    private void init() {
        init(false);
    }

    private void init(boolean forceInit) {
        if (forceInit) {
            surfingTimeServiceRestClient = null;
        }

        if (surfingTimeServiceRestClient == null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            baseUrl = sharedPreferences.getString("surfingTimeUrl", "");
            clientId = sharedPreferences.getString("surfingTimeClientId", "");
            clientSecret = sharedPreferences.getString("surfingTimeClientSecret", "");
            deviceSn = sharedPreferences.getString("deviceSn", "");

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(JacksonConverterFactory.create())
                    .build();

            surfingTimeServiceRestClient = retrofit.create(SurfingTimeServiceRestClient.class);
        }
    }

    private String buildAuthHeader() throws Exception {
        if (StringUtils.isEmpty(authToken)) {
            requestAuthToken();
        }

        return String.format("Bearer %s", authToken);
    }

    private void requestAuthToken() throws Exception {
        if (!isEnabled()) {
            throw new Exception("SurfingTime is not enabled");
        }

        LOGGER.i(TAG, "Requesting a new Auth Token");
        TokenRequest tokenRequest = new TokenRequest();
        tokenRequest.setClientId(clientId);
        tokenRequest.setClientSecret(clientSecret);
        tokenRequest.setDeviceSn(deviceSn);
        Response<TokenResponse> response = surfingTimeServiceRestClient.requestToken(tokenRequest).execute();
        if (response.isSuccessful()) {
            authToken = response.body().getToken();
            failsCount = 0;
        } else {// Something bad happened?
            LOGGER.e(TAG, "SurfingTime auth returned with http error code: " + response.code() + ". Message: " + response.message() + ". Try #" + failsCount);
            init(true);
            failsCount++;
            // The last attempts to get an Authentication token all failed?
            if (failsCount > MAX_FAILS_FOR_DISABLING) {
                // Disable SurfingTime sync, too many errors trying to communicate
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("surfingTimeEnabled", false);
                editor.commit();
            }
            throw new Exception("Failed to request a Bearer Token from SurfingTime");
        }
    }

    private <T> T genericRetryableHttpRequest(RetryableRetrofitCall<T> retryableRetrofitCall) throws Exception {
        init();
        int retryCount = 0;
        int maxRetries = 1; // How many times to retries after initial attempt
        String rawUrl = "";

        while(retryCount <= maxRetries) {
            String authHeader = buildAuthHeader();
            Call<T> call = retryableRetrofitCall.request(authHeader);
            rawUrl = call.request().url().toString();
            LOGGER.i(TAG, "Initiating request to: " + rawUrl);
            Response<T> response = call.execute();
            if (response.isSuccessful()) {
                return response.body();
            } else if (response.code() == 401) {// Refresh token and retry
                LOGGER.e(TAG, "SurfingTime returned with http error code: " + response.code() + ". Message: " + response.message() + ". New Token Required");
                requestAuthToken();
                retryCount++;
            } else if (response.code() >= 400 && response.code() < 500) {
                // Bad client input data, do not retry
                LOGGER.e(TAG, "SurfingTime returned with http error code: " + response.code() + ". Message: " + response.message() + ". Bad client input data, do not retry");
                requestAuthToken();
                break;
            } else {// Most Probably a 500, Retry
                retryCount++;
                LOGGER.e(TAG, "SurfingTime returned with http error code: " + response.code() + ". Message: " + response.message() + ".");
            }
        }

        throw new Exception("Failed to call SurfingTime at: " + rawUrl);
    }

    public boolean isEnabled() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean surfingTimeEnabled = sharedPreferences.getBoolean("surfingTimeEnabled", false);
        return surfingTimeEnabled;
    }

    public static boolean isAvailable(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean surfingTimeEnabled = sharedPreferences.getBoolean("surfingTimeEnabled", false);
        String surfingTimeUrl = sharedPreferences.getString("surfingTimeUrl", "");
        String surfingTimeClientId = sharedPreferences.getString("surfingTimeClientId", "");
        String surfingTimeClientSecret = sharedPreferences.getString("surfingTimeClientSecret", "");
        String deviceSn = sharedPreferences.getString("deviceSn", "");

        return  surfingTimeEnabled &&
                StringUtils.isNotEmpty(surfingTimeUrl) &&
                StringUtils.isNotEmpty(surfingTimeClientId) &&
                StringUtils.isNotEmpty(surfingTimeClientSecret) &&
                StringUtils.isNotEmpty(deviceSn);
    }


}
