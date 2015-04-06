package ru.spbau.lazarevich.pycharmInteractiveCharts.toolWindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

/**
 * Created by Andrey
 */

public class CacheChartsToolWindowFactory implements ToolWindowFactory {
  private int chartHeight = 240;
  private int chartWidth = 320;
  private static int margin = 10;
  private static final String ideaPath = ".idea";
  private static final String imageDir = "charts";
  private static final String missingDirectoryExceptionMessage = "Cannot read from charts directory";
  private static final String readingErrorFromDirectoryExceptionMessage = "IO error occurred during directory initialization";
  private JButton myPreviousImageButton;
  private JButton myNextImageButton;
  private JButton myClearImageButton;
  private JPanel myCacheChartsToolWindowContent;
  private JPanel myChartViewer;
  private JLabel myChartLabel;
  private ToolWindow myCacheChartsToolWindow;
  private VirtualFile myChartsDirectory;
  private VirtualFile[] myCharts;
  private int myCurrentImageIndex;

  public CacheChartsToolWindowFactory() {
    myCurrentImageIndex = 0;
    myNextImageButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        CacheChartsToolWindowFactory.this.drawNextImage();
      }
    });
    myClearImageButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        CacheChartsToolWindowFactory.this.clearImage();
      }
    });
    myPreviousImageButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        CacheChartsToolWindowFactory.this.drawPrevImage();
      }
    });
    myCacheChartsToolWindowContent.addComponentListener(new ComponentListener() {
      @Override
      public void componentResized(ComponentEvent event) {
        CacheChartsToolWindowFactory.this.resizeAndDrawCurrentImage();
      }

      @Override
      public void componentMoved(ComponentEvent event) {

      }

      @Override
      public void componentShown(ComponentEvent event) {
        CacheChartsToolWindowFactory.this.resizeAndDrawCurrentImage();
      }

      @Override
      public void componentHidden(ComponentEvent event) {

      }
    });
  }

  private void resizeAndDrawCurrentImage() {
    recalculateImageScale();
    drawCurrentImage();
  }

  private void recalculateImageScale() {
    int newWidth = myCacheChartsToolWindowContent.getWidth() - 2 * margin;
    if (newWidth < 320) {
      return;
    }
    chartHeight = newWidth * chartHeight / chartWidth;
    chartWidth = newWidth;
  }

  public static BufferedImage resize(BufferedImage img, int newW, int newH) {
    Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
    BufferedImage dimg = UIUtil.createImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

    Graphics2D g2d = dimg.createGraphics();
    g2d.drawImage(tmp, 0, 0, null);
    g2d.dispose();

    return dimg;
  }

  private void drawCurrentImage() {
    if (isEmptyChartDir()) {
      return;
    }
    BufferedImage currentChart = null;
    try {
      currentChart = getCurrentImage();
    }
    catch (IOException e) {
      System.err.println(missingDirectoryExceptionMessage + e.getMessage());
    }
    resizeAndSetImage(currentChart);
  }

  private void drawNextImage() {
    drawImage(true);
  }

  private void drawPrevImage() {
    drawImage(false);
  }

  private void drawImage(boolean next) {
    if (isEmptyChartDir()) {
      return;
    }
    BufferedImage currentChart = null;
    try {
      if (next) {
        currentChart = getNextImage();
      }
      else {
        currentChart = getPrevImage();
      }
    }
    catch (IOException e) {
      System.err.println(missingDirectoryExceptionMessage + e.getMessage());
    }
    resizeAndSetImage(currentChart);
  }

  private void resizeAndSetImage(BufferedImage currentChart) {
    if (currentChart == null) {
      return;
    }
    currentChart = resize(currentChart, chartWidth, chartHeight);
    myChartLabel.setIcon(new ImageIcon(currentChart));
  }

  private static class IMAGE_GETTER_FLAGS
  {
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
      case IMAGE_GETTER_FLAGS.PREV_IMAGE: myCurrentImageIndex--;
        break;
      case IMAGE_GETTER_FLAGS.NEXT_IMAGE: myCurrentImageIndex++;
        break;
    }
    checkFileListBoundaries();
    BufferedImage currentChart = ImageIO.read(new File(myCharts[myCurrentImageIndex].getPath()));
    return currentChart;
  }

  private void checkFileListBoundaries() {
    if (myCurrentImageIndex >= myCharts.length) {
      myCurrentImageIndex = 0;
    }
    if (myCurrentImageIndex < 0) {
      myCurrentImageIndex = myCharts.length - 1;
    }
  }

  private void clearImage() {
    myChartLabel.setIcon(null);
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        for (VirtualFile chart : myCharts) {
          try {
            chart.delete(this);
          }
          catch (IOException e) {
            System.err.println("Cannot remove image: " + chart.getName());
          }
        }
      }
    });
  }

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    myCacheChartsToolWindow = toolWindow;
    this.initializeDirectory(project);
    this.initializeChartFiles();
    this.drawCurrentImage();
    final ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
    Content content = contentFactory.createContent(myCacheChartsToolWindowContent, "", false);
    toolWindow.getContentManager().addContent(content);
  }

  private void initializeDirectory(@NotNull Project project) {
    VirtualFile ideaDir = project.getBaseDir().findChild(ideaPath);
    if (ideaDir == null) {
      return;
    }
    if ((myChartsDirectory = ideaDir.findChild(imageDir)) == null) {
      try {
        myChartsDirectory = ideaDir.createChildDirectory(this, imageDir);
      }
      catch (IOException e) {
        System.err.println(readingErrorFromDirectoryExceptionMessage + e.getMessage());
      }
    }
    VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileListener() {
      @Override
      public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {

      }

      @Override
      public void contentsChanged(@NotNull VirtualFileEvent event) {
        initializeChartFiles();
      }

      @Override
      public void fileCreated(@NotNull VirtualFileEvent event) {
        boolean wasEmpty = isEmptyChartDir();
        initializeChartFiles();
        if (wasEmpty) {
          drawCurrentImage();
        }
      }

      @Override
      public void fileDeleted(@NotNull VirtualFileEvent event) {
        initializeChartFiles();
        if (isEmptyChartDir()) {
          clearImage();
        }
        else {
          drawNextImage();
        }
      }

      @Override
      public void fileMoved(@NotNull VirtualFileMoveEvent event) {
        initializeChartFiles();
        drawCurrentImage();
      }

      @Override
      public void fileCopied(@NotNull VirtualFileCopyEvent event) {

      }

      @Override
      public void beforePropertyChange(@NotNull VirtualFilePropertyEvent event) {

      }

      @Override
      public void beforeContentsChange(@NotNull VirtualFileEvent event) {

      }

      @Override
      public void beforeFileDeletion(@NotNull VirtualFileEvent event) {

      }

      @Override
      public void beforeFileMovement(@NotNull VirtualFileMoveEvent event) {

      }
    });
  }

  private boolean isEmptyChartDir() {
    return myCharts == null || myCharts.length == 0;
  }

  private void initializeChartFiles() {
    myCurrentImageIndex = 0;
    myCharts = myChartsDirectory != null ? myChartsDirectory.getChildren() : null;
  }
}
