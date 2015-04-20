package ru.spbau.lazarevich.pycharmInteractiveCharts.toolWindow;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectCoreUtil;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

public class CacheChartsToolWindowFactory implements ToolWindowFactory {
  private static final String ourIdeaPath = ProjectCoreUtil.DIRECTORY_BASED_PROJECT_DIR;
  private static final String ourChartsDir = "charts";
  private static final String ourMissingDirectoryExceptionMessage = "Cannot read from charts directory";
  private static final String ourReadingErrorFromDirectoryExceptionMessage = "IO error occurred during directory initialization";
  private JButton myPreviousImageButton;
  private JButton myNextImageButton;
  private JButton myClearImageButton;
  private JPanel myCacheChartsToolWindowContent;
  private JPanel myChartViewer;
  private JLabel myChartLabel;
  private JPanel myWidgetViewer;
  private ToolWindow myCacheChartsToolWindow;
  private ChartsManager myChartsManager;
  private WidgetManager myWidgetManager;
  private ArrayList<Component> myWidgets;
  private static final Logger LOG = Logger.getInstance(CacheChartsToolWindowFactory.class.getName());


  public CacheChartsToolWindowFactory() {
    myNextImageButton.setIcon(AllIcons.Actions.Forward);
    myNextImageButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        CacheChartsToolWindowFactory.this.drawNextImage();
      }
    });
    myClearImageButton.setIcon(AllIcons.Actions.Refresh);
    myClearImageButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        CacheChartsToolWindowFactory.this.clearImages();
      }
    });
    myPreviousImageButton.setIcon(AllIcons.Actions.Back);
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
    myWidgetViewer.setLayout(
      new BoxLayout(myWidgetViewer, BoxLayout.Y_AXIS));
  }

  private void renderWidgets(int idx) throws IOException {
    int verticalMargin = 5;
    myWidgetViewer.removeAll();
    myWidgets = myWidgetManager.renderWidgets(myChartsManager.getCurrentImageIndex());
    for (Component widget : myWidgets) {
      JLabel componentLabel = new JLabel(widget.getName(), JLabel.CENTER);
      myWidgetViewer.add(componentLabel);
      myWidgetViewer.add(widget);
      myWidgetViewer.add(Box.createVerticalStrut(verticalMargin));
      myWidgetViewer.revalidate();
      myWidgetViewer.repaint();
    }
  }

  private void resizeAndDrawCurrentImage() {
    recalculateImageScale();
    try {
      setIcon(myChartsManager.redrawCurrentImage());
      renderWidgets(myChartsManager.getCurrentImageIndex());
    }
    catch (IOException e) {
      LOG.warn(ourMissingDirectoryExceptionMessage + e.getMessage());
    }
  }

  private void setIcon(BufferedImage currentChart) {
    if (currentChart != null) {
      myChartLabel.setIcon(new ImageIcon(currentChart));
    }
  }

  private void recalculateImageScale() {
    int margin = 10;
    int newWidth = myCacheChartsToolWindowContent.getWidth() - 2 * margin;
    myChartsManager.setScale(newWidth);
  }

  private void drawNextImage() {
    try {
      setIcon(myChartsManager.drawNext());
      renderWidgets(myChartsManager.getCurrentImageIndex());
    }
    catch (IOException e) {
      LOG.warn(ourMissingDirectoryExceptionMessage + e.getMessage());
    }
  }

  private void drawPrevImage() {
    try {
      setIcon(myChartsManager.drawPrev());
      renderWidgets(myChartsManager.getCurrentImageIndex());
    }
    catch (IOException e) {
      LOG.warn(ourMissingDirectoryExceptionMessage + e.getMessage());
    }
  }


  private void clearImages() {
    myChartLabel.setIcon(null);
    myWidgetManager.clear();
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
            LOG.warn("Cannot remove image: " + chart.getName());
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
    //resizeAndDrawCurrentImage();
    final ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
    Content content = contentFactory.createContent(myCacheChartsToolWindowContent, "", false);
    toolWindow.getContentManager().addContent(content);
  }

  private void initializeDirectory(@NotNull Project project) {
    VirtualFile ideaDir = project.getBaseDir().findChild(ourIdeaPath);
    if (ideaDir == null) {
      return;
    }
    try {
      myChartsManager = new ChartsManager(ideaDir, ourChartsDir);
      myWidgetManager = new WidgetManager(myChartsManager.getChartsDirectory());
    }
    catch (IOException e) {
      LOG.error(ourReadingErrorFromDirectoryExceptionMessage + e.getMessage());
    }
    setListner();
  }

  private void setListner() {
    VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileListener() {
      @Override
      public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {
      }

      @Override
      public void contentsChanged(@NotNull VirtualFileEvent event) {
        myChartsManager.initializeChartFiles();
      }

      @Override
      public void fileCreated(@NotNull VirtualFileEvent event) {
        try {
          myChartsManager.initializeChartFiles();
          setIcon(myChartsManager.redrawCurrentImage());
          renderWidgets(myChartsManager.getCurrentImageIndex());
        }
        catch (IOException e) {
          LOG.warn(ourMissingDirectoryExceptionMessage + e.getMessage());
        }
      }

      @Override
      public void fileMoved(@NotNull VirtualFileMoveEvent event) {
        try {
          setIcon(myChartsManager.redrawAfterMoved());
          renderWidgets(myChartsManager.getCurrentImageIndex());
        }
        catch (IOException e) {
          LOG.warn(ourMissingDirectoryExceptionMessage + e.getMessage());
        }
      }

      @Override
      public void fileDeleted(@NotNull VirtualFileEvent event) {
        setIcon(null);
        myChartsManager.initializeChartFiles();
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
}
