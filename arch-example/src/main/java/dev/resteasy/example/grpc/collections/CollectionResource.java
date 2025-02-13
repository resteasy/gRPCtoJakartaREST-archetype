/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2023 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.resteasy.example.grpc.collections;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("collection")
public class CollectionResource {

    @Path("list/string")
    @POST
    public List<String> listString(List<String> l) {
        return l;
    }

    @POST
    @Path("set")
    public Set<Integer> setInteger(Set<Integer> set) {
        return set;
    }

    @POST
    @Path("hashset")
    public HashSet<Integer> hashsetInteger(HashSet<Integer> set) {
        return set;
    }
}
