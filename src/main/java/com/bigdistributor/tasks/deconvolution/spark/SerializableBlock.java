//package com.bigdistributor.tasks;
//
//
//import net.imglib2.FinalInterval;
//import net.imglib2.Interval;
//import net.imglib2.RandomAccessible;
//import net.imglib2.RandomAccessibleInterval;
//import net.imglib2.img.array.ArrayImg;
//import net.imglib2.iterator.LocalizingZeroMinIntervalIterator;
//import net.imglib2.type.numeric.real.FloatType;
//import net.imglib2.util.Util;
//import net.preibisch.legacy.io.IOFunctions;
//import net.preibisch.mvrecon.process.cuda.Block;
//import net.preibisch.mvrecon.process.fusion.ImagePortion;
//
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.Callable;
//
//public class SerializableBlock implements Serializable
//{
//    /**
//     *
//     */
//    private static final long serialVersionUID = 9068252106055534926L;
//
//    final long[] min, max;
//    final int id;
//
//    public SerializableBlock(final int id, final long[] min, final long[] max )
//    {
//        this.min = min;
//        this.max = max;
//        this.id = id;
//    }
//
//    public long[] min() { return min; }
//    public long[] max() { return max; }
//    public int id() { return id; }
//    public FinalInterval createInterval()
//    {
//        return new FinalInterval( min, max );
//    }
//
//    public static ArrayList<SerializableBlock> splitIntoBlocks(final Interval interval, final int[] blockSize )
//    {
//        if ( blockSize.length != interval.numDimensions() )
//            throw new RuntimeException( "Mismatch between interval dimension and blockSize length." );
//
//        final int[] numBlocks = new int[ blockSize.length ];
//
//        final List< List< Long > > mins = new ArrayList<>();
//        final List< List< Long > > maxs = new ArrayList<>();
//
//        for ( int d = 0; d < blockSize.length; ++d )
//        {
//            List< Long > min = new ArrayList<>();
//            List< Long > max = new ArrayList<>();
//
//            long bs = blockSize[ d ];
//            long pos = interval.min( d );
//
//            while ( pos < interval.max( d ) - 1 )
//            {
//                min.add( pos );
//                max.add( pos + Math.min( bs - 1, interval.max( d ) - pos ) );
//
//                pos += bs - 2; // one overlap, starts at the max - 2 since the most outer pixels are not evaluated with DoG
//                ++numBlocks[ d ];
//            }
//
//            mins.add( min );
//            maxs.add( max );
//        }
//
//        final ArrayList<SerializableBlock> blocks = new ArrayList<>();
//        final LocalizingZeroMinIntervalIterator cursor = new LocalizingZeroMinIntervalIterator( numBlocks );
//        int id = 0;
//
//        while ( cursor.hasNext() )
//        {
//            cursor.fwd();
//
//            final long[] min = new long[ blockSize.length ];
//            final long[] max = new long[ blockSize.length ];
//
//            for ( int d = 0; d < blockSize.length; ++d )
//            {
//                min[ d ] = mins.get( d ).get( cursor.getIntPosition( d ) );
//                max[ d ] = maxs.get( d ).get( cursor.getIntPosition( d ) );
//            }
//
//            blocks.add( new SerializableBlock(id++, min, max) );
//        }
//
//        return blocks;
//    }
//
//    public static void main( String[] args )
//    {
//        ArrayList<SerializableBlock> blocks = splitIntoBlocks( new FinalInterval( new long[] { 19, -5 }, new long[] { 1000, 100 } ), new int[] { 100, 100 } );
//
//        for ( final SerializableBlock b : blocks )
//            System.out.println( Util.printInterval( b.createInterval() ) );
//    }
//
//    public String print() {
//        String out = "(Interval empty)";
//        if (min != null && min.length != 0) {
//            out = "[" + min[0];
//
//            int i;
//            for(i = 1; i < min.length; ++i) {
//                out = out + ", " + min[i];
//            }
//
//            out = out + "] -> [" + max[0];
//
//            for(i = 1; i < max.length; ++i) {
//                out = out + ", " + max[i];
//            }
//
//            out = out + "]";
//
//
//            out = out + ")";
//            return out;
//        } else {
//            return out;
//        }
//    }
//
//
//    public void getBlock(final RandomAccessible<FloatType> source, final RandomAccessibleInterval< FloatType > block )
//    {
//        // set up threads
////        final ArrayList<Callable< Boolean >> tasks = new ArrayList< Callable< Boolean > >();
//
//        for ( int i = 0; i < portionsCopy.size(); ++i )
//        {
//            final int threadIdx = i;
//
//            tasks.add( new Callable< Boolean >()
//            {
//                @SuppressWarnings("unchecked")
//                @Override
//                public Boolean call() throws Exception
//                {
//                    if ( source.numDimensions() == 3  )
//                        Block.copy3dArray( threadIdx, portionsCopy.size(), source, (ArrayImg< FloatType, ?>)block, offset );
//                    else
//                    {
//                        final ImagePortion portion = portionsCopy.get( threadIdx );
//                        copy( portion.getStartPosition(), portion.getLoopSize(), source, block, offset);
//                    }
//
//                    return true;
//                }
//            });
//        }
//
//        try
//        {
//            // invokeAll() returns when all tasks are complete
//            taskExecutor.invokeAll( tasks );
//        }
//        catch ( final InterruptedException e )
//        {
//            IOFunctions.println( "Failed to copy block: " + e );
//            e.printStackTrace();
//            return;
//        }
//    }
//}
//
