package com.thomasmore.ezgreen;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.thomasmore.ezgreen.fragments.AboutFragment;
import com.thomasmore.ezgreen.fragments.AccountFragment;
import com.thomasmore.ezgreen.fragments.ChangePasswordDialog;
import com.thomasmore.ezgreen.fragments.DashboardFragment;
import com.thomasmore.ezgreen.model.Plant;
import com.thomasmore.ezgreen.model.User;
import com.thomasmore.ezgreen.network.NetworkUtil;
import com.thomasmore.ezgreen.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends AppCompatActivity implements ChangePasswordDialog.Listener{

    //public static final String TAG = MainActivity.class.getSimpleName();

    public static User authenticatedUser;
    public static List<Plant> plantData;

    private String CREDENTIALS_EMAIL = "";
    private String CREDENTIALS_TOKEN = "";

    private SharedPreferences spSharedPreferences;
    private CompositeSubscription csSubscription;

    private DashboardFragment fmDashboard;
    private AccountFragment fmAccount;
    private AboutFragment fmAbout;

    private ProgressDialog progressDialog;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_dashboard:
                    getSupportActionBar().setTitle("Dashboard");
                    loadDashboardFragment();
                    return true;
                case R.id.navigation_account:
                    getSupportActionBar().setTitle("Account");
                    loadAccountFragment();
                    return true;
                case R.id.navigation_about:
                    getSupportActionBar().setTitle("About");
                    loadAboutFragment();
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        getSupportActionBar().setTitle("Dashboard");


        csSubscription = new CompositeSubscription();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.progress_retrieve_data));
        progressDialog.setCancelable(false);
        progressDialog.show();

        initSharedPreferences();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionbar_dashboard_refresh:
                refresh();
                break;
            case R.id.actionbar_dashboard_logout:
                logout();
                break;
            default:
                break;
        }
        return true;
    }

    private void initSharedPreferences() {
        spSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        CREDENTIALS_EMAIL = spSharedPreferences.getString(Constants.EMAIL,"");
        CREDENTIALS_TOKEN = spSharedPreferences.getString(Constants.TOKEN,"");

        sessionExists();
    }

    private void sessionExists() {
        if(CREDENTIALS_TOKEN == "" || CREDENTIALS_EMAIL == "") {
            progressDialog.dismiss();
            userAuthentication();
        } else {
            setup();
        }
    }

    private void userAuthentication() {
        Intent intent = new Intent(MainActivity.this, UserAuthActivity.class);
        startActivity(intent);
        finish();
    }

    private void setup() {
        authenticatedUser = new User();
        plantData = new ArrayList<>();

        loadUserProfile();
    }

    private void loadUserProfile() {
        csSubscription.add(NetworkUtil.getRetrofit(CREDENTIALS_TOKEN).getProfile(CREDENTIALS_EMAIL)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleUserResponse,this::handleError));
    }

    private void handleUserResponse(User user) {
        authenticatedUser.setUser_id(user.getUser_id());
        authenticatedUser.setFirst_name(user.getFirst_name());
        authenticatedUser.setLast_name(user.getLast_name());
        authenticatedUser.setEmail(user.getEmail());
        authenticatedUser.setProfile_picture_url(user.getProfile_picture_url());

        if(authenticatedUser.getUser_id() != null ) {
            loadSensorData();
        } else {
            progressDialog.dismiss();
            loadDashboardFragment();
        }
    }

    private void loadSensorData() {
        csSubscription.add(NetworkUtil.getRetrofit().getSensorData(authenticatedUser.getUser_id())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleSensorResponse,this::handleError));
    }

    private void handleSensorResponse(List<Plant> plants) {
        plantData.addAll(plants);

        progressDialog.dismiss();

        loadDashboardFragment();
    }

    private void handleError(Throwable error) {
        progressDialog.dismiss();

        Log.w("Error: ",error.getMessage());
        if (error instanceof HttpException) {
            Log.w("Error","Http Exception");
            /*Gson gson = new GsonBuilder().create();

            try {

                String errorBody = ((HttpException) error).response().errorBody().string();
                Response response = gson.fromJson(errorBody,Response.class);
                showSnackBarMessage(response.getMessage());

            } catch (IOException e) {
                e.printStackTrace();
            }*/
        } else {
            showSnackBarMessage(getString(R.string.snack_network_error));
            //TODO: retry toevoegen
        }
    }

    private void loadDashboardFragment() {
        if(fmDashboard == null) {
            fmDashboard = new DashboardFragment();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrameMain, fmDashboard, DashboardFragment.TAG).commit();
    }

    private void loadAccountFragment() {
        if(fmAccount == null) {
            fmAccount = new AccountFragment();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrameMain, fmAccount, AccountFragment.TAG).commit();
    }

    private void loadAboutFragment() {
        if(fmAbout == null) {
            fmAbout = new AboutFragment();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrameMain, fmAbout, AboutFragment.TAG).commit();
    }

    private void refresh() {
        Intent intent = getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        startActivity(intent);
    }

    private void logout() {
        SharedPreferences.Editor editor = spSharedPreferences.edit();
        editor.putString(Constants.EMAIL,"");
        editor.putString(Constants.TOKEN,"");
        editor.putString(Constants.PROFILE_URL,"");
        editor.apply();

        userAuthentication();
    }

    @Override
    public void onPasswordChanged(String message) {
        showSnackBarMessage(message);
    }

    private void showSnackBarMessage(String message) {
        Snackbar.make(findViewById(R.id.activity_main),message,Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        csSubscription.unsubscribe();
    }
}
