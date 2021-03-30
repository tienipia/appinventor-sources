package com.falab.atp.components;

import java.util.HashMap;
import java.util.Map;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.ButtonBase;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.HVArrangement;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;

@DesignerComponent(description = "Dynamic Components is an extension that creates any component in your App Inventor distribution programmatically, instead of having pre-defined components. Made with &#x2764;&#xfe0f; by Yusuf Cihan.", category = ComponentCategory.EXTENSION, helpUrl = "https://github.com/ysfchn/DynamicComponents-AI2/blob/main/README.md", iconName = "aiwebres/icon.png", nonVisible = true, version = 9, versionName = "2.2.2")
@SimpleObject(external = true)
public final class DynamicButton extends DynamicComponents {

	private static class _Button extends ButtonBase {

		private final DynamicButton parent;
		private final String btnId;

		private _Button(String btnId, ComponentContainer container, DynamicButton parent) {
			super(container);
			this.btnId = btnId;
			this.parent = parent;
		}

		@Override
		public void click() {
			parent.onClick(btnId);
		}
	}

	private final Map<String, _Button> mButtons;

	public DynamicButton(ComponentContainer container) {
		super(container);
		mButtons = new HashMap<>();
	}

	@SimpleFunction(description = "Creates a new dynamic button.")
	public void Create(final AndroidViewComponent target, final String btnId) throws Exception {
		_Button b = new _Button(btnId, (ComponentContainer) target, DynamicButton.this);
		b.Text("asdfqwer");
		mButtons.put(btnId, b);
	}

	@SimpleFunction(description = "Bind to layout")
	public void onBind(final HVArrangement layout) throws YailRuntimeError {
		// TODO Auto-generated method stub

	}

	@SimpleEvent(description = "User tapped and released the button.")
	public void onClick(String btnId) {
		EventDispatcher.dispatchEvent(this, "onClick", btnId);
	}
}
