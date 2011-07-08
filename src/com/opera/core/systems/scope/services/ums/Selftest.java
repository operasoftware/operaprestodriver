package com.opera.core.systems.scope.services.ums;

import java.util.List;
import java.util.logging.Logger;

import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.SelftestCommand;
import com.opera.core.systems.scope.protos.SelftestProtos.RunModulesArg;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.services.ISelftest;

public class Selftest extends AbstractService implements ISelftest {
	private final Logger logger = Logger.getLogger(this.getClass().getName());

	public Selftest(ScopeServices services, String version) {
		super(services, version);
		String serviceName = "selftest";

		if(!isVersionInRange(version, "2.0", serviceName)) {
			throw new UnsupportedOperationException(serviceName + " version " +
				version + " is not supported");
		}

		services.setSelftest(this);
	}

	public void runSelftests(List<String> modules) {
		logger.fine(String.format("runSelftests(%s)", modules.toString()));
		RunModulesArg.Builder builder = RunModulesArg.newBuilder();
		builder.addAllModuleList(modules);
		builder.setOutputType(RunModulesArg.OutputType.MACHINE_READABLE);

		Response response = executeCommand(SelftestCommand.RUN_MODULES, builder);
		logger.fine(String.format("Selftest response: %s", response));
	}
}
