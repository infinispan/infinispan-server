#!/usr/bin/python
import re
import sys
import os
import subprocess
import shutil
from datetime import *
from multiprocessing import Process

try:
  from xml.etree.ElementTree import ElementTree
except:
  print '''
        Welcome to the EDG Release Script.
        This release script requires that you use at least Python 2.5.0.  It appears
        that you do not thave the ElementTree XML APIs available, which are available
        by default in Python 2.5.0.
        '''
  sys.exit(1)
  
from pythonTools import *

modules = []
uploader = None
svn_conn = None

#def getModules(directory):
#    # look at the pom.xml file
#    tree = ElementTree()
#    f = directory + "/pom.xml"
#    print "Parsing %s to get a list of modules in project" % f
#    tree.parse(f)
#    mods = tree.findall(".//{%s}module" % maven_pom_xml_namespace)
#    asVersion = tree.find(".//{%s}version.org.jboss.as" % maven_pom_xml_namespace)
#    print "AS version %s" % asVersion.text
#    for m in mods:
#        modules.append(m.text)

def helpAndExit():
    print '''
        Welcome to the EDG Release Script.
        
        Usage:
        
            $ bin/release.py <version> <branch to tag from>
            
        E.g.,
        
            $ bin/release.py 6.0.0.Alpha1 <-- this will tag off trunk.
            
            $ bin/release.py 6.0.0.Alpha1 branches/6.0.x <-- this will use the appropriate branch
            
        Please ensure you have edited bin/release.py to suit your ennvironment.
        There are configurable variables at the start of this file that is
        specific to your environment.
    '''
    sys.exit(0)

def validateVersion(version):  
  versionPattern = get_version_pattern()
  if versionPattern.match(version):
    return version.strip().upper()
  else:
    print "Invalid version '"+version+"'!\n"
    helpAndExit()

def tagInSubversion(version, newVersion, branch):
  try:
    svn_conn.tag("%s/%s" % (settings[svn_base_key], branch), newVersion, version)
  except:
    print "FATAL: Unable to tag.  Perhaps branch %s does not exist on Subversion URL %s." % (branch, settings[svn_base_key])
    print "FATAL: Cannot continue!"
    sys.exit(200)

def getProjectVersionTag(tree):
  return tree.find("./{%s}version" % (maven_pom_xml_namespace))

def getParentVersionTag(tree):
  return tree.find("./{%s}parent/{%s}version" % (maven_pom_xml_namespace, maven_pom_xml_namespace))

def getPropertiesVersionTag(tree):
  return tree.find("./{%s}properties/{%s}project-version" % (maven_pom_xml_namespace, maven_pom_xml_namespace))

def writePom(tree, pomFile):
  tree.write("tmp.xml", 'UTF-8')
  in_f = open("tmp.xml")
  out_f = open(pomFile, "w")
  try:
    for l in in_f:
      newstr = l.replace("ns0:", "").replace(":ns0", "").replace("ns1", "xsi")
      out_f.write(newstr)
  finally:
    in_f.close()
    out_f.close()        
  if settings['verbose']:
    print " ... updated %s" % pomFile

def patch(pomFile, version):
  ## Updates the version in a POM file
  ## We need to locate //project/parent/version, //project/version and //project/properties/project-version
  ## And replace the contents of these with the new version
  if settings['verbose']:
    print "Patching %s" % pomFile
  tree = ElementTree()
  tree.parse(pomFile)    
  need_to_write = False
  
  tags = []
  tags.append(getParentVersionTag(tree))
  tags.append(getProjectVersionTag(tree))
  tags.append(getPropertiesVersionTag(tree))
  
  for tag in tags:
    if tag != None:
      if tag.text.endswith('-SNAPSHOT'):
        if settings['verbose']:
          print "%s is %s.  Setting to %s" % (str(tag), tag.text, version)
        tag.text=version
        need_to_write = True
      else:
        print "No snapshot mark in version"
    
  if need_to_write:
    # write to file again!
    writePom(tree, pomFile)
  else:
    if settings['verbose']:
      print "File doesn't need updating; nothing replaced!"

def get_poms_to_patch(workingDir):
  # Trying to read the modules from the root pom requires fiddling with system
  # property definition and substituting it. Much easier to simply go over all
  # pom.xml definitions in this tree.

