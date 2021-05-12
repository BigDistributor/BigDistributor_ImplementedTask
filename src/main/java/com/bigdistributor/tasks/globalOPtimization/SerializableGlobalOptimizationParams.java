package com.bigdistributor.tasks.globalOPtimization;

import ij.gui.GenericDialog;
import net.preibisch.stitcher.algorithm.globalopt.GlobalOptimizationParameters;

import java.io.Serializable;

public class SerializableGlobalOptimizationParams implements Serializable {
    public static GlobalOptimizationParameters.GlobalOptType defaultGlobalOpt = GlobalOptimizationParameters.GlobalOptType.SIMPLE;
    public static boolean defaultExpertGrouping = false;
    public static double defaultRelativeError = 2.5D;
    public static double defaultAbsoluteError = 3.5D;
      public GlobalOptimizationParameters.GlobalOptType method;
    public double relativeThreshold;
    public double absoluteThreshold;
    public boolean showExpertGrouping;

    public SerializableGlobalOptimizationParams(double relativeThreshold, double absoluteThreshold, GlobalOptimizationParameters.GlobalOptType method, boolean showExpertGrouping) {
        this.relativeThreshold = relativeThreshold;
        this.absoluteThreshold = absoluteThreshold;
        this.method = method;
        this.showExpertGrouping = showExpertGrouping;
    }

    public  SerializableGlobalOptimizationParams() {
        this(defaultRelativeError, defaultAbsoluteError, defaultGlobalOpt, defaultExpertGrouping);

    }

    public GlobalOptimizationParameters toParams() {
        return new GlobalOptimizationParameters(this.relativeThreshold,this.absoluteThreshold,this.method,this.showExpertGrouping);
    }
}
