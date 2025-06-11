/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fruitoftek.androidfaceattendance.detection;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.hardware.camera2.CameraCharacteristics;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Size;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import com.fruitoftek.androidfaceattendance.R;
import com.fruitoftek.androidfaceattendance.data.model.BioPhotoFeatures;
import com.fruitoftek.androidfaceattendance.data.model.BioPhotos;
import com.fruitoftek.androidfaceattendance.detection.customview.OverlayView;
import com.fruitoftek.androidfaceattendance.detection.customview.OverlayView.DrawCallback;
import com.fruitoftek.androidfaceattendance.detection.dto.FaceRecord;
import com.fruitoftek.androidfaceattendance.detection.env.BorderedText;
import com.fruitoftek.androidfaceattendance.detection.env.ImageUtils;
import com.fruitoftek.androidfaceattendance.detection.env.Logger;
import com.fruitoftek.androidfaceattendance.detection.tracking.MultiBoxTracker;
import com.fruitoftek.androidfaceattendance.facerecognition.FaceDetectionAndRecognitionService;
import com.fruitoftek.androidfaceattendance.facerecognition.dto.RecognitionResult;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {
    private static final Logger LOGGER = new Logger();

    // MobileFaceNet
    private static final int TF_OD_API_INPUT_SIZE = 112;
    private static final boolean MAINTAIN_ASPECT = false;
    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
    private static final boolean SAVE_PREVIEW_BITMAP = false;
    private static final float TEXT_SIZE_DIP = 10;
    OverlayView trackingOverlay;
    private Integer sensorOrientation;

    // Using the FaceNet PyTorch model via DJL for Face Recognition
    private FaceDetectionAndRecognitionService faceDetectionAndRecognitionService;
    private List<BioPhotos> bioPhotos = new ArrayList<>();

    private long lastProcessingTimeMs;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;

    private boolean computingDetection = false;
    private boolean addPending = false;
    private boolean adding = false;

    private long timestamp = 0;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    private MultiBoxTracker tracker;

    private BorderedText borderedText;

    // Face detector
    private FaceDetector faceDetector;

    // here the preview image is drawn in portrait way
    private Bitmap portraitBmp = null;
    // here the face is cropped and drawn
    private Bitmap faceBmp = null;

    private FloatingActionButton fabAdd;
    private LinearLayout bottomSheetLayout;
    private boolean showConfidence = true;
    private boolean showFaceLabel = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fabAdd = findViewById(R.id.fab_add);
        bottomSheetLayout = (LinearLayout) findViewById(R.id.bottom_sheet_layout);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddClick();
            }
        });

        // Real-time contour detection of multiple faces
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setContourMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                        .build();


        FaceDetector detector = FaceDetection.getClient(options);
        faceDetector = detector;
    }


    private void onAddClick() {
        addPending = true;
    }

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        tracker = new MultiBoxTracker(this);

        try {
            LOGGER.i("Creating detector");
            faceDetectionAndRecognitionService = new FaceDetectionAndRecognitionService(this.getApplicationContext());
        } catch (Exception ex) {
            ex.printStackTrace();
            LOGGER.e(ex, "Exception initializing classifier!");
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();
        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);

        int targetW, targetH;
        if (sensorOrientation == 90 || sensorOrientation == 270) {
            targetH = previewWidth;
            targetW = previewHeight;
        } else {
            targetW = previewWidth;
            targetH = previewHeight;
        }
        int cropW = (int) (targetW / 2.0);
        int cropH = (int) (targetH / 2.0);

        croppedBitmap = Bitmap.createBitmap(cropW, cropH, Config.ARGB_8888);

        portraitBmp = Bitmap.createBitmap(targetW, targetH, Config.ARGB_8888);
        faceBmp = Bitmap.createBitmap(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, Config.ARGB_8888);

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        cropW, cropH,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                new DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        tracker.draw(canvas);
                        if (isDebug()) {
                            tracker.drawDebug(canvas);
                        }
                    }
                });

        tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);

        // Initialize face registry
        List<BioPhotos> bioPhotos = initializeFaceRegistry();
        if (!CollectionUtils.isEmpty(bioPhotos)) {
            for(BioPhotos bioPhoto: bioPhotos) {
                registerNewFace(bioPhoto);
            }
        }
    }


    @Override
    protected void processImage() {
        ++timestamp;
        final long currTimestamp = timestamp;
        trackingOverlay.postInvalidate();

        // No mutex needed as this method is not reentrant.
        if (computingDetection) {
            readyForNextImage();
            return;
        }
        computingDetection = true;
        LOGGER.d("Preparing image " + currTimestamp + " for detection in bg thread.");
        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
        readyForNextImage();

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap);
        }

        /** ------------------------------------------------------------------------------------
            NOTE TO MYSELF
            HERE IS WHERE THE MAGIC HAPPENS
            THE FACEDETECTOR INSTANCE TAKES THE croppedBitmap AND OUTPUTS THE FACES DETECTED
            ------------------------------------------------------------------------------------
         **/

        InputImage image = InputImage.fromBitmap(croppedBitmap, 0);
        faceDetector
                .process(image)
                .addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                    @Override
                    public void onSuccess(List<Face> faces) {
                        if (faces.size() == 0) {
                            updateResults(currTimestamp, new LinkedList<>(), rgbFrameBitmap);
                            return;
                        }
                        runInBackground(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        onFacesDetected(currTimestamp, faces, addPending);
                                        addPending = false;
                                    }
                                });
                    }

                });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.tfe_od_camera_connection_fragment_tracking;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.
    private enum DetectorMode {
        TF_OD_API;
    }

    @Override
    protected void setUseNNAPI(final boolean isChecked) {
        // Not used anymore
    }

    @Override
    protected void setNumThreads(final int numThreads) {
        // Not used anymore
    }


    // Face Processing
    private Matrix createTransform(
            final int srcWidth,
            final int srcHeight,
            final int dstWidth,
            final int dstHeight,
            final int applyRotation) {

        Matrix matrix = new Matrix();
        if (applyRotation != 0) {
            if (applyRotation % 90 != 0) {
                LOGGER.w("Rotation of %d % 90 != 0", applyRotation);
            }

            // Translate so center of image is at origin.
            matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f);

            // Rotate around origin.
            matrix.postRotate(applyRotation);
        }

        if (applyRotation != 0) {
            // Translate back from origin centered reference to destination frame.
            matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f);
        }

        return matrix;
    }

    private void showAddFaceDialog(RecognitionResult result, Bitmap fullPhoto) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.tfe_image_edit_dialog, null);
        ImageView ivFace = dialogLayout.findViewById(R.id.dlg_image);
        ivFace.setImageBitmap(result.getCrop());

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dlg, int i) {
                onFaceFeaturesDetected(fullPhoto, result);
                adding = false;
                dlg.dismiss();
            }
        });

        builder.setNegativeButton(R.string.activity_update_biophoto_try, (dialogInterface, i) -> {
            addPending = true;
        });

        builder.setOnDismissListener(dialogInterface -> {
            // We usually want to keep trying to get a new BioPhoto added
            addPending = true;
        });

        builder.setView(dialogLayout);
        builder.show();
    }

    private void updateResults(long currTimestamp, final List<RecognitionResult> mappedRecognitions, Bitmap fullPhoto) {
        tracker.trackResults(mappedRecognitions, currTimestamp);
        trackingOverlay.postInvalidate();
        computingDetection = false;

        if (mappedRecognitions.size() > 0) {
            LOGGER.i("Adding results");
            RecognitionResult result = mappedRecognitions.get(0);
            if (adding && addPending && result.getMatchedBioPhoto() == null) {
                // If adding was enabled and the face is not matching any other face already in the registry
                showAddFaceDialog(result, fullPhoto);
            }
        }

        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        showFrameInfo(previewWidth + "x" + previewHeight);
                        showCropInfo(croppedBitmap.getWidth() + "x" + croppedBitmap.getHeight());
                        showInference(lastProcessingTimeMs + "ms");
                    }
                });
    }

    private void onFacesDetected(long currTimestamp, List<Face> faces, boolean add) {
        final Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(2.0f);

        final List<RecognitionResult> mappedRecognitions = new ArrayList<>();
        List<FaceRecord> faceExpandedList = new ArrayList<>();
        List<BioPhotos> faceRecognizedList = new ArrayList<>();
        // Note this can be done only once
        int sourceW = rgbFrameBitmap.getWidth();
        int sourceH = rgbFrameBitmap.getHeight();
        int targetW = portraitBmp.getWidth();
        int targetH = portraitBmp.getHeight();
        Matrix transform = createTransform(
                sourceW,
                sourceH,
                targetW,
                targetH,
                sensorOrientation);
        final Canvas cv = new Canvas(portraitBmp);

        // draws the original image in portrait mode.
        cv.drawBitmap(rgbFrameBitmap, transform, null);
        Bitmap fullPhoto = Bitmap.createBitmap(portraitBmp);
        final Canvas cvFace = new Canvas(faceBmp);
        for (Face face : faces) {
            LOGGER.i("FACE" + face.toString());
            LOGGER.i("Running detection on face " + currTimestamp);

            final RectF boundingBox = new RectF(face.getBoundingBox());
            final boolean goodConfidence = true; //face.get;
            if (boundingBox != null && goodConfidence) {
                // maps crop coordinates to original
                cropToFrameTransform.mapRect(boundingBox);

                // maps original coordinates to portrait coordinates
                RectF faceBB = new RectF(boundingBox);
                transform.mapRect(faceBB);

                // translates portrait to origin and scales to fit input inference size
                float sx = ((float) TF_OD_API_INPUT_SIZE) / faceBB.width();
                float sy = ((float) TF_OD_API_INPUT_SIZE) / faceBB.height();
                Matrix matrix = new Matrix();
                matrix.postTranslate(-faceBB.left, -faceBB.top);
                matrix.postScale(sx, sy);

                cvFace.drawBitmap(portraitBmp, matrix, null);
                String label = "";
                float confidence = -1f;
                Integer color = Color.BLUE;
                Object extra = null;
                Bitmap crop = null;
                String bottomRecognitionMsg = null;

                if (add) {
                    // Adjust faceBB to maximum values to avoid cropping issues
                    faceBB = new RectF(
                            Math.max(faceBB.left, 0),
                            Math.max(faceBB.top, 0),
                            Math.min(faceBB.right, portraitBmp.getWidth()),
                            Math.min(faceBB.bottom, portraitBmp.getHeight()));
                    crop = Bitmap.createBitmap(portraitBmp,
                            (int) faceBB.left,
                            (int) faceBB.top,
                            (int) faceBB.width(),
                            (int) faceBB.height());
                }

                // Build the expanded instance and add it to the list
                faceExpandedList.add(new FaceRecord(face, faceBmp));

                // Try to recognize the face
                final long startTime = SystemClock.uptimeMillis();
                RecognitionResult recognitionResult = faceDetectionAndRecognitionService.recognizePhoto(faceBmp, bioPhotos);
                lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                if (recognitionResult != null && recognitionResult.getMatchedBioPhoto() != null) {// We found a match
                    // Face was recognized with enough confidence
                    faceRecognizedList.add(recognitionResult.getMatchedBioPhoto());
                    if (add || adding) { // Trying to add a new face?
                        // There should not be any recognition
                        color = Color.YELLOW;
                        bottomRecognitionMsg = getString(R.string.activity_update_biophoto_duplicate);
                    } else {// Ok
                        color = Color.GREEN;
                    }
                }

                if (getCameraFacing() == CameraCharacteristics.LENS_FACING_FRONT) {
                    // camera is frontal so the image is flipped horizontally
                    // flips horizontally
                    Matrix flip = new Matrix();
                    if (sensorOrientation == 90 || sensorOrientation == 270) {
                        flip.postScale(1, -1, previewWidth / 2.0f, previewHeight / 2.0f);
                    } else {
                        flip.postScale(-1, 1, previewWidth / 2.0f, previewHeight / 2.0f);
                    }
                    flip.mapRect(boundingBox);
                }

                if (recognitionResult == null) {
                    //No matches or no BioPhotos entry set provided
                    // Just build a mock RecognitionResult object to make the
                    // MultiBoxTracker work
                    recognitionResult = new RecognitionResult();
                }
                recognitionResult.setColor(color);
                recognitionResult.setLocation(boundingBox);
                recognitionResult.setCrop(crop);
                recognitionResult.setBottomMessage(bottomRecognitionMsg);
                recognitionResult.setShowFaceLabel(showFaceLabel);
                mappedRecognitions.add(recognitionResult);
            }
        }

        // Call API method
        onFacesDetected(fullPhoto, faceExpandedList);

        // Call API method if at least one successful recognition was performed
        if (!faceRecognizedList.isEmpty()) {
            onFacesRecognized(fullPhoto, faceRecognizedList);
        }

        updateResults(currTimestamp, mappedRecognitions, fullPhoto);
    }

    /**
     * Useful methods added for accessing the core functionality without messing up too much
     * with the actual code of the activity
     * ---------------------------------------------------------------------------------------------
     */

    protected void registerNewFace(BioPhotos bioPhoto) {
        LOGGER.i(String.format("registerNewFace %d", bioPhoto.user));
        bioPhotos.add(bioPhoto);
    }

    protected void showBottomSheet(boolean show) {
        if (show) {
            bottomSheetLayout.setVisibility(View.VISIBLE);
        } else {
            bottomSheetLayout.setVisibility(View.INVISIBLE);
        }
    }

    protected void showAddButton(boolean show) {
        if (show) {
            fabAdd.setVisibility(View.VISIBLE);
        } else {
            fabAdd.setVisibility(View.INVISIBLE);
        }
    }

    protected void activateFaceFeaturesDetection(boolean activate) {
        LOGGER.i("onFacesDetected: " + activate);
        addPending = activate;
        adding = activate;
    }

    // To override
    protected List<BioPhotos> initializeFaceRegistry() {
        // Need to override and do something
        LOGGER.i("initializeFaceRegistry");
        return new ArrayList<>();
    }

    // To override
    // Method called when faces are detected.
    protected void onFacesDetected(Bitmap fullPhoto, List<FaceRecord> faces) {
        // Need to override and do something
        LOGGER.i("onFacesDetected");
        for (FaceRecord face: faces) {
            LOGGER.i(face.toString());
        }
    }

    // To override
    // Method called when at least one face was recognized (from the face registry)
    // from the previous face detection
    //
    // Need:
    // * DTO of the face and ID of the person detected
    // * Full photo taken by the camera
    // * Cropped image of the face detected
    protected void onFacesRecognized(Bitmap fullPhoto, List<BioPhotos> recognitions) {
        // Need to override and do something
        LOGGER.i("onFacesRecognized");
        for (BioPhotos faceRecognized : recognitions) {
            LOGGER.i(faceRecognized.toString());
        }
    }

    // To override
    // Method called when FaceFeaturesDetection is ON by calling the method
    // activateFaceFeaturesDetection(true)
    // and a NEW face was detected
    protected void onFaceFeaturesDetected(Bitmap fullPhoto, RecognitionResult recognitionResult) {
        LOGGER.i("onFaceFeaturesDetected");
        BioPhotos bioPhoto = new BioPhotos();
        bioPhoto.user = 0;
        bioPhoto.type = 9;
        bioPhoto.photo = recognitionResult.getCrop();
        bioPhoto.Features = new BioPhotoFeatures();
        bioPhoto.Features.setFeatures(recognitionResult.getFeatures());
        registerNewFace(bioPhoto);
    }

    // Useful extra setters
    // ---------------------------------------------------------------------------------------------
    public void setShowConfidence(boolean showConfidence) {
        this.showConfidence = showConfidence;
    }

    public void setShowFaceLabel(boolean showFaceLabel) {
        this.showFaceLabel = showFaceLabel;
    }
}
