/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The Log class used for performing login of the simulation process.
 * It provides the ability to substitute the output stream by any OutputStream subclass.
 *
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
@SuppressWarnings("unused")
public class Log {
    private static final Logger logger = LoggerFactory.getLogger(Log.class);

    /**
     * The Constant LINE_SEPARATOR.
     */
    private static final String LINE_SEPARATOR = System.lineSeparator();

    /**
     * The output.
     */
    private static OutputStream output;

    /**
     * The disable output flag.
     */
    private static boolean disabled;

    /**
     * Buffer to avoid creating new string builder upon every print.
     */
    private static final StringBuilder buffer = new StringBuilder();

    /**
     * Prints the message.
     *
     * @param message the message
     */
    public static void print(String message) {
        if (isEnabled()) {
            try {
                getOutput().write(message.getBytes());
            } catch (IOException e) {
                logger.error("Error writing to the output stream", e);
            }
        }
    }

    /**
     * Prints the message passed as a non-String object.
     *
     * @param message the message
     */
    public static void print(Object message) {
        if (isEnabled()) {
            print(String.valueOf(message));
        }
    }

    /**
     * Prints the line.
     *
     * @param message the message
     */
    public static void printLine(String message) {
        if (isEnabled()) {
            print(message + LINE_SEPARATOR);
        }
    }

    /**
     * Prints the empty line.
     */
    public static void printLine() {
        if (isEnabled()) {
            print(LINE_SEPARATOR);
        }
    }

    /**
     * Prints the line passed as a non-String object.
     *
     * @param message the message
     */
    public static void printLine(Object message) {
        if (isEnabled()) {
            printLine(String.valueOf(message));
        }
    }

    /**
     * Prints a string formated as in String.format().
     *
     * @param format the format
     * @param args   the args
     */
    public static void format(String format, Object... args) {
        if (isEnabled()) {
            print(String.format(format, args));
        }
    }

    /**
     * Prints a line formated as in String.format().
     *
     * @param format the format
     * @param args   the args
     */
    public static void formatLine(String format, Object... args) {
        if (isEnabled()) {
            printLine(String.format(format, args));
        }
    }

    /**
     * Prints the concatenated text representation of the arguments and a new line.
     *
     * @param messages the messages to print
     */
    public static void printConcatLine(Object... messages) {
        if (isEnabled()) {
            buffer.setLength(0); // Clear the buffer
            for (Object message : messages) {
                buffer.append(message);
            }
            printLine(buffer);
        }
    }

    /**
     * Gets the output.
     *
     * @return the output
     */
    public static OutputStream getOutput() {
        if (output == null) {
            setOutput(System.out);
        }
        return output;
    }

    /**
     * Sets the output.
     *
     * @param _output the new output
     */
    public static void setOutput(OutputStream _output) {
        output = _output;
    }

    /**
     * Checks if the output is disabled.
     *
     * @return true, if is disabled
     */
    public static boolean isEnabled() {
        return !disabled;
    }

    /**
     * Sets the disable output flag.
     *
     * @param _disabled the newly disabled
     */
    public static void setDisabled(boolean _disabled) {
        disabled = _disabled;
    }

    /**
     * Disables the output.
     */
    public static void disable() {
        setDisabled(true);
    }

    /**
     * Enables the output.
     */
    public static void enable() {
        setDisabled(false);
    }

}
