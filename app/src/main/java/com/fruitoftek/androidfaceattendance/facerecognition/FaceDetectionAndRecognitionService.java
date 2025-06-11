package com.fruitoftek.androidfaceattendance.facerecognition;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import com.fruitoftek.androidfaceattendance.data.model.BioPhotoFeatures;
import com.fruitoftek.androidfaceattendance.data.model.BioPhotos;
import com.fruitoftek.androidfaceattendance.facerecognition.djl.FaceFeatureTranslator;
import com.fruitoftek.androidfaceattendance.facerecognition.dto.BioPhotoMatch;
import com.fruitoftek.androidfaceattendance.facerecognition.dto.FeaturesResult;
import com.fruitoftek.androidfaceattendance.facerecognition.dto.RecognitionResult;
import com.fruitoftek.androidfaceattendance.util.BioDataType;
import com.fruitoftek.androidfaceattendance.util.Literals;
import com.fruitoftek.androidfaceattendance.util.Util;

public class FaceDetectionAndRecognitionService {
    private static final String TAG = "FaceDetectionAndRecognitionService";
    private static final String MODEL_ZIP_DOWNLOAD_FILENAME = "face_feature.zip";
    private static final float featureThresholdForMatch = 0.70f;
    private Context context;
    private FaceDetector faceDetector;
    private Criteria<Image, float[]> criteria;
    private ZooModel<Image, float[]> model;
    private Predictor<Image, float[]> predictor;

    public FaceDetectionAndRecognitionService(Context context) {
        this.context = context;
        faceDetector = FaceDetection.getClient(
                new FaceDetectorOptions.Builder()// Real-time contour detection of multiple faces
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setContourMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .build());
        System.setProperty("ai.djl.offline", "true");
        copyPytorchModelFromAssetsToFileSystem();
        criteria = buildCriteria();
        model = loadZooModel(criteria);
        predictor = createPredictor(model);
    }


    /**
     * Process passed in BioPhoto, extracts features and thumbnail from it and then sets them
     * in the corresponding fields of the passed in object
     * @param bioPhoto The BioPhoto object to be updated with new Features and Thumbnail
     * @throws Exception
     */
    public void setBioPhotoFeaturesToBioPhoto(BioPhotos bioPhoto) throws Exception {
        FeaturesResult featuresResult = extractFaceFeaturesFromBioPhoto(bioPhoto);

        BioPhotoFeatures bioPhotoFeatures = new BioPhotoFeatures();
        bioPhotoFeatures.user = bioPhoto.user;
        bioPhotoFeatures.type = bioPhoto.type;
        bioPhotoFeatures.setFeatures(featuresResult.getFeatures());

        BioPhotos thumbNailBioPhoto = new BioPhotos();
        thumbNailBioPhoto.user = bioPhoto.user;
        thumbNailBioPhoto.type = BioDataType.BIOPHOTO_THUMBNAIL_JPG.getType();
        thumbNailBioPhoto.setBioPhotoContent(featuresResult.getCroppedPhoto());
        thumbNailBioPhoto.lastUpdated = Util.getDateTimeNow();
        thumbNailBioPhoto.isSync = Literals.FALSE;

        // Set into the passed object
        bioPhoto.Features = bioPhotoFeatures;
        bioPhoto.Thumbnail = thumbNailBioPhoto;
    }

    public void detectFacesAsync(InputImage image, OnSuccessListener<List<Face>> onSuccessListener) {
        faceDetector.process(image).addOnSuccessListener(onSuccessListener);
    }

    public List<Face> detectFaces(InputImage image) {
        try {
            Task<List<Face>> detectFacesTask = faceDetector.process(image);
            List<Face> faces = Tasks.await(detectFacesTask);
            return faces;
        } catch (Exception ex) {
            Log.e(TAG, "Error detecting faces in image", ex);
        }
        return null;
    }

