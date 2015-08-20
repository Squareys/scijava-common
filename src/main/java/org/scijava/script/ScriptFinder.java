/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2015 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.script;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.scijava.AbstractContextual;
import org.scijava.MenuEntry;
import org.scijava.MenuPath;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

/**
 * Discovers scripts.
 * <p>
 * To accomplish this, we crawl the directories specified by
 * {@link ScriptService#getScriptDirectories()}. By default, those directories
 * include the {@code scripts} and {@code plugins/Scripts} folders off the
 * SciJava application's base directory.
 * </p>
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
public class ScriptFinder extends AbstractContextual {

	private static final String SCRIPT_ICON = "/icons/script_code.png";

	private final ScriptService scriptService;

	@Parameter
	private LogService log;

	public ScriptFinder(final ScriptService scriptService) {
		this.scriptService = scriptService;
		setContext(scriptService.getContext());
	}

	// -- ScriptFinder methods --

	/**
	 * Discovers the scripts.
	 * 
	 * @param scripts The collection to which the discovered scripts are added.
	 */
	public void findScripts(final List<ScriptInfo> scripts) {
		final List<File> directories = scriptService.getScriptDirectories();

		int scriptCount = 0;

		final HashSet<File> scriptFiles = new HashSet<File>();
		for (final File directory : directories) {
			if (!directory.exists()) {
				log.debug("Ignoring non-existent scripts directory: " +
					directory.getAbsolutePath());
				continue;
			}
			final MenuPath prefix = scriptService.getMenuPrefix(directory);
			final MenuPath menuPath = prefix == null ? new MenuPath() : prefix;
			scriptCount +=
				discoverScripts(scripts, scriptFiles, directory, menuPath);
		}

		log.debug("Found " + scriptCount + " scripts");
	}

	// -- Helper methods --

	/**
	 * Looks through a directory, discovering and adding scripts.
	 * 
	 * @param scripts The collection to which the discovered scripts are added.
	 * @param directory The directory in which to look for scripts recursively.
	 * @param menuPath The menu path, which must not be {@code null}.
	 */
	private int discoverScripts(final List<ScriptInfo> scripts,
		final Set<File> scriptFiles, final File directory, final MenuPath menuPath)
	{
		final File[] fileList = directory.listFiles();
		if (fileList == null) return 0; // directory does not exist
		Arrays.sort(fileList);

		int scriptCount = 0;
		final boolean isTopLevel = menuPath.size() == 0;

		for (final File file : fileList) {
			if (scriptFiles.contains(file)) continue; // script already added

			final String name = file.getName().replace('_', ' ');
			if (file.isDirectory()) {
				// recurse into subdirectory
				discoverScripts(scripts, scriptFiles, file, subMenuPath(menuPath, name));
			}
			else if (isTopLevel) {
				// ignore scripts in toplevel script directories
				continue;
			}
			else if (scriptService.canHandleFile(file)) {
				// found a script!
				final int dot = name.lastIndexOf('.');
				final String noExt = dot <= 0 ? name : name.substring(0, dot);
				scripts.add(createEntry(file, subMenuPath(menuPath, noExt)));
				scriptFiles.add(file);
				scriptCount++;
			}
		}

		return scriptCount;
	}

	private MenuPath
		subMenuPath(final MenuPath menuPath, final String subMenuName)
	{
		final MenuPath result = new MenuPath(menuPath);
		result.add(new MenuEntry(subMenuName));
		return result;
	}

	private ScriptInfo
		createEntry(final File scriptFile, final MenuPath menuPath)
	{
		final ScriptInfo info = new ScriptInfo(getContext(), scriptFile);
		info.setMenuPath(menuPath);

		// flag script with special icon
		menuPath.getLeaf().setIconPath(SCRIPT_ICON);

		return info;
	}

}
