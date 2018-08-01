package com.github.conchsk.mysvm;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DefaultRealMatrixPreservingVisitor;

public class Test {
    public static void main(String[] args) {
        //System.out.println(Iris.getDataset());
        double[][] t = new double[10][10];
        for (int i = 0; i < 10; ++i)
            for (int j = 0; j < 10; ++j)
                t[i][j] = i * 10 + j;
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(t);
        m.walkInRowOrder(new DefaultRealMatrixPreservingVisitor() {
            @Override
            public void visit(int row, int column, double value) {
                System.out.println(value);
            }
        });
    }
}