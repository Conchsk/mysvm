package com.github.conchsk.mysvm.dataset;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Iris {
    public static List<LabeledPoint> getDataset(boolean trainOrTest) {
        try {
            List<LabeledPoint> ret = new ArrayList<LabeledPoint>();
            BufferedReader br = null;
            if (trainOrTest)
                br = new BufferedReader(
                        new InputStreamReader(new FileInputStream(Iris.class.getResource("svmtest.train").getPath())));
            else
                br = new BufferedReader(
                        new InputStreamReader(new FileInputStream(Iris.class.getResource("svmtest.test").getPath())));
            String buffer = null;
            while ((buffer = br.readLine()) != null) {
                String[] tmp = buffer.split(",");
                double label = 0.0;
                if (tmp[4].equals("Iris-setosa"))
                    label = 0.0;
                else if (tmp[4].equals("Iris-versicolor"))
                    label = 1.0;
                else
                    label = 2.0;
                ret.add(new LabeledPoint(new double[] { Double.valueOf(tmp[0]), Double.valueOf(tmp[1]),
                        Double.valueOf(tmp[2]), Double.valueOf(tmp[3]) }, label));
            }
            br.close();
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}