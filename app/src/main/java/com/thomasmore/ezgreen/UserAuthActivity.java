package com.thomasmore.ezgreen;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.thomasmore.ezgreen.fragments.LoginFragment;
import com.thomasmore.ezgreen.fragments.ResetPasswordDialog;

public class UserAuthActivity extends AppCompatActivity implements ResetPasswordDialog.Listener {

    public static final String TAG = UserAuthActivity.class.getSimpleName();

    private LoginFragment fmLoginFragment;
    private ResetPasswordDialog fmResetPasswordDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_auth);

        if (savedInstanceState == null) {
            loadFragment();
        }
    }

    private void loadFragment() {
        if (fmLoginFragment == null) {
            fmLoginFragment = new LoginFragment();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrameAuth, fmLoginFragment, LoginFragment.TAG).commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String data = intent.getData().getLastPathSegment();
        Log.d(TAG, "onNewIntent: "+data);

        fmResetPasswordDialog = (ResetPasswordDialog)getSupportFragmentManager().findFragmentByTag(ResetPasswordDialog.TAG);

        if (fmResetPasswordDialog != null)
            fmResetPasswordDialog.setToken(data);
    }

    @Override
    public void onPasswordReset(String message) {
        showSnackBarMessage(message);
    }

    private void showSnackBarMessage(String message) {
        Snackbar.make(findViewById(R.id.activity_user_auth),message,Snackbar.LENGTH_SHORT).show();
    }
}
