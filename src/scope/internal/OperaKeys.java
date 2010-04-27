package com.opera.core.systems.scope.internal;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum OperaKeys {
	//TODO get a list of keys from core
	NULL         ("null"),
	CANCEL       ("cancel"), // ^break
	HELP         ("help"),
	BACK_SPACE   ("back space"),
	TAB          ("Tab"),
	CLEAR        ("clear"),
	RETURN       ("return"),
	ENTER        ("enter"),
	SHIFT        ("Shift"),
	LEFT_SHIFT   ("Shift"), // alias
	CONTROL      ("Control"),
	LEFT_CONTROL ("ctrl"), // alias
	ALT          ("alt"),
	LEFT_ALT     ("alt"), // alias
	PAUSE        ("pause"),
	ESCAPE       ("escape"),
	SPACE        (" "),
	PAGE_UP      ("page up"),
	PAGE_DOWN    ("page down"),
	END          ("end"),
	HOME         ("home"),
	LEFT         ("left"),
	ARROW_LEFT   ("left"), // alias
	UP           ("up"),
	ARROW_UP     ("up"), // alias
	RIGHT        ("right"),
	ARROW_RIGHT  ("right"), // alias
	DOWN         ("down"),
	ARROW_DOWN   ("down"), // alias
	INSERT       ("insert"),
	DELETE       ("del"),
	SEMICOLON    (":"),
	EQUALS       ("="),

	NUMPAD0      ("0"),  // number pad keys
	NUMPAD1      ("1"),
	NUMPAD2      ("2"),
	NUMPAD3      ("3"),
	NUMPAD4      ("4"),
	NUMPAD5      ("5"),
	NUMPAD6      ("6"),
	NUMPAD7      ("7"),
	NUMPAD8      ("8"),
	NUMPAD9      ("9"),
	MULTIPLY     ("*"),
	ADD          ("+"),
	SEPARATOR    (" "),
	SUBTRACT     ("-"),
	DECIMAL      ("."),
	DIVIDE       ("/"),

	F1           ("f1"),  // function keys
	F2           ("f2"),
	F3           ("f3"),
	F4           ("f4"),
	F5           ("f5"),
	F6           ("f6"),
	F7           ("f7"),
	F8           ("f8"),
	F9           ("f9"),
	F10          ("f10"),
	F11          ("f11"),
	F12          ("f12"),
	;
	
	private String value;
	
	public String getValue() {
		return value;
	}
	
	private OperaKeys(String value) {
		this.value = value;
	}
	
	private static final Map<String,String> lookup = new HashMap<String,String>();

	static {
    for(OperaKeys key : EnumSet.allOf(OperaKeys.class))
         lookup.put(key.name(), key.value);
	}

     public static String get(String key) { 
          return lookup.get(key); 
     }
}
