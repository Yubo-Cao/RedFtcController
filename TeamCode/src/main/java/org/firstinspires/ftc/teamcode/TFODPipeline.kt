package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.teamcode.Utils.telemetry;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;
import org.openftc.easyopencv.OpenCvWebcam;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class TFODPipeline extends OpenCvPipeline {
    // model
    private Interpreter tflite;
    private static float min_confidence = 0.5f;
    private static float iou_threshold = 0.5f;

    // camera IO
    boolean viewportPaused;
    private OpenCvWebcam webcam;

    @Override
    public Mat processFrame(Mat input) {
        float[][][][] inputArray = new float[1][640][640][3];
        Mat resized = new Mat();
        Imgproc.resize(input, resized, new Size(640, 640));
        for (int i = 0; i < 640; i++) {
            for (int j = 0; j < 640; j++) {
                double[] pixel = resized.get(i, j);
                inputArray[0][i][j][0] = (float) pixel[0];
                inputArray[0][i][j][1] = (float) pixel[1];
                inputArray[0][i][j][2] = (float) pixel[2];
            }
        }
        float[][][] outputArray = new float[1][25200][8];
        tflite.run(inputArray, outputArray);
        int width = input.cols();
        int height = input.rows();
        List<Detection> result =
                Arrays.stream(outputArray[0])
                        .filter((a) -> a[4] > min_confidence)
                        .map((array) -> Detection.fromArray(array, width, height))
                        .collect(Collectors.toList());
        result = nms(result, 0.5f);

        for (Detection r : result) {
            telemetry.addData("Detection", r);
        }
        throw new RuntimeException("Stop");
    }

    @Override
    public void onViewportTapped() {
        viewportPaused = !viewportPaused;

        if (viewportPaused) {
            webcam.pauseViewport();
        } else {
            webcam.resumeViewport();
        }
    }

    public TFODPipeline(AssetManager assetManager, OpenCvWebcam webcam, float min_confidence, float iou_threshold) {
        try {
            loadModel(assetManager);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.webcam = webcam;
    }

    public TFODPipeline(AssetManager assetManager, OpenCvWebcam webcam) {
        this(assetManager, webcam, min_confidence, iou_threshold);
    }


    public void loadModel(AssetManager assetManager) throws IOException {
        String modelPath = "best.tflite";
        tflite = new Interpreter(loadModelFile(assetManager, modelPath));
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileChannel fileChannel;
        try (FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor())) {
            fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
    }

    public List<Detection> nms(List<Detection> detections, float iouThreshold) {
        detections.sort((d1, d2) -> -Float.compare(d1.confidence, d2.confidence));

        List<Detection> result = new ArrayList<>();
        while (!detections.isEmpty()) {
            Detection detection = detections.remove(0);
            result.add(detection);

            List<Detection> removeList = new ArrayList<>();
            for (Detection d : detections) {
                if (iou(detection.x, detection.y, detection.width, detection.height, d.x, d.y, d.width, d.height) > iouThreshold) {
                    removeList.add(d);
                }
            }
            detections.removeAll(removeList);
        }
        return result;
    }

    private float iou(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2) {
        int x_overlap = Math.max(0, Math.min(x1 + w1, x2 + w2) - Math.max(x1, x2));
        int y_overlap = Math.max(0, Math.min(y1 + h1, y2 + h2) - Math.max(y1, y2));
        int intersection = x_overlap * y_overlap;
        int union = w1 * h1 + w2 * h2 - intersection;
        return (float) intersection / union;
    }
}
