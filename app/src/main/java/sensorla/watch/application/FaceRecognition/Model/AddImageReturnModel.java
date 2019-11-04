package sensorla.watch.application.FaceRecognition.Model;

import com.google.gson.annotations.SerializedName;

public class AddImageReturnModel {

    @SerializedName("ImageId")
    public String ImageId;

    @SerializedName("CollectionARN")
    public String CollectionARN;

    public String getImageId() {
        return ImageId;
    }

    public void setImageId(String imageId) {
        ImageId = imageId;
    }

    public String getCollectionARN() {
        return CollectionARN;
    }

    public void setCollectionARN(String collectionARN) {
        CollectionARN = collectionARN;
    }

    public AddImageReturnModel(String imageId, String collectionARN) {
        ImageId = imageId;
        CollectionARN = collectionARN;
    }
}
