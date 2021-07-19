package com.bigdistributor.tasks.deconvolution;


import com.bigdistributor.tasks.deconvolution.mv.MultiViewDeconvolution;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.preibisch.mvrecon.process.cuda.Block;
import net.preibisch.mvrecon.process.deconvolution.DeconViews;
import net.preibisch.mvrecon.process.deconvolution.init.PsiInitFactory;
import net.preibisch.mvrecon.process.deconvolution.iteration.ComputeBlockThread;
import net.preibisch.mvrecon.process.deconvolution.iteration.ComputeBlockThreadFactory;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.Vector;

public abstract class DistMultiViewDeconvolution<C extends ComputeBlockThread > extends MultiViewDeconvolution< C >
{

    protected final JavaSparkContext context;

    public DistMultiViewDeconvolution(DeconViews views, int numIterations, PsiInitFactory psiInitFactory, ComputeBlockThreadFactory<C> computeBlockFactory, ImgFactory<FloatType> psiFactory,final JavaSparkContext context) {
        super(views, numIterations, psiInitFactory, computeBlockFactory, psiFactory);
        this.context = context;
    }


//    Change to N5 Writer
//    protected static final void writeN5Block(final N5Writer writer, long[] position)
    protected static void writeBack( final Img< FloatType > psi, final Vector< Pair< Pair< Integer, Block >, Img< FloatType > > > blockWritebackQueue )
    {
        for ( final Pair< Pair< Integer, Block >, Img< FloatType > > writeBackBlock : blockWritebackQueue )
        {
            long time = System.currentTimeMillis();
            writeBackBlock.getA().getB().pasteBlock( psi, writeBackBlock.getB() );
            System.out.println( " block " + writeBackBlock.getA().getA() + ", (CPU): paste " + (System.currentTimeMillis() - time) );
        }
    }
}
