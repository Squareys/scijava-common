/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package org.scijava.plugin;

import org.scijava.AbstractContextual;
import org.scijava.Contextual;
import org.scijava.Prioritized;
import org.scijava.Priority;
import org.scijava.util.ClassUtils;

/**
 * Abstract base class for {@link Contextual}, {@link Prioritized} plugins that
 * retain access to their associated {@link PluginInfo} metadata via the
 * {@link HasPluginInfo} interface. This class is intended as a convenient
 * extension point for plugin type implementations.
 * 
 * @author Curtis Rueden
 */
public abstract class SortablePlugin extends AbstractContextual implements
	Prioritized, HasPluginInfo, SciJavaPlugin
{

	/** The priority of the plugin. */
	private double priority = Priority.NORMAL_PRIORITY;

	/** The metadata associated with the plugin. */
	private PluginInfo<?> info;

	// -- Object methods --

	@Override
	public String toString() {
		final PluginInfo<?> info = getInfo();
		return info == null ? getClass().getName() : info.getTitle();
	}

	// -- Prioritized methods --

	@Override
	public double getPriority() {
		return priority;
	}

	@Override
	public void setPriority(final double priority) {
		this.priority = priority;
	}

	// -- HasPluginInfo methods --

	@Override
	public PluginInfo<?> getInfo() {
		return info;
	}

	@Override
	public void setInfo(final PluginInfo<?> info) {
		this.info = info;
	}

	// -- Comparable methods --

	@Override
	public int compareTo(final Prioritized that) {
		if (that == null) return 1;

		// compare priorities
		final int priorityCompare = Priority.compare(this, that);
		if (priorityCompare != 0) return priorityCompare;

		// compare classes
		return ClassUtils.compare(getClass(), that.getClass());
	}

}
