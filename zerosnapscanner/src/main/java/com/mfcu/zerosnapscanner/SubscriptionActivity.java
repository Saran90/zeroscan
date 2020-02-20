package com.mfcu.zerosnapscanner;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mfcu.zerosnapscanner.addSubscription.AddSubscriptionRequest;
import com.mfcu.zerosnapscanner.addSubscription.AddSubscriptionResponse;
import com.mfcu.zerosnapscanner.expandablelayout.ExpandableRelativeLayout;
import com.mfcu.zerosnapscanner.subscriptionPlans.SubscriptionPlansResponse;
import com.mfcu.zerosnapscanner.verifySubscription.VerifySubscriptionRequest;
import com.mfcu.zerosnapscanner.verifySubscription.VerifySubscriptionResponse;
import com.razorpay.Checkout;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;

import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;

import static com.mfcu.zerosnapscanner.ZerosnapScannerUtils.EXTRA_CURRENCY;
import static com.mfcu.zerosnapscanner.ZerosnapScannerUtils.EXTRA_CURRENCY_CODE;
import static com.mfcu.zerosnapscanner.ZerosnapScannerUtils.EXTRA_DOCUMENT_TYPE;
import static com.mfcu.zerosnapscanner.ZerosnapScannerUtils.EXTRA_LICENCE_KEY;
import static com.mfcu.zerosnapscanner.ZerosnapScannerUtils.EXTRA_USER_ID;

public class SubscriptionActivity extends Activity implements PaymentResultWithDataListener {

    private static final String TAG = SubscriptionActivity.class.getName();

    private String ANNUAL_SUBSCRIPTION_ID = "2";
    private String MONTHLY_SUBSCRIPTION_ID = "1";
    private String PAY_AS_YOU_GO_SUBSCRIPTION_ID = "3";
    private String DAILY_SUBSCRIPTION_ID = "4";
    private String WEEKLY_SUBSCRIPTION_ID = "5";

    public static final String SUBSCRIPTION_FLOW = "SUBSCRIPTION_FLOW";
    public static final String PACKAGE = "PACKAGE";
    public static final String NORMAL = "NORMAL";
    public static final String ZEROSNAP_TOKEN = "zerosnap";
    public static final String LICENCE_KEY = "Edapally@8899";

    private String userId,licenceKey;

    private AlertDialog alertDialog;

    TextView monthlyTitleTextView;

    TextView yearlyTitleTextView;

    ExpandableRelativeLayout monthlyExpandableRelativeLayout;

    ExpandableRelativeLayout yearlyExpandableRelativeLayout;

    TextView monthlyPriceTextView;

    TextView annuallyPriceTextView;

    Button monthlyPayButton;

    Button yearlyPayButton;

    private TextView textView;

    private String scanType;

    private List<SubscriptionPlansResponse.DataModel> subscriptDataModels;
    private String monthlyAmount = "";
    private String annualAmount = "";
    private String dailyAmount = "";
    private String weeklyAmount = "";
    private float scanCount = 0;
    private double payAsYouGoAmount = 0;
    //    private SweetAlertDialog pDialog;
    private String selectedSubscriptionId = "", selectedSubscriptionAmount = "",
            selectedSubscriptionOrderId = "", selectedSubscriptionPaymentId = "",
            selectedSubscriptionSecretId = "";
    private String selectedSubscriptionSubscribeId = "";
    private String selectedCurrency;
    private String selectedCurrencyCode;

