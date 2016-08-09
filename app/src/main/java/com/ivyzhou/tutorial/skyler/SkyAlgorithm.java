package com.ivyzhou.tutorial.skyler;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.ivyzhou.tutorial.skyler.weather.Forecast;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Ivy Zhou on 7/20/2016.
 * Performs image processing on a bitmap to find what to set the sky background to
 */
public class SkyAlgorithm {
    private static double Turbidity = 3;
    private static double SolarAzimuth = 0;
    private static double SolarZenith = 0.5;

    private double Yz;
    private double xz;
    private double yz;

    private int colorResult;
    private Forecast currentWeather;
    private Coeff coeffsY;
    private Coeff coeffsx;
    private Coeff coeffsy;
    private double zenith;
    private double azimuth;

    // find average color from camera
    public SkyAlgorithm(Bitmap bitmap, Forecast currentWeather){
        this.currentWeather = currentWeather;
        skyposition();
        CalculateZenitalAbsolutes();
        CalculateCoefficents();

//        // averages the colors out
//        int redColors = 0;
//        int greenColors = 0;
//        int blueColors = 0;
//
//        int height = bitmap.getHeight();
//        int width = bitmap.getWidth();
//        double pixelCount = height * width;
//
//        for (int y = 0; y < height; y++)
//        {
//            for (int x = 0; x < width; x++)
//            {
//                int c = bitmap.getPixel(x, y);
//                redColors += Color.red(c);
//                greenColors += Color.green(c);
//                blueColors += Color.blue(c);
//            }
//        }
//        // calculate average of bitmap r,g,b values
//        int red = (int) (redColors/pixelCount);
//        int green = (int) (greenColors/pixelCount);
//        int blue = (int) (blueColors/pixelCount);
//
//        colorResult = Color.rgb(red, green, blue);
    }

    public int getColorResult() {
        return colorResult;
    }

    // SKY COLOR GENERATION EXPERIMENTS
    public void skyposition() {
        double longitude = 0 * Math.PI / 180;
        double latitude = 50 * Math.PI / 180;
        TimeZone curTZ = TimeZone.getTimeZone(currentWeather.getTimezone());

        // Calculate the julian day
        // algorithm from Jean Meeus's Astronomical Formulae for Calculators
        Date curDate = currentWeather.getDate();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")); // use UTC time
        cal.setTime(curDate);

        double standardTime = cal.get(Calendar.MILLISECOND) / 1000.0 / 60 / 60;
        int julianDate = cal.get(Calendar.DAY_OF_YEAR);
        double standardMeridian = (curTZ.getRawOffset() / 1000 / 60 / 60) * 15 * Math.PI / 180;

        double solarTime = standardTime
                + 0.170 * Math.sin(4 * Math.PI * (julianDate - 80) / 373)
                - 0.129 * Math.sin(2 * Math.PI * (julianDate - 8) / 355)
                + 12 * (standardMeridian - longitude) / Math.PI;

        double solarDeclination = 0.4093 * Math.sin(2 * Math.PI * (julianDate - 81) / 368);
        this.zenith = Math.PI / 2 - Math.asin(
                Math.sin(latitude)* Math.sin(solarDeclination)
                - Math.cos(latitude) * Math.cos(solarDeclination) * Math.cos(Math.PI * solarTime / 12));
        this.azimuth = Math.atan((-Math.cos(solarDeclination) * Math.sin(Math.PI * solarTime / 12))
                        / (Math.cos(latitude) * Math.sin(solarDeclination)
                        - Math.sin(latitude) * Math.cos(solarDeclination) * Math.cos(Math.PI * solarTime / 12)));

    }

