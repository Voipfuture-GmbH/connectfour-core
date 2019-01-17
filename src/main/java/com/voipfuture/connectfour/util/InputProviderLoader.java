package com.voipfuture.connectfour.util;

import com.voipfuture.connectfour.IInputProvider;
import com.voipfuture.connectfour.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Implements dynamic loading and re-loading of {@link IInputProvider} implementations.
 *
 * This class uses a custom classloader to do the actual loading/reloading.
 *
 * @author tobias.gierke@voipfuture.com
 */
public class InputProviderLoader
{
    public static boolean DEBUG = false;

    private static InnerClassloader CLASSLOADER = new InnerClassloader();

    // Class names of IInputProvider implementations that are
    // managed by our custom classloader. Needed so that we ONLY
    // load those with our custom classloader and all other
    // classes get loaded via the system classloader. Needed because
    // otherwise we'd get conflicts with classes that have already been loaded
    // by the system classloader (we're using the system classpath here, just
    // like the system classloader).
    private static final ConcurrentHashMap<String,String> playerClassNames = new ConcurrentHashMap<>();

    private static final class InnerClassloader extends ClassLoader
    {
        private final ConcurrentHashMap<String, Class<?>> loadedClasses = new ConcurrentHashMap<>();

        private boolean isPlayerClass(String name)
        {
            // first check for a full match
            if ( playerClassNames.contains( name ) )
            {
                return true;
            }
            // check for a prefix match in case
            // a IInputProvider declared inner classes
            // that need to be loaded as well
            for (String key : playerClassNames.keySet() )
            {
                if ( name.startsWith( key ) )
                {
                    return true;
                }
            }
            return false;
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
        {
            // everything that is not a IInputProvider class
            // or any of its inner classes will be loaded
            // by the system classloader
            // (to avoid loading the same class in different classloaders ;
            // we're using the same classpath as the system classloader
            if ( !isPlayerClass( name ) )
            {
                try
                {
                    return super.loadClass( name, resolve );
                }
                catch (ClassNotFoundException e)
                {
                    // nop
                }
            }

            synchronized( getClassLoadingLock(name) )
            {
                Class<?> c = loadedClasses.get( name );
                if ( c != null )
                {
                    return c;
                }

                final String path = name.replace( '.', File.separatorChar ) + ".class";
                final byte[] bytes = loadFromClasspath( path );
                if ( bytes != null )
                {
                    c = defineClass( name, bytes, 0, bytes.length, null );
                    if ( resolve )
                    {
                        resolveClass( c );
                    }
                    loadedClasses.put( name, c );
                    return c;
                }
                throw new ClassNotFoundException( "Failed to find class " + name );
            }
        }

        private byte[] loadFromClasspath(String pathToClass)
        {
            final String classpath = System.getProperty( "java.class.path" );
            for (String part : classpath.split( ":" ))
            {
                try
                {
                    if ( part.toLowerCase().endsWith( ".jar" ) )
                    {
                        // JAR file
                        if ( DEBUG )
                        {
                            System.out.println( "Scanning JAR " + part + " ..." );
                        }
                        try (final JarFile f = new JarFile( new File( part ) ))
                        {
                            for (final Enumeration<JarEntry> entries = f.entries();
                                 entries.hasMoreElements(); )
                            {
                                final JarEntry entry = entries.nextElement();
                                if ( entry.getName().equals( pathToClass ) )
                                {
                                    try (InputStream in = f.getInputStream( entry ))
                                    {
                                        byte[] result = in.readAllBytes();
                                        if ( DEBUG )
                                        {
                                            System.out.println( ">>> Found class " + pathToClass + " in " + part );
                                        }
                                        return result;
                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        // directory
                        final File f = new File( part, pathToClass );
                        if ( DEBUG )
                        {
                            System.out.println( "Scanning directory " + part + " , looking for " + f.getAbsolutePath() );
                        }
                        if ( f.exists() )
                        {
                            final byte[] result = Files.readAllBytes( f.toPath() );
                            if ( DEBUG )
                            {
                                System.out.println( ">>> Found class " + pathToClass + " in " + part );
                            }
                            return result;
                        }
                    }
                } catch (IOException ex)
                {
                    // nop
                }
            }
            return null;
        }

        private IInputProvider loadInputProvider(Player player)
        {
            final Object instance;
            try
            {
                if ( DEBUG )
                {
                    System.out.println( "Trying to load algorithm '" + player.algorithm() + "' ..." );
                }
                playerClassNames.put( player.algorithm() , "" );
                final Class<?> clazz = CLASSLOADER.loadClass( player.algorithm() );
                instance = clazz.getDeclaredConstructor( null ).newInstance( null );
            }
            catch (Exception e)
            {
                System.err.println( "Failed to load algorithm '" + player.algorithm() + "' from classpath" );
                throw new RuntimeException( e );
            }
            return (IInputProvider) instance;
        }
    }

    public static IInputProvider getInputProvider(Player player)
    {
        if ( !player.isComputer() )
        {
            throw new IllegalArgumentException( "Only applicable to computer players" );
        }
        return CLASSLOADER.loadInputProvider( player );
    }

    /**
     * Schedules all algorithm implementations for reload.
     */
    public static void reloadAlgorithms()
    {
        if ( DEBUG )
        {
            System.out.println( "Algorithm implementations will be reloaded." );
        }
        CLASSLOADER = new InnerClassloader();
    }
}