package com.xyd.darwin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;

public class BasePusher implements StreamPusher {
    
	private static final String RTSP_REGULAR_MASK = "rtsp://(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):{0,1}(\\d{0,5})/(.*)";
	
	protected final String rtspURL;
	protected final String messageID;
	protected final String serverIP;
	protected final String uri;
	protected final int serverPort;

	public BasePusher(String messageID, String rtspURL) throws Exception {
		this.rtspURL = rtspURL;
		this.messageID = messageID;

        Pattern pattern = Pattern.compile(RTSP_REGULAR_MASK);  
        Matcher matcher = pattern.matcher(rtspURL);  
        if (matcher.find()) {
        	serverIP = matcher.group(1);
        	
            String p = matcher.group(2);
            if(NumberUtils.isCreatable(p)) {
            	serverPort = Integer.parseInt(p);
            } else {
            	serverPort = 554;
            }
            
            String tUri = matcher.group(3);
            if(tUri.endsWith(".sdp")) {
            	uri = "/" + tUri;
            } else {
            	uri = "/" + tUri + ".sdp";
            }
        } else {
        	throw new Exception("RTSP路径格式不正确");
        }
	}

	@Override
	public String getRtspURL() {
		return rtspURL;
	}
	
	@Override
	public String getMessageID() {
		return messageID;
	}

	@Override
	public void push(byte[] buf, int size) {}

}
