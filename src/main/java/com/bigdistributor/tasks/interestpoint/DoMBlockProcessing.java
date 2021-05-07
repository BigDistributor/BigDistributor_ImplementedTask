package com.bigdistributor.tasks.interestpoint;

import com.bigdistributor.tasks.interestpoint.serializable.SerializableDoMParams;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.wrapper.ImgLib2;
import mpicbg.spim.data.sequence.ImgLoader;
import mpicbg.spim.data.sequence.SequenceDescription;
import mpicbg.spim.data.sequence.ViewId;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.real.FloatType;
import net.preibisch.mvrecon.Threads;
import net.preibisch.mvrecon.fiji.spimdata.SpimData2;
import net.preibisch.mvrecon.fiji.spimdata.interestpoints.InterestPoint;
import net.preibisch.mvrecon.process.downsampling.DownsampleTools;
import net.preibisch.mvrecon.process.interestpointdetection.InterestPointTools;
import net.preibisch.mvrecon.process.interestpointdetection.methods.dom.ProcessDOM;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class DoMBlockProcessing {
    public static List<InterestPoint> process(SpimData2 spimData, SerializableDoMParams dom, ViewId vd){

        ImgLoader imgloader = (ImgLoader) ((SequenceDescription) spimData.getSequenceDescription()).getImgLoader();
        AffineTransform3D correctCoordinates = new AffineTransform3D();
        ExecutorService service = Threads.createFixedExecutorService(Threads.numThreads());

        RandomAccessibleInterval<FloatType> input = DownsampleTools.openAndDownsample(imgloader, vd, correctCoordinates, new long[]{(long)dom.downsampleXY, (long)dom.downsampleXY, (long)dom.downsampleZ}, false, true, true, service);
        service.shutdown();
        Image<mpicbg.imglib.type.numeric.real.FloatType> img = ImgLib2.wrapFloatToImgLib1((Img)input);
        List<InterestPoint> ips = ProcessDOM.compute(img, (Img)input, dom.radius1, dom.radius2, dom.threshold, dom.localization, dom.imageSigmaX, dom.imageSigmaY, dom.imageSigmaZ, dom.findMin, dom.findMax, dom.minIntensity, dom.maxIntensity, dom.limitDetections);
        img.close();
        if (dom.limitDetections) {
            ips = InterestPointTools.limitList(dom.maxDetections, dom.maxDetectionsTypeIndex, (List)ips);
        }

        DownsampleTools.correctForDownsampling((List)ips, correctCoordinates);
        return ips;
    }
}