    private void CalculateZenitalAbsolutes()
    {
        double Yz = (4.0453 * Turbidity - 4.9710) * Math.tan((4.0 / 9 - Turbidity / 120f) * (Math.PI - 2 * SolarZenith)) - 0.2155 * Turbidity + 2.4192;
        double Y0 = (4.0453 * Turbidity - 4.9710) * Math.tan((4.0 / 9 - Turbidity / 120f) * (Math.PI)) - 0.2155 * Turbidity + 2.4192; ;
        this.Yz = (float)(Yz / Y0);

        double z3 = (float)Math.pow(SolarZenith, 3);
        double z2 = SolarZenith * SolarZenith;
        double z = SolarZenith;
        Vector3 T_vec = new Vector3(Turbidity * Turbidity, Turbidity, 1.0);

        Vector3 x = new Vector3(
                0.00166f * z3 - 0.00375f * z2 + 0.00209f * z,
                -0.02903f * z3 + 0.06377f * z2 - 0.03202f * z + 0.00394f,
                0.11693f * z3 - 0.21196f * z2 + 0.06052f * z + 0.25886f);
        this.xz = T_vec.dot(x);

        Vector3 y = new Vector3(
                0.00275f * z3 - 0.00610f * z2 + 0.00317f * z,
                -0.04214f * z3 + 0.08970f * z2 - 0.04153f * z + 0.00516f,
                0.15346f * z3 - 0.26756f * z2 + 0.06670f * z + 0.26688f);
        this.yz = T_vec.dot(y);
    }

    private void CalculateCoefficents()
    {
        Coeff coeffsY = new Coeff();
        coeffsY.A = 0.1787f * Turbidity - 1.4630f;
        coeffsY.B = -0.3554f * Turbidity + 0.4275f;
        coeffsY.C = -0.0227f * Turbidity + 5.3251f;
        coeffsY.D = 0.1206f * Turbidity - 2.5771f;
        coeffsY.E = -0.0670f * Turbidity + 0.3703f;
        this.coeffsY = coeffsY;

        Coeff coeffsx = new Coeff();
        coeffsx.A = -0.0193f * Turbidity - 0.2592f;
        coeffsx.B = -0.0665f * Turbidity + 0.0008f;
        coeffsx.C = -0.0004f * Turbidity + 0.2125f;
        coeffsx.D = -0.0641f * Turbidity - 0.8989f;
        coeffsx.E = -0.0033f * Turbidity + 0.0452f;
        this.coeffsx = coeffsx;

        Coeff coeffsy = new Coeff();
        coeffsy.A = -0.0167f * Turbidity - 0.2608f;
        coeffsy.B = -0.0950f * Turbidity + 0.0092f;
        coeffsy.C = -0.0079f * Turbidity + 0.2102f;
        coeffsy.D = -0.0441f * Turbidity - 1.6537f;
        coeffsy.E = -0.0109f * Turbidity + 0.0529f;
        this.coeffsy = coeffsy;
    }

    private double Perez(double zenith, double gamma, Coeff coeffs)
    {
        return  (1 + coeffs.A * Math.exp(coeffs.B / Math.cos(zenith))) *
                (1 + coeffs.C * Math.exp(coeffs.D * gamma)
                        + coeffs.E * Math.pow(Math.cos(gamma), 2));
    }

    private Color RGB(double Y, double x, double y)
    {
        double X = x/y*Y;
        double Z = (1-x-y)/y*Y;
        Color rgb = new Color();
        colorResult = rgb.argb(1, (int) Math.round(3.2406 * X - 1.5372 * Y - 0.4986 * Z),
                (int) Math.round(-0.9689 * X + 1.8758 * Y + 0.0415 * Z),
                (int) Math.round(0.0557 * X - 0.2040 * Y + 1.0570 * Z));
        return rgb;
    }

    private double Gamma(double zenith, double azimuth)
    {
        return Math.acos(
                Math.sin(SolarZenith) * Math.sin(zenith) * Math.cos(azimuth-SolarAzimuth)
                        + Math.cos(SolarZenith)* Math.cos(zenith));
    }

    private Color PixelShader()
    {
        //azimuth = 0;//atan2(p.world_pos.x, p.world_pos.z);
        //double zenith = 90; //Math.acos(p.world_pos.y / length(p.world_pos.xyz));

        double gamma = Gamma(zenith, azimuth);
        zenith = Math.min(zenith, 3.1415926/2.0);
        double Yp = Yz * Perez(zenith, gamma, coeffsY) / Perez(0, SolarZenith, coeffsY);
        double xp = xz * Perez(zenith, gamma, coeffsx) / Perez(0, SolarZenith, coeffsx);
        double yp = yz * Perez(zenith, gamma, coeffsy) / Perez(0, SolarZenith, coeffsy);

        return RGB(Yp, xp, yp);
    }

    private class Coeff {
        public double A;
        public double B;
        public double C;
        public double D;
        public double E;

        public Coeff() {}
    }

}
