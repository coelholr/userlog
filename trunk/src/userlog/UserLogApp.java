/*
 * UserLogApp.java
 */
package userlog;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class UserLogApp extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup() {
        setTempoInicial(System.currentTimeMillis());
        resourceMap = this.getContext().getResourceMap(UserLogApp.class);
        startTableModel();
        setupTrayIcon();
        userView = new UserLogView(this);
    }
    private UserLogTableModel tableModel;

    public UserLogTableModel getTableModel() {
        return tableModel;
    }

    public void setTableModel(UserLogTableModel tableModel) {
        this.tableModel = tableModel;
    }
    private UserLogView userView;

    private void showView() {
        show(this.userView);
    }

    @Override
    protected void shutdown() {
        showView();
        salvaTempoDecorrido();
    }

    private void salvaTempoDecorrido() {
        Properties prop = new Properties();
        long tempoFinal = System.currentTimeMillis();
        long tempoDecorrido = tempoFinal - tempoInicial;
        File logFile = new File((new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath())).getParent() + File.separator + "userlog.xml");

        Date now = new Date();
        DateFormat df1 = DateFormat.getDateInstance(DateFormat.FULL, new Locale("pt", "BR"));

        DateFormat df2 = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT, new Locale("pt", "BR"));
        df2.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));

        try {
            prop.loadFromXML(new FileInputStream(logFile));
        } catch (IOException ioe) {
        }
        if (prop.getProperty(System.getProperty("user.name")) != null) {
            tempoDecorrido = tempoDecorrido + new Long(prop.getProperty(System.getProperty("user.name")));
        }

        prop.setProperty(System.getProperty("user.name"), Long.valueOf(tempoDecorrido).toString());

        try {
            prop.storeToXML(new FileOutputStream(logFile), df2.format(now));
        } catch (IOException ioe) {
        }
    }

    private Properties getSavedProperty() {
        Properties prop = new Properties();
        File logFile = new File((new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath())).getParent() + File.separator + "userlog.xml");

        try {
            prop.loadFromXML(new FileInputStream(logFile));
        } catch (IOException ioe) {
        }

        if (prop.isEmpty()) {
            prop.setProperty(System.getProperty("user.name"), "0");
        }
        return prop;
    }

    private void startTableModel() {
        tableModel = new UserLogTableModel();
        Properties prop = getSavedProperty();
        String[] columnNames = {"Nome do usuário", "Tempo de utilização (ms)"};
        Object[][]rowData = new Object[prop.size()][2];
        Integer[] tempo = new Integer[prop.size()];
        String[] usuario = prop.keySet().toArray(new String[0]);
        for (int i = 0; prop.size() > i; i++) {
            tempo[i] = new Integer(prop.getProperty(usuario[i]));
            rowData[i][0] = usuario[i];
            rowData[i][1] = tempo[i];
        }
        tableModel.setColumnNames(columnNames);
        tableModel.setData(rowData);
    }

    private void setupTrayIcon() {
        TrayIcon trayIcon = null;
        if (SystemTray.isSupported()) {
            // get the SystemTray instance
            SystemTray tray = SystemTray.getSystemTray();
            // load an image
            Image image = Toolkit.getDefaultToolkit().getImage("tray.gif"); // create a action listener to listen for default action executed on the tray icon
            ActionListener listener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    Application.getInstance(userlog.UserLogApp.class).showView();
                }
            };
            ActionListener exitListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    Application.getInstance(userlog.UserLogApp.class).quit(null);
                }
            };
            ActionListener showViewListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    Application.getInstance(userlog.UserLogApp.class).showView();
                }
            };
            // create a popup menu
            PopupMenu popup = new PopupMenu();
            // create menu item for the default action
            MenuItem sairItem = new MenuItem(resourceMap.getString("Application.sair"));
            sairItem.addActionListener(exitListener);
            MenuItem mostrarItem = new MenuItem(resourceMap.getString("Application.mostrar"));
            mostrarItem.addActionListener(showViewListener);
            popup.add(sairItem);
            popup.add(mostrarItem);
            /// ... add other items
            // construct a TrayIcon
            trayIcon = new TrayIcon(image, "Tray Demo", popup);
            // set the TrayIcon properties
            trayIcon.addActionListener(listener);
            // ...
            // add the tray image
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println(e);
            }
        // ...
        }
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override
    protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of UserLogApp
     */
    public static UserLogApp getApplication() {
        return Application.getInstance(UserLogApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(UserLogApp.class, args);
    }
    private ResourceMap resourceMap;

    public ResourceMap getResourceMap() {
        return resourceMap;
    }

    public void setResourceMap(ResourceMap resourceMap) {
        this.resourceMap = resourceMap;
    }
    private long tempoInicial;

    public long getTempoInicial() {
        return tempoInicial;
    }

    public void setTempoInicial(long tempoInicial) {
        this.tempoInicial = tempoInicial;
    }
}
