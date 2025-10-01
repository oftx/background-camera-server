package github.oftx.backgroundcamera.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${storage.base-path}")
    private String storageBasePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Maps URL requests starting with /media/** to the local filesystem path defined in storage.base-path
        // e.g., accessing /media/some-device-id/2025/10/file.jpg will serve the file from the filesystem.
        String resourceLocation = "file:" + storageBasePath + "/";
        registry.addResourceHandler("/media/**")
                .addResourceLocations(resourceLocation);
    }
}
