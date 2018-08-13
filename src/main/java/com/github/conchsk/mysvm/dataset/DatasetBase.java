package com.github.conchsk.mysvm.dataset;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DatasetBase {
    protected List<LabeledPoint> data;

    public DatasetBase() {
        data = new ArrayList<>();
    }

    protected void readCSV(String filename) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(getClass().getClassLoader().getResource(filename).getPath())));
            String buffer = null;
            while ((buffer = br.readLine()) != null) {
                if (buffer.isEmpty())
                    break;
                String[] featuresStr = buffer.substring(0, buffer.lastIndexOf(",")).split(",");
                String labelStr = buffer.substring(buffer.lastIndexOf(",") + 1);
                double[] features = new double[featuresStr.length];
                for (int i = 0; i < features.length; ++i)
                    features[i] = Double.valueOf(featuresStr[i]);
                data.add(new LabeledPoint(features, Double.valueOf(labelStr)));
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
