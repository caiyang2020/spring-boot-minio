package org.sensetimeframework.curator.wrapper;

import lombok.Data;
import org.apache.zookeeper.data.Stat;

@Data
public class ZNodeWrapper {
    private Stat stat;
    private String data;
}
