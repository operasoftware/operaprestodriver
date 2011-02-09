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
package com.opera.core.systems.scope.services.ums;

import java.util.List;

import org.openqa.selenium.WebDriverException;

import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.PrefsCommand;
import com.opera.core.systems.ScopeServices;

//import org.openqa.selenium.WebDriverException;

import com.opera.core.systems.scope.protos.UmsProtos.Response;

import com.opera.core.systems.scope.services.IPrefs;
import com.opera.core.systems.scope.protos.PrefsProtos.GetPrefArg;
import com.opera.core.systems.scope.protos.PrefsProtos.GetPrefArg.Mode;
import com.opera.core.systems.scope.protos.PrefsProtos.ListPrefsArg;
import com.opera.core.systems.scope.protos.PrefsProtos.PrefValue;
import com.opera.core.systems.scope.protos.PrefsProtos.SetPrefArg;
import com.opera.core.systems.scope.protos.PrefsProtos.PrefList;
import com.opera.core.systems.scope.protos.PrefsProtos.Pref;

public class Prefs extends AbstractService implements IPrefs {

	public Prefs(ScopeServices services, String version) {
		super(services, version);
		String serviceName = "prefs";

		if (!isVersionInRange(version, "2.0", serviceName)) {
			throw new UnsupportedOperationException(serviceName + " version " + version + " is not supported");
		}

		services.setPrefs(this);
	}

	public void init() { }

	public String getPref(String section, String key, Mode mode) {
		GetPrefArg.Builder getPrefBuilder = GetPrefArg.newBuilder();
		getPrefBuilder.setSection(section);
		getPrefBuilder.setKey(key);
		getPrefBuilder.setMode(mode);

		Response response = executeCommand(PrefsCommand.GET_PREF, getPrefBuilder);

		PrefValue.Builder prefValueBuilder = PrefValue.newBuilder();
		buildPayload(response, prefValueBuilder);
		PrefValue prefsString = prefValueBuilder.build();

		return prefsString.getValue();
	}

	public List<Pref> listPrefs(boolean sort, String section) {
		ListPrefsArg.Builder listPrefBuilder = ListPrefsArg.newBuilder();
		listPrefBuilder.setSort(sort);
		listPrefBuilder.setSection(section);

		Response response = executeCommand(PrefsCommand.LIST_PREFS, listPrefBuilder);

		PrefList.Builder prefListBuilder = PrefList.newBuilder();
		buildPayload(response, prefListBuilder);
		PrefList prefList = prefListBuilder.build();

		return prefList.getPrefListList();
	}

	public void setPrefs(String section, String key, String value) {
		SetPrefArg.Builder setPrefBuilder = SetPrefArg.newBuilder();
		setPrefBuilder.setSection(section);
		setPrefBuilder.setKey(key);
		setPrefBuilder.setValue(value);

		Response response = executeCommand(PrefsCommand.SET_PREF, setPrefBuilder);

		if (response == null) {
			throw new WebDriverException("Internal error while setting a preference");
		}
	}

}
