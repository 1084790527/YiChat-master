package com.htmessage.fanxinht.acitivity.main.password;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.htmessage.fanxinht.utils.SendCodeUtils;
import com.htmessage.sdk.client.HTClient;
import com.htmessage.fanxinht.HTApp;
import com.htmessage.fanxinht.HTConstant;
import com.htmessage.fanxinht.R;
import com.htmessage.fanxinht.acitivity.login.LoginActivity;
import com.htmessage.fanxinht.utils.OkHttpUtils;
import com.htmessage.fanxinht.utils.Param;
import com.htmessage.fanxinht.utils.Validator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 项目名称：HTOpen
 * 类描述：PasswordPrester 描述:
 * 创建人：songlijie
 * 创建时间：2017/7/7 15:07
 * 邮箱:814326663@qq.com
 */
public class PasswordPrester implements PasswordBasePrester {
    private String TAG = PasswordPrester.class.getSimpleName();
    private PasswordView passwordView;

    public PasswordPrester(PasswordView passwordView) {
        this.passwordView = passwordView;
        this.passwordView.setPresenter(this);
    }

    @Override
    public void sendSMSCode(final String mobile, String countryName, String countryCode) {
        if (TextUtils.isEmpty(mobile)) {
            passwordView.showToast(R.string.mobile_not_be_null);
            return;
        }
        if (countryName.equals(passwordView.getBaseContext().getString(R.string.china)) && countryCode.equals(passwordView.getBaseContext().getString(R.string.country_code))){
            if (!Validator.isMobile(mobile)) {
                passwordView.showToast(R.string.please_input_true_mobile);
                return;
            }
            final Dialog dialog = HTApp.getInstance().createLoadingDialog(passwordView.getBaseActivity(), passwordView.getBaseActivity().getString(R.string.sending));
            dialog.show();
            passwordView.startTimeDown();
           new Handler().postDelayed(new Runnable() {
               @Override
               public void run() {
                   SendCodeUtils.getIntence().sendCodeNoNet(mobile, new SendCodeUtils.SmsCodeListener() {
                       @Override
                       public void onSuccess(String recCode, String recMsg, String smsCode) {
                           dialog.dismiss();
                           passwordView.onSendSMSCodeSuccess(smsCode);
                       }

                       @Override
                       public void onFailure(IOException e) {

                       }
                   });
               }
           },2000);
//            SendCodeUtils.getIntence().sendCode(mobile, new SendCodeUtils.SmsCodeListener() {
//                @Override
//                public void onSuccess(String recCode, String recMsg, final String smsCode) {
//                    passwordView.getBaseActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            dialog.dismiss();
//                            passwordView.onSendSMSCodeSuccess(smsCode);
//                        }
//                    });
//                }
//
//                @Override
//                public void onFailure(final IOException error) {
//                    passwordView.getBaseActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            dialog.dismiss();
//                            passwordView.showToast(error.getMessage());
//                            passwordView.finishTimeDown();
//                        }
//                    });
//                }
//            });
        }else{
            passwordView.onSendSMSCodeSuccess("1234");
        }
    }

    @Override
    public void resetPassword(String cacheCode, String smsCode, String password, String confimPwd, String mobile) {
        if (TextUtils.isEmpty(mobile)) {
            passwordView.showToast(R.string.mobile_not_be_null);
            return;
        }
        if (TextUtils.isEmpty(smsCode) || TextUtils.isEmpty(cacheCode)) {
            passwordView.showToast(R.string.please_input_code);
            return;
        }
        if (TextUtils.isEmpty(password) || TextUtils.isEmpty(confimPwd)) {
            passwordView.showToast(R.string.new_password_cannot_be_empty);
            return;
        }
        if (!cacheCode.equals(smsCode)){
            passwordView.showToast(R.string.code_is_wrong);
            return;
        }
        if (!password.equals(confimPwd)) {
            passwordView.showToast(R.string.Two_input_password);
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(passwordView.getBaseContext());
        progressDialog.setMessage(passwordView.getBaseContext().getString(R.string.are_reset_password));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        List<Param> params = new ArrayList<Param>();
        params.add(new Param("newPassword", password));
        params.add(new Param("tel", mobile));
        new OkHttpUtils(passwordView.getBaseContext()).post(params, HTConstant.URL_RESETPASSWORD, new OkHttpUtils.HttpCallBack() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                progressDialog.dismiss();
                int code = jsonObject.getIntValue("code");
                switch(code){
                    case 1:
                        passwordView.clearCacheCode();
                        passwordView.showToast(R.string.password_reset_success);
                        logOut(passwordView.getIsReset());
                        break;
                    default:
                        passwordView.showToast(R.string.password_reset_failed);
                        break;
                }
            }

            @Override
            public void onFailure(String errorMsg) {
                progressDialog.dismiss();
                passwordView.showToast(R.string.password_reset_failed);
            }
        });
    }

    private void logOut(boolean isReset) {
        if (isReset){
            HTClient.getInstance().logout(new HTClient.HTCallBack() {

                @Override
                public void onSuccess() {
                    passwordView.getBaseActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            // show login scree
                            HTApp.getInstance().setUserJson(null);
                            HTApp.getInstance().finishActivities();
                            passwordView.getBaseActivity().startActivity(new Intent(passwordView.getBaseActivity(), LoginActivity.class));
                            passwordView.getBaseActivity().finish();
                        }
                    });
                }

                @Override
                public void onError() {
                    passwordView.getBaseActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            passwordView.showToast(R.string.logout_failed);
                        }
                    });
                }
            });
        }else{
            passwordView.getBaseActivity().finish();
        }
    }

    @Override
    public void start() {

    }
}
