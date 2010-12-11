package com.opera.core.systems.runner.launcher;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.GeneratedMessage;
import com.opera.core.systems.model.ScreenShotReply;
import com.opera.core.systems.runner.OperaRunner;
import com.opera.core.systems.runner.OperaRunnerException;
import com.opera.core.systems.runner.launcher.OperaLauncherProtocol.MessageType;
import com.opera.core.systems.runner.launcher.OperaLauncherProtocol.ResponseEncapsulation;
import com.opera.core.systems.runner.launcher.OperaLauncherProtos.LauncherHandshakeRequest;
import com.opera.core.systems.runner.launcher.OperaLauncherProtos.LauncherScreenshotRequest;
import com.opera.core.systems.runner.launcher.OperaLauncherProtos.LauncherScreenshotResponse;
import com.opera.core.systems.runner.launcher.OperaLauncherProtos.LauncherStartRequest;
import com.opera.core.systems.runner.launcher.OperaLauncherProtos.LauncherStatusRequest;
import com.opera.core.systems.runner.launcher.OperaLauncherProtos.LauncherStatusResponse;
import com.opera.core.systems.runner.launcher.OperaLauncherProtos.LauncherStopRequest;
import com.opera.core.systems.runner.launcher.OperaLauncherProtos.LauncherStatusResponse.StatusType;
import com.opera.core.systems.settings.OperaDriverSettings;

public class OperaLauncherRunner implements OperaRunner{
	private static Logger logger = Logger.getLogger(OperaLauncherRunner.class.getName());
	
	private OperaLauncherBinary launcherRunner = null;
	
	private OperaDriverSettings settings;
	private OperaLauncherProtocol launcherProtocol = null;
	private String chrashlog = null;
	
	public OperaLauncherRunner(OperaDriverSettings settings){
		this.settings = settings;
		
		if(this.settings.doRunOperaLauncherFromOperaDriver()){
			
			List<String> stringArray = new ArrayList<String>();
			stringArray.add("-host");
			stringArray.add("127.0.0.1");
			stringArray.add("-port");
			stringArray.add(Integer.toString(this.settings.getOperaLauncherListeningPort()));
			stringArray.add("-bin");
			stringArray.add(this.settings.getOperaBinaryLocation());
			
			StringTokenizer tokanizer = new StringTokenizer(this.settings.getOperaBinaryArguments(), " ");
			while(tokanizer.hasMoreTokens()){
				stringArray.add(tokanizer.nextToken());
			}
			
			launcherRunner = new OperaLauncherBinary(this.settings.getOperaLauncherBinary(),stringArray.toArray(new String[stringArray.size()]));
		}
			
		logger.log(Level.INFO, "Waiting for Opera Launcher connection on port " + this.settings.getOperaLauncherListeningPort());
        try {
        	//setup listener server
        	ServerSocket listenerServer = new ServerSocket(settings.getOperaLauncherListeningPort());
        	//try to connect
        	launcherProtocol = new OperaLauncherProtocol(listenerServer.accept());
        	//we did it!
        	logger.log(Level.INFO, "Connected with Opera Launcher on port " + settings.getOperaLauncherListeningPort());
        	listenerServer.close();
        	//Do the handshake!
        	LauncherHandshakeRequest.Builder request = LauncherHandshakeRequest.newBuilder();
            ResponseEncapsulation res = launcherProtocol.sendRequest(MessageType.MSG_HELLO, request.build().toByteArray());
            //Are we happy?
            if(res.IsSuccess()){
            	logger.log(Level.INFO, "Got opera launcher handshake: " + res.getResponse().toString());
            } else {
            	logger.log(Level.WARNING, "Did not get opera launcher handshake: " + res.getResponse().toString());
            	throw new OperaRunnerException("Did not get opera launcher handshake");
            }
        } catch (IOException e) {
        	throw new OperaRunnerException("Unable to listen to opera launcher port " + settings.getOperaLauncherListeningPort(), e);
        }
	}
	
	public void startOpera() {
		logger.log(Level.INFO, "Launcher starting opera...");
        try {
            LauncherStartRequest.Builder request = LauncherStartRequest.newBuilder();
            ResponseEncapsulation res = launcherProtocol.sendRequest(MessageType.MSG_START, request.build().toByteArray());
            if(handleStatusMessage(res.getResponse()) != StatusType.RUNNING){
            	throw new OperaRunnerException("Could not start opera");
            }
        } catch (IOException e){
        	throw new OperaRunnerException("Could not start opera", e);
        }
	}
	
