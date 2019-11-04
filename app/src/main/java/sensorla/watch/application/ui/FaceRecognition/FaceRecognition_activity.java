package sensorla.watch.application.ui.FaceRecognition;

import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sensorla.watch.application.FaceRecognition.Model.AddImageReturnModel;
import sensorla.watch.application.FaceRecognition.Model.ImageModel;
import sensorla.watch.application.FaceRecognition.cloudimagelabeling.CloudImageLabelingProcessor;
import sensorla.watch.application.FaceRecognition.cloudlandmarkrecognition.CloudLandmarkRecognitionProcessor;
import sensorla.watch.application.FaceRecognition.cloudtextrecognition.CloudDocumentTextRecognitionProcessor;
import sensorla.watch.application.FaceRecognition.cloudtextrecognition.CloudTextRecognitionProcessor;
import sensorla.watch.application.FaceRecognition.common.GraphicOverlay;
import sensorla.watch.application.FaceRecognition.common.VisionImageProcessor;
import sensorla.watch.application.MainActivity;
import sensorla.watch.application.R;
import sensorla.watch.application.Service.ApiService;
import sensorla.watch.application.Service.ServiceGenerator.FaceRecognitionServiceGenerator;
import sensorla.watch.application.Service.ServiceGenerator.ServiceGenerator;
import sensorla.watch.application.ui.Login.LoginFragment;
import sensorla.watch.application.ui.Login.SaveSharedPreference;

import static sensorla.watch.application.Constants.WATCH_ID;

public class FaceRecognition_activity extends AppCompatActivity {

    private Uri imageUri;
    private static final String TAG = "FacialRecognitionActivity";
    private static final int PERMISSION_REQUESTS = 1;
    private ImageView preview;
    private GraphicOverlay graphicOverlay;
    private VisionImageProcessor imageProcessor;
    private static final String CLOUD_LABEL_DETECTION = "Cloud Label";
    private static final String CLOUD_LANDMARK_DETECTION = "Landmark";
    private static final String CLOUD_TEXT_DETECTION = "Cloud Text";
    private static final String CLOUD_DOCUMENT_TEXT_DETECTION = "Doc Text";
    private static final int REQUEST_CHOOSE_IMAGE = 1002;
    private String selectedMode = CLOUD_LABEL_DETECTION;
    private static final int REQUEST_IMAGE_CAPTURE = 1001;

    public String msg= "";
    private static final String KEY_IMAGE_URI = "com.googletest.firebase.ml.demo.KEY_IMAGE_URI";
    private static final String KEY_IMAGE_MAX_WIDTH =
            "com.googletest.firebase.ml.demo.KEY_IMAGE_MAX_WIDTH";
    private static final String KEY_IMAGE_MAX_HEIGHT =
            "com.googletest.firebase.ml.demo.KEY_IMAGE_MAX_HEIGHT";
    private static final String KEY_SELECTED_SIZE =
            "com.googletest.firebase.ml.demo.KEY_SELECTED_SIZE";

    private static final String SIZE_PREVIEW = "w:max"; // Available on-screen width.
    private static final String SIZE_1024_768 = "w:1024"; // ~1024*768 in a normal ratio
    private static final String SIZE_640_480 = "w:640"; // ~640*480 in a normal ratio

    boolean isLandScape;
    // Max width (portrait mode)
    private Integer imageMaxWidth;
    // Max height (portrait mode)
    private Integer imageMaxHeight;

    private String selectedSize = SIZE_PREVIEW;
    String myLog = "myLog";

    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;

