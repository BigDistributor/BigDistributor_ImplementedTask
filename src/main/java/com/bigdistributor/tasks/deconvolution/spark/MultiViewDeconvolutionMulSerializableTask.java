package com.bigdistributor.tasks.deconvolution.spark;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;
import net.preibisch.mvrecon.process.cuda.Block;
import net.preibisch.mvrecon.process.deconvolution.DeconView;
import net.preibisch.mvrecon.process.deconvolution.DeconViews;
import net.preibisch.mvrecon.process.deconvolution.iteration.ComputeBlockThread;
import net.preibisch.mvrecon.process.deconvolution.iteration.mul.ComputeBlockMulThread;
import net.preibisch.mvrecon.process.fusion.FusionTools;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiViewDeconvolutionMulSerializableTask implements Runnable {

    private final ArrayList<ComputeBlockMulThread> computeBlockThreads;
    private final int threadId;
    private final AtomicInteger ai;
    private final int numBlocksBatch;
    private final int numBlocksBefore;
    private final List<Block> blocksBatch;
    private final DeconViews views;
    private final Img<FloatType> psi;
    private final Thread[] threads;
    private final float[] max;
    private final Vector<ComputeBlockThread.IterationStatistics> stats;
    private final int totalNumBlocks;
    private final Vector<Pair<Pair<Integer, Block>, Img<FloatType>>> currentBlockWritebackQueue;


    public MultiViewDeconvolutionMulSerializableTask(ArrayList<ComputeBlockMulThread> computeBlockThreads, int threadId, AtomicInteger ai, int numBlocksBatch, int numBlocksBefore, List<Block> blocksBatch, DeconViews views, Img<FloatType> psi, Thread[] threads, float[] max, Vector<ComputeBlockThread.IterationStatistics> stats, int totalNumBlocks, Vector<Pair<Pair<Integer, Block>, Img<FloatType>>> currentBlockWritebackQueue) {
        this.computeBlockThreads = computeBlockThreads;
        this.threadId = threadId;
        this.ai = ai;
        this.numBlocksBatch = numBlocksBatch;
        this.numBlocksBefore = numBlocksBefore;
        this.blocksBatch = blocksBatch;
        this.views = views;
        this.psi = psi;
        this.threads = threads;
        this.max = max;
        this.stats = stats;
        this.totalNumBlocks = totalNumBlocks;
        this.currentBlockWritebackQueue = currentBlockWritebackQueue;
    }

    public void run() {
        // one ComputeBlockThread creates a temporary image for I/O, valid throughout the whole cycle
        final ComputeBlockMulThread blockThread = computeBlockThreads.get(threadId);

        int blockId;

        while ((blockId = ai.getAndIncrement()) < numBlocksBatch) {
            final int blockIdOut = blockId + numBlocksBefore;

            final Block blockStruct = blocksBatch.get(blockId);
            System.out.println(" block " + blockIdOut + ", " + blockStruct.toString());

            long time = System.currentTimeMillis();
            blockStruct.copyBlock(Views.extendMirrorSingle(psi), blockThread.getPsiBlockTmp());
            System.out.println(" block " + blockIdOut + ", thread (" + (threadId + 1) + "/" + threads.length + "), (CPU): copy " + (System.currentTimeMillis() - time));

            final List<DeconView> view = new ArrayList<>();
            final List<RandomAccessibleInterval<FloatType>> imgBlock = new ArrayList<>();
            final List<RandomAccessibleInterval<FloatType>> weightBlock = new ArrayList<>();
            final List<Float> maxIntensityView = new ArrayList<>();
            final List<ArrayImg<FloatType, ?>> kernel1 = new ArrayList<>();
            final List<ArrayImg<FloatType, ?>> kernel2 = new ArrayList<>();

            for (int i = 0; i < views.getViews().size(); ++i) {
                view.add(views.getViews().get(i));
                imgBlock.add(Views.zeroMin(Views.interval(Views.extendZero(views.getViews().get(i).getImage()), blockStruct)));
                weightBlock.add(Views.zeroMin(Views.interval(Views.extendZero(views.getViews().get(i).getWeight()), blockStruct)));
                maxIntensityView.add(max[i]);
                kernel1.add(views.getViews().get(i).getPSF().getKernel1());
                kernel2.add(views.getViews().get(i).getPSF().getKernel2());
            }

            time = System.currentTimeMillis();
            stats.add(blockThread.runIteration(
                    view,
                    imgBlock,//imgBlock,
                    weightBlock,//weightBlock,
                    maxIntensityView,
                    kernel1,
                    kernel2));
            System.out.println(" block " + blockIdOut + ", thread (" + (threadId + 1) + "/" + threads.length + "), (CPU): compute " + (System.currentTimeMillis() - time));

            time = System.currentTimeMillis();

//                            Save block
            if (totalNumBlocks == 1) {
                blockStruct.pasteBlock(psi, blockThread.getPsiBlockTmp());
                System.out.println(" block " + blockIdOut + ", thread (" + (threadId + 1) + "/" + threads.length + "), (CPU): paste " + (System.currentTimeMillis() - time));
            } else {
                // copy to the writequeue
                final Img<FloatType> tmp = blockThread.getPsiBlockTmp().factory().create(blockThread.getPsiBlockTmp(), new FloatType());
                FusionTools.copyImg(blockThread.getPsiBlockTmp(), tmp, views.getExecutorService(), false);
                currentBlockWritebackQueue.add(new ValuePair<>(new ValuePair<>(blockIdOut, blockStruct), tmp));

                System.out.println(" block " + blockIdOut + ", thread (" + (threadId + 1) + "/" + threads.length + "), (CPU): saving for later pasting " + (System.currentTimeMillis() - time));
            }
        }
    }

}
