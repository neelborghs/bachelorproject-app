package com.thomasmore.ezgreen.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thomasmore.ezgreen.MainActivity;
import com.thomasmore.ezgreen.R;
import com.thomasmore.ezgreen.adapters.DashboardAdapter;
import com.thomasmore.ezgreen.model.Response;
import com.thomasmore.ezgreen.model.User;
import com.thomasmore.ezgreen.network.NetworkUtil;
import com.thomasmore.ezgreen.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.thomasmore.ezgreen.MainActivity.authenticatedUser;
import static com.thomasmore.ezgreen.MainActivity.plantData;

public class DashboardFragment extends Fragment {

    public static final String TAG = DashboardFragment.class.getSimpleName();

    private TextView tvWelcome;
    private CardView cvNoModule;

    ProgressDialog progressDialog;

    private CompositeSubscription csSubscription;

    private RecyclerView recyclerView;
    private FloatingActionButton fab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(fabView -> {
            try {
                addModule(fabView);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        csSubscription = new CompositeSubscription();

        initViews(view);

        return view;
    }

    private void initViews(View v) {
        tvWelcome = (TextView)v.findViewById(R.id.dashboard_welcome);
        cvNoModule = (CardView)v.findViewById(R.id.no_module);
        //pbProgressbar = (ProgressBar)v.findViewById(R.id.progress);

        recyclerView = (RecyclerView)v.findViewById(R.id.recycler_view);

        fab = (FloatingActionButton)v.findViewById(R.id.fab);

        tvWelcome.setText(getString(R.string.dashboard_welcome) + " " + authenticatedUser.getFirst_name() + "!");

        if(authenticatedUser.getUser_id() != null) fab.hide();

        if(!MainActivity.plantData.isEmpty()) {
            fab.hide();
            RecyclerView rv = (RecyclerView)v.findViewById(R.id.recycler_view);
            rv.setHasFixedSize(true);

            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            rv.setLayoutManager(llm);

            DashboardAdapter adapter = new DashboardAdapter(plantData);
            rv.setAdapter(adapter);
        } else {
            cvNoModule.setVisibility(View.VISIBLE);
        }

        /*recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE){
                    if(canScroll()) {
                        //hides fab when at top of the list
                        fab.show();
                    }
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 ||dy<0 && fab.isShown())
                    fab.hide();
            }
        });*/
    }

    private void addModule(View v) throws IOException {
        new JsonTask().execute(Constants.PI_URL);
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getString(R.string.progress_search_module));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = null;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                return buffer.toString();

            } catch (MalformedURLException e) {
                Log.w("test",e.toString());
            } catch (IOException e) {
                Log.w("test",e.toString());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    Log.w("test",e.toString());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.w("test","1 =>" + result);
            try {
                JSONArray jArray = new JSONArray("[" + result + "]");
                JSONObject jObject = jArray.getJSONObject(0);
                String userId = jObject.getString("cpuserial");
                insertUserId(userId);

            } catch (JSONException e) {
                Log.w("test",e.toString());
                if (progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
            }
        }
    }

    private void insertUserId(String id) {
        Log.w("test","2 =>" +id);
        User user = new User();
        user.setEmail(MainActivity.authenticatedUser.getEmail());
        user.setUser_id(id);

        csSubscription.add(NetworkUtil.getRetrofit().setUserId(user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(Response response) {
        if (progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        refresh();
    }

    private void handleError(Throwable throwable) {
        Log.w("test",throwable.toString());
        if (progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }

    private void refresh() {
        Intent intent = getActivity().getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        getActivity().finish();
        startActivity(intent);
    }
}
