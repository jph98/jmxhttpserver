package org.jmxline.jmxlineapp;

import javax.management.MXBean;

@MXBean
public interface SimpleMXBean {

    public long getUsed();
}
