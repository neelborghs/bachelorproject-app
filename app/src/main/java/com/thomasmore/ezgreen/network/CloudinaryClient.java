package com.thomasmore.ezgreen.network;

import com.cloudinary.Cloudinary;
import com.thomasmore.ezgreen.utils.Config;

/**
 * Created by Tomas-Laptop on 4/05/2017.
 */

public class CloudinaryClient {

    public static String getImage(String imageName) {

        Cloudinary cloudinary = new Cloudinary(Config.getConfig());

        String url = cloudinary.url().generate(imageName);

        return url;
    }

    /*public static class UploadImage extends AsyncTask<String, Void, String> {
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
            JSONObject result;

            try {
                result = new JSONObject(mCloudinary.uploader().upload(mPhotoFile,
                        ObjectUtils.asMap("public_id", "profile_picture_" + MainActivity.authenticatedUser.getUser_id())));
                response = result.toString();
                Log.w("test","uploaded");
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("test", "11b: " + e.toString());
            }
            return response;
        }
    }*/
}
