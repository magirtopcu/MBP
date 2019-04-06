package org.citopt.connde.util;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class DebugFileLogger {

    private static SimpleFormatter formatter;

    static {
        formatter = new SimpleFormatter();
    }

    public static Logger createLogger(String name, String path) throws IOException {
        Logger logger = Logger.getLogger(name);
        FileHandler fileHandler = new FileHandler(path);
        fileHandler.setFormatter(formatter);
        logger.addHandler(fileHandler);

        return logger;
    }
}
