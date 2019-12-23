package es.lifevit.sdk.utils;

import java.math.BigDecimal;

public class BraceletUtils {

    protected static float getDistanceBySteps(int steps, int height) {
        return getStepDistanceByHeight(height) * ((float) steps);
    }


    protected static int getStepsByDistance(float distanceKm, int height) {
        return (int) (distanceKm / getStepDistanceByHeight(height));
    }


    private static float getStepDistanceByHeight(int height) {
        float stepDistance = 0.0f;
        if (height <= 50.0f) {
            stepDistance = 20.0f;
        } else if (50.0f < height && height <= 60.0f) {
            stepDistance = 22.0f;
        } else if (60.0f < height && height <= 70.0f) {
            stepDistance = 25.0f;
        } else if (70.0f < height && height <= 80.0f) {
            stepDistance = 29.0f;
        } else if (80.0f < height && height <= 90.0f) {
            stepDistance = 33.0f;
        } else if (90.0f < height && height <= 100.0f) {
            stepDistance = 37.0f;
        } else if (100.0f < height && height <= 110.0f) {
            stepDistance = 40.0f;
        } else if (110.0f < height && height <= 120.0f) {
            stepDistance = 44.0f;
        } else if (120.0f < height && height <= 130.0f) {
            stepDistance = 48.0f;
        } else if (130.0f < height && height <= 140.0f) {
            stepDistance = 51.0f;
        } else if (140.0f < height && height <= 150.0f) {
            stepDistance = 55.0f;
        } else if (150.0f < height && height <= 160.0f) {
            stepDistance = 59.0f;
        } else if (160.0f < height && height <= 170.0f) {
            stepDistance = 62.0f;
        } else if (170.0f < height && height <= 180.0f) {
            stepDistance = 66.0f;
        } else if (180.0f < height && height <= 190.0f) {
            stepDistance = 70.0f;
        } else if (190.0f < height && height <= 200.0f) {
            stepDistance = 74.0f;
        } else if (height > 200.0f) {
            stepDistance = 74.0f;
        }
        stepDistance /= 100000.0f;
        return stepDistance;
    }


    protected static float getCaloriesByDistance(float distance, float weight) {
        return 0.6f * weight * distance;
    }


    protected static float getDistanceByCalories(float calories, float weight) {
        return calories / (0.6f * weight);
    }


    protected static double round(double value, int scale, int roundingMode) {
        return new BigDecimal(value).setScale(scale, roundingMode).doubleValue();
    }


    public static String formatTime(int hour, int minute) {
        String hourStr;

        if (hour < 0) {
            hourStr = "00";
        } else if (hour > 23) {
            hourStr = "23";
        } else {
            hourStr = String.valueOf(hour);
        }

        while (hourStr.length() < 2) {
            hourStr = "0" + hourStr;
        }

        String minuteStr;

        if (minute < 0) {
            minuteStr = "00";
        } else if (minute > 59) {
            minuteStr = "59";
        } else {
            minuteStr = String.valueOf(minute);
        }

        while (minuteStr.length() < 2) {
            minuteStr = "0" + minuteStr;
        }

        return hourStr + minuteStr;
    }


}
