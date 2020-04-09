package com.sdk.ltgame.paywall;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.gentop.ltgame.ltgamesdkcore.common.LTGameOptions;
import com.gentop.ltgame.ltgamesdkcore.common.LTGameSdk;
import com.gentop.ltgame.ltgamesdkcore.common.Target;
import com.gentop.ltgame.ltgamesdkcore.impl.OnRechargeListener;
import com.gentop.ltgame.ltgamesdkcore.model.RechargeObject;
import com.gentop.ltgame.ltgamesdkcore.platform.AbsPlatform;
import com.gentop.ltgame.ltgamesdkcore.platform.IPlatform;
import com.gentop.ltgame.ltgamesdkcore.platform.PlatformFactory;
import com.gentop.ltgame.ltgamesdkcore.uikit.BaseActionActivity;
import com.gentop.ltgame.ltgamesdkcore.util.LTGameUtil;
import com.sdk.ltgame.paywall.uikit.PayWallActionActivity;

public class PWPlatform extends AbsPlatform {

    private PayWallHelper mPayWallHelper;


    private PWPlatform(Context context, boolean isServerTest, String appId, String appKey, String adID, String packageID, int target) {
        super(context, isServerTest, appId, appKey, adID, packageID, target);
    }

    @Override
    public void recharge(Activity activity, int target, RechargeObject object, OnRechargeListener listener) {
        mPayWallHelper = new PayWallHelper(activity, object.getmProjectKey(), object.getmSecretKey(),
                object.getAmount(), object.getmCurrency(), object.getmItemName(), object.getPayTest(),
                object.getGoodsID(), object.getParams(), listener, target);
        mPayWallHelper.startPayment();
    }

    @Override
    public void onActivityResult(BaseActionActivity activity, int requestCode, int resultCode, Intent data) {
        mPayWallHelper.onActivityResult(resultCode);
    }

    @Override
    public Class getUIKitClazz() {
        return PayWallActionActivity.class;
    }


    /**
     * 工厂
     */
    public static class Factory implements PlatformFactory {

        @Override
        public IPlatform create(Context context, int target) {
            IPlatform platform = null;
            LTGameOptions options = LTGameSdk.options();
            if (!LTGameUtil.isAnyEmpty(options.getLtAppId(), options.getLtAppKey(), options.getAdID(), options.getPackageID())) {
                platform = new PWPlatform(context, options.getISServerTest(), options.getLtAppId(), options.getLtAppKey(),
                        options.getAdID(), options.getPackageID(), target);
            }
            return platform;
        }

        @Override
        public int getPlatformTarget() {
            return Target.PLATFORM_PAY_WALL;
        }

        @Override
        public boolean checkLoginPlatformTarget(int target) {
            return false;
        }

        @Override
        public boolean checkRechargePlatformTarget(int target) {
            return target == Target.RECHARGE_PAY_WALL;
        }
    }
}
