package org.mdkt.compiler;
public class HelloClass {
   public String hello() { return "hello".concat("-").concat(new HelloClass().show()); }

   public String show() {
      return "show";
   }
}

class WorldClass{

   private String name;

}

class WorldClass2{

   private String name;

}