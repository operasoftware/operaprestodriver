package com.opera.core.systems;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
 * @author stuartk
 * This class tries to find the paths to Opera and Opera Launcher on any system
 */
public class OperaPaths {

  /**
   * This method will try and find Opera on any system. It takes the following
   * steps:
   *  1. Check the environment variable "OPERA_PATH". If it exists, and the
   *     file it points to exists, then return
   *  2. (Unix) Call `which opera` to find the location
   *  3. Check if Opera exists at the default location on the respective OS
   *  4. Give up and return null
   * @return The path to Opera, or null
   */
  public String operaPath() {
    String path = null;
    if ((path = checkPath(System.getenv("OPERA_PATH"))) != null) return path;
    Platform platform = Platform.getCurrent();

    if (platform.is(Platform.LINUX) || platform.is(Platform.UNIX)) {
    	CommandLine line = new CommandLine("which", "opera");
    	line.execute();
    	path = line.getStdOut().trim();
      if (checkPath(path) != null) return path;
    }

    if (platform.is(Platform.LINUX) || platform.is(Platform.UNIX)) {
      if ((path = checkPath("/usr/bin/opera")) != null) return path;

    } else if (platform.is(Platform.WINDOWS)) {
      String progfiles = System.getenv("PROGRAMFILES");
      if (progfiles == null) progfiles = "\\Program Files";

      if ((path = checkPath(progfiles + "\\Opera\\opera.exe")) != null) return path;

    } else if (platform.is(Platform.MAC)) {
      if ((path = checkPath("/Applications/Opera.app/Contents/MacOS/Opera")) != null) return path;

    }

    return null;
  }

  /**
   * This method will try and find Opera Launcher on any system.
   * @return The path to Opera Launcher, or null
   */
	public String launcherPath() {
		String path = null;
		if ((path = checkPath(System.getenv("OPERA_LAUNCHER"))) != null)
			return path;

		return getOperaLauncherPath();
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
		URL res = OperaDriver.class.getClassLoader().getResource(launcherName);
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
					executablePath = FileUtils.getUserDirectoryPath() + IOUtils.DIR_SEPARATOR + "." + launcherName;

					File executable = new File(executablePath);

					if ((!executable.exists()) || FileUtils.isFileNewer(jarFile, executable)) {
						try {
							if (!executable.exists())
								FileUtils.touch(executable);

							IOUtils.copy(res.openStream(),
									new FileOutputStream(executable));
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

		StringBuilder launcherBuilder = new StringBuilder();

		launcherBuilder.append("launcher");
		launcherBuilder.append(File.separatorChar);

		switch (currentPlatform) {
		case LINUX:
		case UNIX:
			launcherBuilder.append(is64 ? "launcher-linux-x86_64" : "launcher-linux-i686");
			break;
		case MAC:
			launcherBuilder.append("launcher-mac");
			break;
		case WINDOWS:
		case VISTA:
		case XP:
			launcherBuilder.append("launcher-win32-i86pc.exe");
			break;
		default:
			throw new WebDriverException("Could not find a platfom that supports bundled launchers, please set it manually");
		}

		return launcherBuilder.toString();
	}

  /**
   * Check that the given path is not null, and exists.
   * @param path The path to check.
   * @return null if the path is null, or does not exist. Otherwise it returns
   * the given path.
   */
  private String checkPath(String path) {
    if (path == null) return null;

    File file = new File(path);
    if (file.exists()) return path;

    return null;
  }
}
