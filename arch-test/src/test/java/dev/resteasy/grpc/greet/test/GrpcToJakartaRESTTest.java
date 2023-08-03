package dev.resteasy.grpc.greet.test;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.utils.TestUtil;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import dev.resteasy.example.grpc.greet.GreetServiceGrpc;
import dev.resteasy.example.grpc.greet.GreetServiceGrpc.GreetServiceBlockingStub;
import dev.resteasy.example.grpc.greet.Greet_proto.GeneralEntityMessage;
import dev.resteasy.example.grpc.greet.Greet_proto.GeneralReturnMessage;
import dev.resteasy.example.grpc.greet.Greet_proto.dev_resteasy_example_grpc_greet___GeneralGreeting;
import dev.resteasy.example.grpc.greet.Greet_proto.dev_resteasy_example_grpc_greet___Greeting;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

@RunWith(Arquillian.class)
@RunAsClient
public class GrpcToJakartaRESTTest {

    private static ManagedChannel channel;
    private static GreetServiceBlockingStub blockingStub;

    @Deployment
    static Archive<?> deploy() throws Exception {
        WebArchive war = TestUtil.prepareArchive(GrpcToJakartaRESTTest.class.getSimpleName());
        war.merge(ShrinkWrap.createFromZipFile(WebArchive.class,
                TestUtil.resolveDependency("dev.resteasy.examples:grpcToRest.example.grpc:war:1.0.0.Final-SNAPSHOT")));
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
        Assert.assertEquals(200, response.getStatus());
        client.close();
    }

    @Test
    public void testGreeting() {
        GeneralEntityMessage.Builder builder = GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setURL("http://localhost:8080/greet/Bill").build();
        try {
            GeneralReturnMessage grm = blockingStub.greet(gem);
            dev_resteasy_example_grpc_greet___Greeting greeting = grm.getDevResteasyExampleGrpcGreetGreetingField();
            Assert.assertEquals("hello, Bill", greeting.getS());
        } catch (StatusRuntimeException e) {
            //
        }
    }

    @Test
    public void testGeneralGreeting() {
        GeneralEntityMessage.Builder builder = GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setURL("http://localhost:8080/salute/Bill?salute=Heyyy").build();
        try {
            GeneralReturnMessage grm = blockingStub.generalGreet(gem);
            dev_resteasy_example_grpc_greet___GeneralGreeting greeting = grm
                    .getDevResteasyExampleGrpcGreetGeneralGreetingField();
            Assert.assertEquals("Heyyy", greeting.getSalute());
            Assert.assertEquals("Bill", greeting.getGreetingSuper().getS());
        } catch (StatusRuntimeException e) {
            //
        }
    }
}
