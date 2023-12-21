package dev.resteasy.grpc;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("")
public class ArrayResource {

    @Path("array/one")
    @GET
    public int[] arrayOne(int[] is) {
        return is;
    }

    @Path("array/two")
    @GET
    public Integer[][] arrayTwo(Integer[][] iis) {
        return iis;
    }
}