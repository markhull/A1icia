package com.hulles.a1icia.cayenne;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.cayenne.CayenneRuntimeException;
import org.apache.cayenne.resource.Resource;
import org.apache.cayenne.resource.ResourceLocator;
import org.apache.cayenne.resource.URLResource;

public class A1iciaResourceLocator implements ResourceLocator {
	List<Resource> cayenneResources;
	List<File> rootDirectories;
	
    public <T> A1iciaResourceLocator(Class<T> a1iciaClass, String... resources) {
    	URL resourceURL;
    	File resourceFile;
    	File root;
    	
    	rootDirectories = new ArrayList<>(3);
    	for (String resource : resources) {
    		System.out.println("ARL resource " + resource);
    		resourceURL = a1iciaClass.getResource(resource);
    		if (resourceURL != null) {
    			System.out.println("ARL URL " + resourceURL);
    			resourceFile = new File(resourceURL.getFile());
                root = resourceFile.getParentFile();
                System.out.println("ARL root " + root);
                if (root == null) {
//                    throw new A1iciaAPIException("Invalid resource: " + resourceFile.getName());
                    throw new CayenneRuntimeException("Invalid resource: %s", resourceFile.getName());
                }
    			rootDirectories.add(root);
    		}
    	}
    }

    @Override
	public Collection<Resource> findResources(String name) {
    	URLResource cayenneResource;
    	File resourceFile;
    	URL resourceURL;
    	
    	System.out.println("ARL name " + name);
    	cayenneResources = new ArrayList<>(3);
    	for (File root : rootDirectories) {
            resourceFile = new File(root, name);
            System.out.println("ARL resource file " + resourceFile.getAbsolutePath());
            if (resourceFile.exists()) {
                try {
                    resourceURL = resourceFile.toURI().toURL();
                    System.out.println("ARL resource URL " + resourceURL);
                }
                catch (MalformedURLException e) {
                    throw new CayenneRuntimeException("Can't convert file to URL: %s", e,
                            resourceFile.getAbsolutePath());
                }
                cayenneResource = new URLResource(resourceURL);
                cayenneResources.add(cayenneResource);
            } else {
                throw new CayenneRuntimeException("Resource file doesn't exist: %s", resourceFile);
            }
    	}
    	return cayenneResources;
    }
}

