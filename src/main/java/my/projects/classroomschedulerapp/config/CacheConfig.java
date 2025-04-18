package my.projects.classroomschedulerapp.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@EnableCaching
public class CacheConfig {

    // This configuration class enables caching in the application
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
                new ConcurrentMapCache("schedulesByDate"),
                new ConcurrentMapCache("scheduleDetails"),
                new ConcurrentMapCache("userDetails"),
                new ConcurrentMapCache("roomDetails"),
                new ConcurrentMapCache("availableRooms"),
                new ConcurrentMapCache("programDetails"),
                new ConcurrentMapCache("programsByDepartment"),
                new ConcurrentMapCache("departmentDetails"),
                new ConcurrentMapCache("courseDetails"),
                new ConcurrentMapCache("coursesByProgram")
        ));
        return cacheManager;
    }
}
