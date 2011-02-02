/* Copyright (C) 2009 Opera Software ASA.  All rights reserved. */
package com.opera.core.systems.runner.launcher;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.winp.WinProcess;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;

public class OperaLauncherBinary extends Thread {

    private Process process;
    private WinProcess winProcess;
    private OutputWatcher watcher;
    private Thread outputWatcherThread;
    private List<String> commands = new ArrayList<String>();
    private static Logger logger = Logger.getLogger(OperaLauncherBinary.class.getName());
    private AtomicBoolean running = new AtomicBoolean(false);

    public OperaLauncherBinary(String location, String... args) {
        super(new ThreadGroup("run-process"), "launcher");
        commands.add(location);

        if (args != null && args.length > 0) {
            commands.addAll(Arrays.asList(args));
        }
        init();
    }
    
    public void kill() {
        watcher.kill();
    }

    public void shutdown()
    {
        if (!running.get())
            return;

        try {
            running.set(false);
            interrupt();
            join(1000);
        } catch (InterruptedException ex) {
            logger.severe("Unable to shutdown Opera binary in a timely fashion.");
            kill();
        }
    }

    public void init() {
        ProcessBuilder builder = new ProcessBuilder(commands);
        
        try {
            process = builder.start();
            builder.redirectErrorStream(true);

            if(Platform.WINDOWS.is(Platform.getCurrent()))
                winProcess = new WinProcess(process);

            watcher = new OutputWatcher(process, winProcess);

            outputWatcherThread = new Thread(getThreadGroup(), watcher , "output-watcher");
            outputWatcherThread.start();

            running.set(true);
        } catch (IOException e) {
            throw new WebDriverException("Could not start the process : " + e.getMessage());
        }
    }

    public String pid() {
        try {
            Field field = process.getClass().getDeclaredField("pid");
            field.setAccessible(true);
            int pid = field.getInt(process);
            return String.valueOf(pid);
        } catch (Exception e) {
            logger.warning("Could not get the pid: " + e.getMessage());
        }
        return "";
    }


    @Override
    public void run()
    {
        logger.fine("Waiting for Launcher binary to exit.");
        int exit = 0;
        while (running.get())
        {
            try
            {
                exit = process.waitFor();
                logger.info("Launcher exited with return value " + exit);
                running.set(false);
            } catch (InterruptedException e) {
                logger.fine("Got interrupted. Will terminate Launcher.");
                process.destroy();
            }
        }
    }


    private class OutputWatcher implements Runnable {
        private Process process;
        private WinProcess winProcess;

        public OutputWatcher(Process process, WinProcess winProcess) {
            this.process = process;
            this.winProcess = winProcess;
        }

        public void run() {
            InputStream stream = process.getInputStream();
            while (running.get()) {
                try {
                    if(stream.read() == -1) {
                    	return;
                    }
                } catch (IOException e) {
                    /* ignored */
                }
            }
        }
        
        public void kill() {
            running.set(false);

            if(winProcess != null)
            {
                winProcess.killRecursively();
                winProcess = null;
            }
            else {
                killProcess(process);
                //TODO process should be gced after forced kill?
                if(process != null) {
                    process.destroy();
                }
                process = null;
            }
        }

        private void killProcess(Process process) {
            try {
                (new ProcessBuilder(new String[] { "kill", "-9", pid() })).start();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Could not kill process: " + e.getMessage());
            }
        }
        
    }
}