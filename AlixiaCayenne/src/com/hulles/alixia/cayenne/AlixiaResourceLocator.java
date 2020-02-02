package com.hulles.alixia.cayenne;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.cayenne.resource.Resource;
import org.apache.cayenne.resource.ResourceLocator;
import org.apache.cayenne.resource.URLResource;

import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SharedUtils;

public class AlixiaResourceLocator implements ResourceLocator {
    Map<String, Resource> resourceMap;
    
    AlixiaResourceLocator() {
    
        resourceMap = new HashMap<>(3);
    }
    
    @Override
    public Collection<Resource> findResources(String name) {
        Resource resource;
        
        SharedUtils.checkNotNull(name);
        resource = resourceMap.get(name);
        if (resource == null) {
            throw new AlixiaException("Unable to find resource by name");
        }
        return Collections.singleton(resource);
    }
    
    public void addResource(String name, URL url) {
        URLResource resource;
        
        SharedUtils.checkNotNull(name);
        SharedUtils.checkNotNull(url);
        resource = new URLResource(url);
        resourceMap.put(name, resource);
    }

    public void addResource(String name, Path path) {
        URL url;
        
        SharedUtils.checkNotNull(name);
        SharedUtils.checkNotNull(path);
        try {
            url = path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new AlixiaException("Unable to create URL from path " + path);
        }
        addResource(name, url);
    }

}
