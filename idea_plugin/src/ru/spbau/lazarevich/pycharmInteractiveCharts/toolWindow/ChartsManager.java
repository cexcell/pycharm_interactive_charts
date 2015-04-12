package ru.spbau.lazarevich.pycharmInteractiveCharts.toolWindow;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.UIUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by cexcell on 12.04.15.
 */
public class ChartsManager {
  private VirtualFile myChartsDirectory;
  private ArrayList<VirtualFile> myCharts;
  private int myCurrentImageIndex;
  private final int minimumChartWidth = 320;
  private final int minimumChartHeight = 240;
  private int chartWidth = minimumChartWidth;
  private int chartHeight = minimumChartHeight;

  public ChartsManager(VirtualFile ideaDir, String imageDir) throws IOException {
    if ((myChartsDirectory = ideaDir.findChild(imageDir)) == null) {
      myChartsDirectory = ideaDir.createChildDirectory(this, imageDir);
    }
  }


  public static BufferedImage resize(BufferedImage img, int newW, int newH) {
    Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
    BufferedImage dimg = UIUtil.createImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

    Graphics2D g2d = dimg.createGraphics();
    g2d.drawImage(tmp, 0, 0, null);
    g2d.dispose();

    return dimg;
  }

  private void checkFileListBoundaries() {
    if (myCurrentImageIndex >= myCharts.size()) {
      myCurrentImageIndex = 0;
    }
    if (myCurrentImageIndex < 0) {
      myCurrentImageIndex = myCharts.size() - 1;
    }
  }

  private BufferedImage redraw(boolean rescale) throws IOException {
    if (isEmptyChartDir()) {
      return null;
    }
    BufferedImage currentChart = null;
    currentChart = getCurrentImage();
    if (rescale && currentChart != null) {
      currentChart = resize(currentChart, chartWidth, chartHeight);
    }
    return currentChart;
  }


  public BufferedImage drawCurrentImage() throws IOException {
    return redraw(false);
  }

  public BufferedImage drawNext() throws IOException {
    return drawImage(true);
  }

  public BufferedImage drawPrev() throws IOException {
    return drawImage(false);
  }

  private BufferedImage drawImage(boolean next) throws IOException {
    if (isEmptyChartDir()) {
      return null;
    }
    BufferedImage currentChart = null;
    if (next) {
      currentChart = getNextImage();
    }
    else {
      currentChart = getPrevImage();
    }
    currentChart = resize(currentChart, chartWidth, chartHeight);
    return currentChart;
  }

  public ArrayList<VirtualFile> getCharts() {
    return new ArrayList<>(myCharts);
  }

  public void clear() {
    myCurrentImageIndex = 0;
    myCharts.clear();
  }

  public void setScale(int newWidth) {
    if (newWidth < minimumChartWidth) {
      return;
    }
    chartHeight = newWidth * chartHeight / chartWidth;
    chartWidth = newWidth;
  }

  //public BufferedImage drawIfEmpty() throws IOException {
  //  boolean wasEmpty = isEmptyChartDir();
  //  initializeChartFiles();
  //  if (wasEmpty) {
  //    return redrawCurrentImage();
  //  }
  //  return null;
  //}

  public BufferedImage redrawAfterMoved() throws IOException {
    initializeChartFiles();
    return drawCurrentImage();
  }

  public BufferedImage redrawCurrentImage() throws IOException {
    return redraw(true);
  }

  private static class IMAGE_GETTER_FLAGS {
    public final static int NEXT_IMAGE = 1;
    public final static int CURRENT_IMAGE = 0;
    public final static int PREV_IMAGE = -1;
  }

  private BufferedImage getPrevImage() throws IOException {
    return getImage(IMAGE_GETTER_FLAGS.PREV_IMAGE);
  }

  private BufferedImage getNextImage() throws IOException {
    return getImage(IMAGE_GETTER_FLAGS.NEXT_IMAGE);
  }

  private BufferedImage getCurrentImage() throws IOException {
    return getImage(IMAGE_GETTER_FLAGS.CURRENT_IMAGE);
  }

  private BufferedImage getImage(int flag) throws IOException {
    switch (flag) {
      case IMAGE_GETTER_FLAGS.PREV_IMAGE:
        myCurrentImageIndex--;
        break;
      case IMAGE_GETTER_FLAGS.NEXT_IMAGE:
        myCurrentImageIndex++;
        break;
    }
    checkFileListBoundaries();
    BufferedImage currentChart = ImageIO.read(new File(myCharts.get(myCurrentImageIndex).getPath()));
    return currentChart;
  }

  private boolean isEmptyChartDir() {
    return myCharts == null || myCharts.isEmpty();
  }

  public void initializeChartFiles() {
    myCurrentImageIndex = 0;
    if (myChartsDirectory != null) {
      if (myCharts == null) {
        myCharts = new ArrayList<VirtualFile>();
      }
      else {
        myCharts.clear();
      }
      for (VirtualFile file : myChartsDirectory.getChildren()) {
        if (file.getExtension().equals("png")) {
          myCharts.add(file);
        }
      }
    }
    else {
      myCharts = null;
    }
  }
}
