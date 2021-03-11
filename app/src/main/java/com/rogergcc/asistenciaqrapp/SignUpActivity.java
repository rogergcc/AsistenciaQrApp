package com.rogergcc.asistenciaqrapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class SignUpActivity extends PhoneNumberActivity  {

    public static final String TAG = SignUpActivity.class.getSimpleName();

    @Override
    protected String getActivityTitle() {
        return getString(R.string.sign_up_title);
    }

    @Override
    protected void doSubmit(String phoneValue) {
        Log.d(TAG, "Using the phone number.");
//        PhoneNumberVerifier.startActionVerify(this, phoneValue);
        Toast.makeText(this, "PHone "+phoneValue, Toast.LENGTH_SHORT).show();
        finish();
    }

}