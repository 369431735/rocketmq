/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.logging.inner;


import java.io.InterruptedIOException;
import java.util.Enumeration;
import java.util.Vector;

/**
 * 追加者
 */
public abstract class Appender {

    /**
     * 写入失败
     */
    public static final int CODE_WRITE_FAILURE = 1;
    /**
     * 刷新失败
     */
    public static final int CODE_FLUSH_FAILURE = 2;
    /**
     * 关闭失败
     */
    public static final int CODE_CLOSE_FAILURE = 3;
    /**
     * 文件打开失败
     */
    public static final int CODE_FILE_OPEN_FAILURE = 4;

    public final static String LINE_SEP = System.getProperty("line.separator");

    boolean firstTime = true;

    protected Layout layout;

    protected String name;

    protected boolean closed = false;

    /**
     * todo 暂不知该方法的作用
     */
    public void activateOptions() {
    }

    abstract protected void append(LoggingEvent event);

    public void finalize() {
        try {
            super.finalize();
        } catch (Throwable throwable) {
            SysLogger.error("Finalizing appender named [" + name + "]. error", throwable);
        }
        if (this.closed) {
            return;
        }

        SysLogger.debug("Finalizing appender named [" + name + "].");
        close();
    }

    public Layout getLayout() {
        return layout;
    }

    public final String getName() {
        return this.name;
    }

    public synchronized void doAppend(LoggingEvent event) {
        if (closed) {
            SysLogger.error("Attempted to append to closed appender named [" + name + "].");
            return;
        }
        this.append(event);
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract void close();

    /**
     * 处理日志异常
     * @param message
     * @param e
     * @param errorCode
     */
    public void handleError(String message, Exception e, int errorCode) {
        if (e instanceof InterruptedIOException || e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        if (firstTime) {
            SysLogger.error(message + " code:" + errorCode, e);
            firstTime = false;
        }
    }

    public void handleError(String message) {
        if (firstTime) {
            SysLogger.error(message);
            firstTime = false;
        }
    }


    public interface AppenderPipeline {
        /**
         * 新增Appender
         * @param newAppender
         */
        void addAppender(Appender newAppender);

        /**
         * 获取所有的Appender
         * @return
         */
        Enumeration getAllAppenders();

        /**
         * 根据name 获取Appender
         * @param name
         * @return
         */
        Appender getAppender(String name);

        /**
         * 查看Appender是否已经存在
         * @param appender
         * @return
         */
        boolean isAttached(Appender appender);

        /**
         * 移除所有Appender
         */
        void removeAllAppenders();

        /***
         * 移除固定的Appender
         * @param appender
         */
        void removeAppender(Appender appender);

        /**
         * 根据name移除Appender
         * @param name
         */
        void removeAppender(String name);
    }


    public static class AppenderPipelineImpl implements AppenderPipeline {


        protected Vector<Appender> appenderList;

        public void addAppender(Appender newAppender) {
            if (newAppender == null) {
                return;
            }

            if (appenderList == null) {
                appenderList = new Vector<Appender>(1);
            }
            if (!appenderList.contains(newAppender)) {
                appenderList.addElement(newAppender);
            }
        }

        public int appendLoopOnAppenders(LoggingEvent event) {
            int size = 0;
            Appender appender;

            if (appenderList != null) {
                size = appenderList.size();
                for (int i = 0; i < size; i++) {
                    appender = appenderList.elementAt(i);
                    appender.doAppend(event);
                }
            }
            return size;
        }

        public Enumeration getAllAppenders() {
            if (appenderList == null) {
                return null;
            } else {
                return appenderList.elements();
            }
        }

        public Appender getAppender(String name) {
            if (appenderList == null || name == null) {
                return null;
            }

            int size = appenderList.size();
            Appender appender;
            for (int i = 0; i < size; i++) {
                appender = appenderList.elementAt(i);
                if (name.equals(appender.getName())) {
                    return appender;
                }
            }
            return null;
        }

        public boolean isAttached(Appender appender) {
            if (appenderList == null || appender == null) {
                return false;
            }

            int size = appenderList.size();
            Appender a;
            for (int i = 0; i < size; i++) {
                a = appenderList.elementAt(i);
                if (a == appender) {
                    return true;
                }
            }
            return false;
        }

        public void removeAllAppenders() {
            if (appenderList != null) {
                int len = appenderList.size();
                for (int i = 0; i < len; i++) {
                    Appender a = appenderList.elementAt(i);
                    a.close();
                }
                appenderList.removeAllElements();
                appenderList = null;
            }
        }

        public void removeAppender(Appender appender) {
            if (appender == null || appenderList == null) {
                return;
            }
            appenderList.removeElement(appender);
        }

        public void removeAppender(String name) {
            if (name == null || appenderList == null) {
                return;
            }
            int size = appenderList.size();
            for (int i = 0; i < size; i++) {
                if (name.equals((appenderList.elementAt(i)).getName())) {
                    appenderList.removeElementAt(i);
                    break;
                }
            }
        }

    }
}
