package com.bigdistributor.tasks.deconvolution.spark;

/*-
 * #%L
 * Software for the reconstruction of multi-view microscopic acquisitions
 * like Selective Plane Illumination Microscopy (SPIM) Data.
 * %%
 * Copyright (C) 2012 - 2021 Multiview Reconstruction developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */


import com.bigdistributor.tasks.deconvolution.DistMultiViewDeconvolution;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.preibisch.legacy.io.IOFunctions;
import net.preibisch.mvrecon.process.cuda.Block;
import net.preibisch.mvrecon.process.deconvolution.DeconView;
import net.preibisch.mvrecon.process.deconvolution.DeconViews;
import net.preibisch.mvrecon.process.deconvolution.init.PsiInitFactory;
import net.preibisch.mvrecon.process.deconvolution.iteration.ComputeBlockThread.IterationStatistics;
import net.preibisch.mvrecon.process.deconvolution.iteration.ComputeBlockThreadFactory;
import net.preibisch.mvrecon.process.deconvolution.iteration.mul.ComputeBlockMulThread;
import net.preibisch.mvrecon.process.fusion.FusionTools;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

public class DistMultiViewDeconvolutionSeq extends DistMultiViewDeconvolution<ComputeBlockMulThread> {


    public DistMultiViewDeconvolutionSeq(DeconViews views, int numIterations, PsiInitFactory psiInitFactory, ComputeBlockThreadFactory<ComputeBlockMulThread> computeBlockFactory, ImgFactory<FloatType> psiFactory, JavaSparkContext context) {
        super(views, numIterations, psiInitFactory, computeBlockFactory, psiFactory, context);
    }

    public boolean initWasSuccessful() {
        return max != null && testBlockIntegrity();
    }

    public boolean testBlockIntegrity() {
        final int totalNumBlocks = views.getViews().get(0).getNumBlocks();
        final List<List<Block>> blocks = views.getViews().get(0).getNonInterferingBlocks();

        for (final DeconView view : views.getViews()) {
            if (view.getNumBlocks() != totalNumBlocks) {
                IOFunctions.println("only a constant number of blocks is supported.");
                return false;
            }

            if (view.getNonInterferingBlocks().size() != blocks.size()) {
                IOFunctions.println("only a constant number of block batches is supported.");
                return false;
            }

            for (int i = 0; i < blocks.size(); ++i) {
                if (blocks.get(i).size() != view.getNonInterferingBlocks().get(i).size()) {
                    IOFunctions.println("only a constant number of blocks within batches is supported.");
                    return false;
                }

                for (int j = 0; j < blocks.get(i).size(); ++j) {
                    final Block blockA = blocks.get(i).get(j);
                    final Block blockB = view.getNonInterferingBlocks().get(i).get(j);

                    for (int d = 0; d < blockA.numDimensions(); ++d) {
                        if (
                                blockA.getBlockSize()[d] != blockB.getBlockSize()[d] ||
                                        blockA.getEffectiveSize()[d] != blockB.getEffectiveSize()[d] ||
                                        blockA.min(d) != blockB.min(d) ||
                                        blockA.max(d) != blockB.max(d)) {
                            IOFunctions.println("Block dimensions/offset/effective sizes not compatible, stopping.");
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    public void runNextIteration() {
        if (this.max == null)
            return;

        ++it;

        IOFunctions.println("iteration: " + it + " (" + new Date(System.currentTimeMillis()) + ")");

        final int totalNumBlocks = views.getViews().get(0).getNumBlocks();
        final List<List<Block>> blocks = views.getViews().get(0).getNonInterferingBlocks();

//TODO convert to serializableBlocks
//        final List<List<SerializableBlock>> serializableBlocks = SerializableUtils.convert(blocks);

        final Vector<IterationStatistics> stats = new Vector<>();

        int currentTotalBlock = 0;

        // keep the last blocks to be written back to the global psi image once it is not overlapping anymore
        final Vector<Pair<Pair<Integer, Block>, Img<FloatType>>> previousBlockWritebackQueue = new Vector<>();
        final Vector<Pair<Pair<Integer, Block>, Img<FloatType>>> currentBlockWritebackQueue = new Vector<>();

        int batch = 0;
        JavaRDD<List<Block>> rdd = context.parallelize(blocks);

//        rdd.map
        for (final List<Block> blocksBatch : blocks) {
            final int numBlocksBefore = currentTotalBlock;
            final int numBlocksBatch = blocksBatch.size();
            currentTotalBlock += numBlocksBatch;

            System.out.println("Processing " + numBlocksBatch + " blocks from batch " + (++batch) + "/" + blocks.size());

            final AtomicInteger ai = new AtomicInteger();
            final Thread[] threads = new Thread[computeBlockThreads.size()];

            for (int t = 0; t < computeBlockThreads.size(); ++t) {
                final int threadId = t;

                threads[threadId] = new Thread(new MultiViewDeconvolutionTask(computeBlockThreads, threadId, ai, numBlocksBatch, numBlocksBefore, blocksBatch,views,psi,threads,max,stats,totalNumBlocks,currentBlockWritebackQueue));
            }

            // run the threads that process all blocks of this batch in parallel (often, this will be just one thread)
            FusionTools.runThreads(threads);

            // write back previous list of blocks
            writeBack(psi, previousBlockWritebackQueue);

            previousBlockWritebackQueue.clear();
            previousBlockWritebackQueue.addAll(currentBlockWritebackQueue);
            currentBlockWritebackQueue.clear();

        } // finish one block batch

        // write back last list of blocks
        writeBack(psi, previousBlockWritebackQueue);

        // accumulate the results from the individual blocks
        final IterationStatistics is = new IterationStatistics();

        for (int i = 0; i < stats.size(); ++i) {
            is.sumChange += stats.get(i).sumChange;
            is.maxChange = Math.max(is.maxChange, stats.get(i).maxChange);
        }

        IOFunctions.println("iteration: " + it + " --- sum change: " + is.sumChange + " --- max change per pixel: " + is.maxChange);

    }
}
