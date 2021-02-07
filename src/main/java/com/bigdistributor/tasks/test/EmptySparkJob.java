package com.bigdistributor.tasks.test;

import com.bigdistributor.biglogger.adapters.Log;
import com.bigdistributor.biglogger.adapters.LoggerManager;
import com.bigdistributor.core.blockmanagement.blockinfo.BasicBlockInfo;
import com.bigdistributor.core.task.JobParams;
import net.imglib2.util.Pair;
import org.apache.spark.api.java.function.VoidFunction;

import java.lang.invoke.MethodHandles;

public class EmptySparkJob<T extends Pair<BasicBlockInfo, JobParams>> implements VoidFunction<T> {
    private static final Log logger = Log.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());
    @Override
    public void call(T infos) {

        BasicBlockInfo binfo = infos.getA();
        JobParams params = infos.getB();
        int blockID = binfo.getBlockId();
        LoggerManager.addRemoteLogger(params);
        logger.info("test");
        logger.blockStarted(blockID, " start processing..");
        logger.blockDone(blockID, " Task done.");
    }
}
