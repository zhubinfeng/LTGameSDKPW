package com.sdk.ltgame.paywall;

import android.app.Activity;
import android.content.Intent;

import com.gentop.ltgame.ltgamesdkcore.impl.OnRechargeListener;
import com.gentop.ltgame.ltgamesdkcore.model.RechargeResult;
import com.gentop.ltgame.ltgamesdkcore.model.Result;
import com.paymentwall.pwunifiedsdk.core.PaymentSelectionActivity;
import com.paymentwall.pwunifiedsdk.core.UnifiedRequest;
import com.paymentwall.pwunifiedsdk.util.Key;
import com.paymentwall.pwunifiedsdk.util.ResponseCode;
import com.paymentwall.sdk.pwlocal.utils.Const;
import com.sdk.ltgame.ltnet.base.Constants;
import com.sdk.ltgame.ltnet.manager.LoginRealizeManager;
import com.sdk.ltgame.ltnet.util.PreferencesUtils;

import java.lang.ref.WeakReference;
import java.util.Map;


class PayWallHelper {
    private WeakReference<Activity> mActivityRef;
    private OnRechargeListener mListener;
    private String mProjectKey;
    private String mSecretKey;
    private double amount;
    private String mCurrency;
    private String mItemName;
    private String mOderID;
    private String mProductID;
    private int mPayTest;
    private Map<String, Object> params;


    PayWallHelper(Activity activity, String mProjectKey, String mSecretKey,
                  double amount, String mCurrency, String mItemName,
                  int mPayTest, String mProductID, Map<String, Object> params,
                  OnRechargeListener listener, int target) {
        this.mActivityRef = new WeakReference<>(activity);
        this.mProjectKey = mProjectKey;
        this.mSecretKey = mSecretKey;
        this.amount = amount;
        this.mCurrency = mCurrency;
        this.mItemName = mItemName;
        this.mProductID = mProductID;
        this.mPayTest = mPayTest;
        this.params = params;
        this.mListener = listener;

    }


    /**
     * 开始支付
     */
    void startPayment() {
        if (PreferencesUtils.getString(mActivityRef.get(), Constants.USER_LT_UID) != null) {
            LoginRealizeManager.createOrder(mActivityRef.get(), mProductID, params, new OnRechargeListener() {

                @Override
                public void onState(Activity activity, RechargeResult result) {
                    if (result != null) {
                        if (result.state == Result.STATE_RECHARGE_SUCCESS) {
                            if (result.getResultModel().getData().getLt_order_id() != null) {
                                mOderID = result.getResultModel().getData().getLt_order_id();
                                UnifiedRequest request = new UnifiedRequest();
                                request.setPwProjectKey(mProjectKey);
                                request.setPwSecretKey(mSecretKey);
                                request.setAmount(amount);
                                request.setCurrency(mCurrency);
                                request.setItemName(mItemName);
                                request.setItemId(mOderID);

                                request.setUserId(PreferencesUtils.getString(mActivityRef.get(), Constants.USER_LT_UID));
                                request.setTimeout(30 * 1000);
                                request.setSignVersion(3);
                                if (mPayTest == 0) {
                                    request.setTestMode(true);
                                } else {
                                    request.setTestMode(false);
                                }
                                request.addPwLocal();
                                request.addPwlocalParams(Const.P.EMAIL, "fixed");
                                request.addPwlocalParams(Const.P.WIDGET, "pw");
                                request.addPwlocalParams(Const.P.PS, "all");
                                request.addPwlocalParams(Const.P.EVALUATION, "1");
                                Intent intent = new Intent(mActivityRef.get(), PaymentSelectionActivity.class);
                                intent.putExtra(Key.REQUEST_MESSAGE, request);
                                mActivityRef.get().startActivityForResult(intent, PaymentSelectionActivity.REQUEST_CODE);
                            }
                        } else {
                            mListener.onState(mActivityRef.get(), RechargeResult.failOf(result.getResultModel().getMsg()));
                        }

                    }
                }

            });
        }
    }


    /**
     * 回调
     */
    void onActivityResult(int resultCode) {
        switch (resultCode) {
            case ResponseCode.ERROR:
                mListener.onState(mActivityRef.get(), RechargeResult.stateOf(ResponseCode.ERROR));
                break;
            case ResponseCode.CANCEL:
                mListener.onState(mActivityRef.get(), RechargeResult.stateOf(ResponseCode.CANCEL));
                break;
            case ResponseCode.SUCCESSFUL:
                mListener.onState(mActivityRef.get(), RechargeResult.stateOf(ResponseCode.SUCCESSFUL));
                break;
            case ResponseCode.FAILED:
                mListener.onState(mActivityRef.get(), RechargeResult.stateOf(ResponseCode.FAILED));
                break;
            case ResponseCode.PROCESSING:
                mListener.onState(mActivityRef.get(), RechargeResult.stateOf(ResponseCode.PROCESSING));
                break;
            case ResponseCode.MERCHANT_PROCESSING:
                mListener.onState(mActivityRef.get(), RechargeResult.stateOf(ResponseCode.MERCHANT_PROCESSING));
                break;
            default:
                break;
        }
    }
}
