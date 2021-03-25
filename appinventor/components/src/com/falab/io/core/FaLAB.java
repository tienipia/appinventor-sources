package com.falab.io.core;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;

@DesignerComponent(version = 1, category = ComponentCategory.EXTENSION, nonVisible = true, description = "FaLAB UDP Extension", iconName = "aiwebres/fa-lab.png")
@SimpleObject(external = true)
public final class FaLAB extends AndroidNonvisibleComponent {
	private final ComponentContainer container;

	public FaLAB(ComponentContainer container) {
		super(container.$form());
		this.container = container;
	}

	@SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "System.currentTimeMillis()")
	public long currentTimeMillis() {
		return System.currentTimeMillis();
	}

}
