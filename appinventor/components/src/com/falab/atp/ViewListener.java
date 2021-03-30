package com.falab.atp;

public class ViewListener {

	public interface Button<T> {

		void click(String id, T obj);
	}

}
