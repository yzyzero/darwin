package com.xyd.darwin;

public abstract class Pusher extends BasePusher {
	protected long indicator;
    private Handler handler = null;
	
	protected Pusher(String messageID, String rtspURL) throws Exception {
		super(messageID, rtspURL);
	}

	public boolean open() {
		if(indicator != 0) {
			open(indicator);
			return true;
		}
		
		return false;
	}

	public void stop() {
		if(indicator != 0) {
			stop(indicator);
		}
	}
	
	public Handler getHandler() {
		return handler;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	abstract void onEvent(EventType eventType, int resultCode, String resultString);
	private void fireEvent(int eventType, int resultCode, String resultString) {
		EventType type = EventType.values()[eventType];
		onEvent(type, resultCode, resultString);
		switch (type) {
		case UNINITIALIZED:
			break;
		case INITIALIZED:
			if(handler != null) 
				handler.initialized(getMessageID());
			break;
		case RUNING:
			// TODO 播放中. resultCode 已播放时长
			break;
		case ERROR:
			// 异常处理
			if(handler != null) 
				handler.finish(getMessageID(), false, new Exception(resultString));
			break;
		case TERMINATE:
			// 任务结束
			if(handler != null) 
				handler.finish(getMessageID(), true, null);
			break;
		default:
			break;
		}
	}
	
	private native void open(long indicator);
	private native void stop(long indicator);
}
