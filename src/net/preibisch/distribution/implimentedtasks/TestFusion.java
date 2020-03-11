package net.preibisch.distribution.implimentedtasks;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import bdv.util.BdvStackSource;
import ij.ImageJ;
import mpicbg.spim.data.SpimDataException;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;
import net.preibisch.distribution.algorithm.blockmanagement.blockinfo.BasicBlockInfo;
import net.preibisch.distribution.algorithm.controllers.items.Metadata;
import net.preibisch.distribution.algorithm.errorhandler.logmanager.MyLogger;
import net.preibisch.distribution.io.img.n5.N5File;
import net.preibisch.distribution.io.img.xml.XMLFile;
import net.preibisch.distribution.tasksparam.FusionClusteringParams;

public class TestFusion {

	private static XMLFile file;
	private static Metadata md;
	private static N5File outputFile;
	private static FusionClusteringParams params;
	private static String input;
	private static BdvStackSource<FloatType> impShow;
	
	static String inputPath = "/Users/Marwan/Desktop/Task/example_dataset/wing/dataset.xml";
	static String outputPath = "/Users/Marwan/Desktop/Task/example_dataset/DistTask/output.n5";
	static String metadataPath = "/Users/Marwan/Desktop/Task/example_dataset/DistTask/metadata.json";
	String paramPath = "/Users/Marwan/Desktop/Task/example_dataset/DistTask/param.json";
	

//	bdv.getViewer().requestRepaint();
	public static void main(String[] args) throws JsonSyntaxException, JsonIOException, IOException, InterruptedException, SpimDataException {
		new ImageJ();

		MyLogger.initLogger();
//		DrosophilaWing.tif
		XMLFile<FloatType> input = XMLFile.XMLFile(inputPath);
		input.show("Input");
		

//				md = Metadata.fromJson(metadataPath);
//		long[] dims = md.getBb().getDimensions(1);
//		outputFile = new N5File(outputPath, dims);
//		impShow = outputFile.show("Result");
		
//		TestFusion.init(inputPath, metadataPath, outputPath, paramPath);
		
//		 file.show("Input");
//		impShow = ImageJFunctions.show(file.getImg(),"Input");
//		ExecutorService service = Threads.createExService(5);
//		int total = md.size();
//		List<MyTask> callableTasks = new ArrayList<>();
//		for (int i = 0; i < total; i++) {
//			MyTask task = new MyTask(i);
//			callableTasks.add(task);
//		}
//		service.invokeAll(callableTasks);

	}
	
	private static void startBDV() throws SpimDataException, IOException {
	

	}

	public static void blockTask(Integer blockID) throws SpimDataException, IOException {
		MyLogger.log().info("Start Block :" + blockID);
		BasicBlockInfo binfo = md.getBlocksInfo().get(blockID);
		System.out.println(binfo.toString());
		System.out.println(binfo.bb().toString());
		RandomAccessibleInterval<FloatType> block = params.fuse(file.getAbsolutePath(), binfo.bb());
//		ImageJFunctions.show(block,String.valueOf(blockID));
		outputFile.saveBlock(block, binfo.getGridOffset());
		MyLogger.log().info("Finish Block :" + blockID);
//		if (impShow!=  null )
//				impShow.close();
//		ImagePlus im = ImageJFunctions.wrap(outputFile.getImg(), "Result");
//		impShow.setCurrent();
//		if (blockID == 30)
//		impShow = outputFile.show("Result");
//		impShow.setImage(im);
//		impShow.show();
	}

	private static void init(String inputPath, String metadataPath, String outputPath, String paramPath)
			throws JsonSyntaxException, JsonIOException, IOException {
		input = inputPath;
		md = Metadata.fromJson(metadataPath);
		long[] dims = md.getBb().getDimensions(1);
		outputFile = new N5File(outputPath, dims);
		outputFile.create();
		
//		impShow = outputFile.show("Result");
//		impShow.setActive(true);
		params = new FusionClusteringParams().fromJson(new File(paramPath));
	}

}

class MyTask implements Callable<Void> {

	private Integer blockId;

	public MyTask(Integer blockId) {
		this.blockId = blockId;
	}

	@Override
	public Void call() throws Exception {
		try {
			TestFusion.blockTask(blockId);
		} catch (SpimDataException | IOException e) {
			MyLogger.log().error(e);
		}
		return null;
	}
}
