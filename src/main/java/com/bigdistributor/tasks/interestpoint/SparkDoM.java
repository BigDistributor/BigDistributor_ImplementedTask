package com.bigdistributor.tasks.interestpoint;

import com.bigdistributor.io.serializers.ViewIdSerializable;
import com.bigdistributor.tasks.interestpoint.serializable.SerializableDoMParams;
import ij.ImageJ;
import mpicbg.spim.data.sequence.ViewId;
import net.preibisch.legacy.io.IOFunctions;
import net.preibisch.mvrecon.fiji.spimdata.SpimData2;
import net.preibisch.mvrecon.fiji.spimdata.XmlIoSpimData2;
import net.preibisch.mvrecon.fiji.spimdata.interestpoints.InterestPoint;
import net.preibisch.mvrecon.process.interestpointdetection.InterestPointTools;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class SparkDoM {
    public static String defaultLabel = "beads";
    private static String defaultXMLPath;

    static {
        IOFunctions.printIJLog = true;
    }


    public Void call() throws Exception {
        // change dom params with options
        final SerializableDoMParams dom = new SerializableDoMParams();

        final String xmlPath = defaultXMLPath;

        SpimData2 spimdata = new XmlIoSpimData2("").load(xmlPath);
        List<ViewId> viewIds = new ArrayList<>(spimdata.getSequenceDescription().getViewDescriptions().keySet());


        final List<ViewId> removed = SpimData2.filterMissingViews(spimdata, viewIds);
        if (removed.size() > 0)
            IOFunctions.println(new Date(System.currentTimeMillis()) + ": Removed " + removed.size() + " views because they are not present.");

        final HashSet<Integer> tiles = new HashSet<>();
        for (final ViewId viewId : viewIds)
            tiles.add(spimdata.getSequenceDescription().getViewDescription(viewId).getViewSetup().getTile().getId());

        final HashSet<Integer> illums = new HashSet<>();
        for (final ViewId viewId : viewIds)
            illums.add(spimdata.getSequenceDescription().getViewDescription(viewId).getViewSetup().getIllumination().getId());


        // how are the detections called (e.g. beads, nuclei, ...)
        final String label = defaultLabel;
        final SparkConf sparkConf = new SparkConf().setAppName(SparkDoM.class.getSimpleName()).setMaster("local");
        final JavaSparkContext sc = new JavaSparkContext(sparkConf);

        List<ViewIdSerializable> viewIdSerializables = new ArrayList<>();
        for (ViewId viewId : viewIds)
            viewIdSerializables.add(new ViewIdSerializable(viewId));


        final JavaRDD<ViewIdSerializable> rddIds = sc.parallelize(viewIdSerializables);

        //TODO update for more timepoints
        final JavaPairRDD<ViewIdSerializable, List<InterestPoint>> rddResults = rddIds.mapToPair(viewid -> {
            SpimData2 localSpimdata = new XmlIoSpimData2("").load(xmlPath);
            List<InterestPoint> interestPoints = DoMBlockProcessing.process(localSpimdata, dom, viewid.toViewId());
            System.out.println("View: s" + viewid.getSetup()+ "_t"+ viewid.getTimepoint() + ": has " + interestPoints.size() + " interest points.");
            Tuple2<ViewIdSerializable, List<InterestPoint>> result = new Tuple2<>(viewid, interestPoints);
            return result;
        });

        final HashMap<ViewId, List<InterestPoint>> points = new HashMap<>();

        for (Tuple2<ViewIdSerializable, List<InterestPoint>> tuple :
                rddResults.collect()) {
            points.put(tuple._1().toViewId(),tuple._2());
        }
        InterestPointTools.addInterestPoints(spimdata, label, points, "");
        SpimData2.saveXML(spimdata, getFile(xmlPath), "");


        IOFunctions.println("(" + new Date(System.currentTimeMillis()) + "): DONE.");

        return null;
    }

    private static String getFile(String path) {
        String[] elms = path.split("/");
        String file = elms[elms.length - 1];
        return file;
    }

    public static void main(final String[] args) throws Exception {
        SparkDoM.defaultXMLPath = "/Users/Marwan/Desktop/Task/data/hdf5/dataset.xml";

        new ImageJ();
        new SparkDoM().call();
    }

}
