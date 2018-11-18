package com.xyd.darwin;

public interface Handler {
	void initialized(String messageID);
	void finish(String messageID, boolean succeed, Throwable cause);
}
