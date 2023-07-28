package com.dotcms.util.es;

import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.servlet.ServletToolInfo;

public class ESSearchUtilToolInfo extends ServletToolInfo {

    @Override
    public String getKey() {
        return "ESSearchUtils";
    }

    @Override
    public String getScope() {
        return ViewContext.REQUEST;
    }

    @Override
    public String getClassname() {
        return ESSearchUtilTool.class.getName();
    }

    @Override
    public Object getInstance(Object initData) {
        ESSearchUtilTool viewTool = new ESSearchUtilTool();
        viewTool.init(initData);

        setScope(ViewContext.APPLICATION);

        return viewTool;
    }

}