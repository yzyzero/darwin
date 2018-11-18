package com.xyd.darwin;

public interface StreamPusher {
	public String getRtspURL();
	public String getMessageID();
	
	// 推送Buffer
	public void push(byte[] buf, int size);
}
