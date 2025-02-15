package org.mdkt.compiler;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class InMemoryJavaCompilerTest {
	private static final Logger logger = LoggerFactory.getLogger(InMemoryJavaCompilerTest.class);

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void compile_WhenTypical() throws Exception {
		StringBuilder sourceCode = new StringBuilder();

		sourceCode.append("package org.mdkt;\n");
		sourceCode.append("public class HelloClass {\n");
		sourceCode.append("   public String hello() { return \"hello\"; }");
		sourceCode.append("}");

		Class<?> helloClass = InMemoryJavaCompiler.newInstance().compile("org.mdkt.HelloClass", sourceCode.toString());
		Assert.assertNotNull(helloClass);
		Assert.assertEquals(1, helloClass.getDeclaredMethods().length);
	}

	@Test
	public void compile_WhenTypicalUpdateClass() throws Exception {
		StringBuilder sourceCode = new StringBuilder();

		sourceCode.append("package org.mdkt.compiler;\n");
		sourceCode.append("public class HelloClass {\n");
		sourceCode.append("   public String hello() { return \"hello1\"; }");
		sourceCode.append("}");

		Class<?> oldClass = HelloClass.class;
		Class<?> newClass = InMemoryJavaCompiler.newInstance().compile("org.mdkt.compiler.HelloClass", sourceCode.toString());

		Assert.assertNotEquals(oldClass.hashCode() , newClass.hashCode());
		Assert.assertNotEquals(oldClass.getDeclaredMethod("hello").invoke(oldClass.newInstance()) ,
				newClass.getDeclaredMethod("hello").invoke(newClass.newInstance()));
	}

	@Test
	public void compileAll_WhenTypical() throws Exception {
		String cls1 = "public class A{ public B b() { return new B(); }}";
		String cls2 = "public class B{ public String toString() { return \"B!\"; }}";
		String cls3 = "import org.mdkt.compiler.ShanhyTest; public class C{ public String hello() { return new ShanhyTest().hello(); }}";

		Map<String, Class<?>> compiled = InMemoryJavaCompiler.newInstance()
				.addSource("A", cls1)
//				.addSource("A", cls1)
//				.addSource("A", cls1)
				.addSource("B", cls2)
				.addSource("C", cls3)
				.compileAll();

		Assert.assertNotNull(compiled.get("A"));
		Assert.assertNotNull(compiled.get("B"));
		Assert.assertNotNull(compiled.get("C"));

		Class<?> aClass = compiled.get("A");
		Object a = aClass.newInstance();
		Assert.assertEquals("B!", aClass.getMethod("b").invoke(a).toString());

		Class<?> cClass = compiled.get("C");
		Object c = cClass.newInstance();
		String helloInvokeResult = cClass.getMethod("hello").invoke(c).toString();
		System.out.println("helloInvokeResult >>> " + helloInvokeResult);
		Assert.assertEquals("Hello Shanhy", helloInvokeResult);
	}

	@Test
	public void compile_WhenSourceContainsInnerClasses() throws Exception {
		StringBuffer sourceCode = new StringBuffer();

		sourceCode.append("package org.mdkt;\n");
		sourceCode.append("public class HelloClass {\n");
		sourceCode.append("   private static class InnerHelloWorld { int inner; }\n");
		sourceCode.append("   public String hello() { return \"hello\"; }");
		sourceCode.append("}");

		Class<?> helloClass = InMemoryJavaCompiler.newInstance().compile("org.mdkt.HelloClass", sourceCode.toString());
		Assert.assertNotNull(helloClass);
		Assert.assertEquals(1, helloClass.getDeclaredMethods().length);
	}

	@Test
	public void compile_whenError() throws Exception {
		thrown.expect(CompilationException.class);
		thrown.expectMessage("Unable to compile the source");
		StringBuffer sourceCode = new StringBuffer();

		sourceCode.append("package org.mdkt;\n");
		sourceCode.append("public classHelloClass {\n");
		sourceCode.append("   public String hello() { return \"hello\"; }");
		sourceCode.append("}");
		InMemoryJavaCompiler.newInstance().compile("org.mdkt.HelloClass", sourceCode.toString());
	}

	@Test
	public void compile_WhenFailOnWarnings() throws Exception {
		thrown.expect(CompilationException.class);
		StringBuffer sourceCode = new StringBuffer();

		sourceCode.append("package org.mdkt;\n");
		sourceCode.append("public class HelloClass {\n");
		sourceCode.append("   public java.util.List<String> hello() { return new java.util.ArrayList(); }");
		sourceCode.append("}");
		InMemoryJavaCompiler.newInstance().compile("org.mdkt.HelloClass", sourceCode.toString());
	}

	@Test
	public void compile_WhenIgnoreWarnings() throws Exception {
		StringBuilder sourceCode = new StringBuilder();

		sourceCode.append("package org.mdkt;\n");
		sourceCode.append("public class HelloClass {\n");
		sourceCode.append("   public java.util.List<String> hello() { return new java.util.ArrayList(); }");
		sourceCode.append("}");
		Class<?> helloClass = InMemoryJavaCompiler.newInstance().ignoreWarnings().compile("org.mdkt.HelloClass", sourceCode.toString());
		List<?> res = (List<?>) helloClass.getMethod("hello").invoke(helloClass.getDeclaredConstructor().newInstance());
		Assert.assertEquals(0, res.size());
	}

	@Test
	public void compile_WhenWarningsAndErrors() throws Exception {
		thrown.expect(CompilationException.class);
		StringBuilder sourceCode = new StringBuilder();

		sourceCode.append("package org.mdkt;\n");
		sourceCode.append("public class HelloClass extends xxx {\n");
		sourceCode.append("   public java.util.List<String> hello() { return new java.util.ArrayList(); }");
		sourceCode.append("}");
		try {
			InMemoryJavaCompiler.newInstance().compile("org.mdkt.HelloClass", sourceCode.toString());
		} catch (Exception e) {
			logger.info("Exception caught: {}", e);
			throw e;
		}
	}

}
