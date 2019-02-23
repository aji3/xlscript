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
import org.xlbean.util.Accessors;
import org.xlbean.util.XlBeanFactory;
import org.xlbean.xlscript.config.NoValidationXlBeanFactory;
import org.xlbean.xlscript.processor.XlScriptSingleDefinitionProcessor;
import org.xlbean.xlscript.processor.XlScriptTableDefinitionProcessor;

/**
 * Extends {@link XlBeanReader} to add function to evaluate XlBean values as
 * Groovy script on read.
 * 
 * <p>
 * It loads all the values from excel file first then evaluate script. It means
 * that all values in excel are accessible when evaluating script.
 * </p>
 * 
 * <p>
 * This class has static initializer for the following initialization:
 * <ol>
 * <li>Set NoValidationXlBeanFactory: to set any type of evaluated value to
 * XlBean</li>
 * <li>Set Accessors#noNullValue flag to false: to allow null value for XlBean
 * because keys are required for Groovy script to be compiled properly.</li>
 * </ol>
 * </p>
 * 
 * @author tanikawa
 *
 */
public class XlScriptReader extends XlBeanReader {

    static {
        XlBeanFactory.setInstance(new NoValidationXlBeanFactory());
        Accessors.setInstance(new Accessors(false, false, false));
    }

    private XlBeanReader reader;
    private XlScriptSingleDefinitionProcessor singleDefinitionProcessor = new XlScriptSingleDefinitionProcessor();
    private XlScriptTableDefinitionProcessor tableDefinitionProcessor = new XlScriptTableDefinitionProcessor();

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
        XlScriptReaderContext scriptContext = new XlScriptReaderContext(
            singleDefinitionProcessor,
            tableDefinitionProcessor);
        scriptContext.setDefinitions(context.getDefinitions());
        scriptContext.setXlBean(context.getXlBean());
        return scriptContext;
    }

    public static class XlScriptReaderBuilder {

        private XlBeanReaderBuilder builder = new XlBeanReaderBuilder();
        private String baseScript;
        private Object baseInstance;

        public XlScriptReaderBuilder baseScript(String baseScript) {
            this.baseScript = baseScript;
            return this;
        }

        public XlScriptReaderBuilder baseInstance(Object baseInstance) {
            this.baseInstance = baseInstance;
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
        public XlScriptReaderBuilder definitionLoader(DefinitionLoader definitionLoader) {
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
        public XlScriptReaderBuilder dataLoader(ExcelDataLoader dataLoader) {
            builder.dataLoader(dataLoader);
            return this;
        }

        public XlScriptReader build() {
            XlScriptReader reader = new XlScriptReader(builder.build());
            if (this.baseInstance != null) {
                reader.singleDefinitionProcessor = new XlScriptSingleDefinitionProcessor(this.baseInstance);
                reader.tableDefinitionProcessor = new XlScriptTableDefinitionProcessor(this.baseInstance);
            } else if (this.baseScript != null) {
                reader.singleDefinitionProcessor = new XlScriptSingleDefinitionProcessor(this.baseScript);
                reader.tableDefinitionProcessor = new XlScriptTableDefinitionProcessor(this.baseScript);
            }
            return reader;
        }
    }

}
