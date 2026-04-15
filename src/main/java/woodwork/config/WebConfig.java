package woodwork.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // map /uploads/** to the physical directory on the hard drive
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
