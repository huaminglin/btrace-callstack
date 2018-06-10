package huaminglin.btrace.callstack.demo;

public class CallstackDemo {
    static {
        System.err.println("static");
    }

    static int staticMethod(int value) {
        return value + 1;
    }
    void throwsExceptionInner() throws Exception {
        System.err.println("throwsExceptionInner begin {");
        throw new Exception();
    }

    private synchronized void throwsExceptionOuter(int value) throws Exception {
        System.err.println("throwsExceptionOuter begin {");
        throwsExceptionInner();
        System.err.println("} throwsExceptionOuter end");
    }

    public static void main(String[] args) {
        System.err.println("main begin {");
        int value = staticMethod(args.length);
        try {
            new CallstackDemo().throwsExceptionOuter(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(3000); // Give seconds for btrace to dump log.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.err.println("} main end");
    }
}
