package com.bigdistributor.tasks.fusion;

import com.bigdistributor.core.app.ApplicationMode;
import com.bigdistributor.core.app.BigDistributorApp;
import com.bigdistributor.core.task.BlockTask;
import mpicbg.spim.data.sequence.ViewId;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;
import net.preibisch.mvrecon.fiji.spimdata.SpimData2;
import net.preibisch.mvrecon.fiji.spimdata.boundingbox.BoundingBox;
import net.preibisch.mvrecon.process.fusion.FusionTools;

import java.util.ArrayList;
import java.util.List;

@BigDistributorApp(task = "fusion" , mode = ApplicationMode.ExecutionNode)
public class Fusion<T extends FloatType, D extends SpimData2, K extends FusionClusteringParams> implements BlockTask<T, D, K> {

    @Override
    public RandomAccessibleInterval<FloatType> blockTask(SpimData2 spimdata, FusionClusteringParams params, Interval interval) {
        BoundingBox bb = new BoundingBox(interval);
        RandomAccessibleInterval<FloatType> block = fuse(spimdata, params, bb);
        return block;
    }

    public RandomAccessibleInterval<FloatType> fuse(SpimData2 spimdata, FusionClusteringParams params, BoundingBox bb) {
        if (params != null) {
            return FusionTools.fuseVirtual(
                    spimdata.getSequenceDescription().getImgLoader(),
                    params.getRegistrations(),
                    spimdata.getSequenceDescription().getViewDescriptions(),
                    params.getViews(),
                    params.useBlending(),
                    params.useContentBased(),
                    params.getInterpolation(),
                    bb,
                    params.getDownsampling(),
                    params.getIntensityAdjustment()).getA();

        } else {
            int defaultInterpolation = 1;
            boolean defaultUseBlending = true;
            double downsampling = 0.0D / 0.0;
            List<ViewId> views = new ArrayList<>(spimdata.getSequenceDescription().getViewDescriptions().keySet());
            FusionTools.ImgDataType imgType = FusionTools.ImgDataType.VIRTUAL;
            return FusionTools.fuseVirtual(spimdata, views, defaultUseBlending, false, defaultInterpolation, bb, downsampling, null).getA();
        }
    }
}
