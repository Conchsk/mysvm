package com.github.conchsk.mysvm.classical;

import java.util.List;
import java.util.Random;

import com.github.conchsk.mysvm.dataset.LabeledPoint;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class MySVM {
    private RealMatrix features;
    private RealVector label;
    private double C;
    private double tol;
    private double eps;
    private KernelInf kernel;

    private Random rand;
    public RealVector alpha;
    private double b;

    public void fit(List<LabeledPoint> data, double C, double tol, double eps, KernelInf kernel) {
        int N = data.size();
        int D = data.get(0).features.length;
        double[][] features = new double[N][D];
        double[] label = new double[N];
        for (int i = 0; i < N; ++i) {
            label[i] = (data.get(i).label == 0.0 ? -1.0 : 1.0);
            double[] featuresTmp = data.get(i).features;
            for (int j = 0; j < D; ++j)
                features[i][j] = featuresTmp[j];
        }

        this.features = new BlockRealMatrix(features);
        this.label = new ArrayRealVector(label);
        this.C = C;
        this.tol = tol;
        this.eps = eps;
        this.kernel = kernel;

        smo();
    }

    public double predict(LabeledPoint data) {
        return calcU(new LinearKernel(), new ArrayRealVector(data.features));
    }

    private double calcU(KernelInf kernel, RealVector x) {
        RealVector kxx = new ArrayRealVector(label.getDimension());
        for (int i = 0; i < label.getDimension(); ++i)
            kxx.setEntry(i, kernel.compute(features.getRowVector(i), x));
        return label.ebeMultiply(alpha).dotProduct(kxx);
    }

    private int takeStep(int i1, int i2) {
        if (i1 == i2)
            return 0;
        double alph1 = alpha.getEntry(i1);
        double alph2 = alpha.getEntry(i2);
        double y1 = label.getEntry(i1);
        double y2 = label.getEntry(i2);
        double E1 = calcU(kernel, features.getRowVector(i1)) - y1;
        double E2 = calcU(kernel, features.getRowVector(i2)) - y2;
        double L = 0;
        double H = 0;

        double s = y1 * y2;// sign (represent y1 == y2 or y1 != y2)
        if (s == 1) {
            L = Math.max(0, alph2 - alph1);
            H = Math.min(C, C + alph2 - alph1);
        } else {
            L = Math.max(0, alph2 + alph1 - C);
            H = Math.min(C, alph2 + alph1);
        }
        if (L == H)
            return 0;

        double k11 = kernel.compute(features.getRowVector(i1), features.getRowVector(i1));
        double k12 = kernel.compute(features.getRowVector(i1), features.getRowVector(i2));
        double k22 = kernel.compute(features.getRowVector(i2), features.getRowVector(i2));
        double eta = 2 * k12 - k11 + k22;

        double a2 = 0.0;
        // positive definite
        if (eta < 0) {
            a2 = alph2 - y2 * (E1 - E2) / eta;
            if (a2 < L)
                a2 = L;
            else if (a2 > H)
                a2 = H;
        }
        // semi-positive definite
        else {

        }
        if (Math.abs(a2 - alph2) < eps * (a2 + alph2 + eps))
            return 0;
        double a1 = alph1 + s * (alph2 - a2);
        alpha.setEntry(i1, a1);
        alpha.setEntry(i2, a2);

        // computing the threshold
        double b1 = E1 + y1 * (a1 - alph1) * k11 + y2 * (a2 - alph2) * k12 + b;
        double b2 = E2 + y1 * (a1 - alph1) * k12 + y2 * (a2 - alph2) * k22 + b;
        b = (b1 + b2) / 2;
        return 1;
    }

    private int examineExample(int i2) {
        double y2 = label.getEntry(i2);
        double alpha2 = alpha.getEntry(i2);
        double E2 = calcU(kernel, features.getRowVector(i2)) - y2;
        double r2 = E2 * y2;
        if ((r2 < -tol && alpha2 < C) || (r2 > tol && alpha2 > 0)) {
            int i1 = -1;
            double deltaE = 0.0;
            for (int i = 0; i < alpha.getDimension(); ++i) {
                double yTmp = label.getEntry(i);
                double alphaTmp = alpha.getEntry(i);
                if (alphaTmp > 0 && alphaTmp < C) {
                    double ETmp = calcU(kernel, features.getRowVector(i)) - yTmp;
                    double delteETmp = Math.abs(ETmp - E2);
                    if (delteETmp > deltaE) {
                        i1 = i;
                        deltaE = delteETmp;
                    }
                }
            }
            if (i1 != -1) {
                if (takeStep(i1, i2) == 1)
                    return 1;
            } else {
                i1 = i2;
                while (i1 == i2)
                    i1 = rand.nextInt(alpha.getDimension());
                if (takeStep(i1, i2) == 1)
                    return 1;
            }
        }
        return 0;
    }

    private void smo() {
        // initialize
        rand = new Random(1234L);
        alpha = new ArrayRealVector(label.getDimension());
        b = 0.0;

        // main routine
        int numChanged = 0;
        int examineAll = 1;
        while (numChanged > 0 || examineAll == 1) {
            numChanged = 0;
            if (examineAll == 1) {
                for (int i = 0; i < alpha.getDimension(); ++i)
                    numChanged += examineExample(i);
            } else {
                for (int i = 0; i < alpha.getDimension(); ++i) {
                    double alphaI = alpha.getEntry(i);
                    if (alphaI > 0 && alphaI < C)
                        numChanged += examineExample(i);
                }
            }
            if (examineAll == 1)
                examineAll = 0;
            else if (numChanged == 0)
                examineAll = 1;
        }
    }
}