package hk.ust.protractor;

import android.util.Log;

import java.util.TooManyListenersException;

/**
 * Created by lianxiang on 2016/12/20.
 */

class Points {
    private double x;
    private double y;
    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public void setPoint(double a, double b) {
        x = a;
        y = b;
    }
    public Points(double a, double b) {
        x = a;
        y = b;
    }
}

class Pair {
    private Template template;
    private double score;
    public Pair(Template t, double s) {
        template = t;
        score = s;
    }
    public Template getTemplate() {
        return template;
    }
    public double getScore() {
        return score;
    }
}

class Template {
    private int index;
    private double[] vector;
    private String name;
    public int getIndex() {
        return index;
    }
    public double[] getVector() {
        return vector;
    }
    public String getName() {
        return name;
    }
    public Template(int i, double[] v, String n) {
        index = i;
        vector = v;
        name = n;
    }
}
public class Recognizer {
    public static Template addGesture(Points[] points, int n, boolean oSensitive, int index, String name) {
        Points[] resampledPoints = resample(points, n);
        if(resampledPoints == null) android.util.Log.d("Resample", "Resample Error");
        else {for(int i = 0; i < resampledPoints.length; ++i){
            android.util.Log.d("Resample", Integer.toString(i));
        android.util.Log.d("Resample", Double.toString(resampledPoints[i].getX()));}}
        double[] vectors = vectorize(resampledPoints, oSensitive);
        return new Template(index, vectors, name);
    }

    public static Pair recognizeGesture(Points[] points, int n, boolean oSensitive, Template[] tem) {
        Points[] resampledPoints = resample(points, n);
        double[] vectors = vectorize(resampledPoints, oSensitive);

        Pair p = recognize(vectors, tem);
        for(int i = 0; i < vectors.length; ++i) {
            Log.d("Recognize", Double.toString(p.getTemplate().getVector()[i]));
            Log.d("Recognize", Double.toString(vectors[i]));
        }
        return p;
    }
    public static Points[] resample(Points[] points, int n) {
        double pathLen = pathLength(points) / (n - 1);
        double dist = 0;
        Points[] newPoints = new Points[n];
        newPoints[0] = points[0];
        int t = 1;
        for(int i = 1; i < points.length; ++i) {
            double tmp = distance(points[i - 1], points[i]);
            if(dist + tmp >= pathLen) {
                double x = points[i - 1].getX() + (pathLen - dist) / tmp * (points[i].getX() - points[i - 1].getX());
                double y = points[i - 1].getY() + (pathLen - dist) / tmp * (points[i].getY() - points[i - 1].getY());

                newPoints[t++] = new Points(x, y);

                // Insert the new point without moving lots of points
                points[i - 1] = new Points(x, y);
                --i;

                dist = 0;
            }
            else {
                dist += tmp;
            }
        }
        newPoints[n - 1] = points[points.length - 1];
        return newPoints;
    }

    private static double pathLength(Points[] points) {
        double dist = 0;
        for(int i = 0; i < points.length - 1; ++i) {
            dist += distance(points[i], points[i + 1]);
        }
        return dist;
    }

    private static double distance(Points a, Points b) {
        double deltaX = a.getX() - b.getX();
        double deltaY = a.getY() - b.getY();
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    public static double[] vectorize(Points[] points, boolean oSensitive) {
        double[] vector = new double[2 * points.length];
        Points centroid = getCentroid(points);
        translate(points, centroid);
        double indicativeAngle = Math.atan2(points[0].getY(), points[0].getX());
        double delta;
        if(oSensitive) {
            delta = (Math.PI / 4) * Math.floor((indicativeAngle + Math.PI / 8) / (Math.PI / 4)) - indicativeAngle;
        }
        else {
            delta = - indicativeAngle;
        }
        double sum = 0;
        for(int i = 0; i < points.length; ++i) {
            double newX = points[i].getX() * Math.cos(delta) - points[i].getY() * Math.sin(delta);
            double newY = points[i].getY() * Math.cos(delta) + points[i].getX() * Math.sin(delta);
            vector[2 * i] = newX;
            vector[2 * i + 1] = newY;
            sum += newX * newX + newY * newY;
        }
        double magnitude = Math.sqrt(sum);
        for(int i = 0; i < vector.length; ++i) {
            vector[i] /= magnitude;
        }
        return vector;
    }


    public static Points getCentroid(Points[] points) {
        double sumX = 0, sumY = 0;
        for(int i = 0; i < points.length; ++i) {
            sumX += points[i].getX();
            sumY += points[i].getY();
        }
        return new Points(sumX / points.length, sumY / points.length);
    }

    public static void translate(Points[] points, Points centroid) {
        for(int i = 0; i < points.length; ++i) {
            points[i].setPoint(points[i].getX() - centroid.getX(), points[i].getY() - centroid.getY());
        }
    }



    public static Pair recognize(double[] vector, Template[] tem) {
        double maxScore = 0;
        Template match = null;
        for(int i = 0; i < tem.length; ++i) {
            double distance = optimalCosDistance(tem[i].getVector(), vector);
            double score = 1 / distance;
            if(score >= maxScore) {
                maxScore = score;
                match = tem[i];
            }
        }
        return new Pair(match, maxScore);
    }

    private static double optimalCosDistance(double[] x, double[] y) {
        double a = 0, b = 0;
        for(int i = 0; i < x.length / 2; ++i) {
            a += x[2 * i] * y[2 * i] + x[2 * i + 1] * y[2 * i + 1];
            b += x[2 * i] * y[2 * i + 1] - x[2 * i + 1] * y[2 * i];
        }
        double angle = Math.atan(b / a);
        return Math.acos(a * Math.cos(angle) + b * Math.cos(angle));
    }

}











