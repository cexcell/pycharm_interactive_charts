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
    private JButton previousImageButton;
    private JButton nextImageButton;
    private JButton clearImageButton;
    private JPanel cacheChartsToolWindowContent;
    private JPanel chartViewer;
    private JLabel chartLabel;
    private ToolWindow cacheChartsToolWindow;
    private VirtualFile chartsDirectory;
    private VirtualFile[] imageFiles;
    private int currentImageIndex;

    public CacheChartsToolWindowFactory() {
        currentImageIndex = 0;
        nextImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CacheChartsToolWindowFactory.this.drawNextImage();
            }
        });
        clearImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CacheChartsToolWindowFactory.this.clearImage();
            }
        });
        previousImageButton.addActionListener(new ActionListener() {
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
        if (imageFiles.length == 0) {
            return;
        }
        BufferedImage currentChart = null;
        try {
            checkFileListBoundaries();
            if (next) {
                currentChart = ImageIO.read(new File(imageFiles[currentImageIndex].getPath()));
                currentImageIndex++;
            } else {
                currentChart = ImageIO.read(new File(imageFiles[currentImageIndex].getPath()));
                currentImageIndex--;
            }
        } catch (IOException e) {
            System.err.println("Cannot read from charts directory: " + e.getMessage());
        }
        if (currentChart != null) {
            chartLabel.setIcon(new ImageIcon(currentChart));
        }
    }

    private void checkFileListBoundaries() {
        if (currentImageIndex >= imageFiles.length) {
            currentImageIndex = 0;
        }
        if (currentImageIndex < 0) {
            currentImageIndex = imageFiles.length - 1;
        }
    }

    private void clearImage() {
        chartLabel.setIcon(null);
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        cacheChartsToolWindow = toolWindow;
        this.initializeDirectory(project);
        this.drawNextImage();
        final ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(cacheChartsToolWindowContent, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    private void initializeDirectory(@NotNull Project project) {
        VirtualFile baseDir = project.getBaseDir();
        if ((chartsDirectory = baseDir.findChild(".charts")) == null) {
            try {
                chartsDirectory = baseDir.createChildDirectory(this, ".charts");
            } catch (IOException e) {
                System.err.println("IO error occurred during directory initialization: " + e.getMessage());
            }
        }
        imageFiles = chartsDirectory.getChildren();
    }
}