	public void stopOpera() {
		logger.log(Level.INFO, "Launcher stopping opera...");
        try {
            LauncherStopRequest.Builder request = LauncherStopRequest.newBuilder();
            ResponseEncapsulation res = launcherProtocol.sendRequest(MessageType.MSG_STOP, request.build().toByteArray());
            if(handleStatusMessage(res.getResponse()) == StatusType.RUNNING){
            	throw new OperaRunnerException("Could not stop opera");
            }
        } catch (IOException e){
        	throw new OperaRunnerException("Could not stop opera", e);
        }
	}
	
	public boolean isOperaRunning() {
		logger.log(Level.INFO, "Get opera status");
        try {
            LauncherStatusRequest.Builder request = LauncherStatusRequest.newBuilder();
            ResponseEncapsulation res = launcherProtocol.sendRequest(MessageType.MSG_STATUS, request.build().toByteArray());
            return handleStatusMessage(res.getResponse()) == StatusType.RUNNING;
        } catch (IOException e){
        	throw new OperaRunnerException("Could not get state of opera", e);
        }
	}
	
	public boolean hasOperaCrashed() {
		return chrashlog != null;
	}
	
	public String getOperaCrashlog() {
		return chrashlog;
	}
	
	public void shutdown() {
		
		try {
			//Send a shutdown to the launcher!
			if(this.settings.doRunOperaLauncherFromOperaDriver()){
				try {
					launcherProtocol.sendRequestWithoutResponse(MessageType.MSG_SHUTDOWN, null);
				} catch (Exception e) {
					//will give us read-response issues!
					e.printStackTrace();
				}
			}
			//Then shutdown the protocol-connection
			launcherProtocol.shutdown();
		} catch (IOException e) {
			//dont care
			throw new OperaRunnerException("Do we get a shutdown exception?", e);
		}
		
		if(launcherRunner != null){
			launcherRunner.shutdown();
			launcherRunner = null;
		}
	}
	
	/**
	 * Handle status message, and updates state
	 * 
	 * @param msg
	 */
	private StatusType handleStatusMessage(GeneratedMessage msg)
    {
        LauncherStatusResponse response = (LauncherStatusResponse) msg;
        
        //LOG RESULT!
        logger.log(Level.INFO, "[LAUNCHER] Status: " + response.getStatus().toString());
        if (response.hasExitcode()){
            logger.log(Level.INFO, "[LAUNCHER] Status: exitCode=" + response.getExitcode());
        }
        if (response.hasCrashlog()){
        	
            logger.log(Level.INFO, "[LAUNCHER] Status: crashLog=yes");
        } else {
        	logger.log(Level.INFO, "[LAUNCHER] Status: crashLog=no");
        }
        if (response.getLogmessagesCount() > 0){
        	for(String message : response.getLogmessagesList()){
        		logger.log(Level.INFO, "[LAUNCHER LOG] " + message);
        	}
        } else {
        	logger.log(Level.INFO, "[LAUNCHER LOG] No log...");
        }

        //Handle state
        StatusType status = response.getStatus();
        if(status == StatusType.CRASHED){
        	if(response.hasCrashlog()){
        		chrashlog = response.getCrashlog().toStringUtf8();
        	} else {
        		chrashlog = "";// != NULL :-|
        	}
        } else {
        	chrashlog = null;
        }
        
        //TODO: send something to the operalistener....
        //if(launcherLastKnowStatus == StatusType.RUNNING && status != StatusType.RUNNING){
        //	if(operaListener != null)
        //		operaListener.operaBinaryStopped(response.getExitcode());
        //}
        
        return status;
    }
	
	/**
	 * Take screenshots!
	 */
	public ScreenShotReply saveScreenshot(long timeout, String... hashes){
		String resultMd5 = null;
		byte[] resultBytes = null;

		logger.log(Level.INFO, "Get opera screenshot");
        try {
            LauncherScreenshotRequest.Builder request = LauncherScreenshotRequest.newBuilder();
            for(int i=0;i<hashes.length;i++){
            	request.addKnownMD5S(hashes[i]);
            }
            request.setKnownMD5STimeoutMs((int)timeout);
            
            ResponseEncapsulation res = launcherProtocol.sendRequest(MessageType.MSG_SCREENSHOT, request.build().toByteArray());
            LauncherScreenshotResponse response = (LauncherScreenshotResponse) res.getResponse();
            resultMd5 = response.getMd5();
            resultBytes = response.getImagedata().toByteArray();
            
        } catch (IOException e){
        	throw new OperaRunnerException("Could not get state of opera", e);
        }
		
		return new ScreenShotReply(resultMd5, resultBytes);
	}
}
