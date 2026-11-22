package com.eteks.sweethome3d;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import com.eteks.sweethome3d.model.*;
import com.eteks.sweethome3d.swing.*;
import com.eteks.sweethome3d.viewcontroller.*;
import com.lowagie.text.pdf.PdfReader;

/** Reproducible GUI acceptance checks, run separately from the provided tests. */
public class CaptureAcceptanceEvidence extends SweetHome3D {
  private final File root;
  private JFrame frame;
  private Home home;
  private HomeController controller;

  private CaptureAcceptanceEvidence(File root) { this.root = root; }

  public static void main(String[] args) {
    try { run(args); } catch (Exception ex) { ex.printStackTrace(); System.exit(1); }
  }

  private static void run(String[] args) throws Exception {
    System.setProperty("com.eteks.sweethome3d.no3D", "true");
    System.setProperty("com.eteks.sweethome3d.preferencesFolder", args[1]);
    CaptureAcceptanceEvidence app = new CaptureAcceptanceEvidence(new File(args[0]));
    app.getUserPreferences().setCheckUpdatesEnabled(false);
    Field updates = SweetHome3D.class.getDeclaredField("checkUpdatesNeeded");
    updates.setAccessible(true);
    updates.set(app, false);
    app.init(new String[0]);
    try {
      app.verify();
      System.out.println("GUI_ACCEPTANCE_PASS");
      System.exit(0);
    } catch (Throwable failure) {
      failure.printStackTrace();
      System.exit(1);
    }
  }

