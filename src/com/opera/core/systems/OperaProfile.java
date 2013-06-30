/*
Copyright 2011-2012 Opera Software ASA

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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opera.core.systems.preferences.OperaFilePreferences;
import com.opera.core.systems.preferences.OperaPreferences;

import org.json.JSONObject;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.io.TemporaryFilesystem;
import org.openqa.selenium.io.Zip;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * OperaProfile is an interface representing a profile (commonly a profile directory) for the Opera
 * web browser.  It's typically used by passing in an instance object of this class to the
 * "opera.profile" capability at construction of {@link OperaDriver}, like this:
 *
 * <pre>
 * {@code
 *   DesiredCapabilities capabilities = DesiredCapabilities.opera();
 *   OperaProfile profile = new OperaProfile();
 *
 *   profile.preferences().set("foo", "bar", "baz");
 *   capabilities.setCapability("opera.profile", profile);
 *
 *   WebDriver driver = new OperaDriver(capabilities);
 * }
 * </pre>
 *
 * Or by passing it in as an object directly to {@link OperaDriver}'s constructor:
 *
 * <pre>
 * {@code
 *   OperaProfile profile = new OperaProfile();
 *   profile.preferences().set("foo", "bar", "baz");
 *
 *   WebDriver driver = new OperaDriver(profile);
 * }
 * </pre>
 *
 * Among Opera's command-line arguments the profile may be referred to as "pd", short for "personal
 * directory".
 *
 * @see OperaPreferences
 */
public class OperaProfile {

  public static final String BASE64_JSON_KEY = "base64";
  private File directory;
  private File preferenceFile;
  private boolean randomProfile = false;
  private OperaPreferences preferences;
  private OperaExtensions oexManager;

  /**
   * Creates a new, fresh random profile for Opera to use.  The actual profile directory will not be
   * generated until Opera is started.
   *
   * This is supported on all products that have the <code>-pd</code> command-line argument, such as
   * Opera Desktop.  core-gogi and Opera Mobile is not supported.
   */
  public OperaProfile() {
    this(TemporaryFilesystem.getDefaultTmpFS().createTempDir("opera", "profile"));
    randomProfile = true;
  }

  /**
   * Creates a representation of the profile in the given directory path.  If you specify a profile
   * directory that does not exist Opera will generate a new, fresh profile in the same location
   * when it is started.
   *
   * @param profileDirectoryPath the path to the profile directory to use
   */
  public OperaProfile(String profileDirectoryPath) {
    this(new File(profileDirectoryPath));
  }

  /**
   * Creates a representation of the profile in the given directory.  If you specify a profile
   * directory that does not exist Opera will generate a new, fresh profile in the same location
   * when it is started.
   *
   * @param profileDirectory the profile to use
   * @throws IllegalArgumentException if <code>profileDirectory</code> is not set
   */
  public OperaProfile(File profileDirectory) {
    // Opera generates profile if it doesn't exist, so we don't have to worry about whether
    // profileDirectory exists or not.
    checkArgument(!profileDirectory.getPath().isEmpty(), "Profile directory path is empty");

    directory = profileDirectory;
    preferenceFile = getPreferenceFile(directory);

    // Log whether directory exists or not for convenience
    if (!directory.exists()) {
      if (!directory.mkdirs()) {
        throw new WebDriverException("Unable to create directory path: " + directory.getPath());
      }
    }

    // Load preferences from profile if preference file exists, or create a new preference file
    setPreferences(new OperaFilePreferences(preferenceFile));

    // Create an instance to manage the installation of custom Opera extensions.
    oexManager = new OperaExtensions(profileDirectory);
  }

  /**
   * Install an Opera 12 extension to the profile
   * @param extensionToInstall Location of the extension
   */
  public void addExtension(File extensionToInstall) throws IOException {
    oexManager.addExtension(extensionToInstall);
  }

  /**
   * Allows access to the preferences in the current profile.
   *
   * @return an {@link OperaPreferences} object representing preferences in the current profile
   */
  public OperaPreferences preferences() {
    return preferences;
  }

  /**
   * Replaces the preferences in the profile with the given preferences.
   *
   * @param newPreferences the new preferences to populate the profile with
   */
  public void setPreferences(OperaPreferences newPreferences) {
    if (!(newPreferences instanceof OperaFilePreferences)) {
      OperaFilePreferences convertedPreferences = new OperaFilePreferences(preferenceFile);
      convertedPreferences.merge(newPreferences);
      preferences = convertedPreferences;
    } else {
      preferences = newPreferences;
    }
  }

  /**
   * Gets the current profile directory.
   *
   * @return the directory where this profile is stored
   */
  public File getDirectory() {
    return directory;
  }

  /**
   * Cleans up the profile.  If the profile was random, it will be deleted.  This method should
   * typically be called when Opera is shut down.
   */
  public void cleanUp() {
    if (randomProfile) {
      TemporaryFilesystem.getDefaultTmpFS().deleteTempDir(getDirectory());
    }
    oexManager.cleanUp();
  }

  /**
   * Converts this instance to its JSON representation.
   *
   * @return the JSON representation of this profile
   * @throws IOException if an I/O error occurs
   */
  public JSONObject toJson() throws IOException {
    String base64 = new Zip().zip(getDirectory());
    Map<String, String> map = ImmutableMap.of(
      "className", this.getClass().getName(),
      BASE64_JSON_KEY, base64);
    
    return new JSONObject(map);
  }

  /**
   * Converts a JSON string object to a profile representation, returning a new instance of {@link
   * OperaProfile}.  This is used by the JsonWireProtocol to transfer a profile.
   *
   * @param json a base64-encoded JSON string (archive) representing a profile
   * @return a new profile representation
   * @throws IOException if an I/O error occurs
   */
  public static OperaProfile fromJson(String json) throws IOException {
    File directory = TemporaryFilesystem.getDefaultTmpFS().createTempDir("opera", "duplicate");
    new Zip().unzip(json, directory);
    return new OperaProfile(directory);
  }

  /**
   * Opera preference files can be named either <code>opera.ini</code> or
   * <code>operaprefs.ini</code> depending on the product.  This method will look in the specified
   * directory for either of the files and return the one that exists.  <code>operaprefs.ini</code>
   * has priority.
   *
   * @param directory the directory to look for a preference file
   * @return a preference file
   */
  private static File getPreferenceFile(final File directory) {
    List<File> candidates = ImmutableList.of(
      new File(directory, "operaprefs.ini"),
      new File(directory, "opera.ini"));

    for (File candidate : candidates) {
      if (candidate.exists()) {
        return candidate;
      }
    }

    return candidates.get(0);
  }

}
