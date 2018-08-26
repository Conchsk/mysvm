package com.github.conchsk.mysvm.dataset;

import java.io.File;
import java.util.List;

public class Toy extends DatasetBase {
    public Toy(String type, double noise) {
        super();
        try {
            String basePath = getClass().getClassLoader().getResource("").getPath();
            if (System.getProperty("os.name").toLowerCase().contains("windows"))
                basePath = basePath.substring(1);
            String tempName = System.currentTimeMillis() + ".csv";
            String cmd = "python " + basePath + "toy.py" + " " + type + " " + noise + " " + basePath + tempName;
            Process process = Runtime.getRuntime().exec(cmd);
            process.waitFor();
            readCSV(tempName);
            File file = new File(basePath + tempName);
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getDataset(List<LabeledPoint> train, List<LabeledPoint> test, double scale) {
        for (int i = 0; i < data.size(); ++i)
            if (Math.random() > scale)
                train.add(data.get(i));
            else
                test.add(data.get(i));
    }
}