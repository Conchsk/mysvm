package com.github.conchsk.mysvm;

import java.util.List;

import com.github.conchsk.mysvm.classical.LinearKernel;
import com.github.conchsk.mysvm.classical.MySVM;
import com.github.conchsk.mysvm.dataset.Iris;
import com.github.conchsk.mysvm.dataset.LabeledPoint;

public class Test {
    public static void main(String[] args) {
        List<LabeledPoint> trainData = Iris.getDataset(true);
        List<LabeledPoint> testData = Iris.getDataset(false);
        MySVM svm = new MySVM();
        svm.fit(trainData, 200, 0.001, 0.001, new LinearKernel());
        System.out.println(svm.alpha);
        for (LabeledPoint lp : testData) {
            double predict = svm.predict(lp);
            System.out.println( "label is: " + lp.label + "; predict is: " + predict);
        }
    }
}