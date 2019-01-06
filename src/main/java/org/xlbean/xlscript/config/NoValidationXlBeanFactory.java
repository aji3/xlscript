package org.xlbean.xlscript.config;

import org.xlbean.XlBean;
import org.xlbean.XlBeanImpl;
import org.xlbean.util.XlBeanFactory;

/**
 * Factory for NoValidationXlBean.
 * 
 * @author tanikawa
 *
 */
public class NoValidationXlBeanFactory extends XlBeanFactory {

    @Override
    public XlBean createBean() {
        return new NoValidationXlBean();
    }

    /**
     * Extends XlBeanImpl to enable any type of value to be put to this instance.
     * 
     * <p>
     * If values other than Map, List or String are to be set to this instance, then
     * methods like {@link XlBean#of} will not work since they are designed for
     * String as leaf object.
     * </p>
     * 
     * @author tanikawa
     *
     */
    @SuppressWarnings("serial")
    public static class NoValidationXlBean extends XlBeanImpl {

        @Override
        protected boolean canPut(Object value) {
            return true;
        }
    }
}
