package org.firstinspires.ftc.teamcode;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;
import org.openftc.easyopencv.OpenCvWebcam;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@TeleOp(name = "Open CV Powered Auton", group = "Linear Opmode")
public class OpenCVAuton extends LinearOpMode {
    OpenCvWebcam webcam;

    public static final Map<String, Integer> LABEL_2_INT =
            mapOf(new String[]{"dragon", "robot", "console"}, new Integer[]{0, 1, 2});
    public static final Map<Integer, String> INT_2_LABEL =
            mapOf(new Integer[]{0, 1, 2}, new String[]{"dragon", "robot", "console"});
    public static final float MIN_CONFIDENCE = 0.5f;
    public static final float IOU_THRESHOLD = 0.5f;

    @Override
    public void runOpMode() {
        int cameraMonitorViewId = hardwareMap.appContext.getResources()
                .getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        webcam = OpenCvCameraFactory.getInstance()
                .createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);
        webcam.setPipeline(new SamplePipeline());


        webcam.setMillisecondsPermissionTimeout(5000);
        webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                webcam.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT);
            }

            @Override
            public void onError(int errorCode) {
                telemetry.addData("Error", errorCode);
                telemetry.update();
            }
        });

        telemetry.addLine("Waiting for start");
        telemetry.update();


        waitForStart();

        while (opModeIsActive()) {
            telemetry.addData("Frame Count", webcam.getFrameCount());
            telemetry.addData("FPS", String.format("%.2f", webcam.getFps()));
            telemetry.addData("Total frame time ms", webcam.getTotalFrameTimeMs());
            telemetry.addData("Pipeline time ms", webcam.getPipelineTimeMs());
            telemetry.addData("Overhead time ms", webcam.getOverheadTimeMs());
            telemetry.addData("Theoretical max FPS", webcam.getCurrentPipelineMaxFps());
            telemetry.update();


            if (gamepad1.a) {
                webcam.stopStreaming();
            }

            sleep(100);
        }
    }


    static class Detection {
        public int x;
        public int y;
        public int width;
        public int height;
        public String label;
        public float confidence;

        public Detection(int x, int y, int width, int height, String label, float confidence) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.label = label;
            this.confidence = confidence;
        }


        @Override
        public String toString() {
            return "Detection{" + "x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + ", label='" + label + '\'' + ", confidence=" + confidence + '}';
        }
    }


    private static <K, V> Map<K, V> mapOf(K[] keys, V[] values) {
        Map<K, V> map = new HashMap<>();
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return map;
    }

    class SamplePipeline extends OpenCvPipeline {
        boolean viewportPaused;
        private Interpreter tflite;

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
            // 0:4: xc, yc, w, h
            // 4: confidence
            // 5:8: classification

            int width = input.cols();
            int height = input.rows();

            List<Detection> result =
                    Arrays.stream(outputArray[0])
                            .filter((a) -> a[4] > MIN_CONFIDENCE)
                            .map((a) -> {
                                int xc = (int) (a[0] * width),
                                        yc = (int) (a[1] * height),
                                        w = (int) (a[2] * width),
                                        h = (int) (a[3] * height);
                                int x = xc - w / 2, y = yc - h / 2;
                                float[] classification = new float[]{a[5], a[6], a[7]};
                                int maxIndex = 0;
                                for (int i = 1; i < classification.length; i++) {
                                    if (classification[i] > classification[maxIndex]) {
                                        maxIndex = i;
                                    }
                                }
                                float confidence = a[4];
                                return new Detection(x, y, w, h, INT_2_LABEL.get(maxIndex), confidence);
                            }).collect(Collectors.toList());
            result = applyNMS(result, 0.5f);

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

        public SamplePipeline() {
            try {
                Context ctx = hardwareMap.appContext;
                AssetManager assetManager = ctx.getAssets();
                loadModel(assetManager);
            } catch (IOException e) {
                e.printStackTrace();
            }
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

        public List<Detection> applyNMS(List<Detection> detections, float iouThreshold) {
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
}