  private void edt(Runnable action) throws Exception { SwingUtilities.invokeAndWait(action); }
  private void check(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
    System.out.println("PASS: " + message);
  }
  private void ready() throws Exception {
    for (int attempt = 0; attempt < 100; attempt++) {
      edt(() -> {
        for (Home candidate : getHomes()) {
          if (getHomeFrameController(candidate) != null) {
            home = candidate;
            controller = getHomeFrameController(candidate).getHomeController();
            frame = getHomeFrame(candidate);
          }
        }
      });
      if (frame != null && frame.isShowing()) return;
      Thread.sleep(100);
    }
    throw new AssertionError("No application window");
  }
  private void capture(String name) throws Exception {
    Thread.sleep(300);
    edt(() -> {
      try {
        Rectangle bounds = frame.getBounds();
        java.util.List<Window> windows = new ArrayList<Window>();
        windows.add(frame);
        for (Window window : Window.getWindows()) {
          Window owner = window.getOwner();
          while (owner != null && owner != frame) owner = owner.getOwner();
          if (window.isShowing() && owner == frame) {
            windows.add(window);
            bounds = bounds.union(window.getBounds());
          }
        }
        BufferedImage image = new BufferedImage(bounds.width,bounds.height,BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE); graphics.fillRect(0,0,bounds.width,bounds.height);
        for (Window window : windows) {
          Graphics2D layer = (Graphics2D)graphics.create();
          layer.translate(window.getX()-bounds.x,window.getY()-bounds.y);
          window.printAll(layer); layer.dispose();
        }
        graphics.dispose();
        ImageIO.write(image,"png",new File(root,"submissions/"+name));
      } catch (IOException ex) { throw new RuntimeException(ex); }
    });
  }
  private java.util.List<Component> children(Component component) {
    java.util.List<Component> result = new ArrayList<Component>();
    result.add(component);
    if (component instanceof Container) {
      for (Component child : ((Container)component).getComponents()) result.addAll(children(child));
    }
    return result;
  }
  private JDialog dialog() {
    for (Window window : Window.getWindows()) {
      if (window instanceof JDialog && window.isShowing()) return (JDialog)window;
    }
    return null;
  }
  private void verify() throws Exception {

    ready();
    edt(() -> frame.setBounds(20, 40, 1250, 850));
    capture("task1-running.png");
    System.out.println("PASS: whole running application captured");

    edt(() -> controller.open(new File(root,"samples/MultiLevelHome.sh3d").getAbsolutePath()));
    for (int attempt=0; attempt<100; attempt++) {
      Thread.sleep(100);
      ready();
      if (home.getLevels().size() == 3) break;
    }
    check(home.getLevels().size() == 3, "Loaded the supplied three-level sample");
    edt(() -> frame.setBounds(20, 40, 1250, 850));
    HomePieceOfFurniture piece = home.getFurniture().get(0);
    edt(() -> home.setSelectedItems(Collections.singletonList(piece)));
    SwingUtilities.invokeLater(() -> controller.getFurnitureController().modifySelectedFurniture());
    for (int attempt=0; attempt<50 && dialog()==null; attempt++) Thread.sleep(100);
    check(dialog()!=null, "Modify furniture dialog opened");
    final String description = "Edited description for internship verification.";
    final JTextField[] descriptionField = new JTextField[1];
    edt(() -> {
      for (Component component : children(dialog())) {
        if (component instanceof HomeFurniturePanel) {
          try {
            Field field = HomeFurniturePanel.class.getDeclaredField("descriptionTextField");
            field.setAccessible(true);
            descriptionField[0] = (JTextField)field.get(component);
          } catch (Exception ex) { throw new RuntimeException(ex); }
        }
      }
      check(descriptionField[0]!=null && descriptionField[0].isEditable(), "Description field is editable");
      descriptionField[0].setText(description);
      descriptionField[0].setCaretPosition(0);
    });
    capture("task2-description.png");
    edt(() -> {
      for (Component component : children(dialog())) {
        if (component instanceof JButton && "OK".equals(((JButton)component).getText())) {
          ((JButton)component).doClick();
          return;
        }
      }
      throw new AssertionError("Missing OK button");
    });
    for (int attempt=0; attempt<50 && !description.equals(piece.getDescription()); attempt++) Thread.sleep(100);
    check(description.equals(piece.getDescription()), "Edited description saved to furniture object");

    FurnitureTable table = (FurnitureTable)controller.getFurnitureController().getView();
    edt(() -> {
      if (!home.getFurnitureVisibleProperties().contains(HomePieceOfFurniture.SortableProperty.VOLUME)) {
        controller.getFurnitureController().toggleFurnitureVisibleProperty(HomePieceOfFurniture.SortableProperty.VOLUME);
      }
      try {
        Object model = table.getModel();
        java.lang.reflect.Method expanded = model.getClass().getDeclaredMethod("isRowExpanded",int.class);
        java.lang.reflect.Method toggle = model.getClass().getDeclaredMethod("toggleRowExpandedState",int.class);
        expanded.setAccessible(true); toggle.setAccessible(true);
        for (int row=table.getRowCount()-1; row>=0; row--) {
          if ((Boolean)expanded.invoke(model,row)) toggle.invoke(model,row);
        }
      } catch (Exception ex) { throw new RuntimeException(ex); }
      for (Component component : children(frame)) {
        if (component instanceof JSplitPane && ((JSplitPane)component).getOrientation()==JSplitPane.HORIZONTAL_SPLIT) {
          ((JSplitPane)component).setDividerLocation(620);
        }
      }
      controller.getFurnitureController().sortFurniture(HomePieceOfFurniture.SortableProperty.VOLUME);
    });
    check(table.getColumnModel().getColumnIndex(HomePieceOfFurniture.SortableProperty.VOLUME)>=0, "Volume is displayed");
    check(home.getFurnitureSortedProperty()==HomePieceOfFurniture.SortableProperty.VOLUME, "Ascending Volume sort selected");
    verifySorted(table, false);
    edt(() -> controller.getFurnitureController().toggleFurnitureSortOrder());
    verifySorted(table, true);
    edt(() -> controller.getFurnitureController().toggleFurnitureSortOrder());
    capture("task3-volume-values.png");
    JPopupMenu popup = table.getComponentPopupMenu();
    check(popup!=null, "Furniture table has a context menu");
    edt(() -> {
      popup.show(table, Math.min(180,table.getWidth()-10), Math.min(100,table.getHeight()-10));
      for (Component component : popup.getComponents()) {
        if (component instanceof JMenu && ((JMenu)component).getText().toLowerCase().contains("sort")) {
          JMenu sort = (JMenu)component;
          MenuSelectionManager.defaultManager().setSelectedPath(new MenuElement[]{popup,sort,sort.getPopupMenu()});
          return;
        }
      }
    });
    capture("task3-volume.png");
    edt(() -> MenuSelectionManager.defaultManager().clearSelectedPath());
    // A normal top-level menu uses the same action maps as the context menu.
    JMenuBar bar = frame.getJMenuBar();
    check(hasMenuItem(bar, "Volume") >= 2, "Volume appears in Furniture sort and display menus");
    check(hasMenuItem(popup, "Volume") >= 2, "Volume appears in context sort and display menus");

    java.util.List<HomePieceOfFurniture.SortableProperty> originalColumns = new ArrayList<>(home.getFurnitureVisibleProperties());
    Level selected = home.getLevels().get(2);
    edt(() -> home.setSelectedLevel(selected));
    edt(() -> {
      try {
        Object model = table.getModel();
        java.lang.reflect.Method expanded = model.getClass().getDeclaredMethod("isRowExpanded",int.class);
        java.lang.reflect.Method toggle = model.getClass().getDeclaredMethod("toggleRowExpandedState",int.class);
        expanded.setAccessible(true); toggle.setAccessible(true);
        for (int row=0; row<table.getRowCount(); row++) {
          if (table.getValueAt(row,0) instanceof HomeFurnitureGroup && !(Boolean)expanded.invoke(model,row)) toggle.invoke(model,row);
        }
      } catch (Exception ex) { throw new RuntimeException(ex); }
    });
    ((HomePane)controller.getView()).printToPDF(new File(root,"submissions/task4-all-levels.pdf").getAbsolutePath());
    check(home.getSelectedLevel()==selected, "PDF printing restored the selected level");
    check(home.getFurnitureVisibleProperties().equals(originalColumns), "Printing preserved on-screen columns");
    PdfReader pdf = new PdfReader(new File(root,"submissions/task4-all-levels.pdf").getAbsolutePath());
    check(pdf.getNumberOfPages()==4, "PDF has one furniture page and three level plans");
    pdf.close();
    edt(() -> {
      // Check failure restoration, a hidden level, and negative / out-of-range requests.
      Home probe = new Home();
      for (int index=0; index<3; index++) {
        Level level = new Level("L"+index,250*index,10,240);
        probe.addLevel(level);
        Wall wall = new Wall(0,0,200,0,10,240); wall.setLevel(level); probe.addWall(wall);
      }
      probe.getLevels().get(1).setViewable(false);
      Level original = probe.getLevels().get(2); probe.setSelectedLevel(original);
      PlanComponent plan = new PlanComponent(probe,getUserPreferences(),null);
      java.awt.print.PageFormat format = new java.awt.print.PageFormat();
      Graphics2D graphics = new BufferedImage(800,1000,BufferedImage.TYPE_INT_RGB).createGraphics();
      try {
        check(plan.print(graphics,format,-1)==java.awt.print.Printable.NO_SUCH_PAGE, "Negative plan page rejected");
        check(plan.print(graphics,format,1)==java.awt.print.Printable.PAGE_EXISTS, "Hidden level skipped in page mapping");
        check(plan.print(graphics,format,2)==java.awt.print.Printable.NO_SUCH_PAGE, "Plan pages end after viewable levels");
        try { plan.print(null,format,0); throw new AssertionError("Expected rendering failure"); }
        catch (NullPointerException expected) { check(probe.getSelectedLevel()==original,"Selection restored on rendering failure"); }
      } finally { graphics.dispose(); }
    });
  }
  private void verifySorted(FurnitureTable table, boolean descending) {
    float previous = descending ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
    for (int row=0; row<table.getRowCount(); row++) {
      float volume = ((HomePieceOfFurniture)table.getValueAt(row,0)).getVolume();
      check(descending ? volume<=previous : volume>=previous, "Volume row order " + row + (descending?" descending":" ascending"));
      previous = volume;
    }
  }
  private int hasMenuItem(Component component, String text) {
    int count = component instanceof JMenuItem && text.equals(((JMenuItem)component).getText()) ? 1 : 0;
    if (component instanceof JMenu) {
      for (Component child : ((JMenu)component).getMenuComponents()) count += hasMenuItem(child,text);
    } else if (component instanceof Container) {
      for (Component child : ((Container)component).getComponents()) count += hasMenuItem(child,text);
    }
    return count;
  }
}
