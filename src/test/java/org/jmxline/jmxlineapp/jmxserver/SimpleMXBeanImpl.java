package org.jmxline.jmxlineapp.jmxserver;

public class SimpleMXBeanImpl implements SimpleMXBean {

    @Override
    public long getUsed() {        
        return 10;
    }

}
