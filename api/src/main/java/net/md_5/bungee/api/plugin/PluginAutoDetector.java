package net.md_5.bungee.api.plugin;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import net.md_5.bungee.api.ProxyServer;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.String;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

public class PluginAutoDetector
{
    public static PluginDescription checkPlugin( JarFile jarFile )
    {
        Enumeration<JarEntry> jarEntries = jarFile.entries();

        if (jarEntries == null)
        {
            ProxyServer.getInstance().getLogger().log( Level.WARNING, "Could not load Plugin. File " + jarFile + " is empty" );
            return null;
        }

        try
        {
            while ( jarEntries.hasMoreElements() )
            {
                JarEntry jarEntry = jarEntries.nextElement();

                if ( jarEntry != null && jarEntry.getName().endsWith(".class") )
                {
                    ClassFile classFile = new ClassFile( new DataInputStream( jarFile.getInputStream( jarEntry ) ) );
                    if ( classFile.getSuperclass().equals( "net.md_5.bungee.api.plugin.Plugin" ) )
                    {
                        PluginDescription pluginDescription = new PluginDescription();
                        pluginDescription.setName( classFile.getName().substring( classFile.getName().lastIndexOf('.') + 1 ) );

                        AnnotationsAttribute visible = (AnnotationsAttribute) classFile.getAttribute( AnnotationsAttribute.visibleTag );

                        for ( Annotation annotation : visible.getAnnotations())
                        {
                            switch (annotation.getTypeName())
                            {
                                case "net.md_5.bungee.api.plugin.annotation.Description":
                                    pluginDescription.setDescription(((StringMemberValue)annotation.getMemberValue("value")).getValue());
                                    break;

                                case "net.md_5.bungee.api.plugin.annotation.Author":
                                    pluginDescription.setAuthor(((StringMemberValue)annotation.getMemberValue("value")).getValue());
                                    break;

                                case "net.md_5.bungee.api.plugin.annotation.Version":
                                    pluginDescription.setVersion(((StringMemberValue)annotation.getMemberValue("value")).getValue());
                                    break;

                                case "net.md_5.bungee.api.plugin.annotation.Depends":
                                    MemberValue[] dependsValues = ((ArrayMemberValue) annotation.getMemberValue("value")).getValue();
                                    HashSet<String> dependsStringValues = new HashSet<>();
                                    for ( MemberValue value : dependsValues )
                                    {
                                        dependsStringValues.add(((StringMemberValue)value).getValue());
                                    }

                                    pluginDescription.setDepends(dependsStringValues);
                                    break;

                                case "net.md_5.bungee.api.plugin.annotation.SoftDepends":
                                    MemberValue[] softDependsValues = ((ArrayMemberValue) annotation.getMemberValue("value")).getValue();
                                    HashSet<String>  softDependsStringValues = new HashSet<>();
                                    for ( MemberValue value : softDependsValues )
                                    {
                                        softDependsStringValues.add(((StringMemberValue)value).getValue());
                                    }

                                    pluginDescription.setSoftDepends(softDependsStringValues);
                                    break;
                            }
                        }

                        pluginDescription.setMain( classFile.getName() );

                        return pluginDescription;
                    }
                }
            }

            return null;
        } catch (IOException e)
        {
            ProxyServer.getInstance().getLogger().log( Level.WARNING, "Could not load Plugin. File " + jarFile + " is corrupted", e );
            return null;
        }
    }
}
