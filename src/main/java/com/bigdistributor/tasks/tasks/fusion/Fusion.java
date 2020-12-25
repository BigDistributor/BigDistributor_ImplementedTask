package com.bigdistributor.tasks.tasks.fusion;

import mpicbg.spim.data.SpimDataException;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;
import net.preibisch.bigdistributor.tasks.SpimHelpers;
import net.preibisch.bigdistributor.algorithm.blockmanagement.blockinfo.BasicBlockInfo;
import net.preibisch.bigdistributor.algorithm.clustering.kafka.KafkaManager;
import net.preibisch.bigdistributor.algorithm.clustering.kafka.KafkaProperties;
import net.preibisch.bigdistributor.algorithm.controllers.items.Metadata;
import net.preibisch.bigdistributor.algorithm.task.BlockTask;
import net.preibisch.bigdistributor.algorithm.task.DistributedTask;
import net.preibisch.bigdistributor.io.img.n5.N5File;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;


public class Fusion implements BlockTask<FusionClusteringParams> {

    public static void main(String[] args) {
        // new ImageJ();
//		 String str = "-t pre -i /Users/Marwan/Desktop/testtask/dataset.xml -o /Users/Marwan/Desktop/testtask/0_output.n5 -m /Users/Marwan/Desktop/testtask/0_metadata.json -p /Users/Marwan/Desktop/testtask/0_param.json -id 1";
//		 System.out.println(String.join(" ", args));
        CommandLine.call(new DistributedTask(new Fusion()), args);
    }

    @Override
    public void blockTask(String inputPath, String metadataPath, String paramPath, String outputPath, Integer blockID) {
        try {
            Metadata md = Metadata.fromJson(metadataPath);
            String jobId = md.getJobID();
            KafkaManager manager = new KafkaManager(jobId);
            manager.log(blockID, "Start process");
            FusionClusteringParams params = new FusionClusteringParams().fromJson(new File(paramPath));
            KafkaProperties.setJobId(jobId);
            BasicBlockInfo binfo = md.getBlocksInfo().get(blockID);
            manager.log(blockID, "Bounding box created: " + params.getBoundingBox().toString());
            RandomAccessibleInterval<FloatType> block = params.fuse(inputPath, SpimHelpers.getBb(binfo));

            N5File outputFile = N5File.open(outputPath);
            outputFile.saveBlock(block, binfo.getGridOffset());
            manager.log(blockID, "Task finished ");
            manager.done(blockID, "Task finished ");
        } catch (SpimDataException | IOException e) {
            KafkaManager manager = new KafkaManager("-1");
            manager.error(blockID, e.toString());
            e.printStackTrace();
        }
    }

}
