package com.bigdistributor.tasks.tasks.nonrigid;

import com.bigdistributor.tasks.serializers.AffineTransform3DJsonSerializer;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import mpicbg.models.AffineModel1D;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.sequence.ViewId;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.real.FloatType;
import com.bigdistributor.tasks.serializers.ViewIdJsonSerializer;
import net.preibisch.bigdistributor.tasks.SpimHelpers;
import net.preibisch.bigdistributor.io.serializers.params.SerializableParams;
import net.preibisch.bigdistributor.tools.helpers.ArrayHelpers;
import net.preibisch.mvrecon.Threads;
import net.preibisch.mvrecon.fiji.spimdata.SpimData2;
import net.preibisch.mvrecon.fiji.spimdata.boundingbox.BoundingBox;
import net.preibisch.mvrecon.process.fusion.transformed.nonrigid.NonRigidParameters;
import net.preibisch.mvrecon.process.fusion.transformed.nonrigid.NonRigidTools;

import java.io.*;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NonRigidClusteringParams extends SerializableParams {
    private BoundingBox boundingBox;
    private Map<ViewId, AffineTransform3D> viewRegistrations;
    private Collection<? extends ViewId> viewsToFuse;
    private Collection<? extends ViewId> viewsToUse;
    private boolean useBlending;
    private boolean useContentBased;
    private NonRigidParameters nonRigidParameters;
    private boolean virtualGrid;
    private int interpolation;
    private double downsampling;
    private Map<? extends ViewId, AffineModel1D> intensityAdjustments;


    public NonRigidClusteringParams() {
        super(NonRigidClusteringParams.class);
    }

    public NonRigidClusteringParams(Map<ViewId, AffineTransform3D> viewRegistrations,
                                    Collection<? extends ViewId> viewsToFuse, Collection<? extends ViewId> viewsToUse, boolean useBlending,
                                    boolean useContentBased, NonRigidParameters nonRigidParameters, boolean virtualGrid, int interpolation,
                                    Interval boundingBox, double downsampling, Map<? extends ViewId, AffineModel1D> intensityAdjustments) {
        super(NonRigidClusteringParams.class);
        this.viewRegistrations = viewRegistrations;
        this.viewsToFuse = viewsToFuse;
        this.viewsToUse = viewsToUse;
        this.useBlending = useBlending;
        this.useContentBased = useContentBased;
        this.nonRigidParameters = nonRigidParameters;
        this.virtualGrid = virtualGrid;
        this.interpolation = interpolation;
        this.boundingBox = new BoundingBox(boundingBox);
        this.downsampling = downsampling;
        this.intensityAdjustments = intensityAdjustments;
    }

    @Override
    protected void init() {
        super.init();
        serializers.put(ViewId.class, new ViewIdJsonSerializer());
        serializers.put(AffineTransform3D.class, new AffineTransform3DJsonSerializer());
    }

    public Map<ViewId, AffineTransform3D> getViewRegistrations() {
        return viewRegistrations;
    }

    public Collection<? extends ViewId> getViewsToFuse() {
        return viewsToFuse;
    }

    public Collection<? extends ViewId> getViewsToUse() {
        return viewsToUse;
    }

    public boolean useBlending() {
        return useBlending;
    }

    public boolean useContentBased() {
        return useContentBased;
    }

    public boolean virtualGrid() {
        return virtualGrid;
    }

    public int getInterpolation() {
        return interpolation;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public double getDownsampling() {
        return downsampling;
    }

    public Map<? extends ViewId, AffineModel1D> getIntensityAdjustments() {
        return intensityAdjustments;
    }

    public NonRigidParameters getNonRigidParameters() {
        return nonRigidParameters;
    }

    public RandomAccessibleInterval<FloatType> process(String input, BoundingBox bb) throws SpimDataException {
        final ExecutorService taskExecutor = Executors.newFixedThreadPool(Threads.numThreads());
        SpimData2 spimdata = SpimHelpers.getSpimData(input);
        return NonRigidTools.fuseVirtualInterpolatedNonRigid(
                spimdata.getSequenceDescription().getImgLoader(),
                getViewRegistrations(),
                spimdata.getViewInterestPoints().getViewInterestPoints(),
                spimdata.getSequenceDescription().getViewDescriptions(),
                getViewsToFuse(),
                getViewsToUse(),
                getNonRigidParameters().getLabels(),
                useBlending(),
                useContentBased(),
                getNonRigidParameters().showDistanceMap(),
                ArrayHelpers.fill(getNonRigidParameters().getControlPointDistance(), 3),
                getNonRigidParameters().getAlpha(),
                false,
                getInterpolation(),
//				params.getBoundingBox(),
                bb,
                getDownsampling(),
                getIntensityAdjustments(),
                taskExecutor).getA();
    }

    public NonRigidClusteringParams fromJson(File file) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
        return this.getGson().fromJson(new FileReader(file), NonRigidClusteringParams.class);
    }

    @Override
    public void toJson(File file) {
        try (PrintWriter out = new PrintWriter(file)) {
            String json = this.getGson().toJson(this);
            out.print(json);
            out.flush();
            out.close();
            System.out.println("File saved: " + file.getAbsolutePath() + " | Size: " + file.length());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
