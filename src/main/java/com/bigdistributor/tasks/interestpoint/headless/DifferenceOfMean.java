//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.bigdistributor.tasks.interestpoint.headless;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import mpicbg.spim.data.sequence.ImgLoader;
import mpicbg.spim.data.sequence.SequenceDescription;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.ViewDescription;
import mpicbg.spim.data.sequence.ViewId;
import mpicbg.spim.data.sequence.ViewSetup;
import net.preibisch.legacy.io.IOFunctions;
import net.preibisch.legacy.segmentation.InteractiveIntegral;
import net.preibisch.mvrecon.fiji.plugin.interestpointdetection.DifferenceOfGUI;
import net.preibisch.mvrecon.fiji.spimdata.SpimData2;
import net.preibisch.mvrecon.fiji.spimdata.interestpoints.InterestPoint;
import net.preibisch.mvrecon.process.downsampling.DownsampleTools;
import net.preibisch.mvrecon.process.fusion.FusionTools;
import net.preibisch.mvrecon.process.interestpointdetection.methods.dom.DoM;
import net.preibisch.mvrecon.process.interestpointdetection.methods.dom.DoMParameters;
import net.preibisch.mvrecon.process.interestpointregistration.pairwise.constellation.grouping.Group;

public class DifferenceOfMean extends DifferenceOfGUI {
    public static int defaultRadius1 = 2;
    public static int defaultRadius2 = 3;
    public static double defaultThreshold = 0.005D;
    public static boolean defaultFindMin = false;
    public static boolean defaultFindMax = true;
    private final List<ViewId> viewIdsToProcess;
    private final SpimData2 spimData;
    int radius1;
    int radius2;
    double threshold;
    boolean findMin;
    boolean findMax;
    private boolean groupIllums;
    private boolean sameMinMax;
    private boolean groupTiles;
    private int brightness;

    public DifferenceOfMean(SpimData2 spimData, List<ViewId> viewIdsToProcess) {
        super(spimData,viewIdsToProcess);
        this.spimData = spimData;
        this.viewIdsToProcess = viewIdsToProcess;
    }

    @Override
    protected void addAddtionalParameters(GenericDialog genericDialog) {
        
    }

    @Override
    protected boolean queryAdditionalParameters(GenericDialog genericDialog) {
        return true;
    }

    public String getDescription() {
        return "Difference-of-Mean (Integral image based)";
    }

    public DifferenceOfMean newInstance(SpimData2 spimData, List<ViewId> viewIdsToProcess) {
        return new DifferenceOfMean(spimData, viewIdsToProcess);
    }

    public HashMap<ViewId, List<InterestPoint>> findInterestPoints(TimePoint t) {
        DoMParameters dom = new DoMParameters();
        dom.imgloader = (ImgLoader)((SequenceDescription)this.spimData.getSequenceDescription()).getImgLoader();
        dom.toProcess = new ArrayList();
        dom.localization = this.localization;
        dom.downsampleZ = this.downsampleZ;
        dom.imageSigmaX = this.imageSigmaX;
        dom.imageSigmaY = this.imageSigmaY;
        dom.imageSigmaZ = this.imageSigmaZ;
        dom.minIntensity = this.minIntensity;
        dom.maxIntensity = this.maxIntensity;
        dom.radius1 = this.radius1;
        dom.radius2 = this.radius2;
        dom.threshold = (float)this.threshold;
        dom.findMin = this.findMin;
        dom.findMax = this.findMax;
        dom.limitDetections = this.limitDetections;
        dom.maxDetections = this.maxDetections;
        dom.maxDetectionsTypeIndex = this.maxDetectionsTypeIndex;
        HashMap<ViewId, List<InterestPoint>> interestPoints = new HashMap();
        Iterator var4 = SpimData2.getAllViewIdsForTimePointSorted(this.spimData, this.viewIdsToProcess, t).iterator();

        while(var4.hasNext()) {
            ViewDescription vd = (ViewDescription)var4.next();

            try {
                if (vd.isPresent()) {
                    dom.toProcess.clear();
                    dom.toProcess.add(vd);
                    if (this.downsampleXYIndex < 1) {
                        dom.downsampleXY = DownsampleTools.downsampleFactor(this.downsampleXYIndex, this.downsampleZ, ((ViewSetup)vd.getViewSetup()).getVoxelSize());
                    } else {
                        dom.downsampleXY = this.downsampleXYIndex;
                    }

                    DoM.addInterestPoints(interestPoints, dom);
                }
            } catch (Exception var7) {
                IOFunctions.println("An error occured (DOM): " + var7);
                IOFunctions.println("Failed to segment angleId: " + ((ViewSetup)vd.getViewSetup()).getAngle().getId() + " channelId: " + ((ViewSetup)vd.getViewSetup()).getChannel().getId() + " illumId: " + ((ViewSetup)vd.getViewSetup()).getIllumination().getId() + ". Continuing with next one.");
                var7.printStackTrace();
            }
        }

        return interestPoints;
    }

