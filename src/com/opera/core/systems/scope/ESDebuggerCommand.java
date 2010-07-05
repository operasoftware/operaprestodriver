package com.opera.core.systems.scope;

import com.opera.core.systems.model.ICommand;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * This enum maps the commands for the EcmaScript Debugger service.
 *
 * The declaration can be found here:
 * http://dragonfly.opera.com/app/scope-interface/EcmascriptDebugger.html
 *
 */
public enum ESDebuggerCommand implements ICommand {
    LIST_RUNTIMES(1),
    EVAL(3),
    INSPECT_DOM(13),
    EXAMINE_OBJECTS(4),
    SET_CONFIGURATION(10),
    RELEASE_OBJECTS(29),
    RUNTIME_STARTED(14),
    RUNTIME_STOPPED(15),
    NEW_SCRIPT(16),
    THREAD_STARTED(17),
    THREAD_FINISHED(18),
    THREAD_STOPPED_AT(19),
    HANDLE_EVENT(20),
    OBJECT_SELECTED(21),
    PARSE_ERROR(28),
    DEFAULT(-1);

    private int code;
    private static final Map<Integer, ESDebuggerCommand> lookup = new HashMap<Integer, ESDebuggerCommand>();

    static {
        for (ESDebuggerCommand command : EnumSet.allOf(ESDebuggerCommand.class))
            lookup.put(command.getCommandID(), command);
    }

    private ESDebuggerCommand(int code) {
        this.code = code;
    }

    public int getCommandID() {
        return code;
    }

    public String getService() {
        return "ecmascript-debugger";
    }

    public static ESDebuggerCommand get(int code) {
        ESDebuggerCommand command = lookup.get(code);
        return (command != null) ? command : DEFAULT;
    }

}
