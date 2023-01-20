package org.firstinspires.ftc.teamcode;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import java.util.List;

@Autonomous(name = "AI-powered Autonomous Mode")
public class Auton extends LinearOpMode {
    private static final String TFOD_MODEL_ASSET = "model.tflite";
    // adb
//    private static final String TFOD_MODEL_FILE = Environment.getExternalStorageDirectory().getPath() + "/model.tflite";

    private static final String[] LABELS = {
            "1 Dragon",
            "2 Robot",
            "3 Console"
    };

    /**
     * Vuforia
     */
    private static final String VUFORIA_KEY =
            "AcUKrUj/////AAABmSXjxFQWh0pBkXaueCZgFLJEhfvozNSyGe2R1/LU1uY184CRr2/yQnRSCnhuR5WC4yX+Bg4wZyDwZIUdT7GMbq5fYqmOofVARi1iV0KjHRL6mOcZ0Mkum7Mt/whgXhvglqMGpXUMeGg/SDut9PVoKAYFhOM/NGrlRK8OWJPWhaMMhydlZb3Kz4BtAhOslCONU8J5lOBj6WyYruNEdJoncEc1kkF7CK6JHJy9C8Wmg816wzqVB98o9Ca+EKiuUb06Y23aOj/SIXyP9m+k3RHarjw1SaAuf48LTLDTJmBlsHhfS4H4bVL9t67NuSVj8xRgkDW/vfz0F6+aUu2SVD7Udt5PhX7qTa4BSbOX0PWgH/aG";
    private VuforiaLocalizer vuforia;

    /**
     * Tensorflow object detection
     */
    private TFObjectDetector tfod;

    /**
     * DriveTrain
     */
    private DriveTrain driveTrain;

    @Override
    public void runOpMode() {
        initializeVuforia();
        initializeTFOD();
        driveTrain = new DriveTrain(hardwareMap);

        if (tfod != null) {
            tfod.activate();
            tfod.setZoom(1.0, 16.0 / 9.0);
        }

        telemetry.addData(">", "Press Play to start op mode");
        telemetry.update();
        waitForStart();

        if (!opModeIsActive()) return;
        if (tfod == null) return;


        String label = "";

        while (opModeIsActive() && label.isEmpty()) {
            List<Recognition> recognitions = tfod.getUpdatedRecognitions();
            if (recognitions == null) continue;
            Log.d("X", "# Objects Detected: " + recognitions.size() + "\n");

            double maxConfience = 0.0;
            for (Recognition recogn : recognitions) {
                if (maxConfience < recogn.getConfidence()) {
                    label = recogn.getLabel();
                    maxConfience = recogn.getConfidence();
                }
            }
        }
        switch (label) {
            case "1 Dragon":
                case1();
                break;
            case "2 Robot":
                case2();
                break;
            case "3 Console":
                case3();
                break;
            default:
                case2();
        }
        case2();
        tfod.shutdown();
    }

    /**
     * Move for case 1
     */
    private void case1() {
        driveTrain.move(0, 0.75);
        driveTrain.wait(1000.0);
        driveTrain.move(-0.75, 0);
        driveTrain.wait(1000.0);
    }

    /**
     * Move for case 2
     */
    private void case2() {
        driveTrain.move(0, 0.75);
        driveTrain.wait(1800.0);
    }

    /**
     * Move for case 2
     */
    private void case3() {
        driveTrain.move(0, 0.75);
        driveTrain.wait(1000.0);
        driveTrain.move(0.75, 0);
        driveTrain.wait(1000.0);
    }

    /**
     * Initialize the Vuforia localization engine.
     */
    private void initializeVuforia() {
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraDirection = CameraDirection.BACK;

        /*
        CameraManager mgr = ClassFactory.getInstance().getCameraManager();
        CameraName name = mgr.getAllWebcams().get(0);
        parameters.cameraName = name;
         */

        // Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);
    }

    /**
     * Initialize the TensorFlow Object Detection engine.
     */
    private void initializeTFOD() {
        int tfodMonitorViewId =
                hardwareMap.appContext.getResources().getIdentifier("tfodMonitorViewId",
                        "id",
                        hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters =
                new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfodParameters.minResultConfidence = 0.55f;
        tfodParameters.isModelTensorFlow2 = true;
        tfodParameters.inputSize = 300;

        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABELS);
//        tfod.loadModelFromFile(TFOD_MODEL_FILE, LABELS);
    }
}
