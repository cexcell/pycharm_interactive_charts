package ru.spbau.lazarevich.pycharmInteractiveCharts.toolWindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.vfs.newvfs.impl.VirtualDirectoryImpl;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Andrey
 */

public class CacheChartsToolWindowFactory implements ToolWindowFactory {
  private int chartHeight = 240;
  private int chartWidth = 320;
  private static int margin = 10;
  private static final String ideaPath = ".idea";
  private static final String chartsDir = "charts";
  private static final String missingDirectoryExceptionMessage = "Cannot read from charts directory";
  private static final String readingErrorFromDirectoryExceptionMessage = "IO error occurred during directory initialization";
  private JButton myPreviousImageButton;
  private JButton myNextImageButton;
  private JButton myClearImageButton;
  private JPanel myCacheChartsToolWindowContent;
  private JPanel myChartViewer;
  private JLabel myChartLabel;
  private ToolWindow myCacheChartsToolWindow;
  private ChartsManager myChartsManager;

  public CacheChartsToolWindowFactory() {
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
    try {
      myChartsManager.redrawCurrentImage();
    }
    catch (IOException e) {
      System.err.println(missingDirectoryExceptionMessage + e.getMessage());
    }
  }

  private void setIcon(BufferedImage currentChart) {
    if (currentChart != null) {
      myChartLabel.setIcon(new ImageIcon(currentChart));
    }
  }

  private void recalculateImageScale() {
    int newWidth = myCacheChartsToolWindowContent.getWidth() - 2 * margin;
    myChartsManager.setScale(newWidth);
  }

  private void drawNextImage() {
    try {
      setIcon(myChartsManager.drawNext());
    }
    catch (IOException e) {
      System.err.println(missingDirectoryExceptionMessage + e.getMessage());
    }
  }

  private void drawPrevImage() {
    try {
      setIcon(myChartsManager.drawPrev());
    }
    catch (IOException e) {
      System.err.println(missingDirectoryExceptionMessage + e.getMessage());
    }
  }


  private void clearImage() {
    myChartLabel.setIcon(null);
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        ArrayList<VirtualFile> charts = myChartsManager.getCharts();
        myChartsManager.clear();
        for (VirtualFile chart : charts) {
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
    myChartsManager.initializeChartFiles();
    resizeAndDrawCurrentImage();
    final ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
    Content content = contentFactory.createContent(myCacheChartsToolWindowContent, "", false);
    toolWindow.getContentManager().addContent(content);
  }

  private void initializeDirectory(@NotNull Project project) {
    VirtualFile ideaDir = project.getBaseDir().findChild(ideaPath);
    if (ideaDir == null) {
      return;
    }
    try {
      myChartsManager = new ChartsManager(ideaDir, chartsDir);
    }
    catch (IOException e) {
      System.err.println(readingErrorFromDirectoryExceptionMessage + e.getMessage());
    }
    setListner();
  }

  private void setListner() {
    VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileListener() {
      @Override
      public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {}

      @Override
      public void contentsChanged(@NotNull VirtualFileEvent event) {
        myChartsManager.initializeChartFiles();
      }

      @Override
      public void fileCreated(@NotNull VirtualFileEvent event) {
        try {
          myChartsManager.initializeChartFiles();
          setIcon(myChartsManager.redrawCurrentImage());
        }
        catch (IOException e) {
          System.err.println(missingDirectoryExceptionMessage + e.getMessage());
        }
      }

      @Override
      public void fileMoved(@NotNull VirtualFileMoveEvent event) {
        try {
          setIcon(myChartsManager.redrawAfterMoved());
        }
        catch (IOException e) {
          System.err.println(missingDirectoryExceptionMessage + e.getMessage());
        }
      }

      @Override
      public void fileDeleted(@NotNull VirtualFileEvent event) {}

      @Override
      public void fileCopied(@NotNull VirtualFileCopyEvent event) {}

      @Override
      public void beforePropertyChange(@NotNull VirtualFilePropertyEvent event) {}

      @Override
      public void beforeContentsChange(@NotNull VirtualFileEvent event) {}

      @Override
      public void beforeFileDeletion(@NotNull VirtualFileEvent event) {}

      @Override
      public void beforeFileMovement(@NotNull VirtualFileMoveEvent event) {}
    });
  }
}
