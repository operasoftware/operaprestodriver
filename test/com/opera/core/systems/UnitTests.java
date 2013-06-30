/*
Copyright 2012 Opera Software ASA

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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses(
    {
        ArchitectureTest.class,
        CanvasTest.class,
        ColorResultTest.class,
        OperaBinaryTest.class,
        OperaColorTest.class,
        OperaExtensionsTest.class,
        OperaFilePreferencesParserTest.class,
        OperaGenericPreferencesTest.class,
        OperaProfileTest.class,
        OperaSettingsCapabilitiesTest.class,
        OperaSettingsTest.class,
        OperaStringsTest.class,
        VersionUtilsTest.class
    }
)
public class UnitTests {

}