    FrameLayout progressBarHolder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_recognition);

        Log.e("face view!","true");
        if (!allPermissionsGranted()) {
            getRuntimePermissions();
        }

        progressBarHolder = (FrameLayout) findViewById(R.id.progressBarHolder);
        startCameraIntentForResult();
        createImageProcessor();

        isLandScape =
                (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);

        if (savedInstanceState != null) {
            imageUri = savedInstanceState.getParcelable(KEY_IMAGE_URI);
            imageMaxWidth = savedInstanceState.getInt(KEY_IMAGE_MAX_WIDTH);
            imageMaxHeight = savedInstanceState.getInt(KEY_IMAGE_MAX_HEIGHT);
            selectedSize = savedInstanceState.getString(KEY_SELECTED_SIZE);

            if (imageUri != null) {

                tryReloadAndDetectInImage();
            }
        }

        //this.finish();
    }

    private class MyTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            inAnimation = new AlphaAnimation(0f, 1f);
            inAnimation.setDuration(200);
            progressBarHolder.setAnimation(inAnimation);
            progressBarHolder.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            outAnimation = new AlphaAnimation(1f, 0f);
            outAnimation.setDuration(200);
            progressBarHolder.setAnimation(outAnimation);
            progressBarHolder.setVisibility(View.GONE);

        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                for (int i = 0; i < 5; i++) {
                    Log.d(myLog, "Emulating some task.. Step " + i);
                    TimeUnit.SECONDS.sleep(1);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //this.finish()
        new MyTask().execute();
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            tryReloadAndDetectInImage();
        } else if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == RESULT_OK) {
            // In this case, imageUri is returned by the chooser, save it.
            imageUri = data.getData();
            tryReloadAndDetectInImage();
        }
        else{
            this.finish();
        }

    }

    private void startCameraIntentForResult() {
        // Clean up last time's image
        imageUri = null;
        //preview.setImageBitmap(null);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "New Picture");
            values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
            imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private void createImageProcessor() {
        switch (selectedMode) {
            case CLOUD_LABEL_DETECTION:
                imageProcessor = new CloudImageLabelingProcessor();
                break;
            case CLOUD_LANDMARK_DETECTION:
                imageProcessor = new CloudLandmarkRecognitionProcessor();
                break;
            case CLOUD_TEXT_DETECTION:
                imageProcessor = new CloudTextRecognitionProcessor();
                break;
            case CLOUD_DOCUMENT_TEXT_DETECTION:
                imageProcessor = new CloudDocumentTextRecognitionProcessor();
                break;
            default:
                throw new IllegalStateException("Unknown selectedMode: " + selectedMode);
        }
    }

    public void finishAcitifity(){
        this.finish();
    }

    private void tryReloadAndDetectInImage() {
        try {
            if (imageUri == null) {
                return;
            }

            Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos); //bm is the bitmap object
            byte[] b = baos.toByteArray();
            String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);

            Log.e("base64", encodedImage);

            //CALL api
            //create service
            final ApiService service= FaceRecognitionServiceGenerator.createService(ApiService.class);
            ImageModel img = new ImageModel(encodedImage,"sze","testpi");//use collection id for each db for instance parkway has their

            final Call<String> apiSearchCall = service.SearchAWSFaceApi(img);
            apiSearchCall.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    //Response<String> data = response;
                    Log.e("","search api response successful="+response.code()+"body:imgid:" +response.body());

                    if(response.isSuccessful() && !response.body().toString().contains("no faces")
                            && !response.body().toString().contains("reference")){
                        Toast.makeText(FaceRecognition_activity.this,"success!",Toast.LENGTH_SHORT).show();
                        //response.body().toString() <result> iss not null then check api result match img id in db then
                        //go to waiting for job page
                        Log.e("","search api response successful="+response.code()+"body:imgid:" +response.body().toString());
                        callLoginAuthentication(response.body());
                        //if (response.body().toString().length() == 36){
                        //to remove when api upload in server
                        //tem hard code
//                            String name = "swezinei@gmail.com";
//                            String pwd = "123456";
//                            callLoginAuthentication(name, pwd);

//                            final ApiService service= ServiceGenerator.createService(ApiService.class);
//                            final Call<String> apiSearchCall = service.checkFaceRecogAuthentication(response.body().toString(),"3");
//                            apiSearchCall.enqueue(new Callback<String>() {
//                                @Override
//                                public void onResponse(Call<String> call, Response<String> response) {
//                                    if (response.isSuccessful() && response.body().toString().contains("SuccessLogin")){
//                                        String apiResult = response.body().toString();
//                                        apiResult.replaceAll("\"","");
//                                        String[] arr = apiResult.split(",");
//                                        callLoginAuthentication(arr[1], arr[2]);
//                                    }
//                                }
//
//                                @Override
//                                public void onFailure(Call<String> call, Throwable t) {
//                                    Toast.makeText(getApplicationContext(),"Failed:"+response.message(), Toast.LENGTH_SHORT).show();
//                                }
//                            });

                        //}

                    }
                    else{

                        Toast.makeText(getApplicationContext(),"Failed:"+response.message(), Toast.LENGTH_SHORT).show();
                        finishAcitifity();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    t.printStackTrace();
                }
            });

//            final Call<AddImageReturnModel> apiAddCall = service.AddAWSFaceApi(img);
//            apiAddCall.enqueue(new Callback<AddImageReturnModel>() {
//                @Override
//                public void onResponse(Call<AddImageReturnModel> call, Response<AddImageReturnModel> response) {
//                    //Response<String> data = response;
//
//                    if(response.isSuccessful()){
//                        Toast.makeText(getApplicationContext(),"success!",Toast.LENGTH_SHORT).show();
//                        Log.e("","api response successful="+response.code()+"body:imgid:" +response.body().getImageId());
//                    }
//                    else{
//                        Toast.makeText(getApplicationContext(),"Failed:"+response.message(), Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<AddImageReturnModel> call, Throwable t) {
//                    t.printStackTrace();
//                    //Log.e("","api onFailure="+ t.printStackTrace());
//                    //Call<String> data = call;
//                }
//            });


        } catch (IOException e) {
            Log.e(TAG, "Error retrieving saved image");
        }
    }

    private void callLoginAuthentication(String imgid){

        ///string imgId, string externalId, string env
        //service.checkFaceRecogAuthentication(response.body().toString(),"3");
        final String deviceID = SaveSharedPreference.getDeviceID(getApplicationContext());
        final ApiService service= ServiceGenerator.createService(ApiService.class);
        Log.e(TAG, "callLoginAuthentication deid:"+ deviceID+"imgid:"+ imgid.substring(1,37));
        //calling api
        String imgid2 = imgid.substring(1,37);
        final Call<String> apiCall=service.checkFaceRecogAuthentication(imgid2,deviceID,"3");
        apiCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()) {
                    Log.e(TAG, "callLoginAuthentication  >>> Success code");
                    Log.e(TAG, "response.body()  >>>"+response.body());
                    if(response.body().contains("Success")) {
                        Log.e(TAG, "callLoginAuthentication  >>> Success");
                        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
                        getApplicationContext().startActivity(myIntent);
                        SaveSharedPreference.saveUserInfo(
                                getApplicationContext(),
                                SaveSharedPreference.loggedInUserInfo(response.body())
                        );

                        SaveSharedPreference.setLoggedIn(getApplicationContext(), true);
                        Toast.makeText(getApplicationContext(), response.body(), Toast.LENGTH_SHORT).show();
                        ((MainActivity) getApplicationContext()).manageMenuItem();

                    }
                    else {
                        Toast.makeText(getApplicationContext(), response.body(), Toast.LENGTH_SHORT).show();
                        finishAcitifity();
                    }
                }
                else{
                    Toast.makeText(getApplicationContext().getApplicationContext(), response.message(), Toast.LENGTH_SHORT).show();
                    finishAcitifity();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(getApplicationContext().getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                finishAcitifity();
            }
        });
    }
}
