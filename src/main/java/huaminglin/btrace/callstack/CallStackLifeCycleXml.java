package huaminglin.btrace.callstack;

import com.sun.btrace.AnyType;
import com.sun.btrace.annotations.BTrace;
import com.sun.btrace.annotations.Kind;
import com.sun.btrace.annotations.Location;
import com.sun.btrace.annotations.OnMethod;
import com.sun.btrace.annotations.ProbeMethodName;
import com.sun.btrace.annotations.Where;

import static com.sun.btrace.BTraceUtils.println;

@BTrace(unsafe = true)
public class CallStackLifeCycleXml {

    @OnMethod(
            clazz = "/huaminglin.btrace.callstack.*/",
            method = "/.*/",
            location = @Location(value = Kind.ENTRY)
    )
    public static void onMethodEntry(
            @ProbeMethodName(fqn = true) String pmn
    ) {
        String classMethod = parseMethod(pmn);
        println(getIndentation() + "<" + formatXmlElementName(classMethod) + " method=\"" + pmn + "\">");
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
        println(getIndentation() + "</" + formatXmlElementName(classMethod) + ">");
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
        println(getIndentation() + "  <exception/>");
        println(getIndentation() + "</" + formatXmlElementName(classMethod) + ">");
    }

    private static String getIndentation() {
        Thread thread = Thread.currentThread();
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
}
