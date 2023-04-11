package org.jboss.resteasy.grpc.server;

import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;

import ${generate-package}.${generate-prefix}ServiceGrpcImpl;

@SuppressWarnings("removal")
@Path("grpcserver")
public class ${generate-prefix}_Server {

   private static final Logger logger = Logger.getLogger(CC1_Server.class.getName());
   private static ServletContext servletContext;
   private static int PORT = 8082;
   private Server server;

   /**
    * Main launches the server from the command line.
    */
   public static void main(String[] args) throws Exception, InterruptedException {
      final CC1_Server server = new CC1_Server();
      server.start();
      server.blockUntilShutdown();
   }

   public static ServletContext getServletContext() {
      return servletContext;
   }

   @Path("context")
   @GET
   public String startContext(@Context HttpServletRequest request) throws Exception {
      servletContext = request.getServletContext();
      return "Got " + this + " servletContext";
   }
   
   @Path("start")
   @GET
   public String startGRPC(@Context HttpServletRequest request) throws Exception {
      servletContext = request.getServletContext();
      final ${generate-prefix}_Server server = new ${generate-prefix}_Server();
      new Thread() {
         @SuppressWarnings({"deprecation", "removal"})
         public void run() {
            try {
               if (System.getSecurityManager() == null) {
                  server.start();
               } else {
                  AccessController.doPrivileged((PrivilegedExceptionAction<Void>) () -> {server.start(); return null;});
               } 
               logger.info("started gRPC server on port " + PORT);
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
	   logger.info("gRPC server ready");
	   return "ready";
   }

   @Path("stop")
   @GET
   public void stopGRPC() throws Exception {
      logger.info("stopping gRPC server on port " + PORT);
      stop();
   }

   /**
    * Start gRPC server.
    */
   @SuppressWarnings({"removal", "deprecation"})
   private void start() throws Exception {
      if (System.getSecurityManager() == null) {
         server = ServerBuilder.forPort(PORT)
               .addService(new ${generate-prefix}ServiceGrpcImpl())
               .build()
               .start();
      } else {
         AccessController.doPrivileged((PrivilegedExceptionAction<Server>) () -> {
            server = ServerBuilder.forPort(PORT)
                  .addService(new ${generate-prefix}ServiceGrpcImpl())
                  .build()
                  .start();
            return server;
         });
      }
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

   /**
    * Stop gRPC server.
    */
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
}

