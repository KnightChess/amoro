/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.netease.arctic.optimizer.flink;

import com.netease.arctic.optimizer.common.OptimizerExecutor;
import org.apache.flink.streaming.api.operators.AbstractStreamOperator;
import org.apache.flink.streaming.api.operators.OneInputStreamOperator;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;

public class FlinkExecutor extends AbstractStreamOperator<Void>
    implements OneInputStreamOperator<String, Void> {

  private final OptimizerExecutor[] allExecutors;
  private OptimizerExecutor executor;

  public FlinkExecutor(OptimizerExecutor[] allExecutors) {
    this.allExecutors = allExecutors;
  }

  @Override
  public void open() throws Exception {
    super.open();
    int subTaskIndex = getRuntimeContext().getIndexOfThisSubtask();
    executor = allExecutors[subTaskIndex];
    new Thread(() -> executor.start(), "flink-optimizer-executor-" + subTaskIndex).start();
  }

  @Override
  public void close() throws Exception {
    executor.stop();
  }

  @Override
  public void processElement(StreamRecord<String> element) {
    executor.setToken(element.getValue());
  }
}
