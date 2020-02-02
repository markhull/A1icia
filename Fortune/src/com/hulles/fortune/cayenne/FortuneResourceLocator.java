package com.hulles.fortune.cayenne;

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

import com.hulles.fortune.SharedUtils;

public class FortuneResourceLocator implements ResourceLocator {
    Map<String, Resource> resourceMap;
    
    FortuneResourceLocator() {
    
        resourceMap = new HashMap<>();
    }
    
    @Override
    public Collection<Resource> findResources(String name) {
        Resource resource;
        
        SharedUtils.checkNotNull(name);
        resource = resourceMap.get(name);
        if (resource == null) {
            throw new RuntimeException("Unable to find resource by name");
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
            throw new RuntimeException("Unable to create URL from path " + path);
        }
        addResource(name, url);
    }
}
