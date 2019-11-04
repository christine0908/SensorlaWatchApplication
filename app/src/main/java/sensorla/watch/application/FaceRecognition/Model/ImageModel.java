package sensorla.watch.application.FaceRecognition.Model;


import com.google.gson.annotations.SerializedName;

public class ImageModel {

    @SerializedName("ImageBytes")
    public String ImageBytes;

    @SerializedName("ImagesName")
    public String ImagesName;

    @SerializedName("CollectionId")
    public String CollectionId ;

    public String getImageBytes() {
        return ImageBytes;
    }

    public void setImageBytes(String imageBytes) {
        ImageBytes = imageBytes;
    }

    public String getImagesName() {
        return ImagesName;
    }

    public void setImagesName(String imagesName) {
        ImagesName = imagesName;
    }

    public ImageModel(String imageBytes, String imagesName, String collectionId) {
        ImageBytes = imageBytes;
        ImagesName = imagesName;
        CollectionId = collectionId;
    }

    public String getCollectionId() {
        return CollectionId;
    }

    public void setCollectionId(String collectionId) {
        CollectionId = collectionId;
    }

}
