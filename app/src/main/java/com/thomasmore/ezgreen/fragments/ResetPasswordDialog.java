package com.thomasmore.ezgreen.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thomasmore.ezgreen.R;
import com.thomasmore.ezgreen.UserAuthActivity;
import com.thomasmore.ezgreen.model.Response;
import com.thomasmore.ezgreen.model.User;
import com.thomasmore.ezgreen.network.NetworkUtil;

import java.io.IOException;

import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.thomasmore.ezgreen.utils.Validation.validateEmail;
import static com.thomasmore.ezgreen.utils.Validation.validateFields;


public class ResetPasswordDialog extends DialogFragment {

    public interface Listener {
        void onPasswordReset(String message);
    }

    public static final String TAG = ResetPasswordDialog.class.getSimpleName();

    private EditText etEmail;
    private EditText etToken;
    private EditText etPassword;
    private Button btnResetPassword;
    private TextView tvMessage;
    private TextInputLayout tiEmail;
    private TextInputLayout tiToken;
    private TextInputLayout tiPassword;
    private ProgressBar ProgressBar;

    private CompositeSubscription csSubscriptions;

    private String email;

    private boolean isInit = true;

    private Listener myListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_reset_password, container, false);

        csSubscriptions = new CompositeSubscription();
        initViews(view);

        return view;
    }

    private void initViews(View v) {
        etEmail = (EditText) v.findViewById(R.id.et_email);
        etToken = (EditText) v.findViewById(R.id.et_token);
        etPassword = (EditText) v.findViewById(R.id.et_password);
        btnResetPassword = (Button) v.findViewById(R.id.btn_reset_password);
        ProgressBar = (ProgressBar) v.findViewById(R.id.progress);
        tvMessage = (TextView) v.findViewById(R.id.tv_message);
        tiEmail = (TextInputLayout) v.findViewById(R.id.ti_email);
        tiToken = (TextInputLayout) v.findViewById(R.id.ti_token);
        tiPassword = (TextInputLayout) v.findViewById(R.id.ti_password);

        btnResetPassword.setOnClickListener(view -> {
            if (isInit) resetPasswordInit();
            else resetPasswordFinish();
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myListener = (UserAuthActivity)context;
    }

    private void setEmptyFields() {
        tiEmail.setError(null);
        tiToken.setError(null);
        tiPassword.setError(null);
        tvMessage.setText(null);
    }

    public void setToken(String token) {
        etToken.setText(token);
    }

    //TODO: passwoorden 2x laten ingeven
    private void resetPasswordInit() {
        setEmptyFields();

        email = etEmail.getText().toString();

        int err = 0;

        if (!validateEmail(email)) {
            err++;
            tiEmail.setError(getString(R.string.reset_pw_error_email));
        }

        if (err == 0) {
            ProgressBar.setVisibility(View.VISIBLE);
            resetPasswordInitProgress(email);
        }
    }

    private void resetPasswordFinish() {
        setEmptyFields();

        String token = etToken.getText().toString();
        String password = etPassword.getText().toString();

        int err = 0;

        if (!validateFields(token)) {
            err++;
            tiToken.setError(getString(R.string.reset_pw_error_token));
        }

        if (!validateFields(password)) {
            err++;
            tiEmail.setError(getString(R.string.reset_pw_error_password));
        }
        if (err == 0) {
            ProgressBar.setVisibility(View.VISIBLE);

            User user = new User();
            user.setPassword(password);
            user.setToken(token);
            resetPasswordFinishProgress(user);
        }
    }

    private void resetPasswordInitProgress(String email) {

        csSubscriptions.add(NetworkUtil.getRetrofit().resetPasswordInit(email)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void resetPasswordFinishProgress(User user) {

        csSubscriptions.add(NetworkUtil.getRetrofit().resetPasswordFinish(email,user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(Response response) {

        ProgressBar.setVisibility(View.GONE);

        if (isInit) {
            isInit = false;
            showMessage(response.getMessage());
            tiEmail.setVisibility(View.GONE);
            tiToken.setVisibility(View.VISIBLE);
            tiPassword.setVisibility(View.VISIBLE);

        } else {
            myListener.onPasswordReset(response.getMessage());
            dismiss();
        }
    }

    private void handleError(Throwable error) {
        ProgressBar.setVisibility(View.GONE);

        if (error instanceof HttpException) {

            Gson gson = new GsonBuilder().create();

            try {
                String errorBody = ((HttpException) error).response().errorBody().string();
                Response response = gson.fromJson(errorBody,Response.class);
                showMessage(response.getMessage());

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showMessage(getString(R.string.snack_network_error));
        }
    }

    private void showMessage(String message) {
        tvMessage.setVisibility(View.VISIBLE);
        tvMessage.setText(message);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        csSubscriptions.unsubscribe();
    }
}
