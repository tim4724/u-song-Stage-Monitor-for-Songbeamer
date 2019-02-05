package com.tim.usong.resource;

import com.tim.usong.view.TutorialView;
import io.dropwizard.views.View;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.net.URI;

@Path("")
public class RootResource {
    @GET
    public Response root() {
        URI songView = UriBuilder.fromUri("/song").build();
        return Response.seeOther(songView).build();
    }

    @GET
    @Path("assets2/css/song2.css")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getAdditionalCss() {
        File f = new File("song.css");
        if (f.exists()) {
            return Response.ok(f).header("Content-Type", "text/css").build();
        }
        return Response.ok().header("Content-Type", "text/css").build();
    }

    @GET
    @Path("tutorial")
    @Produces(MediaType.TEXT_HTML)
    public View getTutorial(@Context HttpServletRequest request) {
        return new TutorialView(request.getLocale());
    }
}
