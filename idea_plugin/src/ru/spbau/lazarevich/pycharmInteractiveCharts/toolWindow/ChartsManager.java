package ru.spbau.lazarevich.pycharmInteractiveCharts.toolWindow;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.UIUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ChartsManager {
  private static final int ourMinimumChartWidth = 320;
  private static final int ourMinimumChartHeight = 240;
  private VirtualFile myChartsDirectory;
  private ArrayList<VirtualFile> myCharts;
  private int myCurrentImageIndex;
  private int myChartWidth = ourMinimumChartWidth;
  private int myChartHeight = ourMinimumChartHeight;

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

  public int getCurrentImageIndex() {
    return myCurrentImageIndex;
  }

  public VirtualFile getChartsDirectory() {
    return myChartsDirectory;
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
    BufferedImage currentChart;
    currentChart = getCurrentImage();
    if (rescale && currentChart != null) {
      currentChart = resize(currentChart, myChartWidth, myChartHeight);
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
    BufferedImage currentChart = next ? getNextImage() : getPrevImage();
    currentChart = resize(currentChart, myChartWidth, myChartHeight);
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
    if (newWidth < ourMinimumChartWidth) {
      return;
    }
    myChartHeight = newWidth * myChartHeight / myChartWidth;
    myChartWidth = newWidth;
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
    return ImageIO.read(new File(myCharts.get(myCurrentImageIndex).getPath()));
  }

  private boolean isEmptyChartDir() {
    return myCharts == null || myCharts.isEmpty();
  }

  public void initializeChartFiles() {
    myCurrentImageIndex = 0;
    if (myChartsDirectory != null) {
      if (myCharts == null) {
        myCharts = new ArrayList<>();
      }
      else {
        myCharts.clear();
      }
      for (VirtualFile file : myChartsDirectory.getChildren()) {
        String extension = file.getExtension();
        if (extension != null && extension.equals("png")) {
          myCharts.add(file);
        }
      }
    }
    else {
      myCharts = null;
    }
  }

  private static class IMAGE_GETTER_FLAGS {
    public final static int NEXT_IMAGE = 1;
    public final static int CURRENT_IMAGE = 0;
    public final static int PREV_IMAGE = -1;
  }
}
