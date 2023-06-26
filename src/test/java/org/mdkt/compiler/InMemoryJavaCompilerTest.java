package org.mdkt.compiler;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class InMemoryJavaCompilerTest {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryJavaCompilerTest.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public String getResourceAsString(String path) throws Exception {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (InputStream srcResStream = classloader.getResourceAsStream(path)) {

            // Thanks to https://www.baeldung.com/convert-input-stream-to-string
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int bytesRead = 0;
            while (srcResStream != null && (bytesRead = srcResStream.read(chunk, 0, chunk.length)) != -1) {
                buf.write(chunk, 0, bytesRead);
            }
            return new String(buf.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    @Test
    public void compile_WhenTypical() throws Exception {
        Class<?> helloClass = InMemoryJavaCompiler.newInstance()
                .compile("org.mdkt.HelloClass", getResourceAsString("compile_WhenTypical/HelloClass.java"));

        Assert.assertNotNull(helloClass);
        Assert.assertEquals(1, helloClass.getDeclaredMethods().length);
    }

    @Test
    public void compileAll_WhenTypical() throws Exception {
        Map<String, Class<?>> compiled = InMemoryJavaCompiler.newInstance()
                .addSource("A", getResourceAsString("compileAll_WhenTypical/A.java"))
                .addSource("B", getResourceAsString("compileAll_WhenTypical/B.java"))
                .compileAll();

        Assert.assertNotNull(compiled.get("A"));
        Assert.assertNotNull(compiled.get("B"));

        Class<?> aClass = compiled.get("A");
        Object a = aClass.getDeclaredConstructor().newInstance();
        Assert.assertEquals("B!", aClass.getMethod("b").invoke(a).toString());
    }

    @Test
    public void compile_WhenSourceContainsInnerClasses() throws Exception {
        Class<?> helloClass = InMemoryJavaCompiler.newInstance()
                .compile("org.mdkt.HelloClass", getResourceAsString("compile_WhenSourceContainsInnerClasses/HelloClass.java"));
        Assert.assertNotNull(helloClass);
        Assert.assertEquals(1, helloClass.getDeclaredMethods().length);
    }

    @Test
    public void compile_whenError() throws Exception {
        thrown.expect(CompilationException.class);
        thrown.expectMessage("Unable to compile the source");
        InMemoryJavaCompiler.newInstance()
                .compile("org.mdkt.HelloClass", getResourceAsString("compile_whenError/HelloClass.java"));
    }

    @Test
    public void compile_WhenFailOnWarnings() throws Exception {
        thrown.expect(CompilationException.class);
        InMemoryJavaCompiler.newInstance()
                .compile("org.mdkt.HelloClass", getResourceAsString("compile_WhenFailOnWarnings/HelloClass.java"));
    }

    @Test
    public void compile_WhenIgnoreWarnings() throws Exception {
        Class<?> helloClass = InMemoryJavaCompiler.newInstance().ignoreWarnings()
                .compile("org.mdkt.HelloClass", getResourceAsString("compile_WhenIgnoreWarnings/HelloClass.java"));
        List<?> res = (List<?>) helloClass.getMethod("hello").invoke(helloClass.getDeclaredConstructor().newInstance());
        Assert.assertEquals(0, res.size());
    }

    @Test
    public void compile_WhenWarningsAndErrors() throws Exception {
        thrown.expect(CompilationException.class);
        try {
            InMemoryJavaCompiler.newInstance()
                    .compile("org.mdkt.HelloClass", getResourceAsString("compile_WhenWarningsAndErrors/HelloClass.java"));
        } catch (Exception e) {
            logger.info("Exception caught: {}", e.getMessage());
            throw e;
        }
    }

    @Test
    public void compile_WhenTypicalUpdateClass() throws Exception {
        Class<?> oldClass = HelloClass.class;
        Class<?> newClass = InMemoryJavaCompiler.newInstance().compile("org.mdkt.compiler.HelloClass",
                getResourceAsString("compile_WhenTypical/HelloClass.java"));

        Assert.assertNotEquals(oldClass.hashCode(), newClass.hashCode());

        Object oldClassInvokeResult = oldClass.getDeclaredMethod("hello").invoke(oldClass.getDeclaredConstructor().newInstance());
        Object newClassInvokeResult = newClass.getDeclaredMethod("hello").invoke(newClass.getDeclaredConstructor().newInstance());

        System.out.println("oldClassInvokeResult = " + oldClassInvokeResult);
        System.out.println("newClassInvokeResult = " + newClassInvokeResult);

        Assert.assertNotEquals(oldClassInvokeResult, newClassInvokeResult);
    }

}