    /**
     * Takes a photo and tries to recognize the face from a list of BioPhotos.
     * @param photo The photo of the person we are trying to recognize
     * @param bioPhotos The set of BioPhotos to search in
     * @return
     */
    public RecognitionResult recognizePhoto(Bitmap photo, List<BioPhotos> bioPhotos) {
        try {
            RecognitionResult result = new RecognitionResult();
            float[] faceFeatures = extractFaceFeaturesFromPhoto(photo);
            BioPhotoMatch matchedBioPhoto = getBioPhotoMatch(faceFeatures, bioPhotos);
            result.setFeatures(faceFeatures);
            if (matchedBioPhoto != null) {
                result.setTitle(matchedBioPhoto.getBioPhoto().getBioPhotoStringIdentifier());
                result.setConfidence(matchedBioPhoto.getSimilitude());
                result.setMatchedBioPhoto(matchedBioPhoto.getBioPhoto());
            }
            return result;
        } catch (Exception ex) {
            String errMsg = "Error while extracting face features from photo";
            Log.e(TAG, errMsg, ex);
        }
        return null;
    }

    public FeaturesResult extractFaceFeaturesFromBioPhoto(BioPhotos bioPhoto) throws Exception {
        try {
            Bitmap fullPhoto = bioPhoto.getPhoto();
            InputImage image = InputImage.fromBitmap(fullPhoto, 0);
            Task<List<Face>> detectFacesTask = faceDetector.process(image);
            List<Face> faces = Tasks.await(detectFacesTask);
            if (faces == null || faces.isEmpty()) {
                throw new Exception("Biophoto for user " + bioPhoto.user + " does not contain 1 face");
            }
            if (faces.size() > 1) {
                throw new Exception("Biophoto for user " + bioPhoto.user + " contains more than 1 Face");
            }
            Face face = faces.get(0);
            Bitmap croppedPhoto = cropPhotoToFace(bioPhoto.getPhoto(), face);
            float[] faceFeatures = extractFaceFeaturesFromPhoto(croppedPhoto);
            return new FeaturesResult(croppedPhoto, faceFeatures);
        } catch (Exception ex) {
            String errMsg = "Error while extracting face features from BioPhoto";
            Log.e(TAG, errMsg, ex);
            throw new Exception(errMsg, ex);
        }
    }

    private Bitmap cropPhotoToFace(Bitmap fullPhoto, Face face) throws Exception {
        try {// Crop the original image to just the face
            /**
             * left	    int: The X coordinate of the left side of the rectangle
             * top	    int: The Y coordinate of the top of the rectangle
             * right	int: The X coordinate of the right side of the rectangle
             * bottom	int: The Y coordinate of the bottom of the rectangle
             */
            RectF boundingBox = new RectF(
                    Math.max(face.getBoundingBox().left, 0),
                    Math.max(face.getBoundingBox().top, 0),
                    Math.min(face.getBoundingBox().right, fullPhoto.getWidth()),
                    Math.min(face.getBoundingBox().bottom, fullPhoto.getHeight())
            );
            Bitmap croppedPhoto = Bitmap.createBitmap(fullPhoto,
                    (int) boundingBox.left,
                    (int) boundingBox.top,
                    (int) boundingBox.width(),
                    (int) boundingBox.height());
            return croppedPhoto;
        } catch (Exception ex) {
            String errMsg = "Error while extracting cropping photo to just face";
            Log.e(TAG, errMsg, ex);
            throw new Exception(errMsg, ex);
        }
    }

    private float[] extractFaceFeaturesFromPhoto(Bitmap croppedPhoto) throws Exception {
        try {
            Image image = ImageFactory.getInstance().fromImage(croppedPhoto);
            float[] features = predictor.predict(image);
            return features;
        } catch (Exception ex) {
            String errMsg = "Error while extracting face features from cropped Photo using predictor";
            Log.e(TAG, errMsg, ex);
            throw new Exception(errMsg, ex);
        }
    }

