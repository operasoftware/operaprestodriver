package com.opera.core.systems;
import java.util.List;

import com.opera.core.systems.model.Canvas;
import com.opera.core.systems.scope.protos.EcmascriptProtos.ReadyStateChange;
import com.opera.core.systems.scope.protos.EsdbgProtos.RuntimeInfo;
import com.opera.core.systems.scope.services.IEcmaScriptDebugger;

public class PseudoEcmaScriptDebugger implements IEcmaScriptDebugger {

    public void setRuntime(RuntimeInfo runtime) { }

    public void setRuntime(Runtime runtime) { }

    public Object scriptExecutor(String script, Object... params) {
        return null;
    }

    public void removeRuntime(int runtimeId) { }

    public List<String> listFramePaths() {
        return null;
    }

    public void init() { }

    public int getRuntimeId() {
        return 0;
    }

    public Integer getObject(String using) {
        return null;
    }

    public Integer executeScriptOnObject(String using, int objectId) {
        return null;
    }

    public Object executeScript(String using, boolean responseExpected) {
        return null;
    }

    public String executeJavascript(String using, boolean responseExpected) {
        return null;
    }

    public String executeJavascript(String using) {
        return null;
    }

    public List<Integer> examineObjects(Integer id) {
        return null;
    }

    public void cleanUpRuntimes() { }

    public void cleanUpRuntimes(int windowId) { }

    public void changeRuntime(String framePath) { }

    public Object callFunctionOnObject(String using, int objectId,
                boolean responseExpected) {
        return null;
    }

    public String callFunctionOnObject(String using, int objectId) {
        return null;
    }

    public void addRuntime(RuntimeInfo info) { }

    public void addRuntime(Runtime started) { }

    public void releaseObjects() { }

    public boolean updateRuntime() {
        return false;
    }

    public void resetRuntimesList() { }

	public void readyStateChanged(ReadyStateChange change) { }

	public void releaseObject(int objectId) { }

	public void resetFramePath() { }

	public Canvas getCanvas() {
		return null;
	}

	public void changeRuntime(int index) { }

	public String executeJavascript(String using, Integer windowId) {
		return null;
	}

	public Object examineScriptResult(Integer id) {
		// TODO Auto-generated method stub
		return null;
	}
}
