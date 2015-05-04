package ru.spbau.lazarevich.pycharmInteractiveCharts.toolWindow;

import com.google.gson.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;

public class WidgetManager {
  private static final Logger LOG = Logger.getInstance(CacheChartsToolWindowFactory.class.getName());
  private static final int floatPrecise = 1000;
  private VirtualFile myDataDirectory;
  private int myCurrentFuncId;
  private ArrayList<Integer> myRelatedCharts = new ArrayList<Integer>();

  WidgetManager(VirtualFile directory) {
    myDataDirectory = directory;
  }

  private static Component getWidgetToPanel(JsonObject widget) {
    String type = widget.get("widgetType").getAsString();
    if (type.equals("WidgetFloat")) {
      return getFloatWidget(widget);
    }
    if (type.equals("WidgetInt")) {
      return getIntWidget(widget);
    }
    else if (type.equals("WidgetText")) {
      return getTextWidget(widget);
    }
    else if (type.equals("WidgetBool")) {
      return getBoolWidget(widget);
    }
    return null;
  }

  //private boolean isHostAvailable() {
  //  Socket s = null;
  //  try {
  //    s = new Socket(ourHost, ourPortNumber);
  //    return true;
  //  }
  //  catch (IOException e) {
  //    return false;
  //  }
  //  finally {
  //    closeSocket(s);
  //  }
  //}

  private static Component getBoolWidget(JsonObject widgetBool) {
    boolean val = widgetBool.get("value").getAsBoolean();
    String name = widgetBool.get("name").getAsString();
    final JCheckBox widget = new JCheckBox();
    widget.setSelected(val);
    widget.setName(name);
    return widget;
  }

  private static Component getTextWidget(JsonObject widgetText) {
    //TODO: implement this
    return null;
  }

  private static JSlider getFloatWidget(JsonObject widgetFloat) {
    float minf = widgetFloat.get("min").getAsFloat();
    float maxf = widgetFloat.get("max").getAsFloat();
    int min = Math.round(minf * floatPrecise) ;
    int max = Math.round(maxf * floatPrecise);
    // Ignore step for a better times
    int step = Math.round(widgetFloat.get("step").getAsFloat() * floatPrecise);
    float valuef = widgetFloat.get("value").getAsFloat();
    int value = Math.round(valuef * floatPrecise);
    final JSlider widget = new JSlider(SwingConstants.HORIZONTAL, min, max, value);
    widget.setExtent(0);
    widget.setName(widgetFloat.get("name").getAsString());
    Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
    table.put(0, new JLabel(String.valueOf(minf)));
    table.put(50, new JLabel(String.valueOf(valuef)));
    table.put(100, new JLabel(String.valueOf(maxf)));
    widget.setLabelTable(table);
    widget.setPaintLabels(true);
    widget.setSnapToTicks(true);
    //widget.setMajorTickSpacing(step);
    //widget.setMinorTickSpacing(1);
    //widget.setPaintLabels(true);
    //widget.setPaintTicks(true);
    return widget;
  }

  private static JSlider getIntWidget(JsonObject widgetInt) {
    int min = widgetInt.get("min").getAsInt();
    int max = widgetInt.get("max").getAsInt();
    int step = widgetInt.get("step").getAsInt();
    int value = widgetInt.get("value").getAsInt();
    final JSlider widget = new JSlider(SwingConstants.HORIZONTAL, min, max, value);
    widget.setExtent(1);
    widget.setName(widgetInt.get("name").getAsString());
    widget.setMajorTickSpacing(step);
    widget.setMinorTickSpacing(1);
    widget.setPaintLabels(true);
    widget.setPaintTicks(true);
    return widget;
  }

  private static String getType(Component component) {
    if (component instanceof JSlider) {
      if (isFloatSlider((JSlider)component)) {
        return "WidgetFloat";
      }
      return "WidgetInt";
    }
    if (component instanceof JCheckBox) {
      return "WidgetBool";
    }
    if (component instanceof JTextArea) {
      return "WidgetText";
    }
    return null;
  }

  private static String getValue(Component component) {
    if (component instanceof JSlider) {
      if (isFloatSlider((JSlider)component)) {
        // I need this "trick" because swing has no built-in slider for float values
        return String.valueOf((float) ((JSlider)component).getValue() / floatPrecise);
      }
      return String.valueOf(((JSlider)component).getValue());
    }
    if (component instanceof JCheckBox) {
      return String.valueOf(((JCheckBox)component).isSelected());
    }
    if (component instanceof JTextArea) {
      return ((JTextArea)component).getText();
    }
    return null;
  }

  private static boolean isFloatSlider(JSlider component) {
    return component.getExtent() == 0;
  }

  public static void closeSocket(Socket socket) {
    if (socket != null) {
      try {
        socket.close();
      }
      catch (IOException e) {
        String ourCannotCloseSocketExceptionMessage = "Cannot close socket: ";
        LOG.warn(ourCannotCloseSocketExceptionMessage + e.getMessage());
      }
    }
  }

  private static String getJsonStringFromStream(VirtualFile widgetDataFile) throws IOException {
    StringBuilder jsonStringBuilder = new StringBuilder();
    BufferedReader inputStream;
    inputStream = new BufferedReader(new InputStreamReader(widgetDataFile.getInputStream()));
    try {
      String inputStr;
      while ((inputStr = inputStream.readLine()) != null) {
        jsonStringBuilder.append(inputStr);
      }
      return jsonStringBuilder.toString();
    }
    finally {
      inputStream.close();
    }
  }

  private static String composeName(int index) {
    String namePrefix = "picharts";
    String extension = ".dat";
    return namePrefix + String.valueOf(index) + extension;
  }

  public ArrayList<Component> renderWidgets(int imageIndex) throws IOException {
    //if (!isHostAvailable()) {
    //  return null;
    //}
    VirtualFile widgetDataFile = myDataDirectory.findChild(composeName(imageIndex));
    if (widgetDataFile != null) {
      String jsonString = getJsonStringFromStream(widgetDataFile);
      JsonElement jsonElement = new JsonParser().parse(jsonString);
      JsonObject jsonObject = jsonElement.getAsJsonObject();
      myCurrentFuncId = jsonObject.get("funcId").getAsInt();
      JsonArray relatedCharts = jsonObject.getAsJsonArray("relatedCharts");
      myRelatedCharts = new ArrayList<Integer>();
      for (JsonElement relatedChart : relatedCharts) {
        myRelatedCharts.add(relatedChart.getAsInt());
      }
      int widgetsNumber = jsonObject.get("widgetsNumber").getAsInt();
      ArrayList<Component> widgets = new ArrayList<Component>();
      for (int i = 0; i < widgetsNumber; i++) {
        widgets.add(getWidgetToPanel(jsonObject.getAsJsonObject("widget" + String.valueOf(i))));
      }
      return widgets;
    }
    return null;
  }

  public JsonObject collectBasicJsonInfo(Component widget) {
    JsonObject jsonObject = new JsonObject();
    Gson gson = new Gson();
    jsonObject.addProperty("funcId", myCurrentFuncId);
    jsonObject.addProperty("relatedCharts", gson.toJson(myRelatedCharts));
    JsonObject argJson = new JsonObject();
    argJson.addProperty("type", getType(widget));
    argJson.addProperty("name", widget.getName());
    argJson.addProperty("value", getValue(widget));
    jsonObject.addProperty("arg", argJson.toString());
    return jsonObject;
  }

  public void clear() {
    //TODO: add some logic here or delete method.
  }
}
