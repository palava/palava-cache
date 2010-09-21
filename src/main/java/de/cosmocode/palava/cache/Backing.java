package de.cosmocode.palava.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

/**
 * A binding annotation for the backing {@link CacheService} of a 
 * {@link BackedComputingCacheService}.
 *
 * @since 2.4
 * @author Willi Schoenborn
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({
    ElementType.PARAMETER,
    ElementType.METHOD
})
@BindingAnnotation
@interface Backing {

}
