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
  //TODO: add my to private fields
  private JButton myPreviousImageButton;
  private JButton myNextImageButton;
  private JButton myClearImageButton;
  private JPanel myCacheChartsToolWindowContent;
  private JPanel myChartViewer;
  private JLabel myChartLabel;
  private ToolWindow myCacheChartsToolWindow;
  private VirtualFile myChartsDirectory;
  private VirtualFile[] myVirtualFiles;
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
  }

  private void drawNextImage() {
    drawImage(true);
  }

  private void drawPrevImage() {
    drawImage(false);
  }

  private void drawImage(boolean next) {
    if (myVirtualFiles.length == 0) {
      return;
    }
    BufferedImage currentChart = null;
    try {
      checkFileListBoundaries();
      if (next) {
        currentChart = ImageIO.read(new File(myVirtualFiles[myCurrentImageIndex].getPath()));
        myCurrentImageIndex++;
      }
      else {
        currentChart = ImageIO.read(new File(myVirtualFiles[myCurrentImageIndex].getPath()));
        myCurrentImageIndex--;
      }
    }
    catch (IOException e) {
      System.err.println("Cannot read from charts directory: " + e.getMessage());
    }
    if (currentChart != null) {
      myChartLabel.setIcon(new ImageIcon(currentChart));
    }
  }

  private void checkFileListBoundaries() {
    if (myCurrentImageIndex >= myVirtualFiles.length) {
      myCurrentImageIndex = 0;
    }
    if (myCurrentImageIndex < 0) {
      myCurrentImageIndex = myVirtualFiles.length - 1;
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
    VirtualFile baseDir = project.getBaseDir();
    if ((myChartsDirectory = baseDir.findChild(".charts")) == null) {
      try {
        myChartsDirectory = baseDir.createChildDirectory(this, ".charts");
      }
      catch (IOException e) {
        System.err.println("IO error occurred during directory initialization: " + e.getMessage());
      }
    }
    myVirtualFiles = myChartsDirectory.getChildren();
  }
}
