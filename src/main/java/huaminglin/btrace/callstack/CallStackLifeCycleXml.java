package huaminglin.btrace.callstack;

import com.sun.btrace.AnyType;
import com.sun.btrace.annotations.BTrace;
import com.sun.btrace.annotations.Kind;
import com.sun.btrace.annotations.Location;
import com.sun.btrace.annotations.OnMethod;
import com.sun.btrace.annotations.ProbeMethodName;
import com.sun.btrace.annotations.Self;
import com.sun.btrace.annotations.Where;

import java.util.HashSet;
import java.util.Set;

import com.sun.btrace.BTraceUtils;

@BTrace(unsafe = true)
public class CallStackLifeCycleXml {

    @OnMethod(
            clazz = "/huaminglin.btrace.callstack.*/",
            method = "/.*/",
            location = @Location(value = Kind.ENTRY)
    )
    public static void onMethodEntry(
            @ProbeMethodName(fqn = true) String pmn,
            @Self Object selfValue,
            AnyType[] arguments
    ) {
        String classMethod = parseMethod(pmn);
        printline("<" + formatXmlElementName(classMethod) + " method=\"" + escapeXml(pmn) + "\">");
        if (selfValue != null) {
            printline("<this>" + selfValue.toString() + "</this>");
        }
        if (arguments != null) {
            for (int i = 0; i < arguments.length; i++) {
                printline("<argument>" + arguments[i].toString() + "</argument>");
            }
        }
    }

    @OnMethod(
            clazz = "/huaminglin.btrace.callstack.*/",
            method = "/.*/",
            location = @Location(value = Kind.RETURN)
    )
    public static void onMethodReturn(
            @ProbeMethodName(fqn = true) String pmn
    ) {
        String classMethod = parseMethod(pmn);
        printline("</" + formatXmlElementName(classMethod) + ">");
    }

    @OnMethod(
            clazz = "/huaminglin.btrace.callstack.*/",
            method = "/.*/",
            location = @Location(value = Kind.ERROR, where = Where.BEFORE)
    )
    public static void onMethodError(
            @ProbeMethodName(fqn = true) String pmn
    ) {
        String classMethod = parseMethod(pmn);
        printline("  <exception/>");
        printline("</" + formatXmlElementName(classMethod) + ">");
    }

    private static String getIndentation(Thread thread) {
        int length = getCallStackDepth();
        StringBuilder result = new StringBuilder();
        result.append(thread.getName() + "-" + thread.getId());
        for (int i = 0; i < length; i++) {
            result.append("  ");
        }
        return result.toString();
    }

    private static int getCallStackDepth() {
        return new Throwable().getStackTrace().length;
    }

    private static String parseMethod(String methodFqn) {
        int leftBtrace = methodFqn.indexOf("(");
        if (leftBtrace < 0) {
            leftBtrace = methodFqn.length();
        }
        int foundSpace = methodFqn.substring(0, leftBtrace).lastIndexOf(" ");
        String classmethod = methodFqn.substring(foundSpace + 1, leftBtrace);
        String[] strings = classmethod.split("#");
        if (strings.length != 2) {
            return "unexpected method fqn";
        }
        String[] classParts = strings[0].split("\\.");
        return classParts[classParts.length - 1] + '.' + strings[1];
    }

    private static String formatXmlElementName(String name) {
        return name.replaceAll("[^-a-zA-Z0-9_.]", "_");
    }

    private static String escapeXml(String value) {
        return value.replaceAll("&", "&amp;").replaceAll(">", "&gt;").replaceAll("<", "&lt;").replaceAll("\"", "&quot;").replaceAll("'", "&apos;");
    }

    private static void printline(String line) {
        Thread thread = Thread.currentThread();
        String indentation = getIndentation(thread);
        if (!THREADS.contains(thread)) {
            THREADS.add(thread);
            // Add thread as root element. XML document requires a single root element.
            BTraceUtils.println(indentation + "<thread>");
        }
        BTraceUtils.println(indentation + line);
    }

    private static Set<Thread> THREADS = new HashSet();
}
