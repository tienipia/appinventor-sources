package com.falab.io.core;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.util.AsynchUtil;

import android.os.Handler;

@DesignerComponent(version = 1, category = ComponentCategory.EXTENSION, nonVisible = true, description = "FaLAB UDP Extension", iconName = "aiwebres/fa-lab.png")
@SimpleObject(external = true)
public final class FaLAB extends AndroidNonvisibleComponent {
	private final ComponentContainer container;

	protected final Handler androidUIHandler = new Handler();

	public FaLAB(ComponentContainer container) {
		super(container.$form());
		this.container = container;
	}

	@SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "System.currentTimeMillis()")
	public long currentTimeMillis() {
		return System.currentTimeMillis();
	}

	@SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "System.currentTimeMillis()")
	public String fromJNIcall() {
		return stringFromJNI();

	}

	@SimpleFunction(description = "Sets the x and y coordinates of the Ball. If CenterAtOrigin is "
			+ "true, the center of the Ball will be placed here. Otherwise, the top left edge of the Ball "
			+ "will be placed at the specified coordinates.")
	public void loadLibrary() {
		AsynchUtil.runAsynchronously(new Runnable() {

			@Override
			public void run() {
				String errMsg = null;
				try {
					System.loadLibrary("hello-jni");
				} catch (Exception e) {
					errMsg = e.getMessage();
				} catch (Error er) {
					errMsg = er.getMessage();
				}

				if (errMsg != null) {
					final String ferrMsg = errMsg;
					androidUIHandler.post(new Runnable() {

						@Override
						public void run() {
							onMessage(ferrMsg);
						}

					});
				}
			}

		});

	}

	/**
	 * Indicates that the user tapped and released the `Button`.
	 */
	@SimpleEvent(description = "User tapped and released the button.")
	public void onMessage(String message) {
		EventDispatcher.dispatchEvent(this, "onMessage", message);
	}

	public native String stringFromJNI();

}
