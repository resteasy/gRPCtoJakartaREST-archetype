package dev.resteasy.grpc.test;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.utils.TestUtil;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
//import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.protobuf.Any;
import com.google.protobuf.Message;

import dev.resteasy.example.grpc.greet.GreetServiceGrpc;
import dev.resteasy.example.grpc.greet.GreetServiceGrpc.GreetServiceBlockingStub;
import dev.resteasy.example.grpc.greet.Greet_proto;
import dev.resteasy.example.grpc.greet.Greet_proto.GeneralEntityMessage;
import dev.resteasy.example.grpc.greet.Greet_proto.GeneralReturnMessage;
import dev.resteasy.example.grpc.greet.Greet_proto.dev_resteasy_example_grpc_greet___GeneralGreeting;
import dev.resteasy.example.grpc.greet.Greet_proto.dev_resteasy_example_grpc_greet___Greeting;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___Integer___Array;
import dev.resteasy.grpc.bridge.runtime.Utility;
import dev.resteasy.grpc.bridge.runtime.protobuf.JavabufTranslator;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

@RunWith(Arquillian.class)
@RunAsClient
public class GrpcToJakartaRESTTest {

    private static ManagedChannel channel;
    private static GreetServiceBlockingStub blockingStub;
    private static JavabufTranslator translator;

    static {
        Class<?> clazz;
        try {
            clazz = Class.forName("dev.resteasy.example.grpc.greet.GreetJavabufTranslator");
            translator = (JavabufTranslator) clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Deployment
    static Archive<?> deploy() throws Exception {
        WebArchive war = TestUtil.prepareArchive(GrpcToJakartaRESTTest.class.getSimpleName());
        String version = System.getProperty("grpc.example.version", "1.0.0.Beta2");
        File file = Maven.resolver().resolve("dev.resteasy.examples:grpcToRest.example.grpc:war:" + version)
                .withoutTransitivity().asSingleFile();
        war.merge(ShrinkWrap.createFromZipFile(WebArchive.class, file));
        WebArchive archive = (WebArchive) TestUtil.finishContainerPrepare(war, null, (Class<?>[]) null);
        // log.info(archive.toString(true));
        // archive.as(ZipExporter.class).exportTo(new File("/tmp/GrpcToJaxrs.jar"), true);
        return archive;
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        channel = ManagedChannelBuilder.forTarget("localhost:9555").usePlaintext().build();
        blockingStub = GreetServiceGrpc.newBlockingStub(channel);
        Client client = ClientBuilder.newClient();
        Response response = client.target("http://localhost:8080/GrpcToJakartaRESTTest/grpcToJakartaRest/grpcserver/context")
                .request().get();
        Assert.assertEquals(204, response.getStatus());
        client.close();
    }

    @Test
    public void testGreeting() throws Exception {
        GeneralEntityMessage.Builder builder = GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setURL("http://localhost:8080/greet/Bill").build();
        try {
            GeneralReturnMessage grm = blockingStub.greet(gem);
            dev_resteasy_example_grpc_greet___Greeting greeting = grm.getDevResteasyExampleGrpcGreetGreetingField();
            Assert.assertEquals("hello, Bill", greeting.getS());
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assert.fail(writer.toString());
            }
        }
    }

    @Test
    public void testGeneralGreeting() throws Exception {
        GeneralEntityMessage.Builder builder = GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setURL("http://localhost:8080/salute/Bill?salute=Heyyy").build();
        try {
            GeneralReturnMessage grm = blockingStub.generalGreet(gem);
            dev_resteasy_example_grpc_greet___GeneralGreeting greeting = grm
                    .getDevResteasyExampleGrpcGreetGeneralGreetingField();
            Assert.assertEquals("Heyyy", greeting.getSalute());
            Assert.assertEquals("Bill", greeting.getS());
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assert.fail(writer.toString());
            }
        }
    }