    private void copyPytorchModelFromAssetsToFileSystem() {
        try {
            Path modelPath = Paths.get(context.getFilesDir().getPath(), MODEL_ZIP_DOWNLOAD_FILENAME);
            if (!Files.exists(modelPath)) {
                try(InputStream inputStream = context.getAssets().open("pytorch_models/face_feature.zip")) {
                    try (FileOutputStream fos = context.openFileOutput(MODEL_ZIP_DOWNLOAD_FILENAME, Context.MODE_PRIVATE)) {
                        byte[] buffer = new byte[8192];
                        int length;
                        while( (length = inputStream.read(buffer)) != -1) {
                            fos.write(buffer, 0, length);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    private Criteria<Image, float[]> buildCriteria() {
        try {
            Log.i(TAG, "Building Criteria face_feature with Engine PyTorch");
            long time = System.currentTimeMillis();
            Criteria<Image, float[]> criteria =
                    Criteria.builder()
                            .setTypes(Image.class, float[].class)
                            .optModelPath(new File(context.getFilesDir(), MODEL_ZIP_DOWNLOAD_FILENAME).toPath())
                            .optModelName("face_feature") // specify model file prefix
                            .optTranslator(new FaceFeatureTranslator())
                            .optProgress(new ProgressBar())
                            .optEngine("PyTorch") // Use PyTorch engine
                            .build();
            Log.i(TAG, "Loading criteria took " + (System.currentTimeMillis()-time) + " ms");
            return criteria;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    private ZooModel<Image, float[]> loadZooModel(Criteria<Image, float[]> criteria) {
        try {
            Log.i(TAG, "Loading ZooModel");
            long time = System.currentTimeMillis();
            ZooModel<Image, float[]> model = criteria.loadModel();
            Log.i(TAG, "Loading ZooModel took " + (System.currentTimeMillis()-time) + " ms");
            return model;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    private Predictor<Image, float[]> createPredictor(ZooModel<Image, float[]> model) {
        Log.i(TAG, "Create Predictor");
        long time = System.currentTimeMillis();
        Predictor<Image, float[]> predictor = model.newPredictor();
        Log.i(TAG, "Create Predictor took " + (System.currentTimeMillis()-time) + " ms");
        return predictor;
    }

    /**
     * Returns the biophoto that makes match with the sampleBioPhoto passed as parameter.
     * Returns null if sampleBioPhoto is not found in the dataset
     * @param faceFeaturesAttPhoto The attendance photo face features to compare against the dataset of biophotos
     * @param bioPhotos The dataset
     * @return
     */
    private BioPhotoMatch getBioPhotoMatch(float[] faceFeaturesAttPhoto, List<BioPhotos> bioPhotos) {
        List<BioPhotoMatch> matchedPhotos = new ArrayList<>();
        for(BioPhotos bioPhoto: bioPhotos) {
            float result = calculateSimilitude(faceFeaturesAttPhoto, bioPhoto.Features.getFeature());
            if (result >= featureThresholdForMatch) {
                matchedPhotos.add(new BioPhotoMatch(result, bioPhoto));
            }
        }

        if (matchedPhotos.isEmpty()) {
            Log.i(TAG, "Not match");
            return null;
        }

        if (matchedPhotos.size() == 1) {
            Log.i(TAG, "Single match");
            return matchedPhotos.get(0);
        }

        // Resolve collision
        Log.i(TAG, "Multiple match, resolving collision");
        return matchedPhotos
                .stream()
                .sorted()
                .findFirst()
                .orElse(null);
    }

    private static float calculateSimilitude(float[] feature1, float[] feature2) {
        float ret = 0.0f;
        float mod1 = 0.0f;
        float mod2 = 0.0f;
        int length = feature1.length;
        for (int i = 0; i < length; ++i) {
            ret += feature1[i] * feature2[i];
            mod1 += feature1[i] * feature1[i];
            mod2 += feature2[i] * feature2[i];
        }
        return (float) ((ret / Math.sqrt(mod1) / Math.sqrt(mod2) + 1) / 2.0f);
    }
}
