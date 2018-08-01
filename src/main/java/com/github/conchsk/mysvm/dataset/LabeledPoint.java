package com.github.conchsk.mysvm.dataset;

import com.fasterxml.jackson.databind.ObjectMapper;

public class LabeledPoint {
    public double[] features;
    public double label;

    public LabeledPoint() {
        this(null, 0.0);
    }

    public LabeledPoint(double[] features, double label) {
        this.features = features;
        this.label = label;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}