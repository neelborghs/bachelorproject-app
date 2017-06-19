package com.thomasmore.ezgreen.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thomasmore.ezgreen.R;
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

public class RegisterFragment extends Fragment {

    public static final String TAG = RegisterFragment.class.getSimpleName();

    private EditText etFirstName;
    private EditText etLastName;
    private EditText etEmail;
    private EditText etPassword;
    private Button btnRegister;
    //private TextView tvLogin;
    private TextInputLayout tiFirstName;
    private TextInputLayout tiLastName;
    private TextInputLayout tiEmail;
    private TextInputLayout tiPassword;
    private ProgressBar Progressbar;

    private CompositeSubscription csSubscription;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        csSubscription = new CompositeSubscription();
        initViews(view);

        return view;
    }

    private void initViews(View v) {
        etFirstName = (EditText) v.findViewById(R.id.et_first_name);
        etLastName = (EditText) v.findViewById(R.id.et_last_name);
        etEmail = (EditText) v.findViewById(R.id.et_email);
        etPassword = (EditText) v.findViewById(R.id.et_password);
        btnRegister = (Button) v.findViewById(R.id.btn_register);
        //tvLogin = (TextView) v.findViewById(R.id.tv_login);
        tiFirstName = (TextInputLayout) v.findViewById(R.id.ti_first_name);
        tiLastName = (TextInputLayout) v.findViewById(R.id.ti_last_name);
        tiEmail = (TextInputLayout) v.findViewById(R.id.ti_email);
        tiPassword = (TextInputLayout) v.findViewById(R.id.ti_password);
        Progressbar = (ProgressBar) v.findViewById(R.id.progress);

        btnRegister.setOnClickListener(view -> register());

        //TODO actionbar title register + button go back

        //TODO user already registered!!!!!!
    }

    private void register() {
        setError();

        String firstName = etFirstName.getText().toString();
        String lastName = etLastName.getText().toString();
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        int err = 0;

        if (!validateFields(firstName)) {
            err++;
            tiFirstName.setError(getString(R.string.register_error_first_name));
        }

        if (!validateFields(lastName)) {
            err++;
            tiLastName.setError(getString(R.string.register_error_last_name));
        }

        if (!validateEmail(email)) {
            err++;
            tiEmail.setError(getString(R.string.register_error_email));
        }

        if (!validateFields(password)) {
            err++;
            tiPassword.setError(getString(R.string.register_error_password));
        }

        if (err == 0) {
            User user = new User();
            user.setFirst_name(firstName);
            user.setLast_name(lastName);
            user.setEmail(email);
            user.setPassword(password);

            Progressbar.setVisibility(View.VISIBLE);
            registerProcess(user);

        } /*else {
            showSnackBarMessage(getString(R.string.snack_user_details_unvalid), false);
        }*/
    }

    private void setError() {
        tiFirstName.setError(null);
        tiLastName.setError(null);
        tiEmail.setError(null);
        tiPassword.setError(null);
    }

    private void registerProcess(User user) {
        csSubscription.add(NetworkUtil.getRetrofit().register(user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(Response response) {
        Progressbar.setVisibility(View.GONE);
        showSnackBarMessage(response.getMessage(), false);
    }

    private void handleError(Throwable error) {
        Progressbar.setVisibility(View.GONE);

        if (error instanceof HttpException) {

            Gson gson = new GsonBuilder().create();

            try {
                String errorBody = ((HttpException) error).response().errorBody().string();
                Response response = gson.fromJson(errorBody,Response.class);
                showSnackBarMessage(response.getMessage(), false);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showSnackBarMessage(getString(R.string.snack_network_error), false);
        }
    }

    private void showSnackBarMessage(String message, boolean showButton) {
        if (getView() != null) {
            if(showButton) {
                //TODO add login button to snackbar
                //Snackbar sb = Snackbar.make(scrollView, message, Snackbar.LENGTH_LONG)
            } else {
                Snackbar.make(getView(),message,Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        csSubscription.unsubscribe();
    }
}
