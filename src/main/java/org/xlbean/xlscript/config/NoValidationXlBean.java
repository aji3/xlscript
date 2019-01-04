package org.xlbean.xlscript.config;

import org.xlbean.XlBeanImpl;

/**
 * 
 * @author tanikawa
 *
 */
@SuppressWarnings("serial")
public class NoValidationXlBean extends XlBeanImpl {

    @Override
    protected boolean canPut(Object value) {
        return true;
    }
}
