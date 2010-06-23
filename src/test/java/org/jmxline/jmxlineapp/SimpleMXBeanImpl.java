package org.jmxline.jmxlineapp;

public class SimpleMXBeanImpl implements SimpleMXBean {

    @Override
    public long getUsed() {        
        return 10;
    }

}
