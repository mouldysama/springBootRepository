package interview.dataspark.currencyexchange;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import com.google.common.cache.CacheBuilder;

/***
 * CacheConfig implements the CachinsConfigurer interface, and sets hooks for all caches used in the application.
 * @author Kenny
 *
 */
@Configuration
@EnableCaching
public class CurrencyExchangeCacheConfig implements CachingConfigurer {

	@Bean
	@Override
	public CacheManager cacheManager() {
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		List<Cache> caches = new ArrayList<Cache>();
	    caches.add(new ConcurrentMapCache("fileNamesInRepository"));
	    caches.add(new ConcurrentMapCache("fileName"));
	    caches.add(new ConcurrentMapCache("singleEntryByCurrencyByDate"));
	    caches.add(new ConcurrentMapCache("singleEntryByCurrencyByDateNewRef"));
	    caches.add(new ConcurrentMapCache("listAllCurrencies"));
	    caches.add(new ConcurrentMapCache("allEntriesByCurrency"));
	    caches.add(new ConcurrentMapCache("singleEntryByCurrencyAndDate"));
	    caches.add(new ConcurrentMapCache("specifiedEntriesByCurrencyAndDateRange"));
	    cacheManager.setCaches(caches);
		return cacheManager;
	}

	@Override
	public CacheResolver cacheResolver() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CacheErrorHandler errorHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public KeyGenerator keyGenerator() {
		return new SimpleKeyGenerator();
	}

}
