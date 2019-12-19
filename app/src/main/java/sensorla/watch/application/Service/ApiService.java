package sensorla.watch.application.Service;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import sensorla.watch.application.FaceRecognition.Model.*;
import sensorla.watch.application.Model.HeartRateModel;

public interface ApiService {

    @GET("DeviceUser")
    Call<String> UpdateDevice(@Query("oldExternalId") String oldExternalId,
                              @Query("externalId") String externalId,
                              @Query("tokenId") String tokenId,
                              @Query("env") String env);

    @GET("DeviceUser")
    Call<String> CreateWODetail(@Query("workOrderId") String workOrderId,
                                @Query("status") String status,
                                @Query("externalId") String externalId,
                                @Query("env") String env,
                                @Query("datetime") String datetime);

    @GET("UserLocation")
    Call<String> GetUserLocation (@Query("UserId") int UserId,
                                  @Query("Lat") Double Lat,
                                  @Query("Long") Double Long,
                                  @Query("env") String env);

    @GET("User")
    Call<String> GetSuperUserPwd (@Query("pwd") String pwd);

    @GET("DeviceUser")
    Call<String> Connect (@Query("deviceId") String deviceId,
                          @Query("userId") String userId,
                          @Query("type") String type,
                          @Query("env")String env);

    @GET("DeviceUser")
    Call<String> Disconnect (@Query("deviceId") String deviceId,
                             @Query("userId") String userId,
                             @Query("type") String type,
                             @Query("env")String env);

    @GET("DeviceUser")
    Call<String> UserLogin (@Query("userName") String userName,
                            @Query("password") String password,
                            @Query("externalId") String externalId,
                            @Query("env")String env);

    @GET("UserLocation")
    Call<String> AllLocations (@Query("userId") String userId,
                               @Query("env")String env);

    @GET("UserLocation")
    Call<String> AllServer (@Query("env") String env);

    @GET("WorkorderDocument")
    Call<String> CreateWO ();


    ///Face Recognition api
    @Headers({"username: dXNlcg==", "password: MTIz"})
    @POST("addimage")
    Call<AddImageReturnModel> AddAWSFaceApi(@Body ImageModel ImageMdl);

    @Headers({"username: dXNlcg==", "password: MTIz"})
    @POST("searchimage")
    Call<String> SearchAWSFaceApi(@Body ImageModel ImageMdl);

    @GET("validationimageid")
    Call<String> checkFaceRecogAuthentication (@Query("imgId") String imgId,
                                               @Query("externalId") String externalId,
                                               @Query("env") String env);
    @GET("adduserlocation")
    Call<String> Indoor(@Query("UserId") int UserId,
                        @Query("Lat") Double Lat,
                        @Query("Long") Double Long,
                        @Query("Altitude") Double Altitude,
                        @Query("env") String env);

    //HeartRate
    @Headers("Authorization: Basic SUZTQ0FQSTpASUZTQ0FQSUlDSElOSVNBTg==")
    @GET("UserHeartRate")
    Call<String> uploadOneHeartRateData(@Query("userId") String UserId,
                           @Query("value") String value,
                           @Query("datetime") String datetime,
                           @Query("env") String env);


    @Headers("Authorization: Basic SUZTQ0FQSTpASUZTQ0FQSUlDSElOSVNBTg==")
    @POST("UserHeartRate")
    Call<String> uploadListHeartRateData(@Body List<Map<String, Object>> heartratedatas);


// Delete this
//    @POST("newheartrate")
//    Call<String> NewHeartRateApi(@Body HeartRateModel mdl);
}
