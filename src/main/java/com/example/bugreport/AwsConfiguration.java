package com.example.bugreport;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;


@Configuration
public class AwsConfiguration {

    /**
     * Allows us to override the URL, so we can test against local stack both inside and outside docker containers
     */
    @Value("${aws.url.override:#{null}}")
    private String overrideURL;

    @PostConstruct
    public void neverCalled() {
        // this is never called with Hoxton.SR8, but called with SR1
        System.out.println("Post construct called with Hoxton.SR8.");
    }

    @Bean
    public AmazonS3 amazonS3() {
        /**
         * BUG: With Hoxton.SR8 the value of overrideURL is always null even if you have set it in the configuration file.
         * The reason for this is that SimpleStorageProtocolResolver gets registered as an ordered BeanFactoryPostProcessor and instantiated during
         * the AbstractApplicationContext.invokeBeanFactoryPostProcessors (PostProcessorRegistrationDelegate:L172) phase of refresh, and not the
         * AbstractApplicationContext.registerBeanPostProcessors.
         * SimpleStorageProtocolResolver requires AmazonS3 as a constructor argument, so this leads to 'early' construction of AmazonS3 and any dependencies
         * it has, like the configuration providing it.
         * The consequence is that the list of postProcessors only contain the 4 factoryPostProcessors, and not the 7 it should be. Most notably the
         * CommonAnnotationBeanPostProcessor and AutowiredAnnotationBeanPostProcessor is missing, which means that @PostConstruct, @Value and @Autowire
         * will not be processed.
         *
         * I hope you can see why this is a very bad thing, as I expect that many applications and even frameworks are instantiating the AWS beans, and with
         * Hoxton.SR8 this is a big issue. Furthermore the symptoms can be different from application to application, and even for seasoned developers it
         * is not trivial to debug the spring initialization code.
         */

        // Put a breakpoint here and run with SR8 vs SR1, walk up the call stack and see which phase of refresh the bean gets instantiated in.
        if (overrideURL != null) {
            return AmazonS3ClientBuilder.standard()
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(overrideURL, Regions.US_EAST_1.getName()))
            .build();

        }
        return AmazonS3ClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .build();
        /**
         * After bean construction you will get a bunch of errors unless your machine has a been configured with aws credentials, this can be ignored as the
         * issue is related to bean initialization.
         */

    }
}
