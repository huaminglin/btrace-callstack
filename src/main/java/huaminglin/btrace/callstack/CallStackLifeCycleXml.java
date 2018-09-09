package huaminglin.btrace.callstack;

import com.sun.btrace.AnyType;
import com.sun.btrace.annotations.BTrace;
import com.sun.btrace.annotations.Duration;
import com.sun.btrace.annotations.Kind;
import com.sun.btrace.annotations.Location;
import com.sun.btrace.annotations.OnMethod;
import com.sun.btrace.annotations.ProbeMethodName;
import com.sun.btrace.annotations.Return;
import com.sun.btrace.annotations.Self;
import com.sun.btrace.annotations.Where;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import com.sun.btrace.BTraceUtils;

@BTrace(unsafe = true)
public class CallStackLifeCycleXml {
    private static String getObjectString(Object object) {
        if (object == null) {
            return "null";
        }
        return object.toString();
    }

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
        printline("<" + formatXmlElementName(classMethod) + " method=\"" + escapeXml(pmn) + "\">", 0);
        if (selfValue != null) {
            printline("<this>" + selfValue.toString() + "</this>", 1);
        }
        if (arguments != null) {
            for (int i = 0; i < arguments.length; i++) {
                printline("<argument>" + getObjectString(arguments[i]) + "</argument>", 1);
            }
        }
    }

    @OnMethod(
            clazz = "/huaminglin.btrace.callstack.*/",
            method = "/.*/",
            location = @Location(value = Kind.RETURN)
    )
    public static void onMethodReturn(
            @ProbeMethodName(fqn = true) String pmn,
            @Self Object selfValue,
            @Return AnyType returnValue
//            @Duration long duration // The following error happens if @Duration is enabled:
// Exception in thread "main" java.lang.VerifyError: (class: huaminglin/btrace/callstack/demo/CallstackDemo, method: main signature: ([Ljava/lang/String;)V) Accessing value from uninitialized register pair 3/4
    ) {
        String classMethod = parseMethod(pmn);
        if (returnValue != AnyType.VOID) {
            printline("<return>" + getObjectString(returnValue) + "</return>", 1);
        }
//        printline("  <duration>" + duration + "</duration>");
        if (selfValue != null) {
            printline("<thisOnReturn>" + getObjectString(selfValue) + "</thisOnReturn>", 1);
        }
        printline("</" + formatXmlElementName(classMethod) + ">", 0);
    }

    @OnMethod(
            clazz = "/huaminglin.btrace.callstack.*/",
            method = "/.*/",
            location = @Location(value = Kind.ERROR, where = Where.BEFORE)
    )
    public static void onMethodError(
            @ProbeMethodName(fqn = true) String pmn,
            @Self Object selfValue,
            Throwable throwable,
            @Duration long duration
    ) {
        String classMethod = parseMethod(pmn);
        printline("<exception>" + throwable + "</exception>", 1);
        if (throwable != null && !EXCEPTION_STACKTRACES.contains(throwable)) {
            EXCEPTION_STACKTRACES.add(throwable);
            printline("<stacktrace>" + LINE_SEPARATOR + stackTraceStr(throwable) + "</stacktrace>", 1);
        }
        if (selfValue != null) {
            printline("<thisOnException>" + getObjectString(selfValue) + "</thisOnException>", 1);
        }
        printline("<duration>" + duration + "</duration>", 1);
        printline("</" + formatXmlElementName(classMethod) + ">", 0);
    }

    private static String getIndentation(Thread thread, int extraIndentation) {
        int length = getCallStackDepth() + extraIndentation;
        StringBuilder result = new StringBuilder();
        result.append(thread.getName() + "-" + thread.getId());
        for (int i = 0; i < length; i++) {
            result.append(ONE_IDENTATION);
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

    private static void printline(String line, int extraIndentation) {
        Thread thread = Thread.currentThread();
        if (!THREADS.contains(thread)) {
            THREADS.add(thread);
            // Add thread as root element. XML document requires a single root element.
            BTraceUtils.println(getIndentation(thread, -1) + "<thread>");
        }
        String indentation = getIndentation(thread, extraIndentation);
        String[] lines = line.split("\n");
        for (int i = 0; i < lines.length; i++) {
            BTraceUtils.println(indentation + lines[i]);
        }
    }

    private static String stackTraceStr(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    private static Set<Thread> THREADS = new HashSet<Thread>();
    private static Set<Throwable> EXCEPTION_STACKTRACES = new HashSet<Throwable>();
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String ONE_IDENTATION = "    ";
}
