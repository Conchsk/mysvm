package com.github.conchsk.mysvm.classical;

import org.apache.commons.math3.linear.RealVector;

public class GussianKernel implements KernelInf {
    private double sigma;

    public GussianKernel(double sigma) {
        this.sigma = sigma;
    }

    @Override
    public double compute(RealVector x1, RealVector x2) {
        return Math.exp(-x1.subtract(x2).getNorm() / (sigma * sigma));
    }
}
