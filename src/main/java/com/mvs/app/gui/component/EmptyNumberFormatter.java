package com.mvs.app.gui.component;

import javax.swing.text.NumberFormatter;
import java.text.NumberFormat;
import java.text.ParseException;

public class EmptyNumberFormatter extends NumberFormatter {
	
	public EmptyNumberFormatter() {
		super();
	}
	
	public EmptyNumberFormatter(NumberFormat numberFormat) {
		super(numberFormat);
	}

	@Override
	public Object stringToValue(String text) throws ParseException {
		if (text.equals(""))
			return null;
		else
			return super.stringToValue(text);
	}
	
	
}

