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

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.jruby.Ruby;
import org.jruby.RubyRuntimeAdapter;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

public class BuildrClasspathContainer implements IClasspathContainer, Serializable {

  private static final long serialVersionUID = -5468412686584809163L;

  private IJavaProject project;

  public BuildrClasspathContainer(IJavaProject project) {
    this.project = project;
  }

  public String getDescription() {
    return "Buildr dependencies";
  }

  public int getKind() {
    return IClasspathContainer.K_APPLICATION;
  }

  public synchronized IClasspathEntry[] getClasspathEntries() {
    try {
      URL buildrUrl = getClass().getClassLoader().getResource("/buildr/gems/");
      buildrUrl = FileLocator.toFileURL(buildrUrl);
      File dir = new File(buildrUrl.getFile());
      List<IClasspathEntry> entries = new ArrayList<>();
      Ruby runtime = JavaEmbedUtils.initialize(Collections.emptyList());
      RubyRuntimeAdapter evaler = JavaEmbedUtils.newRuntimeAdapter();
      // @formatter:off
      IRubyObject ret = evaler.eval(runtime,
          "Dir.chdir('" + project.getProject().getLocation().toOSString() + "')\n" +
          "ENV['GEM_HOME'] = '" + dir.getPath() + "'\n" +
          "ENV['GEM_PATH'] = ENV['GEM_HOME']\n" +
          "require 'rubygems'\n" +
          "require 'bundler/setup'\n" +
          "require 'buildr'\n" +
          "Buildr.application.run\n" +
          "deps = project('"+ project.getProject().getName() + "').compile.dependencies +"
              + " project('" + project.getProject().getName() + "').test.dependencies\n" +
          "deps.map(&:to_s).uniq.join('||')");
      // @formatter:on 
      String deps = (String) ret.toJava(String.class);
      for (Object dep : deps.split("\\|\\|")) {
        entries.add(JavaCore.newLibraryEntry(new Path((String) dep), null, null));
      }
      return entries.toArray(new IClasspathEntry[entries.size()]);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public IPath getPath() {
    return new Path("b2e.BUILDR_CONTAINER/dependencies");
  }

}
