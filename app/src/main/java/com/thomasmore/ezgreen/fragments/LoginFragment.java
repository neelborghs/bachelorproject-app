package com.thomasmore.ezgreen.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thomasmore.ezgreen.MainActivity;
import com.thomasmore.ezgreen.R;
import com.thomasmore.ezgreen.model.Response;
import com.thomasmore.ezgreen.network.NetworkUtil;
import com.thomasmore.ezgreen.utils.Constants;

import java.io.IOException;

import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.thomasmore.ezgreen.utils.Validation.validateEmail;
import static com.thomasmore.ezgreen.utils.Validation.validateFields;

public class LoginFragment extends Fragment {

    public static final String TAG = LoginFragment.class.getSimpleName();

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private TextView tvForgotPassword;
    private TextInputLayout tiEmail;
    private TextInputLayout tiPassword;
    private ProgressBar progressBar;

    private CompositeSubscription csSubscriptions;
    private SharedPreferences spSharedPreferences;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        csSubscriptions = new CompositeSubscription();

        initSharedPreferences();
        initViews(view);

        return view;
    }

    private void initSharedPreferences() {
        spSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    private void initViews(View v) {
        etEmail = (EditText)v.findViewById(R.id.et_email);
        etPassword = (EditText) v.findViewById(R.id.et_password);
        btnLogin = (Button) v.findViewById(R.id.btn_login);
        tiEmail = (TextInputLayout) v.findViewById(R.id.ti_email);
        tiPassword = (TextInputLayout) v.findViewById(R.id.ti_password);
        progressBar = (ProgressBar) v.findViewById(R.id.progress);
        tvRegister = (TextView) v.findViewById(R.id.tv_register);
        tvForgotPassword = (TextView) v.findViewById(R.id.tv_forgot_password);

        btnLogin.setOnClickListener(view -> login());
        tvRegister.setOnClickListener(view -> goToRegister());
        tvForgotPassword.setOnClickListener(view -> showDialog());
    }

    private void login() {
        setError();

        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        int err = 0;

        if(!validateEmail(email)) {
            err++;
            tiEmail.setError(getString(R.string.login_error_email));
        }

        if(!validateFields(password)) {
            err++;
            tiPassword.setError(getString(R.string.login_error_password));
        }

        if(err == 0) {
            loginProcess(email, password);
            hideKeyboard();
            progressBar.setVisibility(View.VISIBLE);
        }

        /*else {
            showSnackBarMessage(getString(R.string.snack_login_details_unvalid));
        }*/
    }

    private void setError() {
        tiEmail.setError(null);
        tiPassword.setError(null);
    }

    private void loginProcess(String email, String password) {
        csSubscriptions.add(NetworkUtil.getRetrofit(email, password).login()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(Response response) {
        progressBar.setVisibility(View.GONE);

        SharedPreferences.Editor editor = spSharedPreferences.edit();
        editor.putString(Constants.TOKEN, response.getToken());
        editor.putString(Constants.EMAIL, response.getMessage());
        editor.apply();

        etEmail.setText(null);
        etPassword.setText(null);

        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void handleError(Throwable error) {
        progressBar.setVisibility(View.GONE);
        if (error instanceof HttpException) {

            Gson gson = new GsonBuilder().create();

            try {
                String errorBody = ((HttpException) error).response().errorBody().string();
                Response response = gson.fromJson(errorBody,Response.class);
                showSnackBarMessage(response.getMessage());

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showSnackBarMessage(getString(R.string.snack_network_error));
        }
    }

    private void showSnackBarMessage(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void goToRegister() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        RegisterFragment frag = new RegisterFragment();
        ft.replace(R.id.fragmentFrameAuth, frag, RegisterFragment.TAG).addToBackStack("");
        ft.commit();
    }

    private void showDialog() {
        ResetPasswordDialog frag = new ResetPasswordDialog();
        frag.show(getFragmentManager(), ResetPasswordDialog.TAG);
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
                .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);

        inputMethodManager.hideSoftInputFromWindow(
                getActivity().getCurrentFocus()
                        .getWindowToken(), 0);
    }

    //TODO kijken of ik nog ergens anders de onDestroy moet toevoegen

    @Override
    public void onDestroy() {
        super.onDestroy();
        csSubscriptions.unsubscribe();
    }
}
