package com.xinqihd.sns.gameserver.db.mongo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated field is excluded from database storing.
 * It is mainly used by my {@link MapDBObject#putAll(Object)}
 * @author wangqi
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExcludeField {

}
