package de.henningbrinkmann.toggl2sheet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.StreamSupport;

@Component
public class ApplicationListenerBean implements ApplicationListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationListenerBean.class);

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            ApplicationContext applicationContext = ((ContextRefreshedEvent) event).getApplicationContext();
            Environment env = applicationContext.getEnvironment();
            LOGGER.info("====== Environment and configuration ======");
            LOGGER.info("Active profiles: {}", Arrays.toString(env.getActiveProfiles()));
            final MutablePropertySources sources = ((AbstractEnvironment) env).getPropertySources();
            StreamSupport.stream(sources.spliterator(), false)
                    .filter(ps -> ps instanceof EnumerablePropertySource)
                    .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
                    .flatMap(Arrays::stream)
                    .distinct()
                    .filter(prop -> !(prop.contains("credentials") || prop.contains("password")))
                    .forEach(prop -> LOGGER.info("{}: {}", prop, env.getProperty(prop)));
            LOGGER.info("===========================================");

            // now you can do applicationContext.getBean(...)
            // ...
        }
    }
}