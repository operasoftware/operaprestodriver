/* Copyright (C) 2009 Opera Software ASA.  All rights reserved. */
package com.opera.core.systems.scope.internal;

import java.io.File;
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

public class OperaBinary extends Thread {

    private ThreadGroup runProcess;
    private Process process;
    private WinProcess winProcess;
    private OutputWatcher watcher;
    private Thread outputWatcherThread;
    List<String> commands;
    private static Logger logger = Logger.getLogger(OperaBinary.class.getName());
    private String processPath;

    public OperaBinary(String location, String... args) {
        super(new ThreadGroup("run-process"), "opera");
        File file = new File(location);
        processPath = file.getAbsolutePath();
        commands = new ArrayList<String>();
        commands.add(location);

        if(args != null && args.length > 0) {
                commands.addAll(Arrays.asList(args));
        }
        init();
        start();
    }

    public void kill() {
        watcher.kill();
    }

    @Override
    protected void finalize() throws Throwable {
        kill();
        super.finalize();
    }

    public Process getProcess() {
        return process;
    }

    public String getProcessPath() {
        return processPath;
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

        } catch (IOException e) {
            throw new WebDriverException("Could not start the process : " + e.getMessage());
        }

    }

    public int getPid() {
        return Platform.WINDOWS.is(Platform.getCurrent()) ? winProcess.getPid() : Integer.parseInt(pid(process));
    }

    private static String pid(Process process) {
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
        logger.fine("Waiting for Opera binary to exit.");
        try
        {
            int exit = process.waitFor();
            logger.info("Opera exited with return value " + exit);
        } catch (InterruptedException e) {
            logger.severe("Got interrupted, killing Opera.");
            kill();
        }
    }


    private static class OutputWatcher implements Runnable {
        private AtomicBoolean running = new AtomicBoolean(true);
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
                    if(stream.read() == -1) return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void kill() {
            running.set(false);
            if(winProcess != null)
                winProcess.killRecursively();
            else {
                killProcess(process);
                //TODO process should be gced after forced kill?
                if(process != null) {
                    process.destroy();
                }
            }
        }

        private void killProcess(Process process) {
            try {
                (new ProcessBuilder(new String[] { "kill", "-9", pid(process) })).start();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Could not kill process: " + e.getMessage());
            }
        }
    }

    /*
     * Some test code for how to use the API.
     */
    /*
    public static void main(String[] args)
    {
        OperaBinary binary = new OperaBinary("/home/janvidar/work/t/spartan/bin", "www.ikke.no");
        try
        {
            binary.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
     */

}
