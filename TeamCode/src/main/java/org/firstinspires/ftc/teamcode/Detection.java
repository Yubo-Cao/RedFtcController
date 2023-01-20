package org.firstinspires.ftc.teamcode;

public class Detection {
    public int x;
    public int y;
    public int width;
    public int height;
    public String label;
    public float confidence;

    public static final String[] LABELS = new String[]{"Dragon", "Robot", "Console"};

    public Detection(int x, int y, int width, int height, String label, float confidence) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.label = label;
        this.confidence = confidence;
    }

    public Detection(int x, int y, int width, int height, float[] classification, float confidence) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.confidence = confidence;
        int maxIndex = 0;
        for (int i = 1; i < classification.length; i++) {
            if (classification[i] > classification[maxIndex]) {
                maxIndex = i;
            }
        }
        this.label = LABELS[maxIndex];
    }

    @Override
    public String toString() {
        return "Detection{" + "x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + ", label='" + label + '\'' + ", confidence=" + confidence + '}';
    }

    public static Detection fromArray(float[] a, int width, int height) {
        int xc = (int) (a[0] * width),
                yc = (int) (a[1] * height),
                w = (int) (a[2] * width),
                h = (int) (a[3] * height);
        int x = xc - w / 2, y = yc - h / 2;
        float confidence = a[4];
        float[] classification = new float[a.length - 5];
        System.arraycopy(a, 5, classification, 0, classification.length);
        return new Detection(x, y, w, h, classification, confidence);
    }

}