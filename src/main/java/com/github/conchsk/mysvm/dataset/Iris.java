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
                br = new BufferedReader(new InputStreamReader(new FileInputStream(
                        Iris.class.getClassLoader().getResource("train.csv").getPath())));
            else
                br = new BufferedReader(new InputStreamReader(new FileInputStream(
                        Iris.class.getClassLoader().getResource("test.csv").getPath())));
            String buffer = null;
            while ((buffer = br.readLine()) != null) {
                String[] tmp = buffer.split(",");
                ret.add(new LabeledPoint(new double[]{Double.valueOf(tmp[0]), Double.valueOf(tmp[1])}, Double.valueOf(tmp[4])));
            }
            br.close();
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}