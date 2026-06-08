package dev.rarehyperion.chatgames.library;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

public final class RedisLibraryLoader implements PluginLoader {

    private static final String PAPER_MAVEN_PUBLIC = "https://repo.papermc.io/repository/maven-public/";

    @Override
    public void classloader(final PluginClasspathBuilder classpathBuilder) {
        final MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(new RemoteRepository.Builder("paper", "default", PAPER_MAVEN_PUBLIC).build());
        resolver.addDependency(new Dependency(new DefaultArtifact("redis.clients:jedis:4.4.8"), null));
        classpathBuilder.addLibrary(resolver);
    }

}
