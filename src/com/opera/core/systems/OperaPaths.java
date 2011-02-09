/*
Copyright 2008-2011 Opera Software ASA

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.opera.core.systems;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.os.CommandLine;

/**
 * This class tries to find the paths to Opera and Opera Launcher on any
 * system. If it cannot find a launcher it will extract the appropriate one
 * from the jar it is in.
 *
 * @author Stuart Knightley, Deniz Turkoglu
 *
 */
public class OperaPaths {

  /**
   * This method will try and find Opera on any system. It takes the following
   * steps:
   *  1. Check the environment variable "OPERA_PATH". If it exists, and the
   *     file it points to exists, then return
   *  2. Check if Opera exists at the default location on the respective OS
   *  3. (Unix) Call `which opera` to find the location
   *  4. Give up and return null
   * @return The path to Opera, or null
   */
	public String operaPath() {
		String path = System.getenv("OPERA_PATH");

		if(isPathValid(path)) return path;

		Platform platform = Platform.getCurrent();

		switch (platform) {
		case LINUX:
		case UNIX:
			path = "/usr/bin/opera";
			if (!isPathValid(path)) {
				CommandLine line = new CommandLine("which", "opera");
				line.execute();
				path = line.getStdOut().trim();
			}
			break;
		case MAC:
			path = "/Applications/Opera.app/Contents/MacOS/Opera";
			break;
		case WINDOWS:
		case XP:
		case VISTA:
			String x86 = System.getenv("ProgramFiles(x86)");
			String progfiles = (x86 == null) ? System.getenv("PROGRAMFILES") : x86;
			if (progfiles == null) progfiles = "\\Program Files";
			path = progfiles + "\\Opera\\opera.exe";
			break;
		default:
			throw new WebDriverException("Auto find is not support on this platform"); //android?
		}

		return (isPathValid(path)) ? path : null;
	}


  /**
   * This method will try and find Opera Launcher on any system.
   * @return The path to Opera Launcher, or null
   */
	public String launcherPath() {
		String path = System.getenv("OPERA_LAUNCHER");
		if (!isPathValid(path)) path = getOperaLauncherPath();

		return path;
	}

	/**
	 * Tries to load the launcher executable from the jar file
	 * A copy of the launcher is put into user directory and
	 * on each call is compared to the content in jar file
	 * @return path to launcher executable
	 */
	public String getOperaLauncherPath() {
		String launcherName = getLauncherNameForOS();

		String executablePath = null;

		URL res = OperaDriver.class.getClassLoader().getResource("launcher/" + launcherName);
		if (res != null) {
			String url = res.toExternalForm();
			if ((url.startsWith("jar:")) || (url.startsWith("wsjar:"))) {
				int idx = url.lastIndexOf('!');
				String filePortion = url.substring(url.indexOf(':') + 1, idx);

				while (filePortion.startsWith("/")) {
					filePortion = filePortion.substring(1);
				}

				if (filePortion.startsWith("file:/")) {
					filePortion = filePortion.substring(6);

					if (filePortion.startsWith("//"))
						filePortion = filePortion.substring(2);

					try {
						filePortion = URLDecoder.decode(filePortion, "UTF-8");
					} catch (UnsupportedEncodingException ex) {
						throw new WebDriverException("Can't decode path name, UTF-8 is not supported");
					}

					File jarFile = new File(filePortion);
					executablePath = FileUtils.getUserDirectoryPath() + File.separatorChar + ".launcher" + File.separatorChar + launcherName;

					File executable = new File(executablePath);

					if ((!executable.exists()) || FileUtils.isFileNewer(jarFile, executable)) {
						try {
							if (!executable.exists()) FileUtils.touch(executable);

							InputStream is = res.openStream();
							OutputStream os = new FileOutputStream(executable);

							IOUtils.copy(is, os);

							is.close();
							os.close();

							executable.setLastModified(jarFile.lastModified());
						} catch (IOException e) {
							throw new WebDriverException("Cant write file to disk : " + e.getMessage());
						}
					}
				}
			} else if (url.startsWith("file:")) {
				File f;
				try {
					f = new File(res.toURI());
				} catch (URISyntaxException e) {
					f = new File(res.getPath());
				}
				executablePath = f.getAbsolutePath();
			}
		}

		if (executablePath != null)
			makeLauncherExecutable(executablePath);

		return executablePath;
	}

	/**
	 * Chmods the file at given path (linux and mac only)
	 * @param executablePath
	 */
	private void makeLauncherExecutable(String executablePath) {
		Platform current = Platform.getCurrent();
		if (current.is(Platform.LINUX) || current.is(Platform.MAC)) {
			CommandLine line = new CommandLine("chmod", "755", executablePath);
			line.execute();
		}
	}

	private String getLauncherNameForOS() {
		boolean is64 = "64".equals(System.getProperty("sun.arch.data.model"));
		Platform currentPlatform = Platform.getCurrent();


		switch (currentPlatform) {
		case LINUX:
		case UNIX:
			return (is64 ? "launcher-linux-x86_64" : "launcher-linux-i686");
		case MAC:
			return "launcher-mac";
		case WINDOWS:
		case VISTA:
		case XP:
			return "launcher-win32-i86pc.exe";
		default:
			throw new WebDriverException("Could not find a platfom that supports bundled launchers, please set it manually");
		}
	}

  /**
   * Check that the given path is not null, and exists.
   * @param path The path to check.
   * @return null if the path is null, or does not exist. Otherwise it returns
   * the given path.
   */
  private boolean isPathValid(String path) {
    if (path == null) return false;

    if(path.length() == 0) return false;

    File file = new File(path);
    return (file.exists());
  }
}
