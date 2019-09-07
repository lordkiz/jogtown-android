package com.jogtown.jogtown.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Conversions {
    public Conversions() {

    }

    public static String formatToHHMMSS(int secs) {
        int hours   = (int) Math.floor(secs / 3600) % 24;
        int minutes = (int) Math.floor(secs / 60) % 60;
        int seconds = secs % 60;

        List<Integer> time = Arrays.asList(hours, minutes, seconds);
        List<String> timeInStrings = new ArrayList<>();

        //This should be a lot easier using java8 streams
        //but that is available from API -24, and I am
        //trying to target even lower android API levels
        for (int t : time) {
            String tS;
            if (t < 10) {
                tS = "0" + Integer.toString(t, 10);
            } else {
                tS = "" + Integer.toString(t, 10);
            }
            timeInStrings.add(tS);
        }

        String formattedHHMMSS = "";

        for (int i = 0; i < timeInStrings.size(); i++) {
            if (!timeInStrings.get(i).equals("00") || i > 0) {
                formattedHHMMSS += timeInStrings.get(i);
                if (i < (timeInStrings.size() - 1)) {
                    formattedHHMMSS += ":";
                }
            }
        }

        return formattedHHMMSS;
    }


    public static String formattedHHMMSSToReadableSpeech(int secs) {
        String formattedHHMMSS = formatToHHMMSS(secs);
        boolean hasHour = false;
        String[] timeParts = formattedHHMMSS.split(":");
        int timePartsLength = timeParts.length;
        if (timePartsLength > 2) {
            hasHour = true;
        }

        String hr = "";
        String min = "";
        String sec = "";

        for (int i = 0; i < timePartsLength; i++) {
            int timePartInt = Integer.parseInt(timeParts[i]);
            if (hasHour) {

                if (timePartInt != 0) {
                    if (i == 0) {
                        String hrPronunciation = timePartInt > 1 ? "hours" : "hour";
                        hr = Integer.toString(timePartInt) + " " + hrPronunciation;
                    } else if (i == 1) {
                        String minPronunciation = timePartInt > 1 ? "minutes" : "minute";
                        min = Integer.toString(timePartInt) + " " + minPronunciation;
                    } else if (i == 2) {
                        String secPronunciation = timePartInt > 1 ? "seconds" : "second";
                        sec = Integer.toString(timePartInt) + " " + secPronunciation;
                    }
                }

            } else {

                if (timePartInt != 0) {
                    if (i == 0) {
                        String minPronunciation = timePartInt > 1 ? "minutes" : "minute";
                        min = Integer.toString(timePartInt) + " " + minPronunciation;
                    } else if (i == 1) {
                        String secPronunciation = timePartInt > 1 ? "seconds" : "second";
                        sec = Integer.toString(timePartInt) + " " + secPronunciation;
                    }
                }

            }
        }

        return hr + ", " + min + ", " + sec + ".";

    }



    public static int calculatePace(int distance, int duration) {
        if (duration == 0 || distance == 0) {
            return 0;
        }
        float km = (float) distance/1000;
        int secsPace = Math.round(duration / km);
        return secsPace;
    }



    public static String displayPace(int distance, int duration) {
        return formatToHHMMSS(calculatePace(distance, duration));
    }

    public static float calculateSpeed(int distance, int duration) {
        if (duration == 0 || distance == 0) {
            return 0.0f;
        }
        return distance /(float) duration;
    }

    public static String displaySpeed(int distance, int duration) {
        float sp = calculateSpeed(distance, duration);
        BigDecimal spToFixed2 = new BigDecimal(sp);
        spToFixed2 = spToFixed2.setScale(2, RoundingMode.HALF_UP);
        String speed = "" + spToFixed2 + " m/s";
        return speed;
    }




    public static float calculateCalories(int distance, int duration, int weight) {
        //Using MET values for physical activities based on
        //https://sites.google.com/site/compendiumofphysicalactivities/
        //Using the 2011 Compendium
        //MET = Metabolic Equivalent

        int pace = calculatePace(distance, duration);

        int met; //for jogging in general
        if (pace < 150) {
            met = 23;
        } else if (pace >= 150 && pace < 225) {
            met = 18;
        } else if (pace >= 225 && pace < 300) {
            met = 14;
        } else if (pace >= 300 && pace < 375) {
            met = 12;
        } else if (pace >= 375 && pace < 450) {
            met = 10;
        } else if (pace >= 450 && pace < 600) {
            met = 8;
        } else if (pace >= 600) {
            met = 6;
        } else {
            met = 5;
        }

        if (pace > 0) {
            int cal = met * weight;
            float perHour = duration / 3600.0f;
            float calories = (float) cal * perHour;
            return calories;
        }
        return 0f;

    }

    public static String displayCalories(int distance, int duration, int weight) {
        float cal = calculateCalories(distance, duration, weight);
        BigDecimal calToFixed2 = new BigDecimal(cal);
        calToFixed2 = calToFixed2.setScale(2, RoundingMode.HALF_UP);
        String calStr = "" + calToFixed2;
        return calStr;
    }



    public static String displayKilometres(int distance) {
        String kmStr = "";
        float km = (float) distance/1000;
        BigDecimal kmToFixed2 = new BigDecimal(km);
        kmToFixed2 = kmToFixed2.setScale(2, RoundingMode.HALF_UP);
        kmStr += kmToFixed2 + "";
        return kmStr;
    }


    public static int getDistanceFromSteps(int steps, String gender) {
        //returns distance in metres
        double AVERAGE_STEP_LENGTH = 78; //in centimetres
        if (gender.equals("female")) {
            AVERAGE_STEP_LENGTH = 70;
        }

        float distanceInKM = (float) (steps * AVERAGE_STEP_LENGTH) / 100000f;

        return Math.round(distanceInKM * 1000);
    }


    public static String formatDateTime(String dateTime) {

        String[] dateArr = dateTime.split("T");
        String[] date = dateArr[0].split("-");

        String month;
        switch(date[1]) {
            case "01":
                month = "Jan";
                break;
            case "02":
                month = "Feb";
                break;
            case "03":
                month = "Mar";
                break;
            case "04":
                month = "Apr";
                break;
            case "05":
                month = "May";
                break;
            case "06":
                month = "Jun";
                break;
            case "07":
                month = "Jul";
                break;
            case "08":
                month = "Aug";
                break;
            case "09":
                month = "Sep";
                break;
            case "10":
                month = "Oct";
                break;
            case "11":
                month = "Nov";
                break;
            case "12":
                month = "Dec";
                break;
            default:
                month = "";
        }


        String formatedDateTime = date[2] + " " + month + " " + date[0];

        return formatedDateTime;
    }



}
