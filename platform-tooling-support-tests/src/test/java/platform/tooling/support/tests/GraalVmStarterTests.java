/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support.tests;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static platform.tooling.support.tests.Projects.copyToWorkspace;

import java.nio.file.Path;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.DisabledOnOpenJ9;
import org.junit.jupiter.api.io.TempDir;

import platform.tooling.support.MavenRepo;
import platform.tooling.support.process.ProcessStarters;

/**
 * @since 1.9.1
 */
@Order(Integer.MIN_VALUE)
@DisabledOnOpenJ9
@EnabledIfEnvironmentVariable(named = "GRAALVM_HOME", matches = ".+")
class GraalVmStarterTests {

	@Test
	@Timeout(value = 10, unit = MINUTES)
	void runsTestsInNativeImage(@TempDir Path workspace) throws Exception {
		var result = ProcessStarters.gradlew() //
				.workingDir(copyToWorkspace(Projects.GRAALVM_STARTER, workspace)) //
				.addArguments("-Dmaven.repo=" + MavenRepo.dir()) //
				.addArguments("javaToolchains", "nativeTest", "--no-daemon", "--stacktrace", "--no-build-cache",
					"--warning-mode=fail") //
				.startAndWait();

		assertEquals(0, result.exitCode());
		assertThat(result.stdOutLines()) //
				.anyMatch(line -> line.contains("CalculatorTests > 1 + 1 = 2 SUCCESSFUL")) //
				.anyMatch(line -> line.contains("CalculatorTests > 1 + 100 = 101 SUCCESSFUL")) //
				.anyMatch(line -> line.contains("ClassLevelAnnotationTests$Inner > test() SUCCESSFUL")) //
				.anyMatch(line -> line.contains("BUILD SUCCESSFUL"));
	}
}
