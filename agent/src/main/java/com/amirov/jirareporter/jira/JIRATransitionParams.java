package com.amirov.jirareporter.jira;

import com.amirov.jirareporter.jira.exceptions.JIRATransitionParamsSetException;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Issue is transitioned from statusName via transitionName.
 * Additionally issue properties is set:
 *  - resolutionName
 *  - fixVersion (TODO)
 */
public class JIRATransitionParams {
    private String statusName;
    private String transitionName;
    private String resolutionName;
    private String fixVersionName;

    public String getStatusName() {
        return statusName;
    }

    public JIRATransitionParams setStatusName(String statusName) {
        this.statusName = statusName;
        return this;
    }

    public String getTransitionName() {
        return transitionName;
    }

    public JIRATransitionParams setTransitionName(String transitionName) {
        this.transitionName = transitionName;
        return this;
    }

    public String getResolutionName() {
        return resolutionName;
    }

    public JIRATransitionParams setResolutionName(String resolutionName) {
        if( resolutionName.toLowerCase().equals("none")) {
            this.resolutionName = null;
        } else {
            this.resolutionName = resolutionName;
        }
        return this;
    }

    public String getFixVersionName() {
        return fixVersionName;
    }

    public JIRATransitionParams setFixVersionName(String fixVersionName) {
        this.fixVersionName = fixVersionName;
        return this;
    }

    public JIRATransitionParams set(String paramName,String paramValue) throws JIRATransitionParamsSetException {
        Method method = null;
        try {
            method = this.getClass().getMethod("set" + StringUtils.capitalize(paramName), String.class);
        } catch (NoSuchMethodException e) {
            throw new JIRATransitionParamsSetException(paramName + "not found",e);
        }
        try {
            method.invoke(this, paramValue);
        } catch (IllegalAccessException e) {
            throw new JIRATransitionParamsSetException(paramName + "can not be set",e);
        } catch (InvocationTargetException e) {
            throw new JIRATransitionParamsSetException(paramName + "can not be set",e);
        }
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.statusName == null ? 0 : this.statusName.hashCode());
        hash = 53 * hash + (this.transitionName == null ? 0 : this.transitionName.hashCode());
        hash = 53 * hash + (this.resolutionName == null ? 0 : this.resolutionName.hashCode());
        hash = 53 * hash + (this.fixVersionName == null ? 0 : this.fixVersionName.hashCode());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if((obj == null) || (obj.getClass() != this.getClass()))
            return false;

        JIRATransitionParams tp = (JIRATransitionParams) obj;
        return this.compareFields(tp);
    }

    public boolean compareFields(JIRATransitionParams toComapre) {
        try {
            Class clazz = this.getClass();
            for (Field field :
                 clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Object value1 = field.get(this);
                Object value2 = field.get(toComapre);

                if(value1 != null && !value1.equals(value2)) {
                    return false;
                } else if( value1 == null && value2 != null ){
                    return false;
                }

            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);

        Class clazz = this.getClass();
        sb.append("JIRATransitionParams").append("{");

        for (Field field :
                clazz.getDeclaredFields()) {
            field.setAccessible(true);

            sb.append(field.getName()).append("=");
            try {
                Object value = field.get(this);
                sb.append(value.toString());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                sb.append("toString()-Fetching error");
            }
            sb.append(";");

        }
        return sb.toString();
    }
}
