package com.github.conchsk.mysvm;

import java.util.Date;

import org.apache.commons.math3.linear.BlockRealMatrix;

public class Main {
    public static void main(String[] args) {
        // Toy toy = new Toy("linear", 2);
        // List<LabeledPoint> trainData = new ArrayList<>();
        // List<LabeledPoint> testData = new ArrayList<>();
        // toy.getDataset(trainData, testData, 0.1);
        // System.out.println(trainData);
        // System.out.println(testData);
//        MySVM svm = new MySVM();
//        svm.fit(trainData, 1.0, 0.001, new LinearKernel());
//        System.out.println(svm.alpha);
//        for (LabeledPoint lp : testData) {
//            double predict = svm.predict(lp);
//            System.out.println("label is: " + lp.label + "; predict is: " + predict);
//        }
        int K = 1000;
        double[][] test = new double[K][K];
        for (int i = 0; i < K; ++i)
            for (int j = 0; j < K; ++j)
                test[i][j] = i;
        double ret = 0.0;
        long start = new Date().getTime();
        for (int i = 0; i < K; ++i)
            for (int j = 0; j < K; ++j)
                for (int k = 0; k < K; ++k)
                    ret += test[i][k] * test[k][j];
        long stop = new Date().getTime();
        System.out.println(stop - start);

        BlockRealMatrix matrix = new BlockRealMatrix(test);
        start = new Date().getTime();
        matrix.multiply(matrix);
        stop = new Date().getTime();
        System.out.println(stop - start);
    }
}