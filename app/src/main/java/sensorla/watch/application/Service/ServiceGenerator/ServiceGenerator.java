package sensorla.watch.application.Service.ServiceGenerator;

import android.text.TextUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {
    // by chris
    public static final String API_Heartrate_URL = "http://api.sensorla.co/api/UserHeartRate/";
    // ends here
    public static final String API_BASE_URL = "http://api.sensorla.co/api/";
    public static final String API_INDOOR_URL = "http://indoor.ap-southeast-1.elasticbeanstalk.com/api/";

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Retrofit.Builder builder = new Retrofit.Builder().baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create());

    private static Retrofit.Builder builderIndoor = new Retrofit.Builder().baseUrl(API_INDOOR_URL)
            .addConverterFactory(GsonConverterFactory.create());

    private static Retrofit.Builder builderHeartRate = new Retrofit.Builder().baseUrl(API_BASE_URL)
            .client(getRequestHeader())
            .addConverterFactory(GsonConverterFactory.create());

// If different base API keep this, same then remove
    private static Retrofit.Builder builderNew = new Retrofit.Builder().baseUrl(API_Heartrate_URL)
            .client(getRequestHeader())
            .addConverterFactory(GsonConverterFactory.create());
// ends herei

    public static <S> S createService(Class<S> serviceClass) {
        Retrofit retrofit;
        builder.client(httpClient.build());
        retrofit = builder.build();
        return retrofit.create(serviceClass);
    }

    public static <S> S createIndoorService(Class<S> serviceClass) {
        Retrofit retrofit;
        builderIndoor.client(httpClient.build());
        retrofit = builderIndoor.build();
        return retrofit.create(serviceClass);
    }

    public static <S> S createHeartRateService(Class<S> serviceClass) {
        Retrofit retrofit;
        builderHeartRate.client(httpClient.build());
        retrofit = builderHeartRate.build();
        return retrofit.create(serviceClass);
    }

// if gives me a different API URL, i will keep this code.
    public static <S> S createNewService(Class<S> serviceClass) {
        Retrofit retrofit;
        builderNew.client(httpClient.build());
        retrofit = builderNew.build();
        return retrofit.create(serviceClass);
    }
    // ends here


    /* Method to set timeout in Retrofit library */
    private static OkHttpClient getRequestHeader() {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .writeTimeout(100, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .build();
        return httpClient;
    }
}