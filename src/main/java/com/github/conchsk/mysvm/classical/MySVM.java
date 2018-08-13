package com.github.conchsk.mysvm.classical;

import java.util.List;

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
    private KernelInf kernel;
    private double eps;

    public double b;
    public RealVector alpha;

    public void fit(List<LabeledPoint> data, double C, double tol, KernelInf kernel) {
        int N = data.size();
        int D = data.get(0).features.length;
        double[][] features = new double[N][D];
        double[] label = new double[N];
        for (int i = 0; i < N; ++i) {
            label[i] = data.get(i).label;
            double[] featuresTmp = data.get(i).features;
            for (int j = 0; j < D; ++j)
                features[i][j] = featuresTmp[j];
        }

        this.features = new BlockRealMatrix(features);
        this.label = new ArrayRealVector(label);
        this.C = C;
        this.tol = tol;
        this.kernel = kernel;
        this.eps = 1e-3;

        smo();
    }

    public double predict(LabeledPoint data) {
        return calcU(new ArrayRealVector(data.features));
    }

    private double calcU(RealVector x) {
        ArrayRealVector kxx = new ArrayRealVector(label.getDimension());
        for (int i = 0; i < label.getDimension(); ++i)
            if (alpha.getEntry(i) > 0.0)
                kxx.setEntry(i, kernel.compute(features.getRowVector(i), x));
        return label.ebeMultiply(alpha).dotProduct(kxx) - b;
    }

    private int takeStep(int i1, int i2) {
        if (i1 == i2)
            return 0;
        RealVector X1 = features.getRowVector(i1);
        RealVector X2 = features.getRowVector(i2);
        double y1 = label.getEntry(i1);
        double y2 = label.getEntry(i2);
        double alph1 = alpha.getEntry(i1);
        double alph2 = alpha.getEntry(i2);
        double E1 = calcU(X1) - y1;
        double E2 = calcU(X2) - y2;
        double L = 0.0;
        double H = 0.0;

        double s = y1 * y2;
        if (s == -1.0) {
            L = Math.max(0, alph2 - alph1);
            H = Math.min(C, C + alph2 - alph1);
        } else {
            L = Math.max(0, alph2 + alph1 - C);
            H = Math.min(C, alph2 + alph1);
        }
        if (Math.abs(L - H) < eps)
            return 0;

        double k11 = kernel.compute(X1, X1);
        double k12 = kernel.compute(X1, X2);
        double k22 = kernel.compute(X2, X2);
        double eta = k11 + k22 - 2 * k12;

        double a2 = 0.0;
        // positive definite
        if (eta > 0) {
            a2 = alph2 + y2 * (E1 - E2) / eta;
            if (a2 < L)
                a2 = L;
            else if (a2 > H)
                a2 = H;
        }
        // semi-positive definite
        else {
            double f1 = y1 * (E1 + b) - alph1 * k11 - s * alph2 * k12;
            double f2 = y2 * (E2 + b) - s * alph1 * k12 - alph2 * k22;
            double L1 = alph1 + s * (alph2 - L);
            double H1 = alph1 + s * (alph2 - H);
            double PsiL = L1 * f1 + L * f2 + 0.5 * L1 * L1 * k11 + 0.5 * L * L * k22 + s * L * L1 * k12;
            double PsiH = H1 * f1 + H * f2 + 0.5 * H1 * H1 * k11 + 0.5 * H * H * k22 + s * H * H1 * k12;
            a2 = PsiL < PsiH ? L : H;
        }
        if (Math.abs(a2 - alph2) < eps)
            return 0;
        double a1 = alph1 + s * (alph2 - a2);

        // store alpha
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
        double E2 = calcU(features.getRowVector(i2)) - y2;
        double r2 = E2 * y2;
        if ((r2 < -tol && alpha2 < C) || (r2 > tol && alpha2 > 0)) {
            int i1 = -1;
            double deltaE = 0.0;
            for (int i = 0; i < alpha.getDimension(); ++i) {
                double yTmp = label.getEntry(i);
                double alphaTmp = alpha.getEntry(i);
                if (alphaTmp > 0 && alphaTmp < C) {
                    double ETmp = calcU(features.getRowVector(i)) - yTmp;
                    double deltaETmp = Math.abs(ETmp - E2);
                    if (deltaETmp > deltaE) {
                        i1 = i;
                        deltaE = deltaETmp;
                    }
                }
            }
            if (i1 != -1) {
                if (takeStep(i1, i2) == 1)
                    return 1;
            }
            for (int i = 0; i < alpha.getDimension(); ++i) {
                if (takeStep(i, i2) == 1)
                    return 1;
            }
        }
        return 0;
    }

    private void smo() {
        // initialize
        b = 0.0;
        alpha = new ArrayRealVector(label.getDimension());

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