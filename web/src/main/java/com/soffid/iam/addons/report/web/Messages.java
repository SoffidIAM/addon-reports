package com.soffid.iam.addons.report.web;

import java.util.Collection;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;

 
public class Messages implements Map<String, String> {
    private static final String BUNDLE_NAME = "com/soffid/iam/addons/report/web/messages"; //$NON-NLS-1$
 
    public Messages() {
        super();
    }
 
    public int size() {
        return Integer.MAX_VALUE;
    }
 
    public boolean isEmpty() {
        return false;
    }
 
    public boolean containsKey(Object key) {
        return true;
    }
 
    public boolean containsValue(Object value) {
        return false;
    }
 
    public String get(Object key) {
        try
        {
            return es.caib.seycon.ng.comu.lang.MessageFactory.getString(BUNDLE_NAME, key.toString());
        }
        catch (MissingResourceException e)
        {
            return '!' + key.toString() + '!';
        }
    }
 
    public String put(String key, String value) {
        return null;
    }
 
    public String remove(Object key) {
        return null;
    }
 
    public void putAll(Map<? extends String, ? extends String> m) {
    }
 
    public void clear() {
    }
 
    public Set<String> keySet() {
        return null;
    }
 
    public Collection<String> values() {
        return null;
    }
 
    public Set<java.util.Map.Entry<String, String>> entrySet() {
        return null;
    }
}
