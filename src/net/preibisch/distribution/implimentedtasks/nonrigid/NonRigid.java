package net.preibisch.distribution.implimentedtasks.nonrigid;

import java.io.File;
import java.io.IOException;

import mpicbg.spim.data.SpimDataException;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;
import net.preibisch.distribution.algorithm.blockmanagement.blockinfo.BasicBlockInfo;
import net.preibisch.distribution.algorithm.clustering.kafka.KafkaManager;
import net.preibisch.distribution.algorithm.clustering.kafka.KafkaProperties;
import net.preibisch.distribution.algorithm.controllers.items.Metadata;
import net.preibisch.distribution.algorithm.task.BlockTask;
import net.preibisch.distribution.algorithm.task.DistributedTask;
import net.preibisch.distribution.io.img.n5.N5File;
import net.preibisch.distribution.tasksparam.FusionClusteringParams;
import net.preibisch.distribution.tasksparam.NonRigidClusteringParams;
import picocli.CommandLine;

public class NonRigid implements BlockTask<FusionClusteringParams> {

	public static void main(String[] args) {
		// new ImageJ();
//		 String str = "-t pre -i /Users/Marwan/Desktop/testtask/dataset.xml -o /Users/Marwan/Desktop/testtask/0_output.n5 -m /Users/Marwan/Desktop/testtask/0_metadata.json -p /Users/Marwan/Desktop/testtask/0_param.json -id 1";
//		 System.out.println(String.join(" ", args));
		CommandLine.call(new DistributedTask(new NonRigid()), args);
	}


	@Override
	public void blockTask(String inputPath, String metadataPath, String paramPath, String outputPath, Integer blockID) {
		try {
			
			KafkaManager.log(blockID, "Start process");
			Metadata md = Metadata.fromJson(metadataPath);
			NonRigidClusteringParams params = new NonRigidClusteringParams().fromJson(new File(paramPath));
			String jobId = md.getJobID();
			KafkaProperties.setJobId(jobId);
			BasicBlockInfo binfo = md.getBlocksInfo().get(blockID);
			KafkaManager.log(blockID, "Bounding box created: " + params.getBoundingBox().toString());
			KafkaManager.log(blockID, "Input loaded. ");
			RandomAccessibleInterval<FloatType> block  = params.process(inputPath,binfo.bb());
			KafkaManager.log(blockID, "Got block. ");
			N5File outputFile = N5File.open(outputPath);
			outputFile.saveBlock(block, binfo.getGridOffset());
			KafkaManager.log(blockID, "Task finished " );
			KafkaManager.done(blockID, "Task finished " );
		} catch (SpimDataException | IOException e) {
			KafkaManager.error(blockID, e.toString());
			e.printStackTrace();
		}
	}
}
