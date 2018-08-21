package com.hulles.a1icia.cayenne;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.cayenne.CayenneRuntimeException;
import org.apache.cayenne.resource.Resource;
import org.apache.cayenne.resource.ResourceLocator;
import org.apache.cayenne.resource.URLResource;

/**
 * A custom resource locator for Cayenne
 * 
 * We need this because the current default locator doesn't work in Java 10.
 * <p>
 * The problem is, getResource returns a URL which encodes a space as %20, but the file name
 * needs a space ' ' instead
 * 
 * @author hulles
 *
 */
public class A1iciaResourceLocator implements ResourceLocator {
	List<File> rootDirectories;
	
    public <T> A1iciaResourceLocator(Class<T> a1iciaClass, String... resources) {
    	URL resourceURL;
    	File resourceFile;
    	File root;
    	String resourcePath;
    	String resourceFileName;
    	
    	rootDirectories = new ArrayList<>(3);
    	for (String resource : resources) {
    		resourceURL = a1iciaClass.getResource(resource);
    		if (resourceURL != null) {
    			resourcePath = resourceURL.getPath();
    			try {
					resourceFileName = URLDecoder.decode(resourcePath, "UTF-8");
				} catch (UnsupportedEncodingException e) {
                    throw new CayenneRuntimeException("Can't decode URL to file: %s", e,
                            resourceURL);
				}
    			resourceFile = new File(resourceFileName);
                root = resourceFile.getParentFile();
                if (root == null) {
                    throw new CayenneRuntimeException("Invalid resource: %s", resourceFile.getName());
                }
    			rootDirectories.add(root);
    		}
    	}
    }

    @Override
	public Collection<Resource> findResources(String name) {
    	List<Resource> cayenneResources;
    	URLResource cayenneResource;
    	File resourceFile;
    	URL resourceURL;
    	String fileName;
    	
    	cayenneResources = new ArrayList<>(3);
		try {
			fileName = URLDecoder.decode(name, "UTF-8");
		} catch (UnsupportedEncodingException e) {
            throw new CayenneRuntimeException("Can't decode URL to file: %s", e,
                    name);
		}
    	for (File root : rootDirectories) {
            resourceFile = new File(root, fileName);
            if (resourceFile.exists()) {
                try {
                    resourceURL = resourceFile.toURI().toURL();
                }
                catch (MalformedURLException e) {
                    throw new CayenneRuntimeException("Can't convert file to URL: %s", e,
                            resourceFile.getAbsolutePath());
                }
                cayenneResource = new URLResource(resourceURL);
                cayenneResources.add(cayenneResource);
            }
    	}
    	return cayenneResources;
    }
}

