package mgplugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    public static final String DBIO_GEN_VIEW_ID = "mgplugin.views.DBIOGenViewId";
    public static final String MG_PLUGIN_CONSOLE = "MgPlugin";
    
    private static final Properties    PLUGIN_PROP    = new Properties();
    private static       Configuration TEMPLAT_CONFIG = null;
    private static Connection CONNECTION = null;
    
    /* ============================================================================================================== */
    // https://wiki.eclipse.org/FAQ_How_do_I_write_to_the_console_from_a_plug-in%3F
    private static MessageConsole findConsole(String name) {
        ConsolePlugin plugin = ConsolePlugin.getDefault();
        IConsoleManager conMan = plugin.getConsoleManager();
        IConsole[] existing = conMan.getConsoles();
        for (int i = 0; i < existing.length; i++) {
            if (name.equals(existing[i].getName())) {
                return (MessageConsole) existing[i];
            }
        }
        // no console found, so create a new one
        MessageConsole myConsole = new MessageConsole(name, null);
        conMan.addConsoles(new IConsole[] { myConsole });
        return myConsole;
    }
    
    // http://www.javased.com/index.php?source_dir=js4emf/org.eclipse.emf.js4emf.ui/src/org/eclipse/emf/js4emf/ui/Activator.java
    public MessageConsole getConsole(String name) {
        IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
        IConsole[] existing = consoleManager.getConsoles();
        for (int i = 0; i < existing.length; i++) {
            if (name.equals(existing[i].getName())) {
                return (MessageConsole) existing[i];
            }
        }
        // no console found, so create a new one
        MessageConsole console = new MessageConsole(name, null);
        consoleManager.addConsoles(new IConsole[] { console });
        return console;
    }

    public void showConsole(String name) {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        try {
            IConsoleView view = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
            view.display(getConsole(name));
        } catch (PartInitException e) {
        }
    }
    
    
    
    public static void console(String msg) {
        try {
            MessageConsole myConsole = findConsole(MG_PLUGIN_CONSOLE);
            MessageConsoleStream out = myConsole.newMessageStream();
            out.println(msg);
        } catch (Exception e) {}
        System.out.println(msg);
    }
    
    public static void console(Throwable ex) {
        console(ex.toString());
    }
    
    public static Connection getConnection() {
        try {
            if ( CONNECTION == null || CONNECTION.isClosed() ) {
                console("Connection db...");
                CONNECTION = DriverManager.getConnection(PLUGIN_PROP.getProperty("db.connection"));
            }
            return CONNECTION;
        } catch (SQLException e) {
            e.printStackTrace();
            console("접속정보 리셋, 재접속"+e.toString());
            
            // 접속 리셋되었으면 다시 접속
            try {
                CONNECTION = DriverManager.getConnection(PLUGIN_PROP.getProperty("db.connection"));
            } catch (SQLException e1) {
                e.printStackTrace();
                console(e.toString());
                return null;
            }
            return CONNECTION;
        }
    }
    
    public static void initThisPlugin(String configPath) {
        
        TEMPLAT_CONFIG = new Configuration(Configuration.VERSION_2_3_29);
        
        Activator.console("MgPlugin start...");
        try (InputStream input = new FileInputStream(configPath + "\\config.properties") ) {
            PLUGIN_PROP.load(input);
            
            Activator.console("Properties loaded...");
        } catch (IOException e) {
            Activator.console(e.toString());
            e.printStackTrace();
        }
        
        try {
            TEMPLAT_CONFIG.setDirectoryForTemplateLoading(new File(configPath + "\\templates"));
            TEMPLAT_CONFIG.setDefaultEncoding("UTF-8");
            TEMPLAT_CONFIG.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            TEMPLAT_CONFIG.setLogTemplateExceptions(false);
            TEMPLAT_CONFIG.setWrapUncheckedExceptions(true);
            TEMPLAT_CONFIG.setFallbackOnNullLoopVariable(false);
            
            Activator.console("Template loaded...");
            
        } catch (IOException e) {
            Activator.console(e.toString());
            e.printStackTrace();
        }
    }
    
    public static void closeThisPlugin() {
        if (CONNECTION != null) {
            try {
                CONNECTION.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        Activator.console("MgPlugin ended...");
    }
    
    public static String getProperty(String key) {
        return PLUGIN_PROP.getProperty(key);
    }
    
    public static Template getTemplate(String name) {
        try {
            return TEMPLAT_CONFIG.getTemplate(name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String getTemplateSource(String name, Map<String, Object> root) {
        String source = "";
        
        try ( StringWriter out = new StringWriter() ) {
            TEMPLAT_CONFIG.getTemplate(name).process(root, out);
            
            source = out.getBuffer().toString();
            
            out.flush();
        } catch (TemplateException | IOException e) {
            e.printStackTrace();
        }
        return source;
    }
    
    /* ============================================================================================================== */
    
    
    
    
    
    
    // The plug-in ID
    public static final String PLUGIN_ID = "MGPlugin"; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;
    
    
    /**
     * The constructor
     */
    public Activator() {
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        
        initThisPlugin(ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().getAbsolutePath() + "\\.metadata\\mgplugin");
        
        super.start(context);
        plugin = this;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        
        closeThisPlugin();
        
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
}
