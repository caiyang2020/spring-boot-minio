package org.sensetimeframework.minio.messages;

import lombok.Data;

@Data
public class Progress {
    private int processed;

    public void increase() {
        processed++;
    }

    public void increase(int num) {
        setProcessed(processed + num);
    }
}
