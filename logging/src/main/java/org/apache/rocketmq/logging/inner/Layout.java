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

/**
 * 布局
 */
public abstract class Layout {

    public abstract String format(LoggingEvent event);

    public String getContentType() {
        return "text/plain";
    }

    /**
     * 头部内容
     * @return
     */
    public String getHeader() {
        return null;
    }

    /**
     * 尾部内容
     * @return
     */
    public String getFooter() {
        return null;
    }

    /**
     * 是否忽略异常
     * @return
     */
    abstract public boolean ignoresThrowable();

}