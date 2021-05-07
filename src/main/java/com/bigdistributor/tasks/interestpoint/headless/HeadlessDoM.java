package com.bigdistributor.tasks.interestpoint.headless;

import ij.ImageJ;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.ViewId;
import net.preibisch.legacy.io.IOFunctions;
import net.preibisch.mvrecon.fiji.spimdata.SpimData2;
import net.preibisch.mvrecon.fiji.spimdata.XmlIoSpimData2;
import net.preibisch.mvrecon.fiji.spimdata.explorer.ExplorerWindow;
import net.preibisch.mvrecon.fiji.spimdata.imgloaders.AbstractImgLoader;
import net.preibisch.mvrecon.fiji.spimdata.interestpoints.InterestPoint;
import net.preibisch.mvrecon.process.interestpointdetection.InterestPointTools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class HeadlessDoM {
    public static boolean defaultDefineAnisotropy = false;
    public static boolean defaultSetMinMax = false;
    public static boolean defaultLimitDetections = false;
    public static String defaultLabel = "beads";

    public static boolean defaultGroupTiles = true;
    public static boolean defaultGroupIllums = true;
    public static ExplorerWindow<?, ?> currentPanel;

    static {
        IOFunctions.printIJLog = true;
    }


    /*
     * Does just the detection, no saving
     *
     * @param data
     * @param viewIds
     * @return
     */
    public boolean detectInterestPoints(
            final SpimData2 data,
            final Collection<? extends ViewId> viewCollection) {
        return detectInterestPoints(data, viewCollection, "", null, false);
    }

    public boolean detectInterestPoints(
            final SpimData2 data,
            final Collection<? extends ViewId> viewCollection,
            final String xmlFileName,
            final boolean saveXML) {
        return detectInterestPoints(data, viewCollection, "", xmlFileName, saveXML);
    }

    public boolean detectInterestPoints(
            final SpimData2 data,
            final Collection<? extends ViewId> viewCollection,
            final String clusterExtension,
            final String xmlFileName,
            final boolean saveXML) {
        // filter not present ViewIds
        final ArrayList<ViewId> viewIds = new ArrayList<>();
        viewIds.addAll(viewCollection);

        final List<ViewId> removed = SpimData2.filterMissingViews(data, viewIds);
        if (removed.size() > 0)
            IOFunctions.println(new Date(System.currentTimeMillis()) + ": Removed " + removed.size() + " views because they are not present.");

        final HashSet<Integer> tiles = new HashSet<>();
        for (final ViewId viewId : viewIds)
            tiles.add(data.getSequenceDescription().getViewDescription(viewId).getViewSetup().getTile().getId());

        final HashSet<Integer> illums = new HashSet<>();
        for (final ViewId viewId : viewIds)
            illums.add(data.getSequenceDescription().getViewDescription(viewId).getViewSetup().getIllumination().getId());


        // how are the detections called (e.g. beads, nuclei, ...)
        final String label = defaultLabel;
        final boolean defineAnisotropy = defaultDefineAnisotropy;
        final boolean setMinMax = defaultSetMinMax;
        final boolean limitDetections = defaultLimitDetections;

        boolean groupTiles = false;
        if (tiles.size() > 1)
            groupTiles = defaultGroupTiles;

        boolean groupIllums = false;
        if (illums.size() > 1)
            groupIllums = defaultGroupIllums;

        final DifferenceOfMean differenceOfMean = new DifferenceOfMean(data, viewIds);

        // the interest point detection should query its parameters
        if (!differenceOfMean.initParameters(defineAnisotropy, setMinMax, limitDetections, groupTiles, groupIllums))
            return false;

        // if grouped, we need to get the min/max intensity for all groups
        differenceOfMean.preprocess();

        // now extract all the detections
        for (final TimePoint tp : SpimData2.getAllTimePointsSorted(data, viewIds)) {
            final HashMap<ViewId, List<InterestPoint>> points = differenceOfMean.findInterestPoints(tp);

            InterestPointTools.addInterestPoints(data, label, points, differenceOfMean.getParameters());

            // update metadata if necessary
            if (data.getSequenceDescription().getImgLoader() instanceof AbstractImgLoader) {
                IOFunctions.println("(" + new Date(System.currentTimeMillis()) + "): Updating metadata ... ");
                try {
                    ((AbstractImgLoader) data.getSequenceDescription().getImgLoader()).updateXMLMetaData(data, false);
                } catch (Exception e) {
                    IOFunctions.println("Failed to update metadata, this should not happen: " + e);
                }
            }

            if (currentPanel != null)
                currentPanel.updateContent();

            // save the xml
            if (saveXML)
                SpimData2.saveXML(data, xmlFileName, clusterExtension);
        }

        IOFunctions.println("(" + new Date(System.currentTimeMillis()) + "): DONE.");

        return true;
    }

    public Void call(String path) throws Exception {
        SpimData2 spimdata = new XmlIoSpimData2("").load(path);
        List<ViewId> views = new ArrayList<>(spimdata.getSequenceDescription().getViewDescriptions().keySet());
        String[] elms = path.split("/");
        String xmlFile = elms[elms.length - 1];
        detectInterestPoints(
                spimdata, views,
                "",
                xmlFile,
                true);
        return null;
    }

    public static void main(final String[] args) throws Exception {
        String defaultXMLfilename = "/Users/Marwan/Desktop/Task/data/hdf5/dataset.xml";

        new ImageJ();
        new HeadlessDoM().call(defaultXMLfilename);
//        new InterestPoint().run(null);
    }

}
