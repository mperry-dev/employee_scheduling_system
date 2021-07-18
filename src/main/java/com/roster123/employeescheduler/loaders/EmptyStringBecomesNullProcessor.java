package com.roster123.employeescheduler.loaders;

import com.opencsv.bean.processor.StringProcessor;

/**
 * opencsv field processor to convert empty string to null (e.g. for empty string type in availability loader)
 */
public class EmptyStringBecomesNullProcessor implements StringProcessor {
    @Override
    public String processString(String value) {
        return "".equals(value)? null: value;
    }

    @Override
    public void setParameterString(String value) {
        // does nothing, just required to have this
    }
}
