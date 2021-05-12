package com.bigdistributor.tasks.deconvolution;

import ij.ImagePlus;
import ij.VirtualStack;
import ij.io.FileInfo;
import ij.io.FileSaver;
import ij.io.TiffEncoder;
import mpicbg.spim.data.sequence.ViewId;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.preibisch.legacy.io.IOFunctions;
import net.preibisch.mvrecon.fiji.plugin.fusion.FusionExportInterface;
import net.preibisch.mvrecon.fiji.plugin.resave.Resave_TIFF;
import net.preibisch.mvrecon.process.export.Calibrateable;
import net.preibisch.mvrecon.process.export.DisplayImage;
import net.preibisch.mvrecon.process.export.ImgExport;
import net.preibisch.mvrecon.process.export.Save3dTIFF;
import net.preibisch.mvrecon.process.interestpointregistration.pairwise.constellation.grouping.Group;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class TiffExporter implements ImgExport, Calibrateable  {
    public static String defaultFN = "";
    public static String defaultPath = null;
    private String path = defaultPath;
    String fnAddition = defaultFN;
    boolean compress = Resave_TIFF.defaultCompress;
    String unit;
    double cal;

    public boolean queryParameters(FusionExportInterface fusion) {
        if (defaultPath == null || defaultPath.length() == 0) {
            defaultPath = fusion.getSpimData().getBasePath().getAbsolutePath();
            if (defaultPath.endsWith("/.")) {
                defaultPath = defaultPath.substring(0, defaultPath.length() - 1);
            }

            if (defaultPath.endsWith("/./")) {
                defaultPath = defaultPath.substring(0, defaultPath.length() - 2);
            }
        }
        return true;
    }

    public TiffExporter(String path) {
        this(path, false);
    }

    public TiffExporter(String path, boolean compress) {
        this.fnAddition = defaultFN;
        this.unit = "px";
        this.cal = 1.0D;
        this.path = path;
        this.compress = compress;
    }

    public <T extends RealType<T> & NativeType<T>> void exportImage(RandomAccessibleInterval<T> img, String title) {
        this.exportImage(img, (Interval)null, 0.0D / 0.0, 0.0D / 0.0, title, (Group)null);
    }

    public <T extends RealType<T> & NativeType<T>> boolean exportImage(RandomAccessibleInterval<T> img, Interval bb, double downsampling, double anisoF, String title, Group<? extends ViewId> fusionGroup) {
        return this.exportImage(img, bb, downsampling, anisoF, title, fusionGroup, 0.0D / 0.0, 0.0D / 0.0);
    }

    public String getFileName(String title) {
        String add;
        if (this.fnAddition.length() > 0) {
            add = this.fnAddition + "_";
        } else {
            add = "";
        }

        String fileName;
        if (!title.endsWith(".tif")) {
            fileName = (new File(this.path, add + title + ".tif")).getAbsolutePath();
        } else {
            fileName = (new File(this.path, add + title)).getAbsolutePath();
        }

        return this.compress ? fileName + ".zip" : fileName;
    }

    public <T extends RealType<T> & NativeType<T>> boolean exportImage(RandomAccessibleInterval<T> img, Interval bb, double downsampling, double anisoF, String title, Group<? extends ViewId> fusionGroup, double min, double max) {
        if (img == null) {
            return false;
        } else {
            double[] minmax = DisplayImage.getFusionMinMax(img, min, max);
            ImagePlus imp = DisplayImage.getImagePlusInstance(img, true, title, minmax[0], minmax[1]);
            DisplayImage.setCalibration(imp, bb, downsampling, anisoF, this.cal, this.unit);
            imp.updateAndDraw();
            String fileName = this.getFileName(title);
            IOFunctions.println(new Date(System.currentTimeMillis()) + ": Saving file " + fileName);
            boolean success;
            if (this.compress) {
                success = (new FileSaver(imp)).saveAsZip(fileName);
            } else {
                success = saveTiffStack(imp, fileName);
            }

            if (success) {
                IOFunctions.println(new Date(System.currentTimeMillis()) + ": Saved file " + fileName);
            } else {
                IOFunctions.println(new Date(System.currentTimeMillis()) + ": FAILED saving file " + fileName);
            }

            return success;
        }
    }

    public static boolean saveTiffStack(ImagePlus imp, String path) {
        FileInfo fi = imp.getFileInfo();
        boolean virtualStack = imp.getStack().isVirtual();
        if (virtualStack) {
            fi.virtualStack = (VirtualStack)imp.getStack();
        }

        fi.info = imp.getInfoProperty();
        fi.description = (new FileSaver(imp)).getDescriptionString();
        DataOutputStream out = null;

        boolean var6;
        try {
            TiffEncoder file = new TiffEncoder(fi);
            out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path)));
            file.write(out);
            out.close();
            return true;
        } catch (IOException var16) {
            IOFunctions.println(new Date(System.currentTimeMillis()) + ": ERROR: Cannot save file '" + path + "':" + var16);
            var6 = false;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException var15) {
                }
            }

        }

        return var6;
    }

    public ImgExport newInstance() {
        return new TiffExporter(this.path);
    }

    public String getDescription() {
        return "Save as (compressed) TIFF stacks";
    }

    public boolean finish() {
        return false;
    }

    public void setCalibration(double pixelSize, String unit) {
        this.cal = pixelSize;
        this.unit = unit;
    }

    public String getUnit() {
        return this.unit;
    }

    public double getPixelSize() {
        return this.cal;
    }
}
