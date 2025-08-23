package dev.resteasy.grpc.maps;

import java.util.Map;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("")
public class MapResource {

    @Path("map/wildcard/wildcard")
    @POST
    public Map<?, ?> mapWildWild(Map<?, ?> m) {
        return m;
    }
}