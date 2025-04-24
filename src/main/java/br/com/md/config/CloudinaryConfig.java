package br.com.md.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dm9wkmkn1",
                "api_key", "119473353439161",
                "api_secret", "CitZImiYHdbh4g3_z0hz0FRjbSs"
        ));
    }
}
