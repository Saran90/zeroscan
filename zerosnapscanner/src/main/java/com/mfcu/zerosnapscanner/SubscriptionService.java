package com.mfcu.zerosnapscanner;

import com.mfcu.zerosnapscanner.addSubscription.AddSubscriptionRequest;
import com.mfcu.zerosnapscanner.addSubscription.AddSubscriptionResponse;
import com.mfcu.zerosnapscanner.applicationdetails.GetApplicationDetailsResponse;
import com.mfcu.zerosnapscanner.cancelSubscription.CancelSubscriptionRequest;
import com.mfcu.zerosnapscanner.cancelSubscription.CancelSubscriptionResponse;
import com.mfcu.zerosnapscanner.checkSubscription.CheckSubscriptionResponse;
import com.mfcu.zerosnapscanner.getSubscription.GetSubscriptionResponse;
import com.mfcu.zerosnapscanner.getcurrencies.GetCurrenciesResponse;
import com.mfcu.zerosnapscanner.subscriptionPlans.SubscriptionPlansResponse;
import com.mfcu.zerosnapscanner.verifySubscription.VerifySubscriptionRequest;
import com.mfcu.zerosnapscanner.verifySubscription.VerifySubscriptionResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

import static com.mfcu.zerosnapscanner.NetworkUtils.ADD_SUSCRIPTION;
import static com.mfcu.zerosnapscanner.NetworkUtils.APPLICATION_DETAILS;
import static com.mfcu.zerosnapscanner.NetworkUtils.CANCEL_SUBSCRIPTION;
import static com.mfcu.zerosnapscanner.NetworkUtils.CHECK_SUSCRIPTION;
import static com.mfcu.zerosnapscanner.NetworkUtils.GET_CURRENCIES;
import static com.mfcu.zerosnapscanner.NetworkUtils.GET_SUBSCRIPTION;
import static com.mfcu.zerosnapscanner.NetworkUtils.NETWORK_AUTHORIZATION;
import static com.mfcu.zerosnapscanner.NetworkUtils.NETWORK_LICENCE_KEY;
import static com.mfcu.zerosnapscanner.NetworkUtils.NETWORK_ZEROSNAP_AUTHORIZATION;
import static com.mfcu.zerosnapscanner.NetworkUtils.SUBSCRIPTIONS;
import static com.mfcu.zerosnapscanner.NetworkUtils.VERIFY_SUSCRIPTION;


public interface SubscriptionService {

    @GET(APPLICATION_DETAILS)
    Call<GetApplicationDetailsResponse> getApplicationDetails(
            @Header(NETWORK_ZEROSNAP_AUTHORIZATION) String zerosnapToken,
            @Header(NETWORK_LICENCE_KEY) String licenceKey,
            @Query("applicationid") String applicationid
    );

    @GET(CHECK_SUSCRIPTION)
    Call<CheckSubscriptionResponse> checkSubscription(
            @Header(NETWORK_ZEROSNAP_AUTHORIZATION) String zerosnapToken,
            @Header(NETWORK_LICENCE_KEY) String licenceKey,
            @Query("client_id") String clientId
    );

    @GET(SUBSCRIPTIONS)
    Call<SubscriptionPlansResponse> subscriptionPlans(
            @Header(NETWORK_ZEROSNAP_AUTHORIZATION) String zerosnapToken,
            @Header(NETWORK_LICENCE_KEY) String licenceKey,
            @Query("currency_code") String currency
    );

    @POST(ADD_SUSCRIPTION)
    Call<AddSubscriptionResponse> addSubscription(
            @Header(NETWORK_ZEROSNAP_AUTHORIZATION) String zerosnapToken,
            @Header(NETWORK_LICENCE_KEY) String licenceKey,
            @Body AddSubscriptionRequest addSubscriptionRequest
    );

    @POST(VERIFY_SUSCRIPTION)
    Call<VerifySubscriptionResponse> verifySubscription(
            @Header(NETWORK_ZEROSNAP_AUTHORIZATION) String zerosnapToken,
            @Header(NETWORK_LICENCE_KEY) String licenceKey,
            @Body VerifySubscriptionRequest verifySubscriptionRequest
    );

    @GET(GET_SUBSCRIPTION)
    Call<GetSubscriptionResponse> getSubscription(
            @Header(NETWORK_ZEROSNAP_AUTHORIZATION) String zerosnapToken,
            @Header(NETWORK_LICENCE_KEY) String licenceKey,
            @Header(NETWORK_AUTHORIZATION) String auth,
            @Query("branch_id") String branchId
    );

    @HTTP(method = "DELETE", path = CANCEL_SUBSCRIPTION, hasBody = true)
    Call<CancelSubscriptionResponse> cancelSubscription(
            @Header(NETWORK_ZEROSNAP_AUTHORIZATION) String zerosnapToken,
            @Header(NETWORK_AUTHORIZATION) String auth,
            @Header(NETWORK_LICENCE_KEY) String licenceKey,
            @Body CancelSubscriptionRequest request
    );

    @GET(GET_CURRENCIES)
    Call<GetCurrenciesResponse> getCurrecncies(
            @Header(NETWORK_ZEROSNAP_AUTHORIZATION) String zerosnapToken
    );
}
