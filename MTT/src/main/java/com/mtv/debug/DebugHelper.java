package com.mtv.debug;


public class DebugHelper {
    static boolean isDebugMode = false;
    public static void print(String content, StackTraceElement stack) {
        String finalContent = content;
        if (isDebugMode) finalContent +=  "\u001B[37m"    //low-visibality
                + "\t :: ["+stack+"]"  //code path
                + "\u001B[0m ";
        System.out.println(finalContent);
    }

    public static void print(String content) {
        String finalContent = content;
        if (isDebugMode) {
            StackTraceElement stack = new Throwable().getStackTrace()[1];
            finalContent += "\u001B[37m"    //low-visibility
                    + "\t :: ["+stack+"]"  //code path
                    + "\u001B[0m ";
        }
        System.out.println(finalContent);
    }
}
