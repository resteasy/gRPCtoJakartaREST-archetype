package org.jboss.resteasy.grpc.server;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;

import ${generate-package}.${generate-prefix}ServiceGrpcImpl;

@Path("grpcserver")
public class ${generate-prefix}_Server {

   private static final Logger logger = Logger.getLogger(${generate-prefix}_Server.class.getName());
   private static ServletConfig servletConfig;
   private static ServletContext servletContext;
   private static int PORT = 8082;
   private Server server;

   @Path("getservletcontext")
   @GET
   public ServletContext getServletContext(@Context HttpServletRequest request) {
      return request.getServletContext();
   }

   public static ServletConfig getServletConfig() {
      return servletConfig;
   }
   
   public static ServletContext getServletContext() {
      return servletContext;
   }
   
   @Path("start")
   @GET
   public String startGRPC(@Context ServletConfig servletConfig) throws Exception {
      ${generate-prefix}_Server.servletConfig = servletConfig;
      servletContext = servletConfig.getServletContext();
      final ${generate-prefix}_Server server = new ${generate-prefix}_Server();
      new Thread() {
         public void run() {
            try {
               server.start();
               System.out.println("started gRPC server on port " + PORT);
               server.blockUntilShutdown();
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
      }.start();
      return "Starting gRPC server on port " + PORT;
   }

   @Path("ready")
   @GET
   public String ready() {
	   System.out.println("gRPC server ready");
	   return "ready";
   }

   @Path("context")
   @GET
   public String startContext(@Context HttpServletRequest request) throws Exception {
      servletContext = request.getServletContext();
      final ${generate-prefix}_Server server = new ${generate-prefix}_Server();
      return "Got " + this + " servletContext";
   }

   @Path("stop")
   @GET
   public void stopGRPC() throws Exception {
      stop();
   }
   
   static public ServletContext getContext() {
      return servletContext;
   }
   
   /**
    * Start gRPC server.
    */
   private void start() throws IOException {
      server = ServerBuilder.forPort(PORT)
            .addService(new ${generate-prefix}ServiceGrpcImpl())
            .build()
            .start();
      logger.info("Server started, listening on " + PORT);
      Runtime.getRuntime().addShutdownHook(new Thread() {
         @Override
         public void run() {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            try {
               ${generate-prefix}_Server.this.stop();
            } catch (InterruptedException e) {
               e.printStackTrace(System.err);
            }
            System.err.println("*** server shut down");
         }
      });
   }

   private void stop() throws InterruptedException {
      if (server != null) {
         server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
      }
   }

   /**
    * Await termination on the main thread since the grpc library uses daemon threads.
    */
   private void blockUntilShutdown() throws InterruptedException {
      if (server != null) {
         server.awaitTermination();
      }
   }

   /**
    * Main launches the server from the command line.
    */
   public static void main(String[] args) throws IOException, InterruptedException {
      final ${generate-prefix}_Server server = new ${generate-prefix}_Server();
      server.start();
      server.blockUntilShutdown();
   }
}

