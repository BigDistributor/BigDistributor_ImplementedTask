package com.bigdistributor.tasks.interestpoint.serializable;

import net.preibisch.mvrecon.process.interestpointdetection.methods.InterestPointParameters;

import java.io.Serializable;

public class SerializableInterestPointParams implements Serializable {
    public double imageSigmaX = 0.5D;
    public double imageSigmaY = 0.5D;
    public double imageSigmaZ = 0.5D;
    public double minIntensity = 0.0D / 0.0;
    public double maxIntensity = 0.0D / 0.0;
    public boolean limitDetections;
    public int maxDetections;
    public int maxDetectionsTypeIndex;
    public int downsampleXY = 1;
    public int downsampleZ = 1;
    public double showProgressMin = 0.0D / 0.0;
    public double showProgressMax = 0.0D / 0.0;

    public SerializableInterestPointParams(InterestPointParameters params) {
        this.imageSigmaX = params.imageSigmaX;
        this.imageSigmaY = params.imageSigmaY;
        this.imageSigmaZ = params.imageSigmaZ;
        this.minIntensity = params.minIntensity;
        this.maxIntensity = params.maxIntensity;
        this.limitDetections = params.limitDetections;
        this.maxDetections = params.maxDetections;
        this.maxDetectionsTypeIndex = params.maxDetectionsTypeIndex;
        this.downsampleXY = params.downsampleXY;
        this.downsampleZ = params.downsampleZ;
        this.showProgressMin = params.showProgressMin;
        this.showProgressMax = params.showProgressMax;
    }

    public SerializableInterestPointParams() {

    }
}
