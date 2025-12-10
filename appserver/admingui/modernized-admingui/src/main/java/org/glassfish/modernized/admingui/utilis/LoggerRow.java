

package org.glassfish.modernized.admingui.utilis;

import java.io.Serializable;

public class LoggerRow implements Serializable {
    private String name;
    private String level;       // e.g. INFO, WARNING, FINE...
    private String customLevel; // free text

    public LoggerRow() {}
    public LoggerRow(String name, String level) {
        this.name = name;
        this.level = level;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getCustomLevel() { return customLevel; }
    public void setCustomLevel(String customLevel) { this.customLevel = customLevel; }
}
