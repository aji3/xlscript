package org.xlbean.xlscript.config;

import org.xlbean.XlBean;
import org.xlbean.util.XlBeanFactory;

public class NoValidationXlBeanFactory extends XlBeanFactory {

    @Override
    public XlBean createBean() {
        return new NoValidationXlBean();
    }
}
