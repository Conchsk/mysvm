package com.github.conchsk.mysvm.classical;

import org.apache.commons.math3.linear.RealVector;

@FunctionalInterface
public interface KernelInf {
    double compute(RealVector x1, RealVector x2);
}