    protected boolean setDefaultValues(int brightness) {
        this.radius1 = defaultRadius1;
        this.radius2 = defaultRadius2;
        this.findMin = false;
        this.findMax = true;
        if (brightness == 0) {
            this.threshold = 0.0024999999441206455D;
        } else if (brightness == 1) {
            this.threshold = 0.019999999552965164D;
        } else if (brightness == 2) {
            this.threshold = 0.07500000298023224D;
        } else {
            if (brightness != 3) {
                return false;
            }

            this.threshold = 0.25D;
        }

        return true;
    }

    protected boolean setAdvancedValues() {
        GenericDialog gd = new GenericDialog("Advanced values");
        gd.addNumericField("Radius_1", (double)defaultRadius1, 0);
        gd.addNumericField("Radius_2", (double)defaultRadius2, 0);
        gd.addNumericField("Threshold", defaultThreshold, 4);
        gd.addCheckbox("Find_minima", defaultFindMin);
        gd.addCheckbox("Find_maxima", defaultFindMax);
        gd.showDialog();
        if (gd.wasCanceled()) {
            return false;
        } else {
            this.radius1 = defaultRadius1 = (int)Math.round(gd.getNextNumber());
            this.radius2 = defaultRadius2 = (int)Math.round(gd.getNextNumber());
            this.threshold = defaultThreshold = gd.getNextNumber();
            this.findMin = defaultFindMin = gd.getNextBoolean();
            this.findMax = defaultFindMax = gd.getNextBoolean();
            return true;
        }
    }

    protected boolean setInteractiveValues() {
        ImagePlus imp;
        if (!this.groupIllums && !this.groupTiles) {
            imp = this.getImagePlusForInteractive("Interactive Difference-of-Gaussian");
        } else {
            imp = this.getGroupedImagePlusForInteractive("Interactive Difference-of-Gaussian");
        }

        if (imp == null) {
            return false;
        } else {
            imp.setDimensions(1, imp.getStackSize(), 1);
            imp.show();
            imp.setSlice(imp.getStackSize() / 2);
            InteractiveIntegral ii = new InteractiveIntegral();
            ii.setInitialRadius(Math.round((float)defaultRadius1));
            ii.setThreshold((float)defaultThreshold);
            ii.setLookForMinima(defaultFindMin);
            ii.setLookForMaxima(defaultFindMax);
            ii.setMinIntensityImage(this.minIntensity);
            ii.setMaxIntensityImage(this.maxIntensity);
            ii.run((String)null);

            while(!ii.isFinished()) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException var4) {
                }
            }

