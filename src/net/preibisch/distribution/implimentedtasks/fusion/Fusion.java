package net.preibisch.distribution.implimentedtasks.fusion;

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
import picocli.CommandLine;


public class Fusion implements BlockTask<FusionClusteringParams>  {

	public static void main(String[] args) {
		// new ImageJ();
//		 String str = "-t pre -i /Users/Marwan/Desktop/testtask/dataset.xml -o /Users/Marwan/Desktop/testtask/0_output.n5 -m /Users/Marwan/Desktop/testtask/0_metadata.json -p /Users/Marwan/Desktop/testtask/0_param.json -id 1";
//		 System.out.println(String.join(" ", args));
		CommandLine.call(new DistributedTask(new Fusion()), args);
	}

	@Override
	public void blockTask(String inputPath, String metadataPath, String paramPath, String outputPath, Integer blockID) {
		try {
			KafkaManager.log(blockID, "Start process");
			Metadata md = Metadata.fromJson(metadataPath);
			FusionClusteringParams params = new FusionClusteringParams().fromJson(new File(paramPath));
			String jobId = md.getJobID();
			KafkaProperties.setJobId(jobId);
			BasicBlockInfo binfo = md.getBlocksInfo().get(blockID);
			KafkaManager.log(blockID, "Bounding box created: " + params.getBoundingBox().toString());
			RandomAccessibleInterval<FloatType> block = params.fuse(inputPath,binfo.bb());
		
			
//			List<ViewId> viewIds = params.getViewIds().get(view);
//			KafkaManager.log(blockID, "Got view ids ");

//			XMLFile inputFile = XMLFile.XMLFile(inputPath, params.getBb(), params.getDownsampling(), viewIds);

//			KafkaManager.log(blockID, "Input loaded. ");
			// XMLFile inputFile = XMLFile.XMLFile(inputPath);
//			RandomAccessibleInterval<FloatType> block = inputFile.fuse(params.getBb(),view);
//			KafkaManager.log(blockID, "Got block. ");
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
