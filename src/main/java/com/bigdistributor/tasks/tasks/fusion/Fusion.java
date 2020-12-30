package com.bigdistributor.tasks.tasks.fusion;

import com.bigdistributor.core.app.ApplicationMode;
import com.bigdistributor.core.app.BigDistributorApp;
import com.bigdistributor.core.app.BigDistributorMainApp;
import com.bigdistributor.core.task.BlockTask;
import com.bigdistributor.core.task.SparkDistributedTask;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;
import net.preibisch.mvrecon.fiji.spimdata.SpimData2;
import net.preibisch.mvrecon.fiji.spimdata.boundingbox.BoundingBox;
import net.preibisch.mvrecon.process.fusion.FusionTools;
import picocli.CommandLine;

@BigDistributorApp(mode = ApplicationMode.ExecutionNode)
public class Fusion<T extends FloatType, D extends SpimData2, K extends FusionClusteringParams> extends BigDistributorMainApp implements BlockTask<T, D, K> {

    public static void main(String[] args) {

        CommandLine.call(new SparkDistributedTask<FloatType, FusionClusteringParams>(new Fusion()), args);

    }

    @Override
    public RandomAccessibleInterval<FloatType> blockTask(SpimData2 spimdata, FusionClusteringParams params, Interval interval) {
        BoundingBox bb = new BoundingBox(interval);
        RandomAccessibleInterval<FloatType> block = fuse(spimdata, params, bb);
        return block;
    }

    public RandomAccessibleInterval<FloatType> fuse(SpimData2 spimdata, FusionClusteringParams params, BoundingBox bb) {
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
    }

}

//    @Override
//    public void blockTask(String inputPath, String metadataPath, String paramPath, String outputPath, Integer blockID) {
//        try {
//            Metadata md = Metadata.fromJson(metadataPath);
//            String jobId = md.getJobID();
//            KafkaManager manager = new KafkaManager(jobId);
//            manager.log(blockID, "Start process");
//            FusionClusteringParams params = new FusionClusteringParams().fromJson(new File(paramPath));
//            KafkaProperties.setJobId(jobId);
//            BasicBlockInfo binfo = md.getBlocksInfo().get(blockID);
//            manager.log(blockID, "Bounding box created: " + params.getBoundingBox().toString());
//            RandomAccessibleInterval<FloatType> block = params.fuse(inputPath, SpimHelpers.getBb(binfo));
//
//            N5File outputFile = N5File.open(outputPath);
//            outputFile.saveBlock(block, binfo.getGridOffset());
//            manager.log(blockID, "Task finished ");
//            manager.done(blockID, "Task finished ");
//        } catch (SpimDataException | IOException e) {
//            KafkaManager manager = new KafkaManager("-1");
//            manager.error(blockID, e.toString());
//            e.printStackTrace();
//        }
//    }