            imp.close();
            if (ii.wasCanceld()) {
                return false;
            } else {
                this.radius1 = defaultRadius1 = ii.getRadius1();
                this.radius2 = defaultRadius2 = ii.getRadius2();
                this.threshold = defaultThreshold = ii.getThreshold();
                this.findMin = defaultFindMin = ii.getLookForMinima();
                this.findMax = defaultFindMax = ii.getLookForMaxima();
                return true;
            }
        }
    }

    public String getParameters() {
        return "DOM r1=" + this.radius1 + " t=" + this.threshold + " min=" + this.findMin + " max=" + this.findMax + " imageSigmaX=" + this.imageSigmaX + " imageSigmaY=" + this.imageSigmaY + " imageSigmaZ=" + this.imageSigmaZ + " downsampleXYIndex=" + this.downsampleXYIndex + " downsampleZ=" + this.downsampleZ + " minIntensity=" + this.minIntensity + " maxIntensity=" + this.maxIntensity;
    }


    public boolean initParameters(boolean defineAnisotropy, boolean setMinMax, boolean limitDetections, boolean groupTiles, boolean groupIllums) {
        this.localization = defaultLocalization ;
        brightness = defaultBrightness ;
        int dsxy = defaultDownsampleXYIndex;
        int dsz = defaultDownsampleZIndex ;
        if (dsz == 0) {
            this.downsampleZ = 1;
        } else if (dsz == 1) {
            this.downsampleZ = 2;
        } else if (dsz == 2) {
            this.downsampleZ = 4;
        } else {
            this.downsampleZ = 8;
        }

        if (dsxy == 0) {
            this.downsampleXYIndex = 1;
        } else if (dsxy == 1) {
            this.downsampleXYIndex = 2;
        } else if (dsxy == 2) {
            this.downsampleXYIndex = 4;
        } else if (dsxy == 3) {
            this.downsampleXYIndex = 8;
        } else if (dsxy == 4) {
            this.downsampleXYIndex = 0;
        } else {
            this.downsampleXYIndex = -1;
        }

        if (setMinMax) {
            this.minIntensity = defaultMinIntensity ;
            this.maxIntensity = defaultMaxIntensity ;
            this.sameMinMax = false;
        } else {
            this.minIntensity = this.maxIntensity = 0.0D / 0.0;
            this.sameMinMax = defaultSameMinMax ;
        }

        if (brightness <= 3) {
            if (!this.setDefaultValues(brightness)) {
                return false;
            }
        } else if (brightness == 4) {
            if (!this.setAdvancedValues()) {
                return false;
            }
        }

        if (defineAnisotropy) {
            this.imageSigmaX = defaultImageSigmaX;
            this.imageSigmaY = defaultImageSigmaY ;
            this.imageSigmaZ = defaultImageSigmaZ ;
        } else {
            this.imageSigmaX = this.imageSigmaY = this.imageSigmaZ = 0.5D;
        }

        if (limitDetections) {
            this.maxDetections = defaultMaxDetections ;
            this.maxDetectionsTypeIndex = defaultMaxDetectionsTypeIndex ;
        }

        return true;
    }

    public void preprocess() {
        if ((this.sameMinMax || this.groupIllums || this.groupTiles) && (Double.isNaN(this.minIntensity) || Double.isNaN(this.maxIntensity))) {
            IOFunctions.println("(" + new Date(System.currentTimeMillis()) + "): Determining it approximate Min & Max for all views at lowest resolution levels ... ");
            IJ.showProgress(0.0D);
            ImgLoader imgLoader = (ImgLoader)((SequenceDescription)this.spimData.getSequenceDescription()).getImgLoader();
            double min = 1.7976931348623157E308D;
            double max = -1.7976931348623157E308D;
            int count = 0;
            Iterator var7 = this.viewIdsToProcess.iterator();

            while(var7.hasNext()) {
                ViewId view = (ViewId)var7.next();
                double[] minmax = FusionTools.minMaxApprox(DownsampleTools.openAtLowestLevel(imgLoader, view));
                min = Math.min(min, minmax[0]);
                max = Math.max(max, minmax[1]);
                IOFunctions.println("(" + new Date(System.currentTimeMillis()) + "): View " + Group.pvid(view) + ", Min=" + minmax[0] + " max=" + minmax[1]);
                ++count;
                IJ.showProgress((double)count / (double)this.viewIdsToProcess.size());
            }

            this.minIntensity = min;
            this.maxIntensity = max;
            IOFunctions.println("(" + new Date(System.currentTimeMillis()) + "): Total Min=" + this.minIntensity + " max=" + this.maxIntensity);
        }

    }

}
