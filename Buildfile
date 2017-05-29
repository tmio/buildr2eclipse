repositories.remote << "https://repo1.maven.org/maven2"

JRUBY='org.jruby:jruby-complete:jar:9.1.9.0'

ECLIPSE_PLUGINS=['org.eclipse.platform:org.eclipse.core.runtime:jar:3.12.0',
'org.eclipse.platform:org.eclipse.ui.ide:jar:3.12.3',
'org.eclipse.platform:org.eclipse.core.resources:jar:3.11.1',
'org.eclipse.platform:org.eclipse.ui:jar:3.108.1',
'org.eclipse.jdt:org.eclipse.jdt.ui:jar:3.12.2',
'org.eclipse.jdt:org.eclipse.jdt:jar:3.12.3',
'org.eclipse.jdt:org.eclipse.jdt.core:jar:3.12.3',
'org.eclipse.platform:org.eclipse.equinox.common:jar:3.8.0',
'org.eclipse.platform:org.eclipse.core.commands:jar:3.8.1',
'org.eclipse.platform:org.eclipse.jface:jar:3.12.2',
'org.eclipse.platform:org.eclipse.ui.workbench:jar:3.108.3',
'org.eclipse.platform:org.eclipse.core.jobs:jar:3.8.0',
'org.eclipse.platform:org.eclipse.osgi:jar:3.11.3'
]

VERSION_NUMBER="1.0.0-SNAPSHOT"

define 'buildr2eclipse', :version => VERSION_NUMBER, :group => 'org.apache.buildr' do
  file(_('buildr/gems')).enhance do 
    system("java -jar #{artifact(JRUBY).to_s} -S gem install buildr -v 1.5.3 --no-rdoc --no-ri -i #{_('buildr/gems')}")
  end
  file(_('jruby.jar')).enhance do
    cp artifact(JRUBY).to_s, _('jruby.jar')
  end
  resources.enhance [file(_('buildr/gems')), file(_('jruby.jar'))]
  compile.using(:target => '1.7', :source => '1.7').with JRUBY, ECLIPSE_PLUGINS
  package(:jar).include(_('jruby.jar'), _('plugin.xml'), _('META-INF'), _('build.properties'), _('buildr'))
end