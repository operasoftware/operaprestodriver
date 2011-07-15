package com.opera.core.systems.scope.services.ums;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	static private Pattern errorPattern = Pattern.compile("Warning: Pattern '([^']+)' did not match any tests\n" +
			                                              "Warning: There is no module named '([^']+)'\n");
	static public List<SelftestResult> parseSelftests(String output) {
		List<SelftestResult> results = new ArrayList<SelftestResult>();

		// Check for non-existent module.
		Matcher matcher = errorPattern.matcher(output);
		if(matcher.matches()) {
			return null;
		}

		String[] lines = output.split("\\n");
		for(String line: lines) {
			/* Each line has the following format:
			 * tag:description result more
			 *
			 * result is PASS, FAIL, or SKIP.
			 */
			String[] pieces          = line.split("\\t");
			String tagAndDescription = pieces[0];
			String resultString      = pieces[1];
			String more              = pieces.length > 2? pieces[2] : null;

			String[] otherPieces = tagAndDescription.split(":", 2);
			String tag           = otherPieces[0];
			String description   = otherPieces[1];

			ResultType result;
			if(resultString.equals("PASS")) {
				result = ResultType.PASS;
			}
			else if(resultString.equals("FAIL")) {
				result = ResultType.FAIL;
			}
			else if(resultString.equals("SKIP")) {
				result = ResultType.SKIP;
			}
			else {
				throw new RuntimeException(String.format("Unknown test result %s", resultString));
			}

			results.add(new SelftestResult(tag, description, result, more));
		}

		return results;
	}
}
