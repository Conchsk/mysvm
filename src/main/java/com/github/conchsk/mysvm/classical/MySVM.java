package com.github.conchsk.mysvm.classical;

import com.github.conchsk.mysvm.dataset.LabeledPoint;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class MySVM {
    private RealMatrix features;
    private RealVector labels;
    private double C, tol, eps;
    private KernelInf kernel;

    private int N;
    private double b;
    public RealVector alphaVector;
    private RealMatrix kernelMatrix;

    public void fit(double[][] features, double[] labels, double C, double tol, KernelInf kernel) {
        this.features = new BlockRealMatrix(features);
        this.labels = new ArrayRealVector(labels);
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
        ArrayRealVector kxx = new ArrayRealVector(N);
        for (int i = 0; i < N; ++i)
            if (alphaVector.getEntry(i) > 0.0)
                kxx.setEntry(i, kernel.compute(features.getRowVector(i), x));
        return labels.ebeMultiply(alphaVector).dotProduct(kxx) - b;
    }

    private int takeStep(int i1, int i2) {
        if (i1 == i2)
            return 0;
        RealVector X1 = features.getRowVector(i1);
        RealVector X2 = features.getRowVector(i2);
        double y1 = labels.getEntry(i1);
        double y2 = labels.getEntry(i2);
        double alph1 = alphaVector.getEntry(i1);
        double alph2 = alphaVector.getEntry(i2);
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

        double k11 = kernelMatrix.getEntry(i1, i1);
        double k12 = kernelMatrix.getEntry(i1, i2);
        double k22 = kernelMatrix.getEntry(i2, i2);
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
        alphaVector.setEntry(i1, a1);
        alphaVector.setEntry(i2, a2);

        // computing the threshold
        double b1 = E1 + y1 * (a1 - alph1) * k11 + y2 * (a2 - alph2) * k12 + b;
        double b2 = E2 + y1 * (a1 - alph1) * k12 + y2 * (a2 - alph2) * k22 + b;
        b = (b1 + b2) / 2;
        return 1;
    }

    private int examineExample(int i2) {
        double y2 = labels.getEntry(i2);
        double alpha2 = alphaVector.getEntry(i2);
        double E2 = calcU(features.getRowVector(i2)) - y2;
        double r2 = E2 * y2;
        if ((r2 < -tol && alpha2 < C) || (r2 > tol && alpha2 > 0)) {
            int i1 = -1;
            double deltaE = 0.0;
            for (int i = 0; i < N; ++i) {
                double yTmp = labels.getEntry(i);
                double alphaTmp = alphaVector.getEntry(i);
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
            for (int i = 0; i < N; ++i) {
                if (takeStep(i, i2) == 1)
                    return 1;
            }
        }
        return 0;
    }

    private void smo() {
        // initialize
        N = features.getRowDimension();
        b = 0.0;
        alphaVector = new ArrayRealVector(N);
        double[][] tempKernelMatrix = new double[N][N];
        for (int i = 0; i < N; ++i)
            for (int j = i; j < N; ++j)
                tempKernelMatrix[i][j] = tempKernelMatrix[j][i] = kernel.compute(features.getRowVector(i), features.getRowVector(j));
        kernelMatrix = new BlockRealMatrix(tempKernelMatrix);

        // main routine
        int numChanged = 0;
        int examineAll = 1;
        while (numChanged > 0 || examineAll == 1) {
            numChanged = 0;
            if (examineAll == 1) {
                for (int i = 0; i < N; ++i)
                    numChanged += examineExample(i);
            } else {
                for (int i = 0; i < N; ++i) {
                    double alphaI = alphaVector.getEntry(i);
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