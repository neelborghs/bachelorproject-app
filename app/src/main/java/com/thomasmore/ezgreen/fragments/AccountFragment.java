package com.thomasmore.ezgreen.fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.thomasmore.ezgreen.BuildConfig;
import com.thomasmore.ezgreen.MainActivity;
import com.thomasmore.ezgreen.R;
import com.thomasmore.ezgreen.model.Response;
import com.thomasmore.ezgreen.model.User;
import com.thomasmore.ezgreen.network.NetworkUtil;
import com.thomasmore.ezgreen.network.PicassoClient;
import com.thomasmore.ezgreen.utils.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static android.app.Activity.RESULT_OK;
import static com.thomasmore.ezgreen.MainActivity.authenticatedUser;


public class AccountFragment extends Fragment {

    public static final String TAG = AccountFragment.class.getSimpleName();

    private EditText etFirstName;
    private EditText etLastName;
    private EditText etEmail;
    private ImageView ivProfilePicture;
    private Button btnChangePassword;

    private ProgressDialog progressDialog;

    private File photoFile;
    private Cloudinary cloudinary;
    private CompositeSubscription csSubscription;
    private String tempUrl;

    private final int REQUEST_CAMERA = 1;
    private final int SELECT_FILE = 2;
    private final int REQUEST_CODE_ALL_PERMISSIONS = 123;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        csSubscription = new CompositeSubscription();

        initViews(view);
        loadUserDetails();

        return view;
    }

    private void initViews(View v) {
        etFirstName = (EditText)v.findViewById(R.id.et_first_name);
        etLastName = (EditText) v.findViewById(R.id.et_last_name);
        etEmail = (EditText)v.findViewById(R.id.et_email);
        btnChangePassword = (Button)v.findViewById(R.id.btn_change_password);

        ivProfilePicture = (ImageView)v.findViewById(R.id.profile_picture);
        downloadImage(authenticatedUser.getProfile_picture_url());
        ivProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MainActivity.authenticatedUser.getUser_id() != null) {
                    checkPermissions();
                } else {
                    Snackbar.make(v,getString(R.string.snack_link_modules), Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        btnChangePassword.setOnClickListener(view -> showChangePasswordDialog());
    }

    private void loadUserDetails() {
        etFirstName.setText(authenticatedUser.getFirst_name());
        etLastName.setText(authenticatedUser.getLast_name());
        etEmail.setText(authenticatedUser.getEmail());
    }

    private void showChangePasswordDialog() {
        ChangePasswordDialog frag = new ChangePasswordDialog();
        frag.show(getFragmentManager(), ChangePasswordDialog.TAG);
    }

    // Permission methods
    private void checkPermissions() {
        List<String> permissionsNeeded = new ArrayList<String>();
        final List<String> permissionsList = new ArrayList<String>();

        if (!addPermission(permissionsList, Manifest.permission.CAMERA))
            permissionsNeeded.add(" Camera");
        if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionsNeeded.add("Write External Storage");

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = getString(R.string.account_permission_grant) + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);
                showMessageOKCancel(message,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                        REQUEST_CODE_ALL_PERMISSIONS);
                            }
                        });
                return;
            }
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ALL_PERMISSIONS);
            return;
        }
        selectImageDialog();
    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ALL_PERMISSIONS:
            {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    selectImageDialog();
                } else {
                    // Permission Denied
                    Toast.makeText(getActivity(), getString(R.string.toast_perm_denied), Toast.LENGTH_SHORT)
                            .show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // Select image methods
    private void selectImageDialog() {

        final CharSequence[] options = { getString(R.string.account_options_camera),
                                        getString(R.string.account_options_gallery),
                                        getString(R.string.account_options_cancel)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals(getString(R.string.account_options_camera))) {
                    Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePhotoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                        // Create the File where the photo should go
                        photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                            return;
                        }
                        // Continue only if the File was successfully created
                        if (photoFile != null) {

                            Uri photoURI = FileProvider.getUriForFile(getActivity(),
                                    BuildConfig.APPLICATION_ID + ".provider",
                                    photoFile);
                            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(takePhotoIntent, 1);
                        }
                    }
                }
                else if (options[item].equals(getString(R.string.account_options_gallery))) {
                    Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);
                }
                else if (options[item].equals(getString(R.string.account_options_cancel))) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CAMERA) {
            if (resultCode == RESULT_OK) {
                uploadImage();
            }

        } else if (requestCode == SELECT_FILE) {
            if (resultCode == RESULT_OK) {
                photoFile = new File(getRealPathFromURI(data.getData()));
                uploadImage();
            }
        }
    }

    private void uploadImage() {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.progress_upload));
        progressDialog.setCancelable(false);
        progressDialog.show();

        cloudinary = new Cloudinary(Config.getConfig());
        UploadImage imgUploader= new UploadImage(resizeImage(photoFile));
        imgUploader.execute();
    }

    private void downloadImage(String url) {

        if(url != "" && url != null) {
            cloudinary = new Cloudinary(Config.getConfig());
            PicassoClient.downloadProfileImage(getActivity().getApplicationContext(),
                    url, ivProfilePicture);
            if (progressDialog != null) progressDialog.dismiss();
        } else {
            if (progressDialog != null) progressDialog.dismiss();
        }
    }

    private class UploadImage extends AsyncTask<String, Void, String> {
        private Cloudinary mCloudinary;
        private File mPhotoFile;

        public UploadImage(File photoFile) {
            super();
            mCloudinary = new Cloudinary(Config.getConfig());
            mPhotoFile = photoFile;
        }

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            JSONObject jsonResponse;

            try {
                jsonResponse = new JSONObject(mCloudinary.uploader().upload(resizeImage(mPhotoFile),
                        ObjectUtils.asMap("public_id", "profile_picture_" + authenticatedUser.getUser_id())));
                response = jsonResponse.toString();
            } catch (IOException e) {
                e.printStackTrace();
                Log.w("test", "11b: " + e.toString());
            }
            return response;
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            getProfilepictureUrl(jsonResponse);
        }
    }

    private File createImageFile() throws IOException {
        String imageFileName = "profileImage";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        return image;
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;

        Cursor cursor = getActivity().getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    @Nullable
    private File resizeImage(File file) {
        try {

            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 6;
            // factor of downsizing the image

            FileInputStream inputStream = new FileInputStream(file);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size we want to scale to
            final int REQUIRED_SIZE=75;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

            // here i override the original image file
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);

            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100 , outputStream);

            return file;
        } catch (Exception e) {
            return null;
        }
    }

    private void getProfilepictureUrl(String jsonResponse) {
        try {
            progressDialog.setMessage(getString(R.string.progress_setup_image));

            JSONObject jsonObject = new JSONObject(jsonResponse);
            String secureUrl = jsonObject.getString("secure_url");

            User user = new User();
            user.setEmail(MainActivity.authenticatedUser.getEmail());
            user.setProfile_picture_url(secureUrl);
            setProfilePictureUrl(user);

            tempUrl = secureUrl;
            MainActivity.authenticatedUser.setProfile_picture_url(secureUrl);
        } catch (JSONException e) {
            e.printStackTrace();
            progressDialog.dismiss();
        }
    }

    private void setProfilePictureUrl(User user) {
        csSubscription.add(NetworkUtil.getRetrofit().setProfileImgUrl(user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(Response response) {
        progressDialog.setMessage(getString(R.string.progress_setup_complete));
        downloadImage(tempUrl);
    }

    private void handleError(Throwable throwable) {
        Log.w("test",throwable.toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        csSubscription.unsubscribe();
    }
}
