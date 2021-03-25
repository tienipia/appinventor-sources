package com.falab.io.net;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;

@DesignerComponent(version = 1, category = ComponentCategory.CONNECTIVITY, nonVisible = true, description = "FaLAB UDP Extension", iconName = "aiwebres/packet.png")
@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.INTERNET," + "android.permission.WRITE_EXTERNAL_STORAGE,"
		+ "android.permission.READ_EXTERNAL_STORAGE," + "android.permission.CHANGE_WIFI_MULTICAST_STATE,"
		+ "android.permission.ACCESS_WIFI_STATE," + "android.permission.ACCESS_NETWORK_STATE")
public final class RESTful extends AndroidNonvisibleComponent {
	private final ComponentContainer container;

	public RESTful(ComponentContainer container) {
		super(container.$form());
		this.container = container;
	}

	@SimpleFunction(description = "HTTP Request")
	public void request(String method, String url) {
		request(method, url, null);
	}

	@SimpleFunction(description = "HTTP Request")
	public void request(String method, String url, String data) {
		if (method != null && url != null) {

		}
	}
}
