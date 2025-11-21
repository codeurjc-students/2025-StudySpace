package com.urjcservice.backend.config;

import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.Software;
import com.urjcservice.backend.projections.RoomWithSoftware;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class RestConfiguration implements RepositoryRestConfigurer {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        //to avoid the error of the infinite recursion
        config.getProjectionConfiguration().addProjection(RoomWithSoftware.class);
        
        //to show the id fields in the json responses
        config.exposeIdsFor(Room.class, Software.class);
    }
}