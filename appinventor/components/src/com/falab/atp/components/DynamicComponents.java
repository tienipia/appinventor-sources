package com.falab.atp.components;

import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.HVArrangement;

import android.content.Context;
import android.view.ViewGroup;

public abstract class DynamicComponents extends AndroidNonvisibleComponent {
	protected final ComponentContainer mContainer;
	protected final Context mContext;
	protected HVArrangement mLayout = null;

	public DynamicComponents(ComponentContainer container) {
		super(container.$form());
		mContainer = container;
		mContext = container.$context();
	}

	protected void _removeView(final HVArrangement item) {
		if (item == null) {
			return;
		}
		ViewGroup mParent = (ViewGroup) item.getView().getParent();
		if (mParent != null) {
			mParent.removeView(item.getView());
		}
	}
}
