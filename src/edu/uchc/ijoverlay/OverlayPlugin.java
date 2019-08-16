// 
// Decompiled by Procyon v0.5.36
// 

package edu.uchc.ijoverlay;

import ij.gui.NonBlockingGenericDialog;
import java.util.Vector;
import java.awt.Choice;
import ij.process.ImageProcessor;
import java.awt.AWTEvent;
import ij.gui.GenericDialog;
import java.util.Arrays;
import ij.IJ;
import ij.WindowManager;
import ij.process.ColorProcessor;
import ij.ImagePlus;
import java.util.ArrayList;
import ij.ImageListener;
import ij.gui.DialogListener;
import ij.plugin.PlugIn;

public class OverlayPlugin implements PlugIn, DialogListener, ImageListener
{
    ArrayList<ImagePlus> implist;
    String[] titles;
    ImagePlus overlayImp;
    ImagePlus firstImp;
    ImagePlus[] overlayImps;
    ColorProcessor ip0cache;
    ColorProcessor[] ipcache;
    double[] xoffset;
    double[] yoffset;
    double[] rotation;
    NonBlockingClosableDialog gd;
    
    public OverlayPlugin() {
        overlayImps = new ImagePlus[3];
        ipcache = new ColorProcessor[3];
        xoffset = new double[3];
        yoffset = new double[3];
        rotation= new double[3]; 
    }
    
    public void run(final String cmd) {
        overlayImp = null;
        implist = null;
        showDialog();
    }
    
    void buildImpList() {
        final int nWindows = WindowManager.getImageCount();
        if (nWindows == 0) {
            implist = null;
            return;
        }
        implist = new ArrayList<ImagePlus>();
        for (int i = 0; i < nWindows; ++i) {
            implist.add(WindowManager.getImage(i + 1));
        }
    }
    
    void getTitles() {
        if (implist == null || implist.size() == 0) {
            titles = null;
            return;
        }
        titles = new String[implist.size() + 1];
        for (int i = 0; i < implist.size(); ++i) {
            final ImagePlus imp = implist.get(i);
            titles[i + 1] = imp.getTitle();
        }
        titles[0] = "None";
    }
    
    void showDialog() {
        buildImpList();
        getTitles();
        if (titles == null) {
            IJ.noImage();
            return;
        }
        (gd = new NonBlockingClosableDialog("Overlay")).addChoice("Image1:", (String[])Arrays.copyOfRange(titles, 1, titles.length), titles[1]);
        gd.addChoice("Image2:", titles, titles[0]);
        gd.addSlider("XOffset:", -100.0, 100.0, 0.0);
        gd.addSlider("YOffset:", -100.0, 100.0, 0.0);
        gd.addSlider("Rotation:", -90, 90, 0.0);
        gd.addChoice("Image3:", titles, titles[0]);
        gd.addSlider("XOffset:", -100.0, 100.0, 0.0);
        gd.addSlider("YOffset:", -100.0, 100.0, 0.0);
        gd.addSlider("Rotation:", -90, 90, 0.0);
        gd.addChoice("Image4:", titles, titles[0]);
        gd.addSlider("XOffset:", -100.0, 100.0, 0.0);
        gd.addSlider("YOffset:", -100.0, 100.0, 0.0);
        gd.addSlider("Rotation:", -90, 90, 0.0);
        gd.addDialogListener((DialogListener)this);
        overlayImp = new ImagePlus("Overlay");
        ImagePlus.addImageListener((ImageListener)this);
        gd.showDialog();
        ImagePlus.removeImageListener((ImageListener)this);
        if (!gd.wasOKed()) {
            if (overlayImp != null) {
                overlayImp.close();
            }
        }
        else {
            overlayImp.duplicate().show();
            overlayImp.close();
            overlayImp = null;
        }
    }
    
