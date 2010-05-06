package com.opera.core.systems.model;

import java.util.List;

public class ScreenShot {
	protected int windowId;
	protected String md5;
	protected byte[] png;

	protected List<ColorResult> colorResult;
	
	public List<ColorResult> getColorResult() {
		return colorResult;
	}

	public void setColorResult(List<ColorResult> colorResult) {
		this.colorResult = colorResult;
	}

	public ScreenShot(String md5, byte[] png) {
		this.md5 = md5;
		this.png = png;
	}
	
	public ScreenShot(String md5, List<ColorResult> colorResult) {
		this.md5 = md5;
		this.colorResult = colorResult;
	}
	
	public int getWindowId() {
		return windowId;
	}

	public void setWindowId(int windowId) {
		this.windowId = windowId;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public byte[] getPng() {
		return png;
	}

	public void setPng(byte[] png) {
		this.png = png;
	}
}
