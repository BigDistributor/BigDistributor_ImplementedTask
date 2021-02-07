package com.bigdistributor.tasks.test;

import com.bigdistributor.biglogger.adapters.Log;
import com.bigdistributor.core.app.ApplicationMode;
import com.bigdistributor.core.app.BigDistributorApp;
import com.bigdistributor.core.app.BigDistributorMainApp;
import com.bigdistributor.core.blockmanagement.blockinfo.BasicBlockInfo;
import com.bigdistributor.core.config.ConfigManager;
import com.bigdistributor.core.config.PropertiesKeys;
import com.bigdistributor.core.task.JobParams;
import com.bigdistributor.core.task.items.Metadata;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

// T: DataType exmpl FloatType
// K: Task Param

@BigDistributorApp(mode = ApplicationMode.ExecutionNode)
public class EmptyTask extends BigDistributorMainApp implements Callable<Integer> {
    @Option(names = {"-id", "--jobid"}, required = true, description = "The path of the Data")
    String jobId;

    @Option(names = {"-m", "--meta"}, required = true, description = "The path of the MetaData file")
    String metadataPath;

    private static final Log logger = Log.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    private Metadata md;

    @Override
    public Integer call() throws Exception {
        md = Metadata.fromJson(metadataPath);
        if (md == null) {
            logger.error("Error metadata file !");
            return null;
        }
        logger.info(jobId + " started!");


        SparkConf sparkConf = new SparkConf().setAppName(jobId).set("spark.master", "local");

        final JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        String server = String.valueOf(ConfigManager.getConfig().get(PropertiesKeys.MQServer));
        String queue = String.valueOf(ConfigManager.getConfig().get(PropertiesKeys.MQQueue));
        List<Pair<BasicBlockInfo,JobParams>> elms = new ArrayList<>();

        for(int i=0; i< 5; i++){

            elms.add(new ValuePair<>(md.getBlocksInfo().get(i),new JobParams(server,queue,jobId,i)));
        }

        sparkContext.parallelize(elms, elms.size()).foreach(new EmptySparkJob<>());
        return 0;
    }

    public static void main(String[] args) {
        args = "-id test22 -m /Users/Marwan/Desktop/metadata.json".split(" ");
        int exitCode = new CommandLine(new EmptyTask()).execute(args);
        System.exit(exitCode);
    }

}
