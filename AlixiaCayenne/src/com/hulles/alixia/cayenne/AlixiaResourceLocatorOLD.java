package com.hulles.alixia.cayenne;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.cayenne.di.ClassLoaderManager;
import org.apache.cayenne.di.spi.DefaultClassLoaderManager;
import org.apache.cayenne.resource.ClassLoaderResourceLocator;
import org.apache.cayenne.resource.Resource;
import org.apache.cayenne.resource.ResourceLocator;
import org.apache.cayenne.resource.URLResource;

import com.hulles.alixia.api.shared.SharedUtils;

/**
 * A custom resource locator for Cayenne
 * 
 * We need this because the current default locator doesn't work in Java 10.
 * <p>
 * One problem is, getResource returns a URL which encodes a space as %20, but the file name
 * needs a space ' ' instead
 * 
 * @author hulles
 *
 */
public class AlixiaResourceLocatorOLD implements ResourceLocator {
//	List<File> rootDirectories;
	Collection<Resource> myResources;
	
    public <T> AlixiaResourceLocatorOLD(Class<T> alixiaClass, String alixiaResource) {
//    	File resourceFile;
//    	File root;
//    	String resourcePath;
//    	String resourceFileName;
    	
    	SharedUtils.checkNotNull(alixiaClass);
        SharedUtils.checkNotNull(alixiaResource);
//    	rootDirectories = new ArrayList<>(3);
    	myResources = new ArrayList<>(3);
        // The way we currently have this set up, we are assuming this is ONLY called
        //   during the config process and is not used for any other purpose
    	addResource(alixiaClass, alixiaResource);
		
/*		if (resourceURL != null) {
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
*/
	}
    
    @Override
	public Collection<Resource> findResources(String name) {
    	Collection<Resource> resources;
    	
    	resources = findDefaultResources(name);
    	if ((resources == null) || (resources.isEmpty())) {
//    		resources = findCustomResources(name);
    		resources = myResources;
    	}
    	return resources;
    }

    /**
     * Approximate the default method that Cayenne uses to find resources, the one
     * that used to work in Eclipse and standalone until we switched to Java 10.
     * 
     * @param name
     * @return
     */
    private static Collection<Resource> findDefaultResources(String name) {
    	ResourceLocator defaultResourceLocator;
    	ClassLoaderManager classLoaderManager;
    	Collection<Resource> configurations;
    	
        classLoaderManager = new DefaultClassLoaderManager();
    	defaultResourceLocator = new ClassLoaderResourceLocator(classLoaderManager);
		configurations = defaultResourceLocator.findResources(name);
		return configurations;
    }
    
    private <T> void addResource(Class<T> alixiaClass, String name) {
        URLResource cayenneResource;
        URL resourceURL;
        
        resourceURL = alixiaClass.getResource(name);
        if (resourceURL != null) {
            cayenneResource = new URLResource(resourceURL);
            myResources.add(cayenneResource);
        }
    }
    
/*    private Collection<Resource> findCustomResources(String name) {
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
*/}

