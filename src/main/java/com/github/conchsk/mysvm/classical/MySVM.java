package com.github.conchsk.mysvm.classical;

import java.util.List;

import com.github.conchsk.mysvm.dataset.LabeledPoint;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class MySVM {
    public static MySVMModel train(List<LabeledPoint> data) {
        ArrayRealVector alpha = new ArrayRealVector(data.size());
        return null;
    }

    public static double calcU(RealMatrix features, RealVector labels, RealVector alpha, double b, KernelInf kernel,
            RealVector x) {
        RealVector kxx = new ArrayRealVector(labels.getDimension());
        for (int i = 0; i < labels.getDimension(); ++i)
            kxx.setEntry(i, kernel.compute(features.getRowVector(i), x));
        return labels.ebeMultiply(alpha).dotProduct(kxx);
    }

    public static double calcE(RealMatrix features, RealVector labels, RealVector alpha, double b, KernelInf kernel) {
        labels.ebeMultiply(alpha)
        return calcU(labels, alpha, features, features.getRowVector(index), kernel, b) - labels.getEntry(index);
    }

    private static void smo() {
        
    }
}