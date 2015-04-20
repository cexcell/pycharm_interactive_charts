package ru.spbau.lazarevich.pycharmInteractiveCharts.toolWindow;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class WidgetManager {
  private VirtualFile myDataDirectory;
  private int myCurrentFuncId;
  private ArrayList<Integer> myRelatedCharts = new ArrayList<>();

  WidgetManager(VirtualFile directory) {
    myDataDirectory = directory;
  }

  public ArrayList<Component> renderWidgets(int imageIndex) throws IOException {
    VirtualFile widgetDataFile = myDataDirectory.findChild(composeName(imageIndex));
    if (widgetDataFile != null) {
      String jsonString = getJsonStringFromStream(widgetDataFile);
      JsonElement jsonElement = new JsonParser().parse(jsonString);
      JsonObject jsonObject = jsonElement.getAsJsonObject();
      myCurrentFuncId = jsonObject.get("funcId").getAsInt();
      JsonArray relatedCharts = jsonObject.getAsJsonArray("relatedCharts");
      myRelatedCharts = new ArrayList<>();
      for (JsonElement relatedChart : relatedCharts) {
        myRelatedCharts.add(relatedChart.getAsInt());
      }
      int widgetsNumber = jsonObject.get("widgetsNumber").getAsInt();
      ArrayList<Component> widgets = new ArrayList<>();
      for (int i = 0; i < widgetsNumber; i++) {
        widgets.add(getWidgetToPanel(jsonObject.getAsJsonObject("widget" + String.valueOf(i))));
      }
      return widgets;
    }
    return null;
  }

  private Component getWidgetToPanel(JsonObject widget) {
    String type = widget.get("widgetType").getAsString();
    switch (type) {
      case "WidgetInt":
        return getIntWidget(widget);
      case "WidgetText":
        return getTextWidget(widget);
      case "WidgetBool":
        return addBoolWidget(widget);
    }
    return null;
  }

  private Component addBoolWidget(JsonObject widgetBool) {
    return null;
  }

  private Component getTextWidget(JsonObject widgetText) {
    return null;
  }

  private JSlider getIntWidget(JsonObject widgetInt) {
    int min = widgetInt.get("min").getAsInt();
    int max = widgetInt.get("max").getAsInt();
    int step = widgetInt.get("step").getAsInt();
    int value = widgetInt.get("value").getAsInt();
    JSlider widget = new JSlider(JSlider.HORIZONTAL, min, max, value);
    //widget.addChangeListener(new ChangeListener() {
    //  @Override
    //  public void stateChanged(ChangeEvent event) {
    //
    //  }
    //});
    widget.setName(widgetInt.get("name").getAsString());
    widget.setMajorTickSpacing(step);
    widget.setMinorTickSpacing(1);
    widget.setPaintLabels(true);
    widget.setPaintTicks(true);
    return widget;
  }

  private String getJsonStringFromStream(VirtualFile widgetDataFile) throws IOException {
    StringBuilder jsonStringBuilder = new StringBuilder();
    try (BufferedReader inputStream = new BufferedReader(new InputStreamReader(widgetDataFile.getInputStream()))) {
      String inputStr;
      while ((inputStr = inputStream.readLine()) != null) {
        jsonStringBuilder.append(inputStr);
      }
      return jsonStringBuilder.toString();
    }
  }

  private String composeName(int index) {
    String namePrefix = "picharts";
    String extension = ".dat";
    return namePrefix + String.valueOf(index) + extension;
  }

  public void clear() {

  }
}
