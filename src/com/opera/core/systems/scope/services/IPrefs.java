package com.opera.core.systems.scope.services;

import java.util.List;

import com.opera.core.systems.scope.protos.PrefsProtos.Pref;
import com.opera.core.systems.scope.protos.PrefsProtos.GetPrefArg.Mode;


public interface IPrefs {
	void init();

	/**
	 * Gets a preference from the operaprefs.ini from 
	 * a specific section. Optionally it can get the
	 * default setting of the preference
	 * 
	 * @param section section to get the preference from
	 * @param key	key in the section
	 * @param mode  get the current or default setting of the preference
	 * 
	 * @return returns the preference in a string
	 */
	String getPref(String section, String key, Mode mode);
	
	/**
	 * Create a key event, this event controls if the key
	 * has been previously pressed before allowing key up
	 * @param key
	 * @param up down if false
	 */
	List<Pref> listPrefs(boolean sort, String section);
	
	/**
	 * Create a key event, this event controls if the key
	 * has been previously pressed before allowing key up
	 * @param key
	 * @param up down if false
	 */
	void setPrefs(String section, String key, String value);
}