    @Test
    public void testListStringGeneric() throws Exception {
        java.util.List<java.lang.String> coll = new java.util.ArrayList<java.lang.String>();
        coll.add("abc");
        GenericType<java.util.List<java.lang.String>> type = new GenericType<java.util.List<java.lang.String>>() {
        };
        Message m = translator.translateToJavabuf(coll, type);
        Any any = Any.pack(m);
        GeneralEntityMessage.Builder builder = GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setAnyField(any).build();
        GeneralReturnMessage response = blockingStub.listString(gem);
        any = response.getAnyField();
        Message result = any.unpack((Class) Utility.extractClassFromAny(any, translator));
        Assert.assertEquals(coll, translator.translateFromJavabuf(result));
    }

    @Test
    public void testHashSetIntegerGenericType() throws Exception {
        java.util.HashSet<java.lang.Integer> set = new java.util.HashSet<java.lang.Integer>();
        set.add(Integer.valueOf(17));
        GenericType<java.util.HashSet<java.lang.Integer>> type = new GenericType<java.util.HashSet<java.lang.Integer>>() {
        };
        Message m = translator.translateToJavabuf(set, type);
        GeneralEntityMessage.Builder builder = GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setJavaUtilHashSet0Field((Greet_proto.java_util___HashSet0) m).build();
        GeneralReturnMessage response = blockingStub.hashsetInteger(gem);
        Message result = response.getJavaUtilHashSet0Field();
        Object o = translator.translateFromJavabuf(result);
        Assert.assertTrue(CollectionEquals.equals(set, o));
    }

    @Test
    public void testArrayOne() throws Exception {
        int[] array = new int[] { (int) 3, (int) 5 };
        dev_resteasy_grpc_arrays___Integer___Array jbArray = (dev_resteasy_grpc_arrays___Integer___Array) translator
                .translateToJavabuf(array);
        GeneralEntityMessage.Builder builder = GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setDevResteasyGrpcArraysIntegerArrayField(jbArray).build();
        GeneralReturnMessage response = blockingStub.arrayOne(gem);
        dev_resteasy_grpc_arrays___Integer___Array result = response.getDevResteasyGrpcArraysIntegerArrayField();
        int[] array2 = (int[]) translator.translateFromJavabuf(result);
        Assert.assertTrue(Arrays.equals(array, array2));
    }

    @Test
    public void testMapWildcardWildcard() throws Exception {
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        map.put(Integer.valueOf(17), Integer.valueOf(19));
        GenericType<Map<Integer, Integer>> type = new GenericType<Map<Integer, Integer>>() {
        };
        Message m = translator.translateToJavabuf(map, type);
        Any any = Any.pack(m);
        GeneralEntityMessage.Builder builder = GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setAnyField(any).build();
        GeneralReturnMessage response = blockingStub.mapWildWild(gem);
        any = response.getAnyField();
        Message result = Utility.unpack(any, translator);
        Assert.assertTrue(map.equals(translator.translateFromJavabuf(result)));
    }

    //////////////////////////////////////////////////////////
    public static class CollectionEquals {

        public static boolean equals(Object o1, Object o2) {
            if (!o1.getClass().equals(o2.getClass())) {
                return false;
            }
            if (!Collection.class.isAssignableFrom(o1.getClass()) || !Collection.class.isAssignableFrom(o2.getClass())) {
                return false;
            }
            Object[] o1s = ((Collection) o1).toArray();
            Object[] o2s = ((Collection) o2).toArray();
            if (o1s.length != o2s.length) {
                return false;
            }
            for (int i = 0; i < o1s.length; i++) {
                Object o1s1 = o1s[i];
                Object o2s1 = o2s[i];
                if (Collection.class.isAssignableFrom(o1s1.getClass()) && Collection.class.isAssignableFrom(o2s1.getClass())) {
                    return equals(o1s1, o2s1);
                }
                if ((o1s1 == null && o2s1 != null) || (o1s1 != null && o2s1 == null)) {
                    return false;
                }
                if (!o1s1.equals(o2s1)) {
                    return false;
                }
            }
            return true;
        }
    }
}
