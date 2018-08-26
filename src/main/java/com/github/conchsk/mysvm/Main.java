package com.github.conchsk.mysvm;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.github.conchsk.mysvm.classical.LinearKernel;
import com.github.conchsk.mysvm.classical.MySVM;
import com.github.conchsk.mysvm.dataset.LabeledPoint;
import com.github.conchsk.mysvm.dataset.Toy;
import org.apache.commons.math3.linear.BlockRealMatrix;

public class Main {
    public static void main(String[] args) {
        Toy toy = new Toy("linear", 2);
        List<LabeledPoint> trainData = new ArrayList<>();
        List<LabeledPoint> testData = new ArrayList<>();
        toy.getDataset(trainData, testData, 0.1);
        System.out.println(trainData);
        System.out.println(testData);
        MySVM svm = new MySVM();
        svm.fit(trainData, 1.0, 0.001, new LinearKernel());
        System.out.println(svm.alphaVector);
        for (LabeledPoint lp : testData) {
            double predict = svm.predict(lp);
            System.out.println("label is: " + lp.label + "; predict is: " + predict);
        }
    }
}