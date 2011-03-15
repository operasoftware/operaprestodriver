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

import com.opera.core.systems.scope.protos.ConsoleLoggerProtos.ConsoleMessage;
import com.opera.core.systems.scope.protos.EsdbgProtos.RuntimeInfo;
import com.opera.core.systems.scope.protos.WmProtos.WindowInfo;

/**
 * Provided support for an alternate protocol. To be removed.
 *
 * @author Deniz Turkoglu
 *
 */
public class UmsEventHandler extends EventHandler {

	public UmsEventHandler(ScopeServices services) {
		super(services);
	}

	@Override
	public void onRuntimeStarted(RuntimeInfo info) {
		services.getDebugger().addRuntime(info);
	}

	@Override
	public void onUpdatedWindow(WindowInfo info) {
		services.getWindowManager().addWindow(info);
	}

	@Override
	public void onMessage(ConsoleMessage message) {
		throw new UnsupportedOperationException();
	}

}
