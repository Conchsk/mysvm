package com.github.conchsk.mysvm.classical;

import org.apache.commons.math3.linear.RealVector;

public class LinearKernel implements KernelInf {
    @Override
    public double compute(RealVector x1, RealVector x2) {
        return x1.dotProduct(x2);
    }
}