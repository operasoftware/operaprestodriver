package com.opera.core.systems.scope.services.ums;

//import java.util.LinkedList;
import java.util.List;

import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.DesktopUtilsCommand;
import com.opera.core.systems.ScopeServices;

//import org.openqa.selenium.WebDriverException;

import com.opera.core.systems.scope.protos.UmsProtos.Response;

import com.opera.core.systems.scope.services.IDesktopUtils;
import com.opera.core.systems.scope.protos.DesktopUtilsProtos.DesktopStringID;
import com.opera.core.systems.scope.protos.DesktopUtilsProtos.DesktopStringText;
import com.opera.core.systems.scope.protos.DesktopUtilsProtos.DesktopOperaPath;
import com.opera.core.systems.scope.protos.DesktopUtilsProtos.DesktopPreferencesPaths;

public class DesktopUtils extends AbstractService implements IDesktopUtils {

	public DesktopUtils(ScopeServices services, String version) {
		super(services, version);
		
		String serviceName = "desktop-utils";
		
		if(!isVersionInRange(version, "2.0", serviceName))
			throw new UnsupportedOperationException(serviceName + " version " + version + " is not supported");
		
		services.setDesktopUtils(this);
	}
	 
	public void init() {}
	  
	public String getString(String enum_text) {
		DesktopStringID.Builder stringBuilder = DesktopStringID.newBuilder();
		stringBuilder.setEnumText(enum_text);
		
		Response response = executeCommand(DesktopUtilsCommand.GET_STRING, stringBuilder);

		DesktopStringText.Builder stringTextBuilder = DesktopStringText.newBuilder();
		buildPayload(response, stringTextBuilder);
		DesktopStringText string_text = stringTextBuilder.build();

		// Remember to remove all CRLF
		return removeCR(string_text.getText());
	}
	
	public String removeCR(String text) {
		// Hack to remove all the \r's as we sometimes get just \n and sometimes
		// \r\n then the string comparison doesn't work
		return text.replaceAll("(\\r)", "");
	}

	public String getOperaPath() {
		Response response = executeCommand(DesktopUtilsCommand.GET_OPERA_PATH, null);

		DesktopOperaPath.Builder pathBuilder = DesktopOperaPath.newBuilder();
		buildPayload(response, pathBuilder);
		DesktopOperaPath string_path = pathBuilder.build();

		return string_path.getPath();
	}

	public List<String> getPreferencesPaths() {
		Response response = executeCommand(DesktopUtilsCommand.GET_PREFERENCES_PATHS, null);

		DesktopPreferencesPaths.Builder pathsBuilder = DesktopPreferencesPaths.newBuilder();
		buildPayload(response, pathsBuilder);
		DesktopPreferencesPaths string_paths = pathsBuilder.build();

		return string_paths.getPreferencePathListList();
	}

}
