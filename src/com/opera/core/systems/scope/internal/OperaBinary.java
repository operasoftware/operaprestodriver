/* Copyright (C) 2009 Opera Software ASA.  All rights reserved. */
package com.opera.core.systems.scope.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.openqa.selenium.WebDriverException;

public class OperaBinary {

	private Process process;
	private OutputWatcher watcher;
	private Thread outputWatcherThread;
	List<String> commands;
	
	
	public void kill() {
		watcher.kill();
	}
	
	public Process getProcess() {
		return process;
	}
	
	public OperaBinary(String location, String... args) {
		commands = new ArrayList<String>();
		commands.add(location);
		if(args != null && args.length > 0) {
			commands.addAll(Arrays.asList(args));
		}
		start();
	}
	
	public void start() {
		ProcessBuilder builder = new ProcessBuilder(commands);
		try {
			process = builder.start();
			builder.redirectErrorStream(true);
			watcher = new OutputWatcher(process);
			outputWatcherThread = new Thread(null, watcher , "output-watcher");
	        outputWatcherThread.start();
		} catch (IOException e) {
			throw new WebDriverException("Could not start the process : " + e.getMessage());
		}
	}
	
	private static class OutputWatcher implements Runnable {
		private AtomicBoolean running = new AtomicBoolean(true);
		private Process process;

		public OutputWatcher(Process process) {
			this.process = process;
		}

		public void run() {
			InputStream stream = process.getInputStream();
			while (running.get()) {
				try {
					if(stream.read() == -1) return;
				} catch (IOException e) {
					//System.err.println(e);
				}
			}
		}
		
		public void kill() {
			running.set(false);
			if(process != null)
				process.destroy();
		}

	}

}
