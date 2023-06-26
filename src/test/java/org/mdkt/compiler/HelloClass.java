package org.mdkt.compiler;

public class HelloClass {

    public String hello() {
        return "hello-java-class".concat("-").concat(new HelloClass().show());
    }
    public String show() {
        return "show-java-class";
    }
}