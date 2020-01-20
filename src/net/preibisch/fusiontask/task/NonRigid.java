package net.preibisch.fusiontask.task;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.janelia.saalfeldlab.n5.N5FSWriter;
import org.janelia.saalfeldlab.n5.N5Writer;
import org.janelia.saalfeldlab.n5.RawCompression;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.sequence.ViewId;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;
import net.preibisch.distribution.algorithm.blockmanager.block.BasicBlockInfo;
import net.preibisch.distribution.algorithm.clustering.kafka.KafkaManager;
import net.preibisch.distribution.algorithm.clustering.kafka.KafkaProperties;
import net.preibisch.distribution.algorithm.clustering.scripting.JobType;
import net.preibisch.distribution.algorithm.controllers.items.BlocksMetaData;
import net.preibisch.distribution.algorithm.task.params.FusionParams;
import net.preibisch.distribution.algorithm.task.params.NonRigidParams;
import net.preibisch.distribution.io.img.XMLFile;
import net.preibisch.distribution.io.img.n5.N5File;
import net.preibisch.distribution.tools.helpers.ArrayHelpers;
import net.preibisch.mvrecon.Threads;
import net.preibisch.mvrecon.fiji.spimdata.boundingbox.BoundingBox;
import net.preibisch.mvrecon.process.fusion.transformed.nonrigid.NonRigidTools;
import picocli.CommandLine;
import picocli.CommandLine.Option;

public class NonRigid implements Callable<Void> {
	@Option(names = { "-t", "--task" }, required = true, description = "The path of the Data")
	private String task;

	@Option(names = { "-o", "--output" }, required = true, description = "The path of the Data")
	private String output;

	@Option(names = { "-i", "--input" }, required = true, description = "The path of the Data")
	private String input;

	@Option(names = { "-m", "--meta" }, required = true, description = "The path of the MetaData file")
	private String metadataPath;
	
	@Option(names = { "-p", "--param" }, required = true, description = "The path of the MetaData file")
	private String paramPath;
	
	@Option(names = { "-v","-view" }, required = false, description = "The id of block")
	private int view;
	@Option(names = { "-id" }, required = false, description = "The id of block")
	private Integer id;

	@Override
	public Void call() throws Exception {
		
		try {
			System.out.println("id: "+id);
			id = id - 1;
		} catch (Exception e) {
			KafkaManager.error(-1, e.toString());
			System.out.println("Error id");
			throw new Exception("Specify id!");
		}
//		try {
			System.out.println("task: "+task);
			JobType type = JobType.of(task);
			
			switch (type) {
			case PREPARE:
				System.out.println(type.toString());
				generateN5(input, metadataPath,paramPath, output, id,view);
				return null;
			case PROCESS:
				System.out.println(type.toString());
				blockTask(input, metadataPath, paramPath, output, id,view);
				return null;
			}
		return null;
	}

	public static void blockTask(String inputPath, String metadataPath, String paramPath, String outputPath, int id,int view) {
		try {
			final ExecutorService taskExecutor = Executors.newFixedThreadPool( Threads.numThreads() );
			KafkaManager.log(id, "Start process");
			BlocksMetaData md = BlocksMetaData.fromJson(metadataPath);
			NonRigidParams params = NonRigidParams.fromJson(paramPath);
			String jobId = md.getJobId();
			KafkaProperties.setJobId(jobId);
			BasicBlockInfo binfo = md.getBlocksInfo().get(id);
			KafkaManager.log(id, "Bounding box created: " + params.getBoundingBox().toString());
			KafkaManager.log(id, "Input loaded. ");
			RandomAccessibleInterval<FloatType> block  = NonRigidTools.fuseVirtualInterpolatedNonRigid(
					params.getSpimData().getSequenceDescription().getImgLoader(),
					params.getViewRegistrations(),
					params.getSpimData().getViewInterestPoints().getViewInterestPoints(),
					params.getSpimData().getSequenceDescription().getViewDescriptions(),
					params.getViewsToFuse(),
					params.getViewsToUse(),
					params.getNonRigidParameters().getLabels(),
					params.useBlending(),
					params.useContentBased(),
					params.getNonRigidParameters().showDistanceMap(),
					ArrayHelpers.fill( params.getNonRigidParameters().getControlPointDistance(), 3 ),
					params.getNonRigidParameters().getAlpha(),
					false,
					params.getInterpolation(),
					params.getBoundingBox(),
					params.getDownsampling(),
					params.getIntensityAdjustments(),
					taskExecutor ).getA();
			KafkaManager.log(id, "Got block. ");
			N5File outputFile = N5File.open(outputPath);
			outputFile.saveBlock(block, binfo.getGridOffset());
			KafkaManager.log(id, "Task finished " + id);
			KafkaManager.done(id, "Task finished " + id);
		} catch (SpimDataException | IOException e) {
			KafkaManager.error(id, e.toString());
			e.printStackTrace();
		}
	}

	public static void generateN5(String inputPath, String metadataPath,String paramPath, String outputPath, int id,int view) {
		try {
			KafkaManager.log(id, "Start generate n5");
			BlocksMetaData md = BlocksMetaData.fromJson(metadataPath);
			FusionParams params = FusionParams.fromJson(paramPath);
			BoundingBox bb = new BoundingBox(params.getBb());
			int down = ((int)params.getDownsampling()==0) ? 1 : (int) params.getDownsampling();
			long[] dims = bb.getDimensions(down);
			int blockUnit = md.getBlockUnit();
			N5File outputFile = new N5File(outputPath, dims, blockUnit);
			outputFile.create();
			KafkaManager.log(id, "N5 Generated");
			KafkaManager.done(id, "N5 Generated");
		} catch (JsonSyntaxException | JsonIOException | IOException e) {

			KafkaManager.error(id, e.toString());
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// new ImageJ();
//		 String str = "-t pre -i /Users/Marwan/Desktop/testtask/dataset.xml -o /Users/Marwan/Desktop/testtask/0_output.n5 -m /Users/Marwan/Desktop/testtask/0_metadata.json -p /Users/Marwan/Desktop/testtask/0_param.json -id 1";
//		 System.out.println(String.join(" ", args));
		CommandLine.call(new Fusion(), args);
	}
}

