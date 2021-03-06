package com.bigdistributor.tasks.fusion;

import com.bigdistributor.core.task.items.SerializableParams;
import com.bigdistributor.io.mvrecon.SpimHelpers;
import com.bigdistributor.io.serializers.AffineTransform3DJsonSerializer;
import com.bigdistributor.io.serializers.IntervalSerializer;
import com.bigdistributor.io.serializers.ViewIdJsonSerializer;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.Primitives;
import mpicbg.models.AffineModel1D;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.sequence.ViewDescription;
import mpicbg.spim.data.sequence.ViewId;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.real.FloatType;
import net.preibisch.mvrecon.fiji.spimdata.SpimData2;
import net.preibisch.mvrecon.fiji.spimdata.boundingbox.BoundingBox;
import net.preibisch.mvrecon.process.fusion.FusionTools;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Set;

public class FusionClusteringParams extends SerializableParams<FusionClusteringParams> {
    private BoundingBox boundingBox;
    private double downsampling;
    private Map<ViewId, AffineTransform3D> registrations;
    private Set<ViewDescription> views;
    private boolean useBlending;
    private boolean useContentBased;
    private int interpolation;
    private Map<ViewId, AffineModel1D> intensityAdjustment;


    @Override
    protected void init() {
        super.init();
        serializers.put(ViewId.class, new ViewIdJsonSerializer());
        serializers.put(AffineTransform3D.class, new AffineTransform3DJsonSerializer());
        serializers.put(Interval.class,new IntervalSerializer());
    }

    public FusionClusteringParams(BoundingBox boundingBox, double downsampling,
                                  Map<ViewId, AffineTransform3D> registrations, Set<ViewDescription> views, boolean useBlending,
                                  boolean useContentBased, int interpolation, Map<ViewId, AffineModel1D> intensityAdjustment) {
        super();
        this.downsampling = downsampling;
        this.registrations = registrations;
        this.views = views;
        this.useBlending = useBlending;
        this.useContentBased = useContentBased;
        this.interpolation = interpolation;
        this.boundingBox = new BoundingBox(boundingBox);
        this.intensityAdjustment = intensityAdjustment;
    }

    public FusionClusteringParams() {
        super();
    }

    public Map<ViewId, AffineTransform3D> getRegistrations() {
        return registrations;
    }

    public Set<ViewDescription> getViews() {
        return views;
    }

    public Map<ViewId, AffineModel1D> getIntensityAdjustment() {
        return intensityAdjustment;
    }

    public boolean useBlending() {
        return useBlending;
    }

    public boolean useContentBased() {
        return useContentBased;
    }

    public int getInterpolation() {
        return interpolation;
    }

    public double getDownsampling() {
        return downsampling;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public FusionClusteringParams fromJson(File file) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
        return Primitives.wrap(FusionClusteringParams.class).cast(fromJson(file));
    }


    public RandomAccessibleInterval<FloatType> fuse(String path, BoundingBox bb) throws SpimDataException {
        SpimData2 spimdata = SpimHelpers.getSpimData(path);
        return FusionTools.fuseVirtual(
                spimdata.getSequenceDescription().getImgLoader(),
                getRegistrations(),
                spimdata.getSequenceDescription().getViewDescriptions(),
                getViews(),
                useBlending(),
                useContentBased(),
                getInterpolation(),
//			getBoundingBox(),
                bb,
                getDownsampling(),
                getIntensityAdjustment()).getA();
    }
}