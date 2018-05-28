package osgi;

import com.dscsag.plm.spi.NotificationEventConstants;
import com.dscsag.plm.spi.interfaces.ECTRService;
import com.dscsag.plm.spi.interfaces.logging.PlmLogger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Activator to register provided services
 */
public class Activator implements BundleActivator
{
  private PlmLogger logger;

  private TrayIcon trayIcon;

  public void start(BundleContext context) throws Exception
  {
    ECTRService ectrService = getService(context, ECTRService.class);
    if(ectrService!=null)
      logger = ectrService.getPlmLogger();

    String[] topics = {NotificationEventConstants.STARTING, NotificationEventConstants.STARTED,
            NotificationEventConstants.CONFIGURATION_CHANGED, NotificationEventConstants.BEFORE_SHUTDOWN, NotificationEventConstants.SHUTDOWN};

    Dictionary<String, Object> props = new Hashtable<>();
    props.put(EventConstants.EVENT_TOPIC, topics);
    context.registerService(EventHandler.class.getName(), new EventListener(), props);

    if (SystemTray.isSupported()) {
      BufferedImage image = new BufferedImage(32, 16, BufferedImage.TYPE_INT_ARGB);
      Graphics2D x = image.createGraphics();
      x.setColor(Color.BLUE);
      x.fillRect(0,0,32,16);
      trayIcon = new TrayIcon(image,"ECTR Notifications");
      final SystemTray tray = SystemTray.getSystemTray();
      tray.add(trayIcon);
      log("Tray installed...");
    }
    else
      log("Tray is not supported.");

  }

  @Override
  public void stop(BundleContext bundleContext) throws Exception
  {
    //empty
  }

  private class EventListener implements EventHandler
  {
    @Override
    public void handleEvent(Event event)
    {
      log("Handle event " + event.getTopic());
      if (NotificationEventConstants.CONFIGURATION_CHANGED.equals(event.getTopic()))
      {
        Object property = event.getProperty(NotificationEventConstants.CONFIGURATION_CHANGED_PATH);
        log("Configuration path " + property);
      }
    }
  }

  private void log(String message)
  {
    if (logger != null)
      logger.trace(message);
    if(trayIcon!=null)
      trayIcon.displayMessage("ECTR Event",message, TrayIcon.MessageType.INFO);

  }

  private <T> T getService(BundleContext context, Class<T> clazz) throws Exception
  {
    ServiceReference<T> serviceRef = context.getServiceReference(clazz);
    if (serviceRef != null)
      return context.getService(serviceRef);
    throw new Exception("Unable to find implementation for service " + clazz.getName());
  }

}