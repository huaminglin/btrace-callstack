# btrace-callstack
Use btrace to probe Java process and get a XML file to describe call stack lifecycle of Java threads

1. Install btrace-boot.jar to local maven repository
mvn install:install-file -Dfile=btrace-boot.jar -DgroupId=com.sun.tools.btrace -DartifactId=btrace-boot -Dversion=1.3.9 -Dpackaging=jar

3. Setup BTRACE_HOME environment variable
# Use 1.3.9.
# It seems that btrace 1.3.11 doesn't work as expected. If Kind.ERROR is triggered, the exception can't be caught.

export BTRACE_HOME=$HOME/bin/btrace-bin-1.3.9

4. Compile the btrace script
$BTRACE_HOME/bin/btracec -d /tmp -trusted src/main/java/huaminglin/btrace/callstack/CallStackLifeCycleXml.java

5. Compile current maven project
mvn package

6. Run current project and btrace it
$BTRACE_HOME/bin/btracer -u -o /tmp/callstack-lifecycle.btrace -scp target/btrace-callstack-1.0-SNAPSHOT.jar /tmp/huaminglin/btrace/callstack/CallStackLifeCycleXml.class huaminglin.btrace.callstack.demo.CallstackDemo

7. Filter the xml lines from the .btrace file by thread
# Get the lines for thread "main-1" and strip the prefix
grep -oP "^main-1        \K.*"  /tmp/callstack-lifecycle.btrace > /tmp/callstack-lifecycle-main-1.xml

8. Use an XML file viewer to load /tmp/callstack-lifecycle-main-1.xml
Append the missing closing tags manually:
  </CallstackDemo.main></thread>
