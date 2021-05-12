package com.bigdistributor.tasks.globalOPtimization;

import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.generic.base.Entity;
import mpicbg.spim.data.sequence.Angle;
import mpicbg.spim.data.sequence.Channel;
import mpicbg.spim.data.sequence.Illumination;
import mpicbg.spim.data.sequence.Tile;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.ViewId;
import net.imglib2.util.Pair;
import net.preibisch.mvrecon.fiji.spimdata.SpimData2;
import net.preibisch.mvrecon.fiji.spimdata.XmlIoSpimData2;
import net.preibisch.mvrecon.process.interestpointregistration.pairwise.constellation.grouping.Group;
import net.preibisch.stitcher.algorithm.SpimDataFilteringAndGrouping;
import net.preibisch.stitcher.algorithm.globalopt.GlobalOptStitcher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


public class HeadlessGlobalOptimization {
    private static String defaultXMLfilename;
    private String xmlPath = defaultXMLfilename;

    public void run() throws SpimDataException {

        SpimData2 spimdata = new XmlIoSpimData2("").load(xmlPath);
        List<ViewId> viewIds = new ArrayList<>(spimdata.getSequenceDescription().getViewDescriptions().keySet());

        final SpimDataFilteringAndGrouping<SpimData2> grouping = new SpimDataFilteringAndGrouping<>(spimdata);
        grouping.addFilters(viewIds.stream().map(vid -> spimdata.getSequenceDescription().getViewDescription(vid)).collect(Collectors.toList()));

        // Defaults for grouping
        // the default grouping by channels and illuminations
        final HashSet<Class<? extends Entity>> defaultGroupingFactors = new HashSet<>();
        defaultGroupingFactors.add(Illumination.class);
        defaultGroupingFactors.add(Channel.class);
        // the default comparision by tiles
        final HashSet<Class<? extends Entity>> defaultComparisonFactors = new HashSet<>();
        defaultComparisonFactors.add(Tile.class);

        SerializableGlobalOptimizationParams params = new SerializableGlobalOptimizationParams();


        grouping.addComparisonAxis(Tile.class);
        grouping.addGroupingFactor(Channel.class);
        grouping.addGroupingFactor(Illumination.class);
        grouping.addApplicationAxis(TimePoint.class);
        grouping.addApplicationAxis(Angle.class);


        final ArrayList<Pair<Group<ViewId>, Group<ViewId>>> removedInconsistentPairs = new ArrayList<>();

        if (!GlobalOptStitcher.processGlobalOptimization(spimdata, grouping, params.toParams(), removedInconsistentPairs, false))
            return;
//
//        GlobalOptStitcher.removeInconsistentLinks(removedInconsistentPairs, spimdata.getStitchingResults().getPairwiseResults());
//
//        SpimData2.saveXML(spimdata, "dataset.xml", "");

    }

    public static void main(String[] args) throws SpimDataException {
        HeadlessGlobalOptimization.defaultXMLfilename = "/Users/Marwan/Desktop/Task/data/hdf5/dataset.xml";

        new HeadlessGlobalOptimization().run();
    }

}