#  getModules(workingDir)
#  print 'Available modules are ' + str(modules)
  pomsToPatch = [workingDir + "/pom.xml"]
  for m in modules:
    pomsToPatch.append(workingDir + "/" + m + "/pom.xml")
    # Look for additional POMs that are not directly referenced!
  for additionalPom in GlobDirectoryWalker(workingDir, 'pom.xml'):
    if additionalPom not in pomsToPatch:
      pomsToPatch.append(additionalPom)
      
  return pomsToPatch

def updateVersions(version, workingDir, trunkDir):
  svn_conn.checkout(settings[svn_base_key] + "/tags/" + version, workingDir)
    
  pomsToPatch = get_poms_to_patch(workingDir)

  print "Patch poms: %s" % pomsToPatch

  for pom in pomsToPatch:
    patch(pom, version)

  print "Update version in data grid constants file"
  ## Now look for version in data grid constants
  version_java = workingDir + "/integration/src/main/java/com/redhat/datagrid/DataGridConstants.java"

  f_in = open(version_java)
  f_out = open(version_java+".tmp", "w")

  regexp = re.compile('\s*private static final (String (MAJOR|MINOR|MICRO|MODIFIER)|boolean SNAPSHOT)')
  pieces = version.split('.')
  try:
    for l in f_in:
      print "Check line %s" %l
      if regexp.match(l):
        if l.find('MAJOR') > -1:
          f_out.write('   private static final String MAJOR = "%s";\n' % pieces[0])
        elif l.find('MINOR') > -1:
          f_out.write('   private static final String MINOR = "%s";\n' % pieces[1])
        elif l.find('MICRO') > -1:
          f_out.write('   private static final String MICRO = "%s";\n' % pieces[2])
        elif l.find('MODIFIER') > -1:
          f_out.write('   private static final String MODIFIER = "%s";\n' % pieces[3])
        elif l.find('SNAPSHOT') > -1:
          f_out.write('   private static final boolean SNAPSHOT = false;\n')
      else:
        f_out.write(l)
  finally:
    f_in.close()
    f_out.close()

  os.rename(version_java+".tmp", version_java)

  # Now make sure this goes back into SVN.
  checkInMessage = "EDG Release Script: Updated version numbers"
  svn_conn.checkin(workingDir, checkInMessage)

def buildAndTest(workingDir):
  os.chdir(workingDir)
  maven_build_distribution()

def getModuleName(pomFile):
  tree = ElementTree()
  tree.parse(pomFile)
  return tree.findtext("./{%s}artifactId" % maven_pom_xml_namespace)

def do_task(target, args, async_processes):
  if settings['multi_threaded']:
    async_processes.append(Process(target = target, args = args))  
  else:
    target(*args)

### This is the starting place for this script.
def release():
  global settings
  global uploader
  global svn_conn
  assert_python_minimum_version(2, 5)
  require_settings_file()
    
  # We start by determining whether the version passed in is a valid one
  if len(sys.argv) < 2:
    helpAndExit()
  
  base_dir = os.getcwd()
  version = validateVersion(sys.argv[1])
  branch = "trunk"
  if len(sys.argv) > 2:
    branch = sys.argv[2]
    
  print "Releasing EDG version %s from branch '%s'" % (version, branch)
  print "Please stand by!"
  
  ## Set up network interactive tools
  if settings['dry_run']:
    # Use stubs
    print "*** This is a DRY RUN.  No changes will be committed.  Used to test this release script only. ***"
    print "Your settings are %s" % settings
    uploader = DryRunUploader()
    svn_conn = DryRunSvnConn()
  else:
    uploader = Uploader()
    svn_conn= SvnConn()
  
  ## Release order:
  # Step 1: Tag in SVN
  newVersion = "%s/tags/%s" % (settings[svn_base_key], version)
  print "Step 1: Tagging %s in SVN as %s" % (branch, newVersion)
  tagInSubversion(version, newVersion, branch)
  print "Step 1: Complete"
  
  workingDir = settings[local_tags_dir_key] + "/" + version
    
  # Step 2: Update version in tagged files
  print "Step 2: Updating version number in source files"
  updateVersions(version, workingDir, base_dir)
  print "Step 2: Complete"
  
  # Step 3: Build and test in Maven2
  print "Step 3: Build and test in Maven2"
  buildAndTest(workingDir)
  print "Step 3: Complete"
  
  async_processes = []
  
  ## Wait for processes to finish
  for p in async_processes:
    p.start()
  
  for p in async_processes:
    p.join()
  
  print "\n\n\nDone!"

if __name__ == "__main__":
  release()
