package edu.umich.soar.editor.icons;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import edu.umich.soar.editor.Activator;

public class SoarIcons {
	
	public static enum IconFiles
	{
		A_FLAG,
		ATTRIBUTE,
		ENUMERATION,
		FLOAT,
		INTEGER,
		LINKED_ATTRIBUTE,
		RULE,
		S_FLAG,
		SAMPLE,
		STRING,
	}
	
	public static void init(ImageRegistry registry)
	{
        try 
        {
            //get the file path to where the plugin is installed
            Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
            URL path = bundle.getEntry("/");
            URL baseIconURL = new URL(path,"icons/");
            for (IconFiles file : IconFiles.values())
            {
            	String filename = file.name() + ".png";
            	ImageDescriptor desc = ImageDescriptor.createFromURL(new URL(baseIconURL, filename));
                registry.put(filename, desc);
            }
        }
        catch (MalformedURLException e) 
        {
            System.out.print("Malformed URL");
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
	}
	
    public static Image get(IconFiles file) 
    {
        return Activator.getDefault().getImageRegistry().get(file.name() + ".png");
    }
}