    public void onCloseClicked(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void onSelectedSubscriptionCloseClicked() {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void onMonthlyClicked(View view) {
        yearlyExpandableRelativeLayout.collapse();
        monthlyExpandableRelativeLayout.expand();
        yearlyTitleTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.ic_arrow_down), null);
        monthlyTitleTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.ic_arrow_up), null);
    }

    public void onYearlyClicked(View view) {
        monthlyExpandableRelativeLayout.collapse();
        yearlyExpandableRelativeLayout.expand();
        monthlyTitleTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.ic_arrow_down), null);
        yearlyTitleTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.ic_arrow_up), null);
    }

    public void onMonthlyPayClicked(View view) {
        scanCount = 0;
        selectedSubscriptionId = subscriptDataModels.get(0).getSubscriptionPlanId();
        selectedSubscriptionAmount = subscriptDataModels.get(0).getSubscriptionAmount();
        processOrder();
    }

    public void onAnnuallyPayClicked(View view) {
        scanCount = 0;
        selectedSubscriptionId = subscriptDataModels.get(1).getSubscriptionPlanId();
        selectedSubscriptionAmount = subscriptDataModels.get(1).getSubscriptionAmount();
        processOrder();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        userId = getIntent().getStringExtra(EXTRA_USER_ID);
        licenceKey = getIntent().getStringExtra(EXTRA_LICENCE_KEY);
        scanType = getIntent().getStringExtra(EXTRA_DOCUMENT_TYPE);
        selectedCurrency = getIntent().getStringExtra(EXTRA_CURRENCY);
        selectedCurrencyCode = getIntent().getStringExtra(EXTRA_CURRENCY_CODE);
        loadViewForPackage();
        new FileDeleteAsync().execute();
    }

    private void initViews(){
        monthlyTitleTextView = findViewById(R.id.tv_monthly);
        yearlyTitleTextView = findViewById(R.id.tv_yearly);
        monthlyExpandableRelativeLayout = findViewById(R.id.erl_monthly);
        yearlyExpandableRelativeLayout = findViewById(R.id.erl_yearly);
        monthlyPriceTextView = findViewById(R.id.tv_monthly_price);
        annuallyPriceTextView = findViewById(R.id.tv_yearly_price);
        monthlyPayButton = findViewById(R.id.btn_monthly_pay);
        yearlyPayButton = findViewById(R.id.btn_yearly_pay);
    }

    private void deleteZerosnapFolder() {
        String mPath = Environment.getExternalStorageDirectory().toString() + "/" + getString(R.string.app_name);
        File file = new File(mPath);
        if (file.isDirectory())
            if (file.exists()) {
                String[] children = file.list();
                if (children != null) {
                    for (int i = 0; i < children.length; i++) {
                        new File(file, children[i]).delete();
                    }
                }
                file.delete();
            }

    }

    public class FileDeleteAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            deleteZerosnapFolder();
            return null;
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }

    public void loadView() {
        setContentView(R.layout.activity_subscription);
        initViews();
        getSubscriptionPlans();
        yearlyExpandableRelativeLayout.collapse();
        monthlyExpandableRelativeLayout.collapse();
        Checkout.preload(getApplicationContext());
    }

    public void loadViewForPackage() {
        setContentView(R.layout.activity_subscription);
        initViews();
        getSubscriptionPlans();
        yearlyExpandableRelativeLayout.collapse();
        monthlyExpandableRelativeLayout.expand();
        Checkout.preload(getApplicationContext());
    }

    public void getSubscriptionPlans() {
        showDialog();
        SubscriptionService subscriptionService = RetrofitClientInstance
                .getRetrofitInstance().create(SubscriptionService.class);
        Call<SubscriptionPlansResponse> subscriptionPlans = subscriptionService
                .subscriptionPlans(ZEROSNAP_TOKEN,
                        LICENCE_KEY,selectedCurrency);
        RetrofitApiHelper<SubscriptionPlansResponse> retrofitApiHelper =
                new RetrofitApiHelper<SubscriptionPlansResponse>();
        retrofitApiHelper.performApiCall(subscriptionPlans,
                new IRetrofitApiHelper<SubscriptionPlansResponse>() {
                    @Override
                    public void onSuccess(Response<SubscriptionPlansResponse> response) {
                        Log.d("TAG", "Response OK ");
                        hideDialog();
                        if (response.body().getStatus() == 200) {
                            subscriptDataModels =
                                    response.body().getDataModel();
                            monthlyAmount = subscriptDataModels.get(0).getSubscriptionAmount();
                            annualAmount = subscriptDataModels.get(1).getSubscriptionAmount();
                            String currency = Utils.getCurrencySymbol(selectedCurrency);
                            monthlyPriceTextView.setText(currency+monthlyAmount);
                            annuallyPriceTextView.setText(currency+annualAmount);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        hideDialog();
                        Toast.makeText(SubscriptionActivity.this,
                                message, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                });
    }

    public void processOrder() {
        showDialog();
        AddSubscriptionRequest addSubscriptionRequest = new AddSubscriptionRequest();
        addSubscriptionRequest.setClientId(userId);
        addSubscriptionRequest.setSubscriptionPlanId(selectedSubscriptionId);
        addSubscriptionRequest.setTotalAmount(selectedSubscriptionAmount);
        addSubscriptionRequest.setDeviceType("android");
        addSubscriptionRequest.setDeviceToken("");
        addSubscriptionRequest.setCurrencyCode(selectedCurrency);
        addSubscriptionRequest.setScanCount(String.valueOf(scanCount));
        SubscriptionService subscriptionService = RetrofitClientInstance
                .getRetrofitInstance().create(SubscriptionService.class);
        Call<AddSubscriptionResponse> addSubscription = subscriptionService
                .addSubscription(ZEROSNAP_TOKEN, LICENCE_KEY, addSubscriptionRequest);
        RetrofitApiHelper<AddSubscriptionResponse> retrofitApiHelper =
                new RetrofitApiHelper<>();
        retrofitApiHelper.performApiCall(addSubscription,
                new IRetrofitApiHelper<AddSubscriptionResponse>() {
                    @Override
                    public void onSuccess(Response<AddSubscriptionResponse> response) {
                        Log.d("TAG", "Response OK ");
                        hideDialog();
                        if (response.body().getStatus() == 200) {
                            selectedSubscriptionOrderId = response.body().getDataModel().getOrderId();
                            selectedSubscriptionSubscribeId = response.body().getDataModel().getRazorPaySubscriptionId();
                            startPayment(selectedSubscriptionSubscribeId, selectedSubscriptionOrderId, selectedSubscriptionAmount);
                        }else {
                            Toast.makeText(SubscriptionActivity.this,
                                    response.body().getStatusMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        hideDialog();
                    }
                });
    }

    public void startPayment(String subscriptionId, String orderId, String amount) {
        changeDialogTitle("Configuring Payment...");
        /**
         * Instantiate Checkout
         */
        Checkout checkout = new Checkout();

        /**
         * Set your logo here
         */
//        checkout.setImage(R.mipmap.ic_launcher);

        /**
         * Reference to current activity
         */
        final Activity activity = this;

        /**
         * Pass your payment options to the Razorpay Checkout as a JSONObject
         */
        try {
            JSONObject options = new JSONObject();

            /**
             * Merchant Name
             * eg: ACME Corp || HasGeek etc.
             */
            options.put("name", "Zerosnap");

            /**
             * Description can be anything
             * eg: Order #123123 - This order number is passed by you for your internal reference. This is not the `razorpay_order_id`.
             *     Invoice Payment
             *     etc.
             */
            options.put("description", "Order #123456");
            if (selectedSubscriptionId.equalsIgnoreCase(PAY_AS_YOU_GO_SUBSCRIPTION_ID))
                options.put("order_id", orderId);
            else {
                options.put("subscription_id", subscriptionId);
                options.put("recurring", 1);
            }
            options.put("currency", selectedCurrency);
            Log.d("Currency: ",selectedCurrency);

            /**
             * Amount is always passed in currency subunits
             * Eg: "500" = INR 5.00
             */
            double amountInt = Double.parseDouble(amount) * 100;
            options.put("amount", /*String.valueOf(amountInt)*/100);

            checkout.open(activity, options);
        } catch (Exception e) {
            Log.e(TAG, "Error in starting Razorpay Checkout", e);
        }
    }

    public void changeDialogTitle(String title) {
        updateAlertTitle(title);
    }

    public void showDialog() {
        showProgressDialog("Processing...");
    }

    public void hideDialog() {
        hideProgressDialog();
    }

    public void verifySubscription() {
        showDialog();
        VerifySubscriptionRequest verifySubscriptionRequest = new VerifySubscriptionRequest();
        if (selectedSubscriptionId.equalsIgnoreCase(PAY_AS_YOU_GO_SUBSCRIPTION_ID))
            verifySubscriptionRequest.setOrderId(selectedSubscriptionOrderId);
        else
            verifySubscriptionRequest.setSubscriptionId(selectedSubscriptionSubscribeId);
        verifySubscriptionRequest.setPaymentId(selectedSubscriptionPaymentId);
        verifySubscriptionRequest.setSignature(selectedSubscriptionSecretId);
        SubscriptionService subscriptionService = RetrofitClientInstance
                .getRetrofitInstance().create(SubscriptionService.class);
        Call<VerifySubscriptionResponse> verifySubscription = subscriptionService
                .verifySubscription(ZEROSNAP_TOKEN,
                        LICENCE_KEY, verifySubscriptionRequest);
        RetrofitApiHelper<VerifySubscriptionResponse> retrofitApiHelper =
                new RetrofitApiHelper<>();
        retrofitApiHelper.performApiCall(verifySubscription,
                new IRetrofitApiHelper<VerifySubscriptionResponse>() {
                    @Override
                    public void onSuccess(Response<VerifySubscriptionResponse> response) {
                        Log.d("TAG", "Response OK ");
                        if (response.body().getStatus() == 200) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    hideDialog();
                                    navigateToSubscriptionPage();
                                }
                            }, 2000);
                        } else if (response.body().getStatus() == 400) {
                            Toast.makeText(SubscriptionActivity.this,
                                    response.body().getStatusMessage(), Toast.LENGTH_SHORT).show();
                            hideDialog();
                            setResult(RESULT_CANCELED);
                            finish();
                        }else {
                            Toast.makeText(SubscriptionActivity.this,
                                    response.body().getStatusMessage(), Toast.LENGTH_SHORT).show();
                            hideDialog();
                            setResult(RESULT_CANCELED);
                            finish();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        hideDialog();
                        Toast.makeText(SubscriptionActivity.this,
                                message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onPaymentSuccess(String s, PaymentData paymentData) {
//        hideDialog();
        updateAlertTitle("Verifying Payment...");
        Log.d(TAG, "Success! " + s);
        selectedSubscriptionOrderId = paymentData.getOrderId();
        selectedSubscriptionPaymentId = paymentData.getPaymentId();
        selectedSubscriptionSecretId = paymentData.getSignature();
        verifySubscription();
    }

    @Override
    public void onPaymentError(int i, String s, PaymentData paymentData) {
        hideDialog();
        Log.d(TAG, s);
        Log.d(TAG, "Failed! " + s);
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        hideDialog();
    }

    public void showProgressDialog(String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.myDialog));
        View inflate = getLayoutInflater().inflate(R.layout.sweet_alert_progress,
                null, false);
        textView  = inflate.findViewById(R.id.statusTextView);
        textView.setText(title);
        builder.setView(inflate);
        alertDialog = builder.create();
        alertDialog.setCancelable(false);
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (!alertDialog.isShowing())
            alertDialog.show();
    }

    private void hideProgressDialog(){
        if (alertDialog!=null)
            alertDialog.dismiss();
    }

    private void updateAlertTitle(String title){
        if (textView!=null)
            textView.setText(title);
    }

    private void navigateToSubscriptionPage(){
        setResult(RESULT_OK);
        finish();
    }
}
