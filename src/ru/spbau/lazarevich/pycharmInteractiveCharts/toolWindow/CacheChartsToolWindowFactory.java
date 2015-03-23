package ru.spbau.lazarevich.pycharmInteractiveCharts.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by Andrey
 */

public class CacheChartsToolWindowFactory implements ToolWindowFactory {
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

  public static final String ideaPath = ".idea";
  private static final String imageDir= "charts";
  private static final String missingDirectoryExceptionMessage = "Cannot read from charts directory";
  private static final String readingErrorFromDirectoryExceptionMessage = "IO error occurred during directory initialization";

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
  }

  private void drawNextImage() {
    drawImage(true);
  }

  private void drawPrevImage() {
    drawImage(false);
  }

  private void drawImage(boolean next) {
    if (myCharts == null || myCharts.length == 0) {
      return;
    }
    BufferedImage currentChart = null;
    try {
      checkFileListBoundaries();
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
    if (currentChart != null) {
      myChartLabel.setIcon(new ImageIcon(currentChart));
    }
  }

  private BufferedImage getPrevImage() throws IOException {
    BufferedImage currentChart = ImageIO.read(new File(myCharts[myCurrentImageIndex].getPath()));
    myCurrentImageIndex--;
    return currentChart;
  }

  private BufferedImage getNextImage() throws IOException {
    BufferedImage currentChart = ImageIO.read(new File(myCharts[myCurrentImageIndex].getPath()));
    myCurrentImageIndex++;
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
  }

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    myCacheChartsToolWindow = toolWindow;
    this.initializeDirectory(project);
    this.drawNextImage();
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
    myChartsDirectory.refresh(true, false, new Runnable() {
      @Override
      public void run() {
        myCharts = myChartsDirectory.getChildren();
      }
    });
  }
}
