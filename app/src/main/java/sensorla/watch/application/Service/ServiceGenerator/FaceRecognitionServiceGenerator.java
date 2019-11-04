package sensorla.watch.application.Service.ServiceGenerator;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FaceRecognitionServiceGenerator {
    public static final String API_BASE_URL = "http://facerekognitionapiapplication-test.nemrtkmw62.ap-southeast-1.elasticbeanstalk.com/api/";
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .client(getRequestHeader())
            .addConverterFactory(GsonConverterFactory.create());

    public static <S> S createService(Class<S> serviceClass) {
        Retrofit retrofit;
        builder.client(httpClient.build());
        retrofit = builder.build();
        return retrofit.create(serviceClass);
    }

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
