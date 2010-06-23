package org.jmxline.jmxlineapp.jmxserver;

import javax.management.MXBean;

@MXBean
public interface SimpleMXBean {

    public long getUsed();
}
