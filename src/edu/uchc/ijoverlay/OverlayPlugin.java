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
        this.overlayImps = new ImagePlus[3];
        this.ipcache = new ColorProcessor[3];
        this.xoffset = new double[3];
        this.yoffset = new double[3];
        this.rotation= new double[3]; 
    }
    
    public void run(final String cmd) {
        this.overlayImp = null;
        this.implist = null;
        this.showDialog();
    }
    
    void buildImpList() {
        final int nWindows = WindowManager.getImageCount();
        if (nWindows == 0) {
            this.implist = null;
            return;
        }
        this.implist = new ArrayList<ImagePlus>();
        for (int i = 0; i < nWindows; ++i) {
            this.implist.add(WindowManager.getImage(i + 1));
        }
    }
    
    void getTitles() {
        if (this.implist == null || this.implist.size() == 0) {
            this.titles = null;
            return;
        }
        this.titles = new String[this.implist.size() + 1];
        for (int i = 0; i < this.implist.size(); ++i) {
            final ImagePlus imp = this.implist.get(i);
            this.titles[i + 1] = imp.getTitle();
        }
        this.titles[0] = "None";
    }
    
    void showDialog() {
        this.buildImpList();
        this.getTitles();
        if (this.titles == null) {
            IJ.noImage();
            return;
        }
        (this.gd = new NonBlockingClosableDialog("Overlay")).addChoice("Image1:", (String[])Arrays.copyOfRange(this.titles, 1, this.titles.length), this.titles[1]);
        this.gd.addChoice("Image2:", this.titles, this.titles[0]);
        this.gd.addSlider("XOffset:", -100.0, 100.0, 0.0);
        this.gd.addSlider("YOffset:", -100.0, 100.0, 0.0);
        gd.addSlider("Rotation:", -90, 90, 0.0);
        this.gd.addChoice("Image3:", this.titles, this.titles[0]);
        this.gd.addSlider("XOffset:", -100.0, 100.0, 0.0);
        this.gd.addSlider("YOffset:", -100.0, 100.0, 0.0);
        gd.addSlider("Rotation:", -90, 90, 0.0);
        this.gd.addChoice("Image4:", this.titles, this.titles[0]);
        this.gd.addSlider("XOffset:", -100.0, 100.0, 0.0);
        this.gd.addSlider("YOffset:", -100.0, 100.0, 0.0);
        gd.addSlider("Rotation:", -90, 90, 0.0);
        this.gd.addDialogListener((DialogListener)this);
        this.overlayImp = new ImagePlus("Overlay");
        ImagePlus.addImageListener((ImageListener)this);
        this.gd.showDialog();
        ImagePlus.removeImageListener((ImageListener)this);
        if (!this.gd.wasOKed()) {
            if (this.overlayImp != null) {
                this.overlayImp.close();
            }
        }
        else {
            this.overlayImp.duplicate().show();
            this.overlayImp.close();
            this.overlayImp = null;
        }
    }
    
    public synchronized boolean dialogItemChanged(final GenericDialog gd, final AWTEvent e) {
        final int index0 = gd.getNextChoiceIndex();
        final int[] index2 = { gd.getNextChoiceIndex(), 0, 0 };
        this.xoffset[0] = gd.getNextNumber();
        this.yoffset[0] = gd.getNextNumber();
        rotation[0] = gd.getNextNumber();
        index2[1] = gd.getNextChoiceIndex();
        this.xoffset[1] = gd.getNextNumber();
        this.yoffset[1] = gd.getNextNumber();
        rotation[2] = gd.getNextNumber();
        index2[2] = gd.getNextChoiceIndex();
        this.xoffset[2] = gd.getNextNumber();
        this.yoffset[2] = gd.getNextNumber();
        rotation[2] = gd.getNextNumber();
        if (this.implist.get(index0) != this.firstImp) {
            this.firstImp = this.implist.get(index0);
            this.ip0cache = (ColorProcessor)this.firstImp.getProcessor().duplicate().convertToRGB();
        }
        for (int k = 0; k < 3; ++k) {
            if (index2[k] != 0) {
                if (this.implist.get(index2[k] - 1) != this.overlayImps[k]) {
                    this.overlayImps[k] = this.implist.get(index2[k] - 1);
                    this.ipcache[k] = (ColorProcessor)this.overlayImps[k].getProcessor().duplicate().convertToRGB();
                }
            }
            else {
                this.overlayImps[k] = null;
            }
        }
        this.updateImage();
        return true;
    }
    
    synchronized void updateImage() {
        final ColorProcessor ip = (ColorProcessor)this.ip0cache.duplicate();
        for (int k = 0; k < 3; ++k) {
            if (this.overlayImps[k] != null) {
            	ColorProcessor ip2;
            	if (rotation[k] == 0) {
            		ip2 = (ColorProcessor) ipcache[k];
            	} else {
	                ip2 = (ColorProcessor) ipcache[k].duplicate();
	                ip2.setBackgroundValue(0);
	                ip2.setInterpolationMethod(ImageProcessor.BICUBIC);
	                ip2.rotate(rotation[k]);
            	}
                ip.copyBits((ImageProcessor)ip2, (int)this.xoffset[k], (int)this.yoffset[k], 13);
            }
        }
        this.overlayImp.setProcessor("Overlay", (ImageProcessor)ip);
        this.overlayImp.show();
    }
    
    public synchronized void imageClosed(final ImagePlus imp) {
        if (imp == this.overlayImp) {
            final ImageProcessor ip = this.overlayImp.getProcessor();
            (this.overlayImp = new ImagePlus("Overlay", ip)).show();
            return;
        }
        final Vector<Choice> choices = (Vector<Choice>)this.gd.getChoices();
        if (this.implist.size() == 1) {
            this.gd.dispose();
            this.gd.quit();
            return;
        }
        for (int i = 0; i < this.implist.size(); ++i) {
            if (this.implist.get(i) == imp) {
                this.implist.remove(i);
                break;
            }
        }
        this.getTitles();
        if (imp == this.firstImp) {
            this.firstImp = this.implist.get(0);
            this.ip0cache = (ColorProcessor)this.firstImp.getProcessor().duplicate().convertToRGB();
        }
        for (int k = 0; k < 3; ++k) {
            if (this.overlayImps[k] == imp) {
                this.overlayImps[k] = null;
            }
        }
        for (int k = 0; k < 4; ++k) {
            choices.get(k).removeAll();
        }
        for (int k = 1; k < 4; ++k) {
            choices.get(k).add(this.titles[0]);
        }
        for (int k = 0; k < 4; ++k) {
            for (int l = 1; l < this.titles.length; ++l) {
                choices.get(k).add(this.titles[l]);
            }
        }
        choices.get(0).select(this.firstImp.getTitle());
        for (int k = 1; k < 4; ++k) {
            if (this.overlayImps[k - 1] != null) {
                choices.get(k).select(this.overlayImps[k - 1].getTitle());
            }
            else {
                choices.get(k).select(0);
            }
        }
        this.updateImage();
    }
    
    public synchronized void imageOpened(final ImagePlus imp) {
        if (imp == this.overlayImp) {
            return;
        }
        final Vector<Choice> choices = (Vector<Choice>)this.gd.getChoices();
        for (int i = 0; i < choices.size(); ++i) {
            final Choice c = choices.get(i);
            c.add(imp.getTitle());
        }
        this.implist.add(imp);
    }
    
    public synchronized void imageUpdated(final ImagePlus imp) {
        if (imp == this.overlayImp) {
            return;
        }
        boolean update = false;
        if (imp == this.firstImp) {
            update = true;
            this.ip0cache = (ColorProcessor)this.firstImp.getProcessor().duplicate().convertToRGB();
        }
        for (int i = 0; i < 3; ++i) {
            if (imp == this.overlayImps[i]) {
                update = true;
                this.ipcache[i] = (ColorProcessor)imp.getProcessor().duplicate().convertToRGB();
            }
        }
        if (update) {
            this.updateImage();
        }
    }
    
    class NonBlockingClosableDialog extends NonBlockingGenericDialog
    {
        public NonBlockingClosableDialog(final String title) {
            super(title);
        }
        
        public synchronized void quit() {
            this.notify();
        }
    }
}
