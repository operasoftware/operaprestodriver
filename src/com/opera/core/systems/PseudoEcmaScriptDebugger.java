package com.opera.core.systems;
import java.util.List;

import com.opera.core.systems.scope.protos.EsdbgProtos.RuntimeInfo;
import com.opera.core.systems.scope.services.IEcmaScriptDebugger;

public class PseudoEcmaScriptDebugger implements IEcmaScriptDebugger {

    public void setRuntime(RuntimeInfo runtime) {
        // TODO Auto-generated method stub

    }

    public void setRuntime(Runtime runtime) {
        // TODO Auto-generated method stub

    }

    public Object scriptExecutor(String script, Object... params) {
        return null;
    }

    public void removeRuntime(int runtimeId) {
        // TODO Auto-generated method stub

    }

    public List<String> listFramePaths() {
        // TODO Auto-generated method stub
        return null;
    }

    public void init() {
        // TODO Auto-generated method stub

    }

    public int getRuntimeId() {
        // TODO Auto-generated method stub
        return 0;
    }

    public Integer getObject(String using) {
        // TODO Auto-generated method stub
        return null;
    }

    public Integer executeScriptOnObject(String using, int objectId) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object executeScript(String using, boolean responseExpected) {
        // TODO Auto-generated method stub
        return null;
    }

    public String executeJavascript(String using, boolean responseExpected) {
        // TODO Auto-generated method stub
        return null;
    }

    public String executeJavascript(String using) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<Integer> examineObjects(Integer id) {
        // TODO Auto-generated method stub
        return null;
    }

    public void cleanUpRuntimes() {
        // TODO Auto-generated method stub

    }

    public void cleanUpRuntimes(int windowId) {
        // TODO Auto-generated method stub

    }

    public void changeRuntime(int index) {
        // TODO Auto-generated method stub

    }

    public void changeRuntime(String framePath) {
        // TODO Auto-generated method stub

    }

    public Object callFunctionOnObject(String using, int objectId,
                boolean responseExpected) {
        // TODO Auto-generated method stub
        return null;
    }

    public String callFunctionOnObject(String using, int objectId) {
        // TODO Auto-generated method stub
        return null;
    }

    public void addRuntime(RuntimeInfo info) {
        // TODO Auto-generated method stub

    }

    public void addRuntime(Runtime started) {
        // TODO Auto-generated method stub

    }

    public void releaseObjects() {
        // TODO Auto-generated method stub

    }

    public boolean updateRuntime() {
        // TODO Auto-generated method stub
        return false;
    }

    public void resetRuntimesList() {
        // TODO Auto-generated method stub

    }
}
