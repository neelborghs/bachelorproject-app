package com.thomasmore.ezgreen.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.thomasmore.ezgreen.MainActivity;
import com.thomasmore.ezgreen.R;
import com.thomasmore.ezgreen.model.Response;
import com.thomasmore.ezgreen.model.User;
import com.thomasmore.ezgreen.network.NetworkUtil;
import com.thomasmore.ezgreen.utils.Constants;

import java.io.IOException;

import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.thomasmore.ezgreen.utils.Validation.validateFields;

/**
 * Created by Tomas-Laptop on 6/05/2017.
 */

public class ChangePasswordDialog extends DialogFragment {

    public static final String TAG = ChangePasswordDialog.class.getSimpleName();

    public interface Listener {
        void onPasswordChanged(String message);
    }

    private EditText etOldPassword;
    private EditText etNewPassword;
    private Button btnChangePassword;
    private Button btnCancel;
    private TextView tvMessage;
    private TextInputLayout tiOldPassword;
    private TextInputLayout tiNewPassword;
    private ProgressBar pbProgressBar;

    private SharedPreferences spSharedPreferences;
    private CompositeSubscription csSubscription;

    private String CREDENTIALS_EMAIL = "";
    private String CREDENTIALS_TOKEN = "";

    private Listener myListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_change_password,container,false);
        csSubscription = new CompositeSubscription();

        getData();
        initViews(view);

        return view;
    }

    private void getData() {
        spSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        CREDENTIALS_TOKEN = spSharedPreferences.getString(Constants.TOKEN,"");
        CREDENTIALS_EMAIL = spSharedPreferences.getString(Constants.EMAIL,"");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myListener = (MainActivity)context;
    }

    private void initViews(View v) {
        etOldPassword = (EditText) v.findViewById(R.id.et_old_password);
        etNewPassword = (EditText) v.findViewById(R.id.et_new_password);
        tiOldPassword = (TextInputLayout) v.findViewById(R.id.ti_old_password);
        tiNewPassword = (TextInputLayout) v.findViewById(R.id.ti_new_password);
        tvMessage = (TextView) v.findViewById(R.id.tv_message);
        btnChangePassword = (Button) v.findViewById(R.id.btn_change_password);
        btnCancel = (Button) v.findViewById(R.id.btn_cancel);
        pbProgressBar = (ProgressBar) v.findViewById(R.id.progress);

        btnChangePassword.setOnClickListener(view -> changePassword());
        btnCancel.setOnClickListener(view -> dismiss());
    }

    private void changePassword() {
        setError();

        String oldPassword = etOldPassword.getText().toString();
        String newPassword = etNewPassword.getText().toString();

        int err = 0;

        if (!validateFields(oldPassword)) {

            err++;
            tiOldPassword.setError("Password should not be empty !");
        }

        if (!validateFields(newPassword)) {

            err++;
            tiNewPassword.setError("Password should not be empty !");
        }

        if (err == 0) {

            User user = new User();
            user.setPassword(oldPassword);
            user.setNewPassword(newPassword);
            changePasswordProgress(user);
            pbProgressBar.setVisibility(View.VISIBLE);

        }
    }

    private void setError() {
        tiOldPassword.setError(null);
        tiNewPassword.setError(null);
    }

    private void changePasswordProgress(User user) {

        csSubscription.add(NetworkUtil.getRetrofit(CREDENTIALS_TOKEN).changePassword(CREDENTIALS_EMAIL,user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(Response response) {

        pbProgressBar.setVisibility(View.GONE);
        myListener.onPasswordChanged(response.getMessage());
        dismiss();
    }

    private void handleError(Throwable error) {

        pbProgressBar.setVisibility(View.GONE);

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
            showMessage("Network Error !");
        }
    }

    private void showMessage(String message) {

        tvMessage.setVisibility(View.VISIBLE);
        tvMessage.setText(message);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        csSubscription.unsubscribe();
    }
}
