package org.albianj.aop;

import java.util.List;

/**
 * Created by xuhaifeng on 16/5/30.
 */
public interface IAlbianServiceAopAttribute {
    String getBeginWith();
    void setBeginWith(String beginWith);
    String getNotBeginWith();
    void setNotBeginWith(String notBeginWith);
    String getEndWith();
    void setEndWith(String endWith);
    String getNotEndWith();
    void setNotEndWith(String notEndWith);
    String getContain();
    void setContain(String contain);
    String getNotContain();
    void setNotContain(String notContain);
    String getStringExceptions();
    void setStringExceptions(String exceptions);
    List<Class> getExceptions();
    void setExceptions(List<Class> exceptions);

    String getServiceName();
    void setServiceName(String serviceName);

    boolean matches(String name);
    boolean matchsException(String name,Throwable e);
    }
