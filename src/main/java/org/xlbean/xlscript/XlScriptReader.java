package org.xlbean.xlscript;

import java.io.File;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.xlbean.XlBean;
import org.xlbean.data.ExcelDataLoader;
import org.xlbean.definition.BeanDefinitionLoader;
import org.xlbean.definition.DefinitionLoader;
import org.xlbean.definition.ExcelCommentDefinitionLoader;
import org.xlbean.definition.ExcelR1C1DefinitionLoader;
import org.xlbean.reader.XlBeanReader;
import org.xlbean.reader.XlBeanReaderContext;
import org.xlbean.xlscript.processor.XlScriptProcessorProvider;

/**
 * Extends {@link XlBeanReader} to add function to evaluate XlBean values as
 * Groovy script on read.
 * 
 * <p>
 * This class loads all the values from excel file first then evaluate. It means
 * that all values in excel are accessible when evaluating script.
 * </p>
 * 
 * <p>
 * By default, order of evaluation is based on the location of Definition. From
 * left to right and top to bottom. You can change order by scriptOrder option.
 * The default scriptOrder is 1000 so any value above this will be executed
 * after and below before.
 * </p>
 * 
 * @author tanikawa
 *
 */
public class XlScriptReader extends XlBeanReader {

    private XlBeanReader reader;
    private XlScriptProcessorProvider scriptProcessorProvider = new XlScriptProcessorProvider();

    /**
     * Constructor which uses {@link XlBeanReader} to read excel file.
     */
    public XlScriptReader() {
        this(new XlBeanReader());
    }

    /**
     * Constructor which uses given {@code reader} to read excel file.
     */
    public XlScriptReader(XlBeanReader reader) {
        this.reader = reader;
    }

    /**
     * Read {@code excelFile} by using {@link XlBeanReader} instance set by
     * constructor, then evaluate all the values in {@link XlBean}.
     */
    @Override
    public XlBean read(File excelFile) {
        XlScriptReaderContext context = toScriptContext(reader.readContext(excelFile));
        context.evalAll();
        return context.getXlBean();
    }

    /**
     * Read {@code in} by using {@link XlBeanReader} instance set by constructor,
     * then evaluate all the values in {@link XlBean}.
     */
    @Override
    public XlBean read(InputStream in) {
        XlScriptReaderContext context = toScriptContext(reader.readContext(in));
        context.evalAll();
        return context.getXlBean();
    }

    /**
     * Read {@code definitionSource} and {@code dataSource} by using
     * {@link XlBeanReader} instance set by constructor, then evaluate all the
     * values in {@link XlBean}.
     */
    @Override
    public XlBean read(Object definitionSource, Workbook dataSource) {
        XlScriptReaderContext context = toScriptContext(reader.readContext(definitionSource, dataSource));
        context.evalAll();
        return context.getXlBean();
    }

    /**
     * Read {@code excelFile} by using {@link XlBeanReader} instance set by
     * constructor, then evaluate all the values in {@link XlBean}.
     */
    @Override
    public XlBeanReaderContext readContext(File excelFile) {
        XlScriptReaderContext context = toScriptContext(reader.readContext(excelFile));
        context.evalAll();
        return context;
    }

    /**
     * Read {@code in} by using {@link XlBeanReader} instance set by constructor,
     * then evaluate all the values in {@link XlBean}.
     */
    @Override
    public XlBeanReaderContext readContext(InputStream in) {
        XlScriptReaderContext context = toScriptContext(reader.readContext(in));
        context.evalAll();
        return context;
    }

    /**
     * Read {@code definitionSource} and {@code dataSource} by using
     * {@link XlBeanReader} instance set by constructor, then evaluate all the
     * values in {@link XlBean}.
     */
    @Override
    public XlBeanReaderContext readContext(Object definitionSource, Workbook dataSource) {
        XlScriptReaderContext context = toScriptContext(reader.readContext(definitionSource, dataSource));
        context.evalAll();
        return context;
    }

    private XlScriptReaderContext toScriptContext(XlBeanReaderContext context) {
        XlScriptReaderContext scriptContext = new XlScriptReaderContext(scriptProcessorProvider);
        scriptContext.setDefinitions(context.getDefinitions());
        scriptContext.setXlBean(context.getXlBean());
        return scriptContext;
    }

    public static class Builder {

        private XlBeanReaderBuilder builder = new XlBeanReaderBuilder();
        private XlScriptProcessorProvider scriptProcessorProvider = new XlScriptProcessorProvider();

        public Builder baseScript(String baseScript) {
            this.scriptProcessorProvider.setBaseScript(baseScript);
            return this;
        }

        public Builder baseInstance(Object baseInstance) {
            this.scriptProcessorProvider.setBaseInstance(baseInstance);
            return this;
        }

        /**
         * Set DefinitionLoader of the XlBeanReader instance.
         * 
         * @see ExcelR1C1DefinitionLoader
         * @see ExcelCommentDefinitionLoader
         * @see BeanDefinitionLoader
         * 
         * @param definitionLoader
         * @return
         */
        public Builder definitionLoader(DefinitionLoader definitionLoader) {
            builder.definitionLoader(definitionLoader);
            return this;
        }

        /**
         * Set ExcelDataLoader of the XlBeanReader instance.
         * 
         * @see ExcelDataLoader
         * 
         * @param dataLoader
         * @return
         */
        public Builder dataLoader(ExcelDataLoader dataLoader) {
            builder.dataLoader(dataLoader);
            return this;
        }

        /**
         * Set given {@code key} and {@code value} to common context which is used for
         * bindings for all {@link eval} method calls.
         * 
         * @param key
         * @param value
         */
        public Builder addBaseBinding(String key, Object value) {
            scriptProcessorProvider.addBaseBinding(key, value);
            return this;
        }

        public Builder scriptProcessorProvider(XlScriptProcessorProvider scriptProcessorProvider) {
            this.scriptProcessorProvider = scriptProcessorProvider;
            return this;
        }

        /**
         * If both {@code baseInstance} and {@code baseSript} is set,
         * {@code baseInstance} will be applied.
         * 
         * @return
         */
        public XlScriptReader build() {
            XlScriptReader reader = new XlScriptReader(builder.build());
            reader.scriptProcessorProvider = this.scriptProcessorProvider;
            return reader;
        }
    }

}
