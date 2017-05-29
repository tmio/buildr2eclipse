/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership.  The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.buildr.eclipse;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class BuildrNature implements IProjectNature {

  /**
   * ID of this project nature
   */
  public static final String NATURE_ID = "b2e.buildrNature";

  private IProject project;

  @Override
  public void configure() throws CoreException {
    if (project.hasNature(JavaCore.NATURE_ID)) {
      IJavaProject targetProject = JavaCore.create(project);
      IClasspathEntry[] entries = targetProject.getRawClasspath();
      Path buildrContainer = new Path("b2e.BUILDR_CONTAINER/dependencies");
      for (IClasspathEntry entry : entries) {
        if (entry.getPath().equals(buildrContainer)) {
          return;
        }
      }

      IClasspathEntry entry = JavaCore.newContainerEntry(buildrContainer);
      IClasspathEntry[] newEntries = new IClasspathEntry[entries.length + 1];
      System.arraycopy(entries, 0, newEntries, 0, entries.length);
      newEntries[entries.length] = entry;
      targetProject.setRawClasspath(newEntries, true, new NullProgressMonitor());
    }
  }

  @Override
  public void deconfigure() throws CoreException {
    if (project.hasNature(JavaCore.NATURE_ID)) {
      IJavaProject targetProject = JavaCore.create(project);
      IClasspathEntry[] entries = targetProject.getRawClasspath();
      Path buildrContainer = new Path("b2e.BUILDR_CONTAINER/dependencies");
      List<IClasspathEntry> newEntries = new ArrayList<>();
      for (IClasspathEntry entry : entries) {
        if (!entry.getPath().equals(buildrContainer)) {
          newEntries.add(entry);
        }
      }

      targetProject.setRawClasspath(newEntries.toArray(new IClasspathEntry[newEntries.size()]), true,
          new NullProgressMonitor());
    }
  }

  @Override
  public IProject getProject() {
    return project;
  }

  @Override
  public void setProject(IProject project) {
    this.project = project;
  }

}
