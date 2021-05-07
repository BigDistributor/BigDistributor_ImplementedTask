package com.bigdistributor.tasks.interestpoint.serializable;

import net.preibisch.mvrecon.process.interestpointdetection.methods.dom.DoMParameters;

import java.io.Serializable;

public class SerializableDoMParams extends SerializableInterestPointParams implements Serializable {
    public int localization = 1;
    public int radius1 = 2;
    public int radius2 = 3;
    public float threshold = 0.005F;
    public boolean findMin = false;
    public boolean findMax = true;

    public SerializableDoMParams(){
        super();
    }
    public SerializableDoMParams(DoMParameters params) {
        super(params);
        this.localization = params.localization;
        this.radius1 = params.radius1;
        this.radius2 = params.radius2;
        this.threshold = params.threshold;
        this.findMin = params.findMin;
        this.findMax = params.findMax;
    }
}