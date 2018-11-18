package com.xyd.darwin;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilePusher extends Pusher {
	private static final Logger logger = LoggerFactory.getLogger(FilePusher.class);

	/**
	 * 多文件播放
	 * @param messageID
	 * @param rtspURL
	 * @param file
	 * @param duration
	 * @param loop
	 * @param order	是否顺序播放，true 顺序播放；false 随机播放
	 * @throws Exception 
	 */
	public FilePusher(String messageID, String rtspURL, File[] files, int duration, int loop, boolean order) throws Exception {
		super(messageID, rtspURL);
		
		int fileNumber = files.length;
		
		if(fileNumber < 1) {
    		throw new FileNotFoundException("没选择任何文件");
		} else if(fileNumber == 1) {
			if(files[0].exists()) {
	    		indicator = createSingle(rtspURL, files[0].getAbsolutePath(), duration, loop);
	    	} else {
	    		throw new FileNotFoundException(files[0].getAbsolutePath() + "不存在");
	    	}
		} else {
			String fileList[] = new String[fileNumber];
			for (int i = 0; i < fileNumber; i++) {
				if(files[i].exists()) {
					fileList[i] = files[i].getAbsolutePath();
		    	} else {
		    		throw new FileNotFoundException(files[i].getAbsolutePath() + "不存在");
		    	}
			}

			indicator = createMultiple(rtspURL, fileList, duration, loop, order);
		}
	}
	/**
	 * 
	 * 向Darwin推送MP3音频文件
	 * 
	 * @param messageID 	消息ID
	 * @param rtspURL 		目标RTSP地址
	 * @param file 			本地音频文件
	 * @param duration 		播发时长
	 * @param loop 			循环次数
	 * @throws Exception
	 */
	public FilePusher(String messageID, String rtspURL, File file, int duration, int loop) throws Exception {
		super(messageID, rtspURL);
    	if(file.exists()) {
    		indicator = createSingle(rtspURL, file.getAbsolutePath(), duration, loop);
    	} else {
    		throw new FileNotFoundException(file.getAbsolutePath() + "不存在");
    	}
	}
	
	@Override
	void onEvent(EventType eventType, int resultCode, String resultString) {
		// TODO 音频文件推送事件处理
		logger.info("{}({}): {}", eventType.toString(), resultCode, resultString);
	}
	
	private native long createSingle(String rtspURL, String file, int duration, int loop);
	private native long createMultiple(String rtspURL, String[] file, int duration, int loop, boolean order);
	
	public static void main(String[] args) throws Exception {
		Object objWait = new Object();
		
		String tomcatHome = System.getProperty("catalina.base");
		if(StringUtils.isEmpty(tomcatHome)) {
			tomcatHome = "C:\\apache-tomcat-9.0.8";
		}
		if(args.length == 3) {
			tomcatHome = args[0];
		}
		//动态DLL库加载,建议放在TOMCAT的web.xml中加载
		/**
		 * 	<listener>
		 *		<listener-class>com.xyd.ebwp.GlobalInitialize</listener-class>
		 *	</listener>
		 */
		System.load(tomcatHome + "\\lib\\libgcc_s_seh-1.dll");
		System.load(tomcatHome + "\\lib\\libwinpthread-1.dll");
		System.load(tomcatHome + "\\lib\\libstdc++-6.dll");
		System.load(tomcatHome + "\\lib\\msc_x64.dll");
		System.load(tomcatHome + "\\lib\\darwin.dll");
		//下面代码是在业务处理过程中推流的代码
		final Handler handler = new Handler() {//播放回调接口
			@Override
			public void initialized(String messageID) {
				//开始播放
				logger.debug("开始播放。");
			}

			@Override
			public void finish(String messageID, boolean succeed, Throwable cause) {
				//结束播放
				logger.debug("结束播放。");
				synchronized(objWait) {
					objWait.notify();
				}
			}
			
		};
		Runnable runner = new Runnable() {
			@Override
			public void run() {
				try {
					String foldID = "C:\\apache-tomcat-9.0.8";
					String fileID = "1526984057353754";
					String messageID = "test";//消息ID根据业务自定义
					if(args.length == 1) {
						messageID = args[0];
					}else if(args.length == 2) {
						fileID = args[0];
						messageID = args[1];
					}else if(args.length == 3) {
						foldID = args[0];
						fileID = args[1];
						messageID = args[2];
					}
					File file = new File(foldID+"\\resources\\audio\\"+fileID+".mp3");//文件必须绝对路径
					FilePusher pusher = new FilePusher(messageID,"rtsp://127.0.0.1:554/"+messageID, file, 360000000, 0);
					pusher.setHandler(handler);
					pusher.open();
					synchronized(objWait) {
						objWait.wait();
					}
				}catch(Exception e) {
					logger.error(e.toString(), e);
				}
			}
		};
		Thread thread = new Thread(runner);
		thread.start();
	}
}
