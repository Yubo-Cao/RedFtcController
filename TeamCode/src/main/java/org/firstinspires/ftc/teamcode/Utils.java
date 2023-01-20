package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Utils {
    public static Telemetry telemetry;

    public static void log(String message) {
        telemetry.addData("Log", message);
        telemetry.update();
    }

    public static void log(String message, Object... args) {
        telemetry.addData("Log", String.format(message, args));
        telemetry.update();
    }
}
