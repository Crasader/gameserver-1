package com.xinqihd.sns.gameserver.admin.util;

import static java.awt.GraphicsEnvironment.*;

import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.util.WindowUtils;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.Timeline.TimelineState;
import org.pushingpixels.trident.callback.UIThreadTimelineCallbackAdapter;
import org.pushingpixels.trident.ease.Sine;

import com.xinqihd.sns.gameserver.admin.gui.LoginDialog;
import com.xinqihd.sns.gameserver.admin.gui.MainFrame;
import com.xinqihd.sns.gameserver.admin.gui.ext.HtmlDialog;

public class MyWindowUtil {
	
	/**
	 * Get the screen rectangle.
	 * @return
	 */
	public static Rectangle getScreenSize() {
		return getUsableDeviceBounds(getLocalGraphicsEnvironment().
				getDefaultScreenDevice().getDefaultConfiguration());
	}
	
	/**
	 * Create an animation JComponent.
	 * @param comp
	 * @param to
	 * @return
	 */
	public static Timeline createLocationTimeline(Component comp, Point to) {
		return createLocationTimeline(comp, to, 600);
	}
	
	/**
	 * Create an animation JComponent.
	 * @param comp
	 * @param to
	 * @return
	 */
	public static Timeline createLocationTimeline(Component comp, Point to, int duration) {
		Timeline timeline = new Timeline(LoginDialog.getInstance());
		to.y = MainFrame.screenHeight;
		timeline.addPropertyToInterpolate("location", comp.getLocation(), to);
		timeline.setDuration(duration);
		timeline.setEase(new Sine());
		return timeline;
	}
	
	/**
	 * Get the screen width and height
	 * @param gc
	 * @return
	 */
  private static Rectangle getUsableDeviceBounds(GraphicsConfiguration gc) {
    Rectangle bounds = gc.getBounds();
    Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
    
    bounds.x += insets.left;
    bounds.y += insets.top;
    bounds.width -= (insets.left + insets.right);
    bounds.height -= (insets.top + insets.bottom);
    
    return bounds;
  }
  
//----------------------- Window transparency convenience support
  
  public static void fadeOutAndDispose(final Window window,
          int fadeOutDuration) {
      fadeOutAndEnd(window, fadeOutDuration, false);
  }

  public static void fadeOutAndExit(Window window, int fadeOutDuration) {
      fadeOutAndEnd(window, fadeOutDuration, true);
  }
  /**
   * @param window
   * @param fadeOutDuration
   */
  private static void fadeOutAndEnd(final Window window, int fadeOutDuration, 
          final boolean exit) {
      Timeline dispose = new Timeline(new WindowFader(window));
      dispose.addPropertyToInterpolate("opacity", 1.0f,
//              AWTUtilitiesWrapper.getWindowOpacity(window), 
              0.0f);
      dispose.addCallback(new UIThreadTimelineCallbackAdapter() {
          @Override
          public void onTimelineStateChanged(TimelineState oldState,
                  TimelineState newState, float durationFraction,
                  float timelinePosition) {
              if (newState == TimelineState.DONE) {
                  if (exit) {
                      Runtime.getRuntime().exit(0);
                  } else {
                      window.dispose();
                  }
              }
          }
      });
      dispose.setDuration(fadeOutDuration);
      dispose.play();
  }
  
  public static class WindowFader {
      private Window window;
      
      public WindowFader(Window window) {
          this.window = window;
      }
      
      public void setOpacity(float opacity) {
          AWTUtilitiesWrapper.setWindowOpacity(window, opacity);
      }
  }

  public static JDialog getCenterDialog(int width, int height, 
  		JComponent panel, JButton okButton) {
  	
  	JDialog dialog = new JDialog();
		dialog.setLayout(new MigLayout("wrap 1", "[100%]"));
		dialog.add(panel, "width 100%, height 90%, grow");
		if ( okButton != null ) {
			dialog.add(okButton, "align center");
		}
		dialog.setSize(width, height);
		Point cpoint = WindowUtils.getPointForCentering(dialog);
		dialog.setLocation(cpoint);
		dialog.setModal(true);
		return dialog;
  }
  
  public static HtmlDialog getHtmlDialog(String content, int width, int height) {
  	HtmlDialog dialog = new HtmlDialog(content, width, height);
		return dialog;
  }
}
