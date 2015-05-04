package ru.spbau.lazarevich.pycharmInteractiveCharts.toolWindow;

import com.google.gson.JsonObject;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class CacheChartsToolWindowFactory implements ToolWindowFactory {
  private static final String ourIdeaPath = ProjectCoreUtil.DIRECTORY_BASED_PROJECT_DIR;
  private static final String ourChartsDir = "charts";
  private static final String ourDatExtension = "dat";
  private static final String ourChartExtension = "png";
  private static final String ourMissingDirectoryExceptionMessage = "Cannot read from charts directory: ";
  private static final String ourReadingErrorFromDirectoryExceptionMessage = "IO error occurred during directory initialization: ";
  private static final Logger LOG = Logger.getInstance(CacheChartsToolWindowFactory.class.getName());
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

  private void sendWidgetInfo(JsonObject jsonObject) {
    Socket socket = null;
    try {
      int ourPortNumber = 4000;
      String ourHost = "localhost";
      socket = new Socket(ourHost, ourPortNumber);
      socket.getOutputStream().write(jsonObject.toString().getBytes());
      byte[] buf = new byte[1024];
      int r = socket.getInputStream().read(buf);
      if (r != -1) {
        String data = new String(buf, 0, r);
        if (data.equals("OK")) {
          resizeAndDrawCurrentImage();
        }
      }
    }
    catch (IOException e) {
      String ourCannotSendJsonExceptionMessage = "Cannot send information to server via socket.";
      LOG.warn(ourCannotSendJsonExceptionMessage + e.getMessage());
    }
    finally {
      WidgetManager.closeSocket(socket);
    }
  }

  private void renderWidgets() throws IOException {
    if (!myChartsManager.isAvailable()) {
      return;
    }
    int verticalMargin = 5;
    myWidgetViewer.removeAll();
    ArrayList<Component> widgets = myWidgetManager.renderWidgets(myChartsManager.getCurrentImageIndex());
    if (widgets == null) {
      return;
    }
    for (Component widget : widgets) {
      JLabel componentLabel = new JLabel(widget.getName(), SwingConstants.CENTER);
      myWidgetViewer.add(componentLabel);
      setListner(widget);
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
      //renderWidgets();
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
      renderWidgets();
    }
    catch (IOException e) {
      LOG.warn(ourMissingDirectoryExceptionMessage + e.getMessage());
    }
  }

  private void drawPrevImage() {
    try {
      setIcon(myChartsManager.drawPrev());
      renderWidgets();
    }
    catch (IOException e) {
      LOG.warn(ourMissingDirectoryExceptionMessage + e.getMessage());
    }
  }

  private void clearImages() {
    myChartLabel.setIcon(null);
    myWidgetManager.clear();
    myWidgetViewer.removeAll();
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        ArrayList<VirtualFile> charts = myChartsManager.getCharts();
        for (VirtualFile chart : charts) {
          try {
            String associatedDatFileName = chart.getName();
            associatedDatFileName = associatedDatFileName.substring(0, associatedDatFileName.length() - 3) +
                                    ourDatExtension;
            VirtualFile associatedDatFile = myChartsManager.getChartsDirectory().findChild(associatedDatFileName);
            if (associatedDatFile != null) {
              associatedDatFile.delete(this);
            }
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
          VirtualFile file = event.getFile();
          String ext = file.getExtension();
          if (ext != null && ext.equals(ourChartExtension)) {
            myChartsManager.refreshChartFiles();
            setIcon(myChartsManager.redrawCurrentImage());
          }
          if (ext != null && ext.equals(ourDatExtension)) {
            renderWidgets();
          }
        }
        catch (IOException e) {
          LOG.warn(ourMissingDirectoryExceptionMessage + e.getMessage());
        }
      }

      @Override
      public void fileMoved(@NotNull VirtualFileMoveEvent event) {
      }

      @Override
      public void fileDeleted(@NotNull VirtualFileEvent event) {
        VirtualFile file = event.getFile();
        String ext = file.getExtension();
        if (ext != null && ext.equals(ourChartExtension)) {
          setIcon(null);
        }
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

  private void setListner(Component component) {
    if (component instanceof JSlider) {
      JSlider widget = (JSlider)component;
      widget.addChangeListener(new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent event) {
          JSlider widget = (JSlider)event.getSource();
          if (widget.getValueIsAdjusting()) {
            JsonObject jsonObject = CacheChartsToolWindowFactory.this.myWidgetManager.collectBasicJsonInfo(widget);
            sendWidgetInfo(jsonObject);
          }
        }
      });
    }
    if (component instanceof JCheckBox) {
      JCheckBox widget = (JCheckBox)component;
      widget.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent event) {
          JCheckBox widget = (JCheckBox)event.getSource();
          JsonObject jsonObject = CacheChartsToolWindowFactory.this.myWidgetManager.collectBasicJsonInfo(widget);
          sendWidgetInfo(jsonObject);
        }
      });
    }
    if (component instanceof JTextArea) {
      //TODO: implement
    }
  }
}
