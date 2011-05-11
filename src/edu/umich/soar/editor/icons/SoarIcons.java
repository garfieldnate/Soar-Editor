package edu.umich.soar.editor.icons;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

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
            URL baseIconURL = new URL(Platform.getBundle(Activator.PLUGIN_ID).getEntry("/"),"icons/");
            for (IconFiles file : IconFiles.values())
            {
            	String filename = file.name() + ".png";
            	ImageDescriptor desc = ImageDescriptor.createFromURL(new URL(baseIconURL, filename));
                registry.put(filename, desc);
            }
        } catch (MalformedURLException e) 
        {
            System.out.print("Malformed URL");
        }
	}
	
    public static Image get(IconFiles file) 
    {
        return Activator.getDefault().getImageRegistry().get(file.name() + ".png");
    }
}