    public synchronized boolean dialogItemChanged(final GenericDialog gd, final AWTEvent e) {
        final int index0 = gd.getNextChoiceIndex();
        final int[] index2 = { gd.getNextChoiceIndex(), 0, 0 };
        xoffset[0] = gd.getNextNumber();
        yoffset[0] = gd.getNextNumber();
        rotation[0] = gd.getNextNumber();
        index2[1] = gd.getNextChoiceIndex();
        xoffset[1] = gd.getNextNumber();
        yoffset[1] = gd.getNextNumber();
        rotation[2] = gd.getNextNumber();
        index2[2] = gd.getNextChoiceIndex();
        xoffset[2] = gd.getNextNumber();
        yoffset[2] = gd.getNextNumber();
        rotation[2] = gd.getNextNumber();
        if (implist.get(index0) != firstImp) {
            firstImp = implist.get(index0);
            ip0cache = (ColorProcessor)firstImp.getProcessor().duplicate().convertToRGB();
        }
        for (int k = 0; k < 3; ++k) {
            if (index2[k] != 0) {
                if (implist.get(index2[k] - 1) != overlayImps[k]) {
                    overlayImps[k] = implist.get(index2[k] - 1);
                    ipcache[k] = (ColorProcessor)overlayImps[k].getProcessor().duplicate().convertToRGB();
                }
            }
            else {
                overlayImps[k] = null;
            }
        }
        updateImage();
        return true;
    }
    
    synchronized void updateImage() {
        final ColorProcessor ip = (ColorProcessor)ip0cache.duplicate();
        for (int k = 0; k < 3; ++k) {
            if (overlayImps[k] != null) {
            	ColorProcessor ip2;
            	if (rotation[k] == 0) {
            		ip2 = (ColorProcessor) ipcache[k];
            	} else {
	                ip2 = (ColorProcessor) ipcache[k].duplicate();
	                ip2.setBackgroundValue(0);
	                ip2.setInterpolationMethod(ImageProcessor.BICUBIC);
	                ip2.rotate(rotation[k]);
            	}
                ip.copyBits((ImageProcessor)ip2, (int)xoffset[k], (int)yoffset[k], 13);
            }
        }
        overlayImp.setProcessor("Overlay", (ImageProcessor)ip);
        overlayImp.show();
    }
    
    public synchronized void imageClosed(final ImagePlus imp) {
        if (imp == overlayImp) {
            final ImageProcessor ip = overlayImp.getProcessor();
            (overlayImp = new ImagePlus("Overlay", ip)).show();
            return;
        }
        final Vector<Choice> choices = (Vector<Choice>)gd.getChoices();
        if (implist.size() == 1) {
            gd.dispose();
            gd.quit();
            return;
        }
        for (int i = 0; i < implist.size(); ++i) {
            if (implist.get(i) == imp) {
                implist.remove(i);
                break;
            }
        }
        getTitles();
        if (imp == firstImp) {
            firstImp = implist.get(0);
            ip0cache = (ColorProcessor)firstImp.getProcessor().duplicate().convertToRGB();
        }
        for (int k = 0; k < 3; ++k) {
            if (overlayImps[k] == imp) {
                overlayImps[k] = null;
            }
        }
        for (int k = 0; k < 4; ++k) {
            choices.get(k).removeAll();
        }
        for (int k = 1; k < 4; ++k) {
            choices.get(k).add(titles[0]);
        }
        for (int k = 0; k < 4; ++k) {
            for (int l = 1; l < titles.length; ++l) {
                choices.get(k).add(titles[l]);
            }
        }
        choices.get(0).select(firstImp.getTitle());
        for (int k = 1; k < 4; ++k) {
            if (overlayImps[k - 1] != null) {
                choices.get(k).select(overlayImps[k - 1].getTitle());
            }
            else {
                choices.get(k).select(0);
            }
        }
        updateImage();
    }
    
    public synchronized void imageOpened(final ImagePlus imp) {
        if (imp == overlayImp) {
            return;
        }
        final Vector<Choice> choices = (Vector<Choice>)gd.getChoices();
        for (int i = 0; i < choices.size(); ++i) {
            final Choice c = choices.get(i);
            c.add(imp.getTitle());
        }
        implist.add(imp);
    }
    
    public synchronized void imageUpdated(final ImagePlus imp) {
        if (imp == overlayImp) {
            return;
        }
        boolean update = false;
        if (imp == firstImp) {
            update = true;
            ip0cache = (ColorProcessor)firstImp.getProcessor().duplicate().convertToRGB();
        }
        for (int i = 0; i < 3; ++i) {
            if (imp == overlayImps[i]) {
                update = true;
                ipcache[i] = (ColorProcessor)imp.getProcessor().duplicate().convertToRGB();
            }
        }
        if (update) {
            updateImage();
        }
    }
    
    class NonBlockingClosableDialog extends NonBlockingGenericDialog
    {
        public NonBlockingClosableDialog(final String title) {
            super(title);
        }
        
        public synchronized void quit() {
            notify();
        }
    }
}
