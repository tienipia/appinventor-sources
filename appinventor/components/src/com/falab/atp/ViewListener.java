package com.falab.atp;

import com.google.appinventor.components.runtime.ButtonBase;

public class ViewListener {

	public interface Button<T> {

		void click(ButtonBase b, String id, T obj);
	}

}
